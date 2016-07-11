/*
 * $Id: Processor.java,v 2.61 2009/08/21 12:46:17 obecker Exp $
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is: this file
 *
 * The Initial Developer of the Original Code is Oliver Becker.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): Anatolij Zubow, Thomas Behrends.
 */

package net.sf.joost.stx;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.URIResolver;

import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import net.sf.joost.CSTX;
import net.sf.joost.OptionalLog;
import net.sf.joost.IOutputURIResolver;
import net.sf.joost.ITransformerHandlerResolver;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.instruction.AbstractGroupBase;
import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.instruction.PSiblingsFactory;
import net.sf.joost.instruction.AbstractProcessBase;
import net.sf.joost.instruction.TemplateFactory;
import net.sf.joost.instruction.TransformFactory;

/**
 * Processes an XML document as SAX XMLFilter. Actions are contained within an
 * array of templates, received from a transform node.
 *
 * @version $Revision: 2.61 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

public class Processor extends XMLFilterImpl
                       implements LexicalHandler /* , DeclHandler */
{
  /**
   * Possible actions when no matching template was found. Set by
   * <code>stx:options' no-match-events</code>
   */
  public static final byte PASS_THROUGH_NONE = 0x0, // default, see Context
      PASS_THROUGH_ELEMENT = 0x1, PASS_THROUGH_TEXT = 0x2, PASS_THROUGH_COMMENT = 0x4, PASS_THROUGH_PI = 0x8,
      PASS_THROUGH_ATTRIBUTE = 0x10, PASS_THROUGH_ALL = ~PASS_THROUGH_NONE; // all
                                                                            // bits
                                                                            // set

  /** The node representing the transformation sheet */
  private TransformFactory.Instance transformNode;

  /**
   * Array of global visible templates (templates with an attribute
   * <code>visibility="global"</code>).
   */
  private TemplateFactory.Instance [] globalTemplates;

  /** The Context object */
  private Context context;

  /**
   * Depth in the subtree to be skipped; increased by startElement and decreased
   * by endElement.
   */
  private int skipDepth = 0;

  /**
   * Set to true between {@link #startCDATA} and {@link #endCDATA}, needed for
   * CDATA processing
   */
  private boolean insideCDATA = false;

  /**
   * Set to true between {@link #startDTD} and {@link #endDTD}, needed for
   * ignoring comments
   */
  private boolean insideDTD = false;

  /** Buffer for collecting character data into single text nodes */
  private StringBuffer collectedCharacters = new StringBuffer ();

  /** Last event (this Processor uses one look-ahead) */
  private SAXEvent lastElement = null;

  /** The namespaces of the current scope */
  private Hashtable <String, String> inScopeNamespaces;

  /** The namespace context as a stack */
  private final Stack <Hashtable <String, String>> namespaceContext = new Stack<> ();

  /** Flag that controls namespace contexts */
  private boolean nsContextActive = false;

  /**
   * Stack for input events (of type {@link SAXEvent}). Every
   * <code>startElement</code> event pushes itself on this stack, every
   * <code>endElement</code> event pops its event from the stack. Character
   * events (text()), comments, PIs will be put on the stack before processing
   * and removed immediately afterwards. This stack is needed for matching and
   * for position counting within the parent of each event.
   */
  private Stack <SAXEvent> eventStack;

  /**
   * Stack needed for inner processing (buffers, documents). This stack stores
   * the event stack for <code>stx:process-document</code> and character data
   * that has been already read as look-ahead ({@link #collectedCharacters}).
   */
  private final Stack <Serializable> innerProcStack = new Stack<> ();

  public Properties outputProperties;

  private final boolean isProcessorClass = getClass ().equals (Processor.class);

  // **********************************************************************
  /**
   * Inner class for data which is processing/template specific. Objects of this
   * class will be put on the instance stack {@link Processor#dataStack}.
   */
  public final class Data
  {
    /**
     * Last process status while processing this template. The values used are
     * defined in {@link CSTX} as "process state values".
     */
    private short lastProcStatus;

    /** The last instantiated template */
    public TemplateFactory.Instance template;

    /** The next instruction to be executed */
    private AbstractInstruction instruction;

    /** The current group */
    public AbstractGroupBase currentGroup;

    /** The context position of the current node (from {@link Context}) */
    private long contextPosition;

    /** Next group in the processing, contains the visible templates */
    private AbstractGroupBase targetGroup;

    /** current table of local variables in {@link #template} */
    private Hashtable <String, Value> localVars;

    /** passed parameters to {@link #template} (only for the debugging) */
    private Hashtable <String, Value> passedParams;

    /**
     * <code>stx:process-siblings</code> instruction (for stx:process-siblings)
     */
    private PSiblingsFactory.Instance psiblings;

    /** current event (for stx:process-siblings) */
    private SAXEvent sibEvent;

    /**
     * Constructor for the initialization of all fields, needed for
     * <code>stx:process-siblings</code>
     */
    Data (final short lps,
          final TemplateFactory.Instance t,
          final AbstractInstruction i,
          final Hashtable <String, Value> pp,
          final Context c,
          final SAXEvent se)
    {
      lastProcStatus = lps;
      template = t;
      instruction = i;
      currentGroup = c.currentGroup;
      contextPosition = c.position;
      targetGroup = c.targetGroup;
      localVars = (Hashtable <String, Value>) c.localVars.clone ();
      passedParams = pp;
      psiblings = c.psiblings;
      sibEvent = se;
    }

    /** Constructor for "descendant or self" processing */
    Data (final short lps,
          final TemplateFactory.Instance t,
          final AbstractInstruction i,
          final Hashtable <String, Value> pp,
          final Context c)
    {
      lastProcStatus = lps;
      template = t;
      instruction = i;
      currentGroup = c.currentGroup;
      contextPosition = c.position;
      targetGroup = c.targetGroup;
      localVars = (Hashtable <String, Value>) c.localVars.clone ();
      passedParams = pp;
    }

    /**
     * Initial constructor for the first element of the data stack.
     *
     * @param c
     *        the initial context
     */
    Data (final Context c)
    {
      targetGroup = c.targetGroup;
      // other fields are default initialized with 0 or null resp.
    }

    /**
     * Constructor used when processing a built-in template.
     *
     * @param data
     *        a {@link Processor.Data} element that will be copied partially
     */
    Data (final Data data)
    {
      targetGroup = data.targetGroup;
      currentGroup = data.currentGroup;
      // other fields are default initialized with 0 or null resp.
    }

    // methods

    /** returns the value of {@link #passedParams} */
    public Hashtable getPassedParams ()
    {
      return passedParams;
    }

    /** returns the value of {@link #localVars} */
    public Hashtable getLocalVars ()
    {
      return localVars;
    }

    /** returns the value of {@link #targetGroup} */
    public AbstractGroupBase getTargetGroup ()
    {
      return targetGroup;
    }

    /** just for debugging */
    @Override
    public String toString ()
    {
      return "Data{" +
             template +
             "," +
             contextPosition +
             "," +
             java.util.Arrays.asList (targetGroup.visibleTemplates) +
             "," +
             lastProcStatus +
             "}";
    }
  } // inner class Data

  // **********************************************************************

  /** Stack for {@link Processor.Data} objects */
  private final DataStack dataStack = new DataStack ();

  /**
   * Inner class that implements a stack for {@link Processor.Data} objects.
   * <p>
   * I've implemented my own (typed) stack to circumvent the costs of type casts
   * for the Data objects. However, I've noticed no notable performance gain.
   */
  public final class DataStack
  {
    private Data [] stack = new Data [32];
    private int objCount = 0;

    void push (final Data d)
    {
      if (objCount == stack.length)
      {
        final Data [] tmp = new Data [objCount << 1];
        System.arraycopy (stack, 0, tmp, 0, objCount);
        stack = tmp;
      }
      stack[objCount++] = d;
    }

    Data peek ()
    {
      return stack[objCount - 1];
    }

    Data pop ()
    {
      return stack[--objCount];
    }

    public int size ()
    {
      return objCount;
    }

    Data elementAt (final int pos)
    {
      return stack[pos];
    }

    public Data [] getStack ()
    {
      return stack;
    }

    // for debugging
    @Override
    public String toString ()
    {
      final StringBuffer sb = new StringBuffer ('[');
      for (int i = 0; i < objCount; i++)
      {
        if (i > 0)
          sb.append (',');
        sb.append (stack[i].toString ());
      }
      sb.append (']');
      return sb.toString ();
    }
  } // inner class DataStack

  // **********************************************************************

  private static Logger log = OptionalLog.getLog (Processor.class);

  //
  // Constructors
  //

  /**
   * Constructs a new <code>Processor</code> instance by parsing an STX
   * transformation sheet. This constructor attempts to create its own
   * {@link XMLReader} object.
   *
   * @param src
   *        the source for the STX transformation sheet
   * @param pContext
   *        the parse context
   * @throws IOException
   *         if <code>src</code> couldn't be retrieved
   * @throws SAXException
   *         if a SAX parser couldn't be created
   */
  public Processor (final InputSource src, final ParseContext pContext) throws IOException, SAXException
  {
    this (null, src, pContext);
  }

  /**
   * Constructs a new <code>Processor</code> instance by parsing an STX
   * transformation sheet.
   *
   * @param reader
   *        the parser that is used for reading the transformation sheet
   * @param src
   *        the source for the STX transformation sheet
   * @param pContext
   *        a parse context
   * @throws IOException
   *         if <code>src</code> couldn't be retrieved
   * @throws SAXException
   *         if a SAX parser couldn't be created
   */
  public Processor (XMLReader reader, final InputSource src, final ParseContext pContext) throws IOException,
                                                                                          SAXException
  {
    if (reader == null)
      reader = createXMLReader ();

    // create a Parser for parsing the STX transformation sheet
    final Parser stxParser = new Parser (pContext);
    reader.setContentHandler (stxParser);
    reader.setErrorHandler (pContext.getErrorHandler ());

    // parse the transformation sheet
    reader.parse (src);

    init (stxParser.getTransformNode ());

    // re-use this XMLReader for processing
    setParent (reader);
  }

  /**
   * Constructs a new Processor instance from an existing Parser (Joost
   * representation of an STX transformation sheet)
   *
   * @param stxParser
   *        the Joost representation of a transformation sheet
   * @throws SAXException
   *         if {@link #createXMLReader} fails
   */
  public Processor (final Parser stxParser) throws SAXException
  {
    init (stxParser.getTransformNode ());
    setParent (createXMLReader ());
  }

  /**
   * Constructs a copy of the given Processor.
   *
   * @param proc
   *        the original Processor object
   * @throws SAXException
   *         if the construction of a new XML parser fails
   */
  public Processor (final Processor proc) throws SAXException
  {
    final HashMap copies = new HashMap ();
    globalTemplates = AbstractInstruction.deepTemplateArrayCopy (proc.globalTemplates, copies);
    init ((TransformFactory.Instance) proc.transformNode.deepCopy (copies));
    setParent (createXMLReader ());
    setTransformerHandlerResolver (proc.context.defaultTransformerHandlerResolver.customResolver);
    setOutputURIResolver (proc.context.outputUriResolver);

  }

  /**
   * Constructs a copy of this Processor.
   *
   * @throws SAXException
   *         if the construction of a new XML parser fails
   */
  public Processor copy () throws SAXException
  {
    return new Processor (this);
  }

  //
  // Methods
  //

  /**
   * Create an <code>XMLReader</code> object (a SAX Parser)
   *
   * @throws SAXException
   *         if a SAX Parser couldn't be created
   */
  public static XMLReader createXMLReader () throws SAXException
  {
    // Using pure SAX2, not JAXP
    XMLReader reader = null;
    try
    {
      // try default parser implementation
      reader = XMLReaderFactory.createXMLReader ();
    }
    catch (final SAXException e)
    {
      final String prop = System.getProperty ("org.xml.sax.driver");
      if (prop != null)
      {
        // property set, but still failed
        throw new SAXException ("Can't create XMLReader for class " + prop);
        // leave the method here
      }
      // try another SAX implementation
      final String PARSER_IMPLS[] = { "org.apache.xerces.parsers.SAXParser", // Xerces
                                      "org.apache.crimson.parser.XMLReaderImpl", // Crimson
                                      "gnu.xml.aelfred2.SAXDriver" // Aelfred
                                                                   // nonvalidating
      };
      for (final String element : PARSER_IMPLS)
      {
        try
        {
          reader = XMLReaderFactory.createXMLReader (element);
          break; // for (...)
        }
        catch (final SAXException e1)
        {} // continuing
      }
      if (reader == null)
      {
        throw new SAXException ("Can't find SAX parser implementation.\n" +
                                "Please specify a parser class via the system property " +
                                "'org.xml.sax.driver'");
      }
    }

    if (CSTX.DEBUG)
      log.debug ("Using " + reader.getClass ().getName ());
    return reader;
  }

  /**
   * Initialize a <code>Processor</code> object
   */
  private void init (final TransformFactory.Instance pTransformNode)
  {
    context = new Context ();

    context.emitter = initializeEmitter (context);

    eventStack = context.ancestorStack;

    setErrorHandler (context.errorHandler); // register error handler

    context.currentProcessor = this;
    context.currentGroup = context.targetGroup = transformNode = pTransformNode;

    // first Data frame; needed for the first target group
    dataStack.push (new Data (context));

    // initialize namespaces
    initNamespaces ();

    // array of global templates
    if (globalTemplates == null)
    {
      // note: getGlobalTemplates() returns null at the second invocation!
      final Vector tempVec = transformNode.getGlobalTemplates ();
      globalTemplates = new TemplateFactory.Instance [tempVec.size ()];
      tempVec.toArray (globalTemplates);
      Arrays.sort (globalTemplates);
    }
    initOutputProperties ();
  }

  /**
   * Create a fresh namespace hashtable
   */
  private void initNamespaces ()
  {
    inScopeNamespaces = new Hashtable<> ();
    inScopeNamespaces.put ("xml", NamespaceSupport.XMLNS);
  }

  /**
   * The initialization of the emitter could be overriden for debug purpose.
   *
   * @param ctx
   *        The current context
   * @return an emitter-instance
   */
  protected Emitter initializeEmitter (final Context ctx)
  {
    return new Emitter (ctx.errorHandler);
  }

  /**
   * Initialize the output properties to the values specified in the
   * transformation sheet or to their default values, resp.
   */
  public void initOutputProperties ()
  {
    outputProperties = new Properties ();
    outputProperties.setProperty (OutputKeys.ENCODING, transformNode.outputEncoding);
    outputProperties.setProperty (OutputKeys.MEDIA_TYPE, "text/xml");
    outputProperties.setProperty (OutputKeys.METHOD, transformNode.outputMethod);
    outputProperties.setProperty (OutputKeys.OMIT_XML_DECLARATION, "no");
    outputProperties.setProperty (OutputKeys.STANDALONE, "no");
    outputProperties.setProperty (OutputKeys.VERSION, "1.0");
  }

  /**
   * Assigns a parent to this filter instance. Attempts to register itself as a
   * lexical handler on this parent.
   */
  @Override
  public void setParent (final XMLReader parent)
  {
    super.setParent (parent);
    parent.setContentHandler (this); // necessary??

    try
    {
      parent.setProperty ("http://xml.org/sax/properties/lexical-handler", this);
    }
    catch (final SAXException ex)
    {
      log.warn ("Accessing " + parent + ": " + ex);
    }
  }

  /**
   * Registers a content handler.
   */
  @Override
  public void setContentHandler (final ContentHandler handler)
  {
    context.emitter.setContentHandler (handler);
  }

  /**
   * Registers a lexical handler.
   */
  public void setLexicalHandler (final LexicalHandler handler)
  {
    context.emitter.setLexicalHandler (handler);
  }

  /**
   * Registers a declaration handler. Does nothing at the moment.
   */
  public void setDeclHandler (final DeclHandler handler)
  {}

  /** Standard prefix for SAX2 properties */
  private static String PROP_PREFIX = "http://xml.org/sax/properties/";

  /**
   * Set the property of a value on the underlying XMLReader.
   */
  @Override
  public void setProperty (final String prop, final Object value) throws SAXNotRecognizedException,
                                                                  SAXNotSupportedException
  {
    if ((PROP_PREFIX + "lexical-handler").equals (prop))
      setLexicalHandler ((LexicalHandler) value);
    else
      if ((PROP_PREFIX + "declaration-handler").equals (prop))
        setDeclHandler ((DeclHandler) value);
      else
        super.setProperty (prop, value);
  }

  /**
   * Registers a <code>ErrorListener</code> object for reporting errors while
   * processing (transforming) the XML input
   */
  public void setErrorListener (final ErrorListener listener)
  {
    context.errorHandler.errorListener = listener;
  }

  /**
   * @return the output encoding specified in the STX transformation sheet
   */
  public String getOutputEncoding ()
  {
    return transformNode.outputEncoding;
  }

  /**
   * Sets a global parameter of the STX transformation sheet
   *
   * @param name
   *        the (expanded) parameter name
   * @param value
   *        the parameter value
   */
  public void setParameter (String name, final Object value)
  {
    if (!name.startsWith ("{"))
      name = "{}" + name;
    context.globalParameters.put (name, new Value (value));
  }

  /**
   * Returns a global parameter of the STX transformation sheet
   *
   * @param name
   *        the (expanded) parameter name
   * @return the parameter value or <code>null</code> if this parameter isn't
   *         present
   */
  public Object getParameter (String name)
  {
    if (!name.startsWith ("{"))
      name = "{}" + name;
    final Value param = context.globalParameters.get (name);
    try
    {
      if (param != null)
        return param.toJavaObject (Object.class);
    }
    catch (final EvalException ex)
    {
      // shouldn't happen here
      log.error ("Internal error", ex);
    }
    return null;
  }

  /**
   * Clear all preset parameters
   */
  public void clearParameters ()
  {
    context.globalParameters.clear ();
  }

  /**
   * Registers a custom {@link ITransformerHandlerResolver} object.
   *
   * @param resolver
   *        the resolver to be registered
   */
  public void setTransformerHandlerResolver (final ITransformerHandlerResolver resolver)
  {
    context.defaultTransformerHandlerResolver.customResolver = resolver;
  }

  /**
   * Registers a URIResolver for <code>stx:process-document</code>
   *
   * @param resolver
   *        the resolver to be registered
   */
  public void setURIResolver (final URIResolver resolver)
  {
    context.uriResolver = resolver;
  }

  /**
   * Registers an {@link IOutputURIResolver} for <code>stx:result-document</code>
   *
   * @param resolver
   *        the resolver to be registered
   */
  public void setOutputURIResolver (final IOutputURIResolver resolver)
  {
    context.outputUriResolver = resolver;
  }

  /**
   * Registers a message emitter for <code>stx:message</code>
   *
   * @param emitter
   *        the emitter object to be registered
   */
  public void setMessageEmitter (final IStxEmitter emitter)
  {
    context.messageEmitter = emitter;
  }

  /**
   * Starts the inner processing of a new buffer or another document by saving
   * the text data already read and jumping to the targetted group (if
   * specified).
   */
  public void startInnerProcessing ()
  {
    // there might be characters already read
    innerProcStack.push (collectedCharacters.toString ());
    collectedCharacters.setLength (0);
    innerProcStack.push (inScopeNamespaces);
    initNamespaces ();
    // possible jump to another group (changed visibleTemplates)
    dataStack.push (new Data (CSTX.PR_BUFFER, null, null, null, context));
  }

  /**
   * Ends the inner processing by restoring the collected text data.
   */
  public void endInnerProcessing () throws SAXException
  {
    // look-ahead mechanism
    if (lastElement != null)
      processLastElement (true);

    if (collectedCharacters.length () != 0)
      processCharacters ();

    // Clean up dataStack: terminate pending stx:process-siblings
    clearProcessSiblings ();

    // remove Data object from startInnerProcessing()
    context.localVars = dataStack.pop ().localVars;
    inScopeNamespaces = (Hashtable <String, String>) innerProcStack.pop ();
    collectedCharacters.append (innerProcStack.pop ());
  }

  /**
   * Check for the next best matching template after
   * <code>stx:process-self</code>
   *
   * @param temp
   *        a template matching the current node
   * @return <code>true</code> if this template hasn't been processed before
   */
  private boolean foundUnprocessedTemplate (final TemplateFactory.Instance temp)
  {
    for (int top = dataStack.size () - 1; top >= 0; top--)
    {
      final Data d = dataStack.elementAt (top);
      if (d.lastProcStatus == CSTX.PR_SELF)
      { // stx:process-self
        if (d.template == temp)
          return false; // no, this template was already in use
        // else continue
      }
      else
        return true; // yes, no process-self on top of the stack
    }
    return true; // yes, reached bottom of the stack
  }

  /**
   * @return the matching template for the current event stack.
   */
  private TemplateFactory.Instance findMatchingTemplate () throws SAXException
  {
    TemplateFactory.Instance found = null;
    TemplateFactory.Instance [] category = null;
    int tempIndex = -1;

    final Data top = dataStack.peek ();

    // Is the previous instruction not an stx:process-self?
    // used for performance (to prevent calling foundUnprocessedTemplate())
    final boolean notSelf = (top.lastProcStatus != CSTX.PR_SELF);

    // the three precedence categories
    final TemplateFactory.Instance precCats[][] = { top.targetGroup.visibleTemplates,
                                                    top.targetGroup.groupTemplates,
                                                    globalTemplates };

    // look up for a matching template in the categories
    for (int i = 0; i < precCats.length && category == null; i++)
      for (int j = 0; j < precCats[i].length; j++)
        if (precCats[i][j].matches (context, true) && (notSelf || foundUnprocessedTemplate (precCats[i][j])))
        {
          // bingo!
          category = precCats[i];
          tempIndex = j;
          break;
        }

    if (category != null)
    { // means, we found a template
      found = category[tempIndex];
      final double priority = found.getPriority ();
      // look for more templates with the same priority in the same
      // category
      if (++tempIndex < category.length && priority == category[tempIndex].getPriority ())
      {
        for (; tempIndex < category.length && priority == category[tempIndex].getPriority (); tempIndex++)
        {
          if (category[tempIndex].matches (context, false))
            context.errorHandler.error ("Ambigous template rule with priority " +
                                        priority +
                                        ", found matching template rule already in line " +
                                        found.lineNo,
                                        category[tempIndex].publicId,
                                        category[tempIndex].systemId,
                                        category[tempIndex].lineNo,
                                        category[tempIndex].colNo);
        }
      }
    }

    return found;
  }

  /** contains the last return value after processing STX instructions */
  private int processStatus;

  /**
   * Performs the processing of the linked instruction chain until an end
   * condition was met. This method stores the last return value in the class
   * member variable {@link #processStatus}.
   *
   * @param inst
   *        the first instruction in the chain
   * @param event
   *        the current event
   * @param skipProcessBase
   *        set if ProcessBase instructions shouldn't be reported
   * @return the last processed instruction
   */
  private AbstractInstruction doProcessLoop (AbstractInstruction inst,
                                             final SAXEvent event,
                                             final boolean skipProcessBase) throws SAXException
  {
    processStatus = CSTX.PR_CONTINUE;

    while (inst != null && processStatus == CSTX.PR_CONTINUE)
    {
      // check, if this is the original class: call process() directly
      if (isProcessorClass)
      {
        while (inst != null && processStatus == CSTX.PR_CONTINUE)
        {

          if (CSTX.DEBUG)
            if (log.isDebugEnabled ())
              log.debug (inst.lineNo + ": " + inst);

          processStatus = inst.process (context);
          inst = inst.next;
        }
      }
      // otherwise: this is a derived class
      else
      {
        while (inst != null && processStatus == CSTX.PR_CONTINUE)
        {
          // skip ProcessBase if requested
          if (skipProcessBase && inst.getNode () instanceof AbstractProcessBase)
            processStatus = inst.process (context);
          else
            processStatus = processInstruction (inst, event);
          inst = inst.next;
        }
      }

      if (processStatus == CSTX.PR_ATTRIBUTES)
      {
        // stx:process-attributes encountered
        // (i.e. the current node must be an element with attributes)
        processAttributes (event.attrs);
        processStatus = CSTX.PR_CONTINUE;
      }
    }
    return inst;
  }

  /**
   * Process an instruction. This method should be overridden for debug
   * purposes.
   *
   * @param inst
   *        The instruction which should be processed
   * @param event
   *        The current event
   * @return see {@link AbstractInstruction#process}
   * @throws SAXException
   *         in case of parse-errors
   */
  protected int processInstruction (final AbstractInstruction inst, final SAXEvent event) throws SAXException
  {
    // process instruction
    return inst.process (context);
  }

  /**
   * Processes the upper most event on the event stack.
   */
  private void processEvent () throws SAXException
  {
    final SAXEvent event = eventStack.peek ();
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
      {
        log.debug (event.toString ());
        log.debug (context.localVars.toString ());
      }

    if (dataStack.peek ().lastProcStatus == CSTX.PR_SIBLINGS)
      processSiblings ();

    final TemplateFactory.Instance temp = findMatchingTemplate ();
    if (temp != null)
    {
      AbstractInstruction inst = temp;
      context.localVars.clear ();
      final Hashtable currentParams = context.passedParameters;

      inst = doProcessLoop (inst, event, false);

      if (CSTX.DEBUG)
        if (log.isDebugEnabled ())
        {
          log.debug ("stop " + processStatus);
          log.debug (context.localVars.toString ());
        }

      switch (processStatus)
      {
        case CSTX.PR_CONTINUE:
          // templated finished
          if (event.type == SAXEvent.ELEMENT || event.type == SAXEvent.ROOT)
          {
            skipDepth = 1;
            collectedCharacters.setLength (0); // clear text
            insideCDATA = false; // reset if there was a CDATA section
          }
          break;

        case CSTX.PR_CHILDREN:
          // stx:process-children encountered
          dataStack.push (new Data (CSTX.PR_CHILDREN, temp, inst, currentParams, context));

          if (context.targetHandler != null)
          {
            // instruction had a filter attribute
            startExternDocument ();
            if (collectedCharacters.length () > 0)
            {
              context.targetHandler.characters (collectedCharacters.toString ().toCharArray (),
                                                0,
                                                collectedCharacters.length ());
              collectedCharacters.setLength (0);
            }
            skipDepth = 1;
          }
          break;

        case CSTX.PR_SELF:
          // stx:process-self encountered
          dataStack.push (new Data (CSTX.PR_SELF, temp, inst, currentParams, context));
          if (context.targetHandler != null)
          {
            // instruction had a filter attribute
            switch (event.type)
            {
              case SAXEvent.ELEMENT:
                startExternDocument ();
                context.targetHandler.startElement (event.uri, event.lName, event.qName, event.attrs);
                skipDepth = 1;
                break;

              case SAXEvent.TEXT:
                startExternDocument ();
                context.targetHandler.characters (event.value.toCharArray (), 0, event.value.length ());
                endExternDocument ();
                break;

              case SAXEvent.CDATA:
                startExternDocument ();
                context.targetHandler.startCDATA ();
                context.targetHandler.characters (event.value.toCharArray (), 0, event.value.length ());
                context.targetHandler.endCDATA ();
                endExternDocument ();
                break;

              case SAXEvent.PI:
                startExternDocument ();
                context.targetHandler.processingInstruction (event.qName, event.value);
                endExternDocument ();
                break;

              case SAXEvent.COMMENT:
                startExternDocument ();
                context.targetHandler.comment (event.value.toCharArray (), 0, event.value.length ());
                endExternDocument ();
                break;

              case SAXEvent.ROOT:
                context.targetHandler.startDocument ();
                skipDepth = 1;
                break;

              case SAXEvent.ATTRIBUTE:
                // nothing to do
                break;

              default:
                log.error ("Unexpected event: " + event);
            }
          }
          else
            processEvent (); // recurse
          if (event.type == SAXEvent.TEXT ||
              event.type == SAXEvent.CDATA ||
              event.type == SAXEvent.COMMENT ||
              event.type == SAXEvent.PI ||
              event.type == SAXEvent.ATTRIBUTE)
          {
            // no children present, continue processing
            dataStack.pop ();

            inst = doProcessLoop (inst, event, false);

            if (CSTX.DEBUG)
              if (log.isDebugEnabled ())
                log.debug ("stop " + processStatus);

            switch (processStatus)
            {
              case CSTX.PR_CHILDREN:
              case CSTX.PR_SELF:
                final AbstractNodeBase start = inst.getNode ();
                context.errorHandler.error ("Encountered '" +
                                            start.qName +
                                            "' after stx:process-self",
                                            start.publicId,
                                            start.systemId,
                                            start.lineNo,
                                            start.colNo);
                // falls through, if the error handler returns

              case CSTX.PR_ERROR:
                throw new SAXException ("Non-recoverable error");

              case CSTX.PR_SIBLINGS:
                dataStack.push (new Data (CSTX.PR_SIBLINGS, temp, inst, currentParams, context, event));
                break;
              // case PR_ATTRIBUTES: won't happen
              // case PR_CONTINUE: nothing to do
            }
          }
          break;

        case CSTX.PR_SIBLINGS:
          // stx:process-siblings encountered
          if (event.type == SAXEvent.ELEMENT || event.type == SAXEvent.ROOT)
          {
            // end of template reached, skip contents
            skipDepth = 1;
            collectedCharacters.setLength (0); // clear text
          }
          dataStack.push (new Data (CSTX.PR_SIBLINGS, temp, inst, currentParams, context, event));
          break;

        // case PR_ATTRIBUTES: won't happen

        case CSTX.PR_ERROR:
          // errorHandler returned after a fatal error
          throw new SAXException ("Non-recoverable error");

        default:
          // Mustn't happen
          final String msg = "Unexpected return value from process() " + processStatus;
          log.error (msg);
          throw new SAXException (msg);
      }
    }
    else
    {
      // no template found, default action
      final AbstractGroupBase tg = context.targetGroup;
      final Emitter emitter = context.emitter;
      switch (event.type)
      {
        case SAXEvent.ROOT:
          dataStack.push (new Data (dataStack.peek ()));
          break;

        case SAXEvent.ELEMENT:
          if ((tg.passThrough & PASS_THROUGH_ELEMENT) != 0)
            emitter.startElement (event.uri, event.lName, event.qName, event.attrs, event.namespaces, tg);
          dataStack.push (new Data (dataStack.peek ()));
          break;

        case SAXEvent.TEXT:
          if ((tg.passThrough & PASS_THROUGH_TEXT) != 0)
          {
            emitter.characters (event.value.toCharArray (), 0, event.value.length (), tg);
          }
          break;

        case SAXEvent.CDATA:
          if ((tg.passThrough & PASS_THROUGH_TEXT) != 0)
          {
            emitter.startCDATA (tg);
            emitter.characters (event.value.toCharArray (), 0, event.value.length (), tg);
            emitter.endCDATA ();
          }
          break;

        case SAXEvent.COMMENT:
          if ((tg.passThrough & PASS_THROUGH_COMMENT) != 0)
            emitter.comment (event.value.toCharArray (), 0, event.value.length (), tg);
          break;

        case SAXEvent.PI:
          if ((tg.passThrough & PASS_THROUGH_PI) != 0)
            emitter.processingInstruction (event.qName, event.value, tg);
          break;

        case SAXEvent.ATTRIBUTE:
          if ((tg.passThrough & PASS_THROUGH_ATTRIBUTE) != 0)
            emitter.addAttribute (event.uri, event.qName, event.lName, event.value, tg);
          break;

        default:
          log.error ("no default action for " + event);
      }
    }
  }

  /**
   * Process last element start (stored as {@link #lastElement} in
   * {@link #startElement startElement})
   */
  private void processLastElement (final boolean hasChildren) throws SAXException
  {
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
        log.debug (lastElement.toString ());

    // determine if the look-ahead is a text node
    final String s = collectedCharacters.toString ();
    if (s.length () == 0 || (context.targetGroup.stripSpace && s.trim ().length () == 0))
    {
      if (hasChildren)
        lastElement.enableChildNodes (true);
    }
    else
    {
      // set string value of the last element
      lastElement.value = s;
      lastElement.enableChildNodes (true);
    }

    // put last element on the event stack
    eventStack.peek ().countElement (lastElement.uri, lastElement.lName);
    eventStack.push (lastElement);

    lastElement = null;
    processEvent ();
  }

  /**
   * Process a text node (from several consecutive <code>characters</code>
   * events)
   */
  private void processCharacters () throws SAXException
  {
    final String s = collectedCharacters.toString ();

    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
        log.debug ("'" + s + "'");

    if (skipDepth > 0 && context.targetHandler != null)
    {
      if (insideCDATA)
      {
        context.targetHandler.startCDATA ();
        context.targetHandler.characters (s.toCharArray (), 0, s.length ());
        context.targetHandler.endCDATA ();
      }
      else
        context.targetHandler.characters (s.toCharArray (), 0, s.length ());
      collectedCharacters.setLength (0);
      return;
    }

    if (context.targetGroup.stripSpace && s.trim ().length () == 0)
    {
      collectedCharacters.setLength (0);
      return; // white-space only characters found, do nothing
    }

    SAXEvent ev;
    if (insideCDATA)
    {
      eventStack.peek ().countCDATA ();
      ev = SAXEvent.newCDATA (s);
    }
    else
    {
      eventStack.peek ().countText ();
      ev = SAXEvent.newText (s);
    }

    eventStack.push (ev);
    processEvent ();
    eventStack.pop ();

    collectedCharacters.setLength (0);
  }

  /**
   * Simulate events for each of the attributes of the current element. This
   * method will be called due to an <code>stx:process-attributes</code>
   * instruction.
   *
   * @param attrs
   *        the attributes to be processed
   */
  private void processAttributes (final Attributes attrs) throws SAXException
  {
    // actually only the target group need to be put on this stack ..
    // (for findMatchingTemplate)
    dataStack.push (new Data (CSTX.PR_ATTRIBUTES, null, null, null, context));
    for (int i = 0; i < attrs.getLength (); i++)
    {
      if (CSTX.DEBUG)
        if (log.isDebugEnabled ())
          log.debug (attrs.getQName (i));
      final SAXEvent ev = SAXEvent.newAttribute (attrs, i);
      eventStack.push (ev);
      processEvent ();
      eventStack.pop ();
      if (CSTX.DEBUG)
        if (log.isDebugEnabled ())
          log.debug ("done " + attrs.getQName (i));
    }
    final Data d = dataStack.pop ();
    // restore position, current group and variables
    context.position = d.contextPosition;
    context.currentGroup = d.currentGroup;
    context.localVars = d.localVars;
  }

  /**
   * Check and process pending templates whose processing was suspended by an
   * stx:process-siblings instruction
   */
  private void processSiblings () throws SAXException
  {
    Data stopData;
    int stopPos = 0;
    do
    {
      // check, if one of the last consecutive stx:process-siblings
      // terminates
      int stackPos = dataStack.size () - 1;
      Data data = dataStack.peek ();
      final Hashtable <String, Value> storedVars = context.localVars;
      stopData = null;
      do
      {
        context.localVars = data.localVars;
        if (!data.psiblings.matches (context))
        {
          stopData = data;
          stopPos = stackPos;
        }
        data = dataStack.elementAt (--stackPos);
      } while (data.lastProcStatus == CSTX.PR_SIBLINGS);
      context.localVars = storedVars;
      if (stopData != null) // the first of the non-matching process-sibs
        clearProcessSiblings (stopData, false);
      // If after clearing the process siblings instructions there is
      // a new PR_SIBLINGS on the stack, its match conditions must
      // be checked here, too.
    } while (stopData != null &&
             dataStack.size () == stopPos + 1 &&
             dataStack.peek ().lastProcStatus == CSTX.PR_SIBLINGS);
  }

  /**
   * Clear all consecutive pending <code>stx:process-siblings</code>
   * instructions on the top of {@link #dataStack}. Does nothing if there's no
   * <code>stx:process-siblings</code> pending.
   */
  private void clearProcessSiblings () throws SAXException
  {
    // find last of these consecutive stx:process-siblings instructions
    Data data, stopData = null;
    for (int i = dataStack.size () - 1; (data = dataStack.elementAt (i)).lastProcStatus == CSTX.PR_SIBLINGS; i--)
    {
      stopData = data;
    }
    if (stopData != null) // yep, found at least one
      clearProcessSiblings (stopData, true);
  }

  /**
   * Clear consecutive pending <code>stx:process-siblings</code> instructions on
   * the top of {@link #dataStack} until the passed object is encountered.
   *
   * @param stopData
   *        data for the last <code>stx:process-siblings</code> instruction
   * @param clearLast
   *        <code>true</code> if the template in <code>stopData</code> itself
   *        must be cleared
   */
  private void clearProcessSiblings (final Data stopData, final boolean clearLast) throws SAXException
  {
    // replace top-most event and local variables
    SAXEvent topEvent = null;
    // if clearLast==true then there's no event to remove,
    // because the end of of the parent has been encountered
    if (clearLast)
      topEvent = eventStack.peek ();
    else
      topEvent = eventStack.pop ();
    final Hashtable storedVars = context.localVars;
    Data data;
    do
    {
      data = dataStack.pop ();
      // put back stored event
      eventStack.push (data.sibEvent);
      context.position = data.contextPosition; // restore position
      context.localVars = data.localVars; // restore variables
      AbstractInstruction inst = data.instruction;

      do
      {
        inst = doProcessLoop (inst, topEvent, false);

        if (CSTX.DEBUG)
          if (log.isDebugEnabled ())
          {
            log.debug ("stop " + processStatus);
            log.debug (String.valueOf (context.localVars));
          }

        switch (processStatus)
        {
          case CSTX.PR_CHILDREN:
          case CSTX.PR_SELF:
            final AbstractNodeBase start = inst.getNode ();
            context.errorHandler.error ("Encountered '" +
                                        start.qName +
                                        "' after stx:process-siblings",
                                        start.publicId,
                                        start.systemId,
                                        start.lineNo,
                                        start.colNo);
            // falls through, if the error handler returns
          case CSTX.PR_ERROR:
            throw new SAXException ("Non-recoverable error");
            // case PR_ATTRIBUTES: won't happen
            // case PR_CONTINUE or PR_SIBLINGS: ok, nothing to do
        }

        // ignore further stx:process-siblings instructions in this
        // template if the processing was stopped by another
        // stx:process-siblings or clearLast==true
      } while (processStatus == CSTX.PR_SIBLINGS && (clearLast || data != stopData));

      if (processStatus == CSTX.PR_SIBLINGS)
      {
        // put back the last stx:process-siblings instruction
        stopData.instruction = inst;
        // there might have been a group attribute
        stopData.targetGroup = context.targetGroup;
        stopData.psiblings = context.psiblings;
        stopData.localVars = context.localVars;
        context.localVars = storedVars;
        dataStack.push (stopData);
      }
      // remove this event
      eventStack.pop ();
    } while (data != stopData); // last object

    // If the instruction before the last cleared process-siblings is a
    // process-self, we have to clear it too
    if (dataStack.peek ().lastProcStatus == CSTX.PR_SELF)
    {
      final SAXEvent selfEvent = data.sibEvent;
      // prepare the event stack
      eventStack.push (selfEvent);
      // put another namespace context on the stack because endElement()
      // will remove it
      namespaceContext.push (namespaceContext.peek ());
      // postpone the processing of character data
      final StringBuffer postponedCharacters = collectedCharacters;
      collectedCharacters = new StringBuffer ();
      endElement (selfEvent.uri, selfEvent.lName, selfEvent.qName);
      collectedCharacters = postponedCharacters;
    }

    // restore old event stack
    if (!clearLast)
      eventStack.push (topEvent);
  }

  /**
   * Emits a <code>startDocument</code> event to an external handler (in
   * {@link Context#targetHandler}), followed by all necessary namespace
   * declarations (<code>startPrefixMapping</code> events).
   */
  private void startExternDocument () throws SAXException
  {
    try
    {
      context.targetHandler.startDocument ();

      // declare current namespaces
      for (final Enumeration <String> e = inScopeNamespaces.keys (); e.hasMoreElements ();)
      {
        final String prefix = e.nextElement ();
        if (!prefix.equals ("xml"))
          context.targetHandler.startPrefixMapping (prefix, inScopeNamespaces.get (prefix));
      }

      // If the Map interface would be used:
      //
      // Map.Entry[] nsEntries = new Map.Entry[inScopeNamespaces.size()];
      // inScopeNamespaces.entrySet().toArray(nsEntries);
      // for (int i=0; i<nsEntries.length; i++) {
      // String prefix = (String)nsEntries[i].getKey();
      // if (!prefix.equals("xml"))
      // context.targetHandler.startPrefixMapping(
      // prefix, (String)nsEntries[i].getValue());
      // }

    }
    catch (final RuntimeException e)
    {
      // wrap exception
      java.io.StringWriter sw = null;
      sw = new java.io.StringWriter ();
      e.printStackTrace (new java.io.PrintWriter (sw));
      final AbstractNodeBase nb = context.currentInstruction;
      context.errorHandler.fatalError ("External processing failed: " +
                                       sw,
                                       nb.publicId,
                                       nb.systemId,
                                       nb.lineNo,
                                       nb.colNo,
                                       e);
    }
  }

  /**
   * Emits an <code>endDocument</code> event to an external handler (in
   * {@link Context#targetHandler}), preceded by all necessary namespace
   * undeclarations (<code>endPrefixMapping</code> events).
   */
  private void endExternDocument () throws SAXException
  {
    try
    {
      // undeclare current namespaces
      for (final Enumeration <String> e = inScopeNamespaces.keys (); e.hasMoreElements ();)
      {
        final String prefix = e.nextElement ();
        if (!prefix.equals ("xml"))
          context.targetHandler.endPrefixMapping (prefix);
      }

      // If the Map interface would be used
      //
      // Map.Entry[] nsEntries = new Map.Entry[inScopeNamespaces.size()];
      // inScopeNamespaces.entrySet().toArray(nsEntries);
      // for (int i=0; i<nsEntries.length; i++) {
      // String prefix = (String)nsEntries[i].getKey();
      // if (!prefix.equals("xml"))
      // context.targetHandler.endPrefixMapping(prefix);
      // }

      context.targetHandler.endDocument ();
      context.targetHandler = null;
    }
    catch (final RuntimeException e)
    {
      // wrap exception
      java.io.StringWriter sw = null;
      sw = new java.io.StringWriter ();
      e.printStackTrace (new java.io.PrintWriter (sw));
      final AbstractNodeBase nb = context.currentInstruction;
      context.errorHandler.fatalError ("External processing failed: " +
                                       sw,
                                       nb.publicId,
                                       nb.systemId,
                                       nb.lineNo,
                                       nb.colNo,
                                       e);
    }
  }

  // **********************************************************************

  //
  // from interface ContentHandler
  //

  @Override
  public void startDocument () throws SAXException
  {
    // perform this only at the begin of a transformation,
    // not at the begin of processing another document
    if (innerProcStack.empty ())
    {
      // initialize all group stx:variables
      transformNode.initGroupVariables (context);
      context.emitter.startDocument ();
    }
    else
    { // stx:process-document
      innerProcStack.push (eventStack);
      context.ancestorStack = eventStack = new Stack<> ();
    }

    eventStack.push (SAXEvent.newRoot ());

    processEvent ();
  }

  @Override
  public void endDocument () throws SAXException
  {
    if (collectedCharacters.length () != 0)
      processCharacters ();

    if (skipDepth == 1 && context.targetHandler != null && dataStack.peek ().lastProcStatus == CSTX.PR_CHILDREN)
    {
      // provisional fix for bug #765301
      // (see comment in endElement below)
      skipDepth = 0;
      endExternDocument ();
    }

    if (skipDepth == 0)
    {
      clearProcessSiblings ();
      final Data data = dataStack.pop ();
      context.currentGroup = data.currentGroup;
      context.targetGroup = data.targetGroup;
      final short prStatus = data.lastProcStatus;
      if (data.template == null)
      {
        // default action: nothing to do
      }
      else
        if (prStatus == CSTX.PR_CHILDREN || prStatus == CSTX.PR_SELF)
        {
          context.position = data.contextPosition; // restore position
          context.localVars = data.localVars;
          AbstractInstruction inst = data.instruction;
          inst = doProcessLoop (inst, eventStack.peek (), true);

          switch (processStatus)
          {
            case CSTX.PR_CHILDREN:
            case CSTX.PR_SELF:
              final AbstractNodeBase start = inst.getNode ();
              context.errorHandler.error ("Encountered '" +
                                          start.qName +
                                          "' after stx:process-" +
                                          // prStatus must be either PR_CHILDREN
                                          // or PR_SELF, see above
                                          (prStatus == CSTX.PR_CHILDREN ? "children" : "self"),
                                          start.publicId,
                                          start.systemId,
                                          start.lineNo,
                                          start.colNo);
              // falls through if the error handler returns
            case CSTX.PR_ERROR:
              throw new SAXException ("Non-recoverable error");
              // case PR_ATTRIBUTE:
              // case PR_SIBLINGS:
              // not possible because the context node is the document node
          }
        }
        else
        {
          log.error ("encountered 'else' " + prStatus);
        }
    }
    else
    {
      // no stx:process-children in match="/"
      skipDepth--;
      if (skipDepth == 0 && context.targetHandler != null)
        endExternDocument ();
    }

    if (skipDepth == 0)
    {
      // look at the previous process status on the stack
      if (dataStack.peek ().lastProcStatus == CSTX.PR_SELF)
        endDocument (); // recurse (process-self)
      else
      {
        eventStack.pop ();

        if (innerProcStack.empty ())
        {
          transformNode.exitRecursionLevel (context);
          context.emitter.endDocument (transformNode);
        }
        else
          eventStack = context.ancestorStack = (Stack <Object>) innerProcStack.pop ();
      }
    }
    else
      log.error ("skipDepth at document end: " + skipDepth);
  }

  @Override
  public void startElement (final String uri,
                            final String lName,
                            final String qName,
                            final Attributes attrs) throws SAXException
  {
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
      {
        log.debug (qName);
        log.debug ("eventStack: " + eventStack);
        log.debug ("dataStack: " + dataStack);
      }

    // look-ahead mechanism
    if (lastElement != null)
      processLastElement (true);

    if (collectedCharacters.length () != 0)
      processCharacters ();

    if (skipDepth > 0)
    {
      skipDepth++;
      if (context.targetHandler != null)
        context.targetHandler.startElement (uri, lName, qName, attrs);
      return;
    }

    lastElement = SAXEvent.newElement (uri, lName, qName, attrs, false, inScopeNamespaces);

    if (!nsContextActive)
    {
      namespaceContext.push (inScopeNamespaces);
      inScopeNamespaces = (Hashtable <String, String>) inScopeNamespaces.clone ();
    }
    nsContextActive = false;
  }

  @Override
  public void endElement (final String uri, final String lName, final String qName) throws SAXException
  {
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
      {
        log.debug (qName + " (skipDepth: " + skipDepth + ")");
        // log.debug("eventStack: " + eventStack.toString());
        // log.debug("dataStack: " + dataStack.toString());
      }

    if (lastElement != null)
      processLastElement (false);

    if (collectedCharacters.length () != 0)
      processCharacters ();

    if (skipDepth == 1 && context.targetHandler != null && dataStack.peek ().lastProcStatus == CSTX.PR_CHILDREN)
    {
      // provisional fix for bug #765301
      // (This whole external filter stuff needs to be rewritten to
      // enable the functionality for stx:process-siblings. Using
      // skipDepth isn't really a good idea ...)
      skipDepth = 0;
      endExternDocument ();
    }

    if (skipDepth == 0)
    {
      clearProcessSiblings ();

      final Data data = dataStack.pop ();
      final short prStatus = data.lastProcStatus;
      context.currentGroup = data.currentGroup;
      context.targetGroup = dataStack.peek ().targetGroup;
      if (data.template == null)
      {
        // perform default action?
        if ((data.targetGroup.passThrough & PASS_THROUGH_ELEMENT) != 0)
          context.emitter.endElement (uri, lName, qName, data.targetGroup);
      }
      else
        if (prStatus == CSTX.PR_CHILDREN || prStatus == CSTX.PR_SELF)
        {
          context.position = data.contextPosition; // restore position
          context.localVars = data.localVars;
          AbstractInstruction inst = data.instruction;
          inst = doProcessLoop (inst, eventStack.peek (), true);

          if (CSTX.DEBUG)
            if (log.isDebugEnabled ())
              log.debug ("stop " + processStatus);

          switch (processStatus)
          {
            case CSTX.PR_CHILDREN:
            case CSTX.PR_SELF:
            {
              final AbstractNodeBase start = inst.getNode ();
              context.errorHandler.error ("Encountered '" +
                                          start.qName +
                                          "' after stx:process-" +
                                          // prStatus must be either PR_CHILDREN
                                          // or PR_SELF, see above
                                          (prStatus == CSTX.PR_CHILDREN ? "children" : "self"),
                                          start.publicId,
                                          start.systemId,
                                          start.lineNo,
                                          start.colNo);
              throw new SAXException ("Non-recoverable error");
            }

            case CSTX.PR_SIBLINGS:
              dataStack.push (new Data (CSTX.PR_SIBLINGS,
                                        data.template,
                                        inst,
                                        data.passedParams,
                                        context,
                                        eventStack.peek ()));
              break;

            // case PR_ATTRIBUTES: won't happen

            case CSTX.PR_ERROR:
              throw new SAXException ("Non-recoverable error");
          }
        }
        else
        {
          log.error ("encountered 'else' " + prStatus);
        }
    }
    else
    {
      skipDepth--;
      if (context.targetHandler != null)
      {
        context.targetHandler.endElement (uri, lName, qName);
        if (skipDepth == 0)
          endExternDocument ();
      }
    }

    if (skipDepth == 0)
    {
      // look at the previous process status on the data stack
      if (dataStack.peek ().lastProcStatus == CSTX.PR_SELF)
      {
        endElement (uri, lName, qName); // recurse (process-self)
      }
      else
      {
        eventStack.pop ();
        inScopeNamespaces = namespaceContext.pop ();
      }
    }
  }

  @Override
  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    if (skipDepth > 0)
    {
      if (context.targetHandler != null)
        context.targetHandler.characters (ch, start, length);
      return;
    }
    collectedCharacters.append (ch, start, length);
  }

  @Override
  public void ignorableWhitespace (final char [] ch, final int start, final int length) throws SAXException
  {
    characters (ch, start, length);
  }

  @Override
  public void processingInstruction (final String target, final String data) throws SAXException
  {
    if (insideDTD)
      return;

    if (lastElement != null)
      processLastElement (true);

    if (collectedCharacters.length () != 0)
      processCharacters ();

    if (skipDepth > 0)
    {
      if (context.targetHandler != null)
        context.targetHandler.processingInstruction (target, data);
      return;
    }

    // don't modify the event stack after process-self
    eventStack.peek ().countPI (target);

    eventStack.push (SAXEvent.newPI (target, data));

    processEvent ();

    eventStack.pop ();
  }

  @Override
  public void startPrefixMapping (final String prefix, final String uri) throws SAXException
  {
    if (lastElement != null)
      processLastElement (true);

    if (skipDepth > 0)
    {
      if (context.targetHandler != null)
        context.targetHandler.startPrefixMapping (prefix, uri);
      return;
    }

    if (!nsContextActive)
    {
      namespaceContext.push (inScopeNamespaces);
      inScopeNamespaces = (Hashtable <String, String>) inScopeNamespaces.clone ();
      nsContextActive = true;
    }
    if (uri.equals ("")) // undeclare namespace
      inScopeNamespaces.remove (prefix);
    else
      inScopeNamespaces.put (prefix, uri);
  }

  @Override
  public void endPrefixMapping (final String prefix) throws SAXException
  {
    if (context.targetHandler != null)
      context.targetHandler.endPrefixMapping (prefix);
  }

  // public void skippedEntity(String name)
  // {
  // }

  /**
   * Store the locator in the context object
   */
  @Override
  public void setDocumentLocator (final Locator locator)
  {
    context.locator = locator;
  }

  //
  // from interface LexicalHandler
  //

  public void startDTD (final String name, final String publicId, final String systemId)
  {
    insideDTD = true;
  }

  public void endDTD ()
  {
    insideDTD = false;
  }

  public void startEntity (final java.lang.String name) throws SAXException
  {}

  public void endEntity (final java.lang.String name) throws SAXException
  {}

  public void startCDATA () throws SAXException
  {
    if (!context.targetGroup.recognizeCdata)
      return;

    if (CSTX.DEBUG)
      log.debug ("");

    if (skipDepth > 0)
    {
      if (context.targetHandler != null)
        context.targetHandler.startCDATA ();
      return;
    }

    if (collectedCharacters.length () != 0)
    {
      if (lastElement != null)
        processLastElement (true);
      processCharacters ();
      if (skipDepth > 0)
      {
        if (context.targetHandler != null)
          context.targetHandler.startCDATA ();
        return;
      }
    }

    insideCDATA = true;
  }

  public void endCDATA () throws SAXException
  {
    if (!context.targetGroup.recognizeCdata)
      return;

    if (skipDepth > 0)
    {
      if (context.targetHandler != null)
        context.targetHandler.endCDATA ();
      return;
    }

    if (lastElement != null)
      processLastElement (true);

    processCharacters (); // test for emptiness occurs there

    insideCDATA = false;
  }

  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
        log.debug (new String (ch, start, length));

    if (insideDTD)
      return;

    if (lastElement != null)
      processLastElement (true);

    if (collectedCharacters.length () != 0)
      processCharacters ();

    if (skipDepth > 0)
    {
      if (context.targetHandler != null)
        context.targetHandler.comment (ch, start, length);
      return;
    }

    // don't modify the event stack after process-self
    eventStack.peek ().countComment ();

    eventStack.push (SAXEvent.newComment (new String (ch, start, length)));

    processEvent ();

    eventStack.pop ();
  }

  //
  // ----------------------------new methods-----------------------------
  //

  /**
   * Returns a reference to the event stack.
   *
   * @return the event stack
   */
  public Stack <SAXEvent> getEventStack ()
  {
    return this.eventStack;
  }

  /**
   * Returns a reference to the data stack.
   *
   * @return the data stack
   */
  protected DataStack getDataStack ()
  {
    return this.dataStack;
  }

  /**
   * Returns a ref to the current context of the processing.
   *
   * @return the current context
   */
  public Context getContext ()
  {
    return this.context;
  }

  /**
   * Returns a ref to the registered emitter
   *
   * @return the emitter
   */
  public Emitter getEmitter ()
  {
    return context.emitter;
  }

  /**
   * Returns a ref to the last element (look ahead)
   *
   * @return the last element
   */
  protected SAXEvent getLastElement ()
  {
    return this.lastElement;
  }

  // **********************************************************************

  // private static long maxUsed = 0;
  // private static int initWait = 0;

  // private void traceMemory()
  // {
  // System.gc();
  // if (initWait < 20) {
  // initWait++;
  // return;
  // }

  // long total = Runtime.getRuntime().totalMemory();
  // long free = Runtime.getRuntime().freeMemory();
  // long used = total-free;
  // maxUsed = (used>maxUsed) ? used : maxUsed;
  // log.debug((total - free) + " = " + total + " - " + free +
  // " [" + maxUsed + "]");

  // /*
  // log.debug("templateStack: " + templateStack.size());
  // log.debug("templateProcStack: " + templateProcStack.size());
  // log.debug("categoryStack: " + categoryStack.size());
  // log.debug("eventStack: " + eventStack.size());
  // log.debug("newNs: " + newNs.size());
  // log.debug("collectedCharacters: " + collectedCharacters.capacity());
  // */
  // }
}
