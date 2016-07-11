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

import javax.annotation.Nonnull;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.URIResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import net.sf.joost.IOutputURIResolver;
import net.sf.joost.ITransformerHandlerResolver;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.instruction.AbstractGroupBase;
import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.instruction.AbstractProcessBase;
import net.sf.joost.instruction.PSiblingsFactory;
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
  public static final byte PASS_THROUGH_NONE = 0x0; // default, see Context
  public static final byte PASS_THROUGH_ELEMENT = 0x1;
  public static final byte PASS_THROUGH_TEXT = 0x2;
  public static final byte PASS_THROUGH_COMMENT = 0x4;
  public static final byte PASS_THROUGH_PI = 0x8;
  public static final byte PASS_THROUGH_ATTRIBUTE = 0x10;
  // all bits set
  public static final byte PASS_THROUGH_ALL = ~PASS_THROUGH_NONE;

  /** The node representing the transformation sheet */
  private TransformFactory.Instance m_aTransformNode;

  /**
   * Array of global visible templates (templates with an attribute
   * <code>visibility="global"</code>).
   */
  private TemplateFactory.Instance [] m_aGlobalTemplates;

  /** The Context object */
  private Context m_aContext;

  /**
   * Depth in the subtree to be skipped; increased by startElement and decreased
   * by endElement.
   */
  private int m_nSkipDepth = 0;

  /**
   * Set to true between {@link #startCDATA} and {@link #endCDATA}, needed for
   * CDATA processing
   */
  private boolean m_bInsideCDATA = false;

  /**
   * Set to true between {@link #startDTD} and {@link #endDTD}, needed for
   * ignoring comments
   */
  private boolean m_bInsideDTD = false;

  /** Buffer for collecting character data into single text nodes */
  private StringBuffer m_aCollectedCharacters = new StringBuffer ();

  /** Last event (this Processor uses one look-ahead) */
  private SAXEvent m_aLastElement = null;

  /** The namespaces of the current scope */
  private Hashtable <String, String> m_aInScopeNamespaces;

  /** The namespace context as a stack */
  private final Stack <Hashtable <String, String>> m_aNamespaceContext = new Stack<> ();

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
  private Stack <SAXEvent> m_aEventStack;

  /**
   * Stack needed for inner processing (buffers, documents). This stack stores
   * the event stack for <code>stx:process-document</code> and character data
   * that has been already read as look-ahead ({@link #m_aCollectedCharacters}).
   */
  private final Stack <Serializable> m_aInnerProcStack = new Stack<> ();

  public Properties m_aOutputProperties;

  private final boolean m_bIsProcessorClass = getClass ().equals (Processor.class);

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
    public Hashtable <String, Value> getPassedParams ()
    {
      return passedParams;
    }

    /** returns the value of {@link #localVars} */
    public Hashtable <String, Value> getLocalVars ()
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
             java.util.Arrays.asList (targetGroup.m_aVisibleTemplates) +
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

  private static Logger log = LoggerFactory.getLogger (Processor.class);

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
   * @param aReader
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
  public Processor (final XMLReader aReader, final InputSource src, final ParseContext pContext) throws IOException,
                                                                                                 SAXException
  {
    XMLReader reader = aReader;
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
    final HashMap <Object, Object> copies = new HashMap<> ();
    m_aGlobalTemplates = AbstractInstruction.deepTemplateArrayCopy (proc.m_aGlobalTemplates, copies);
    init ((TransformFactory.Instance) proc.m_aTransformNode.deepCopy (copies));
    setParent (createXMLReader ());
    setTransformerHandlerResolver (proc.m_aContext.defaultTransformerHandlerResolver.customResolver);
    setOutputURIResolver (proc.m_aContext.outputUriResolver);

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
    m_aContext = new Context ();

    m_aContext.m_aEmitter = initializeEmitter (m_aContext);

    m_aEventStack = m_aContext.ancestorStack;

    setErrorHandler (m_aContext.m_aErrorHandler); // register error handler

    m_aContext.currentProcessor = this;
    m_aContext.currentGroup = m_aContext.targetGroup = m_aTransformNode = pTransformNode;

    // first Data frame; needed for the first target group
    dataStack.push (new Data (m_aContext));

    // initialize namespaces
    initNamespaces ();

    // array of global templates
    if (m_aGlobalTemplates == null)
    {
      // note: getGlobalTemplates() returns null at the second invocation!
      final Vector <TemplateFactory.Instance> tempVec = m_aTransformNode.getGlobalTemplates ();
      m_aGlobalTemplates = new TemplateFactory.Instance [tempVec.size ()];
      tempVec.toArray (m_aGlobalTemplates);
      Arrays.sort (m_aGlobalTemplates);
    }
    initOutputProperties ();
  }

  /**
   * Create a fresh namespace hashtable
   */
  private void initNamespaces ()
  {
    m_aInScopeNamespaces = new Hashtable<> ();
    m_aInScopeNamespaces.put ("xml", NamespaceSupport.XMLNS);
  }

  /**
   * The initialization of the emitter could be overriden for debug purpose.
   *
   * @param ctx
   *        The current context
   * @return an emitter-instance
   */
  @Nonnull
  protected Emitter initializeEmitter (@Nonnull final Context ctx)
  {
    return new Emitter (ctx.m_aErrorHandler);
  }

  /**
   * Initialize the output properties to the values specified in the
   * transformation sheet or to their default values, resp.
   */
  public void initOutputProperties ()
  {
    m_aOutputProperties = new Properties ();
    m_aOutputProperties.setProperty (OutputKeys.ENCODING, m_aTransformNode.m_sOutputEncoding);
    m_aOutputProperties.setProperty (OutputKeys.MEDIA_TYPE, "text/xml");
    m_aOutputProperties.setProperty (OutputKeys.METHOD, m_aTransformNode.m_sOutputMethod);
    m_aOutputProperties.setProperty (OutputKeys.OMIT_XML_DECLARATION, "no");
    m_aOutputProperties.setProperty (OutputKeys.STANDALONE, "no");
    m_aOutputProperties.setProperty (OutputKeys.VERSION, "1.0");
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
    m_aContext.m_aEmitter.setContentHandler (handler);
  }

  /**
   * Registers a lexical handler.
   */
  public void setLexicalHandler (final LexicalHandler handler)
  {
    m_aContext.m_aEmitter.setLexicalHandler (handler);
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
    m_aContext.m_aErrorHandler.m_aErrorListener = listener;
  }

  /**
   * @return the output encoding specified in the STX transformation sheet
   */
  public String getOutputEncoding ()
  {
    return m_aTransformNode.m_sOutputEncoding;
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
    m_aContext.globalParameters.put (name, new Value (value));
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
    final Value param = m_aContext.globalParameters.get (name);
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
    m_aContext.globalParameters.clear ();
  }

  /**
   * Registers a custom {@link ITransformerHandlerResolver} object.
   *
   * @param resolver
   *        the resolver to be registered
   */
  public void setTransformerHandlerResolver (final ITransformerHandlerResolver resolver)
  {
    m_aContext.defaultTransformerHandlerResolver.customResolver = resolver;
  }

  /**
   * Registers a URIResolver for <code>stx:process-document</code>
   *
   * @param resolver
   *        the resolver to be registered
   */
  public void setURIResolver (final URIResolver resolver)
  {
    m_aContext.m_aURIResolver = resolver;
  }

  /**
   * Registers an {@link IOutputURIResolver} for
   * <code>stx:result-document</code>
   *
   * @param resolver
   *        the resolver to be registered
   */
  public void setOutputURIResolver (final IOutputURIResolver resolver)
  {
    m_aContext.outputUriResolver = resolver;
  }

  /**
   * Registers a message emitter for <code>stx:message</code>
   *
   * @param emitter
   *        the emitter object to be registered
   */
  public void setMessageEmitter (final IStxEmitter emitter)
  {
    m_aContext.messageEmitter = emitter;
  }

  /**
   * Starts the inner processing of a new buffer or another document by saving
   * the text data already read and jumping to the targetted group (if
   * specified).
   */
  public void startInnerProcessing ()
  {
    // there might be characters already read
    m_aInnerProcStack.push (m_aCollectedCharacters.toString ());
    m_aCollectedCharacters.setLength (0);
    m_aInnerProcStack.push (m_aInScopeNamespaces);
    initNamespaces ();
    // possible jump to another group (changed visibleTemplates)
    dataStack.push (new Data (CSTX.PR_BUFFER, null, null, null, m_aContext));
  }

  /**
   * Ends the inner processing by restoring the collected text data.
   */
  public void endInnerProcessing () throws SAXException
  {
    // look-ahead mechanism
    if (m_aLastElement != null)
      processLastElement (true);

    if (m_aCollectedCharacters.length () != 0)
      processCharacters ();

    // Clean up dataStack: terminate pending stx:process-siblings
    clearProcessSiblings ();

    // remove Data object from startInnerProcessing()
    m_aContext.localVars = dataStack.pop ().localVars;
    m_aInScopeNamespaces = (Hashtable <String, String>) m_aInnerProcStack.pop ();
    m_aCollectedCharacters.append (m_aInnerProcStack.pop ());
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
    final TemplateFactory.Instance precCats[][] = { top.targetGroup.m_aVisibleTemplates,
                                                    top.targetGroup.m_aGroupTemplates,
                                                    m_aGlobalTemplates };

    // look up for a matching template in the categories
    for (int i = 0; i < precCats.length && category == null; i++)
      for (int j = 0; j < precCats[i].length; j++)
        if (precCats[i][j].matches (m_aContext, true) && (notSelf || foundUnprocessedTemplate (precCats[i][j])))
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
          if (category[tempIndex].matches (m_aContext, false))
            m_aContext.m_aErrorHandler.error ("Ambigous template rule with priority " +
                                              priority +
                                              ", found matching template rule already in line " +
                                              found.lineNo,
                                              category[tempIndex].m_sPublicID,
                                              category[tempIndex].m_sSystemID,
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
      if (m_bIsProcessorClass)
      {
        while (inst != null && processStatus == CSTX.PR_CONTINUE)
        {

          if (CSTX.DEBUG)
            if (log.isDebugEnabled ())
              log.debug (inst.lineNo + ": " + inst);

          processStatus = inst.process (m_aContext);
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
            processStatus = inst.process (m_aContext);
          else
            processStatus = processInstruction (inst, event);
          inst = inst.next;
        }
      }

      if (processStatus == CSTX.PR_ATTRIBUTES)
      {
        // stx:process-attributes encountered
        // (i.e. the current node must be an element with attributes)
        processAttributes (event.m_aAttrs);
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
    return inst.process (m_aContext);
  }

  /**
   * Processes the upper most event on the event stack.
   */
  private void processEvent () throws SAXException
  {
    final SAXEvent event = m_aEventStack.peek ();
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
      {
        log.debug (event.toString ());
        log.debug (m_aContext.localVars.toString ());
      }

    if (dataStack.peek ().lastProcStatus == CSTX.PR_SIBLINGS)
      processSiblings ();

    final TemplateFactory.Instance temp = findMatchingTemplate ();
    if (temp != null)
    {
      AbstractInstruction inst = temp;
      m_aContext.localVars.clear ();
      final Hashtable currentParams = m_aContext.m_aPassedParameters;

      inst = doProcessLoop (inst, event, false);

      if (CSTX.DEBUG)
        if (log.isDebugEnabled ())
        {
          log.debug ("stop " + processStatus);
          log.debug (m_aContext.localVars.toString ());
        }

      switch (processStatus)
      {
        case CSTX.PR_CONTINUE:
          // templated finished
          if (event.m_nType == SAXEvent.ELEMENT || event.m_nType == SAXEvent.ROOT)
          {
            m_nSkipDepth = 1;
            m_aCollectedCharacters.setLength (0); // clear text
            m_bInsideCDATA = false; // reset if there was a CDATA section
          }
          break;

        case CSTX.PR_CHILDREN:
          // stx:process-children encountered
          dataStack.push (new Data (CSTX.PR_CHILDREN, temp, inst, currentParams, m_aContext));

          if (m_aContext.targetHandler != null)
          {
            // instruction had a filter attribute
            startExternDocument ();
            if (m_aCollectedCharacters.length () > 0)
            {
              m_aContext.targetHandler.characters (m_aCollectedCharacters.toString ().toCharArray (),
                                                   0,
                                                   m_aCollectedCharacters.length ());
              m_aCollectedCharacters.setLength (0);
            }
            m_nSkipDepth = 1;
          }
          break;

        case CSTX.PR_SELF:
          // stx:process-self encountered
          dataStack.push (new Data (CSTX.PR_SELF, temp, inst, currentParams, m_aContext));
          if (m_aContext.targetHandler != null)
          {
            // instruction had a filter attribute
            switch (event.m_nType)
            {
              case SAXEvent.ELEMENT:
                startExternDocument ();
                m_aContext.targetHandler.startElement (event.m_sURI,
                                                       event.m_sLocalName,
                                                       event.m_sQName,
                                                       event.m_aAttrs);
                m_nSkipDepth = 1;
                break;

              case SAXEvent.TEXT:
                startExternDocument ();
                m_aContext.targetHandler.characters (event.m_sValue.toCharArray (), 0, event.m_sValue.length ());
                endExternDocument ();
                break;

              case SAXEvent.CDATA:
                startExternDocument ();
                m_aContext.targetHandler.startCDATA ();
                m_aContext.targetHandler.characters (event.m_sValue.toCharArray (), 0, event.m_sValue.length ());
                m_aContext.targetHandler.endCDATA ();
                endExternDocument ();
                break;

              case SAXEvent.PI:
                startExternDocument ();
                m_aContext.targetHandler.processingInstruction (event.m_sQName, event.m_sValue);
                endExternDocument ();
                break;

              case SAXEvent.COMMENT:
                startExternDocument ();
                m_aContext.targetHandler.comment (event.m_sValue.toCharArray (), 0, event.m_sValue.length ());
                endExternDocument ();
                break;

              case SAXEvent.ROOT:
                m_aContext.targetHandler.startDocument ();
                m_nSkipDepth = 1;
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
          if (event.m_nType == SAXEvent.TEXT ||
              event.m_nType == SAXEvent.CDATA ||
              event.m_nType == SAXEvent.COMMENT ||
              event.m_nType == SAXEvent.PI ||
              event.m_nType == SAXEvent.ATTRIBUTE)
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
                m_aContext.m_aErrorHandler.error ("Encountered '" +
                                                  start.m_sQName +
                                                  "' after stx:process-self",
                                                  start.m_sPublicID,
                                                  start.m_sSystemID,
                                                  start.lineNo,
                                                  start.colNo);
                // falls through, if the error handler returns

              case CSTX.PR_ERROR:
                throw new SAXException ("Non-recoverable error");

              case CSTX.PR_SIBLINGS:
                dataStack.push (new Data (CSTX.PR_SIBLINGS, temp, inst, currentParams, m_aContext, event));
                break;
              // case PR_ATTRIBUTES: won't happen
              // case PR_CONTINUE: nothing to do
            }
          }
          break;

        case CSTX.PR_SIBLINGS:
          // stx:process-siblings encountered
          if (event.m_nType == SAXEvent.ELEMENT || event.m_nType == SAXEvent.ROOT)
          {
            // end of template reached, skip contents
            m_nSkipDepth = 1;
            m_aCollectedCharacters.setLength (0); // clear text
          }
          dataStack.push (new Data (CSTX.PR_SIBLINGS, temp, inst, currentParams, m_aContext, event));
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
      final AbstractGroupBase tg = m_aContext.targetGroup;
      final Emitter emitter = m_aContext.m_aEmitter;
      switch (event.m_nType)
      {
        case SAXEvent.ROOT:
          dataStack.push (new Data (dataStack.peek ()));
          break;

        case SAXEvent.ELEMENT:
          if ((tg.m_nPassThrough & PASS_THROUGH_ELEMENT) != 0)
            emitter.startElement (event.m_sURI,
                                  event.m_sLocalName,
                                  event.m_sQName,
                                  event.m_aAttrs,
                                  event.m_aNamespaces,
                                  tg);
          dataStack.push (new Data (dataStack.peek ()));
          break;

        case SAXEvent.TEXT:
          if ((tg.m_nPassThrough & PASS_THROUGH_TEXT) != 0)
          {
            emitter.characters (event.m_sValue.toCharArray (), 0, event.m_sValue.length (), tg);
          }
          break;

        case SAXEvent.CDATA:
          if ((tg.m_nPassThrough & PASS_THROUGH_TEXT) != 0)
          {
            emitter.startCDATA (tg);
            emitter.characters (event.m_sValue.toCharArray (), 0, event.m_sValue.length (), tg);
            emitter.endCDATA ();
          }
          break;

        case SAXEvent.COMMENT:
          if ((tg.m_nPassThrough & PASS_THROUGH_COMMENT) != 0)
            emitter.comment (event.m_sValue.toCharArray (), 0, event.m_sValue.length (), tg);
          break;

        case SAXEvent.PI:
          if ((tg.m_nPassThrough & PASS_THROUGH_PI) != 0)
            emitter.processingInstruction (event.m_sQName, event.m_sValue, tg);
          break;

        case SAXEvent.ATTRIBUTE:
          if ((tg.m_nPassThrough & PASS_THROUGH_ATTRIBUTE) != 0)
            emitter.addAttribute (event.m_sURI, event.m_sQName, event.m_sLocalName, event.m_sValue, tg);
          break;

        default:
          log.error ("no default action for " + event);
      }
    }
  }

  /**
   * Process last element start (stored as {@link #m_aLastElement} in
   * {@link #startElement startElement})
   */
  private void processLastElement (final boolean hasChildren) throws SAXException
  {
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
        log.debug (m_aLastElement.toString ());

    // determine if the look-ahead is a text node
    final String s = m_aCollectedCharacters.toString ();
    if (s.length () == 0 || (m_aContext.targetGroup.m_bStripSpace && s.trim ().length () == 0))
    {
      if (hasChildren)
        m_aLastElement.enableChildNodes (true);
    }
    else
    {
      // set string value of the last element
      m_aLastElement.m_sValue = s;
      m_aLastElement.enableChildNodes (true);
    }

    // put last element on the event stack
    m_aEventStack.peek ().countElement (m_aLastElement.m_sURI, m_aLastElement.m_sLocalName);
    m_aEventStack.push (m_aLastElement);

    m_aLastElement = null;
    processEvent ();
  }

  /**
   * Process a text node (from several consecutive <code>characters</code>
   * events)
   */
  private void processCharacters () throws SAXException
  {
    final String s = m_aCollectedCharacters.toString ();

    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
        log.debug ("'" + s + "'");

    if (m_nSkipDepth > 0 && m_aContext.targetHandler != null)
    {
      if (m_bInsideCDATA)
      {
        m_aContext.targetHandler.startCDATA ();
        m_aContext.targetHandler.characters (s.toCharArray (), 0, s.length ());
        m_aContext.targetHandler.endCDATA ();
      }
      else
        m_aContext.targetHandler.characters (s.toCharArray (), 0, s.length ());
      m_aCollectedCharacters.setLength (0);
      return;
    }

    if (m_aContext.targetGroup.m_bStripSpace && s.trim ().length () == 0)
    {
      m_aCollectedCharacters.setLength (0);
      return; // white-space only characters found, do nothing
    }

    SAXEvent ev;
    if (m_bInsideCDATA)
    {
      m_aEventStack.peek ().countCDATA ();
      ev = SAXEvent.newCDATA (s);
    }
    else
    {
      m_aEventStack.peek ().countText ();
      ev = SAXEvent.newText (s);
    }

    m_aEventStack.push (ev);
    processEvent ();
    m_aEventStack.pop ();

    m_aCollectedCharacters.setLength (0);
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
    dataStack.push (new Data (CSTX.PR_ATTRIBUTES, null, null, null, m_aContext));
    for (int i = 0; i < attrs.getLength (); i++)
    {
      if (CSTX.DEBUG)
        if (log.isDebugEnabled ())
          log.debug (attrs.getQName (i));
      final SAXEvent ev = SAXEvent.newAttribute (attrs, i);
      m_aEventStack.push (ev);
      processEvent ();
      m_aEventStack.pop ();
      if (CSTX.DEBUG)
        if (log.isDebugEnabled ())
          log.debug ("done " + attrs.getQName (i));
    }
    final Data d = dataStack.pop ();
    // restore position, current group and variables
    m_aContext.position = d.contextPosition;
    m_aContext.currentGroup = d.currentGroup;
    m_aContext.localVars = d.localVars;
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
      final Hashtable <String, Value> storedVars = m_aContext.localVars;
      stopData = null;
      do
      {
        m_aContext.localVars = data.localVars;
        if (!data.psiblings.matches (m_aContext))
        {
          stopData = data;
          stopPos = stackPos;
        }
        data = dataStack.elementAt (--stackPos);
      } while (data.lastProcStatus == CSTX.PR_SIBLINGS);
      m_aContext.localVars = storedVars;
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
      topEvent = m_aEventStack.peek ();
    else
      topEvent = m_aEventStack.pop ();
    final Hashtable storedVars = m_aContext.localVars;
    Data data;
    do
    {
      data = dataStack.pop ();
      // put back stored event
      m_aEventStack.push (data.sibEvent);
      m_aContext.position = data.contextPosition; // restore position
      m_aContext.localVars = data.localVars; // restore variables
      AbstractInstruction inst = data.instruction;

      do
      {
        inst = doProcessLoop (inst, topEvent, false);

        if (CSTX.DEBUG)
          if (log.isDebugEnabled ())
          {
            log.debug ("stop " + processStatus);
            log.debug (String.valueOf (m_aContext.localVars));
          }

        switch (processStatus)
        {
          case CSTX.PR_CHILDREN:
          case CSTX.PR_SELF:
            final AbstractNodeBase start = inst.getNode ();
            m_aContext.m_aErrorHandler.error ("Encountered '" +
                                              start.m_sQName +
                                              "' after stx:process-siblings",
                                              start.m_sPublicID,
                                              start.m_sSystemID,
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
        stopData.targetGroup = m_aContext.targetGroup;
        stopData.psiblings = m_aContext.psiblings;
        stopData.localVars = m_aContext.localVars;
        m_aContext.localVars = storedVars;
        dataStack.push (stopData);
      }
      // remove this event
      m_aEventStack.pop ();
    } while (data != stopData); // last object

    // If the instruction before the last cleared process-siblings is a
    // process-self, we have to clear it too
    if (dataStack.peek ().lastProcStatus == CSTX.PR_SELF)
    {
      final SAXEvent selfEvent = data.sibEvent;
      // prepare the event stack
      m_aEventStack.push (selfEvent);
      // put another namespace context on the stack because endElement()
      // will remove it
      m_aNamespaceContext.push (m_aNamespaceContext.peek ());
      // postpone the processing of character data
      final StringBuffer postponedCharacters = m_aCollectedCharacters;
      m_aCollectedCharacters = new StringBuffer ();
      endElement (selfEvent.m_sURI, selfEvent.m_sLocalName, selfEvent.m_sQName);
      m_aCollectedCharacters = postponedCharacters;
    }

    // restore old event stack
    if (!clearLast)
      m_aEventStack.push (topEvent);
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
      m_aContext.targetHandler.startDocument ();

      // declare current namespaces
      for (final Enumeration <String> e = m_aInScopeNamespaces.keys (); e.hasMoreElements ();)
      {
        final String prefix = e.nextElement ();
        if (!prefix.equals ("xml"))
          m_aContext.targetHandler.startPrefixMapping (prefix, m_aInScopeNamespaces.get (prefix));
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
      final AbstractNodeBase nb = m_aContext.currentInstruction;
      m_aContext.m_aErrorHandler.fatalError ("External processing failed: " +
                                             sw,
                                             nb.m_sPublicID,
                                             nb.m_sSystemID,
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
      for (final Enumeration <String> e = m_aInScopeNamespaces.keys (); e.hasMoreElements ();)
      {
        final String prefix = e.nextElement ();
        if (!prefix.equals ("xml"))
          m_aContext.targetHandler.endPrefixMapping (prefix);
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

      m_aContext.targetHandler.endDocument ();
      m_aContext.targetHandler = null;
    }
    catch (final RuntimeException e)
    {
      // wrap exception
      java.io.StringWriter sw = null;
      sw = new java.io.StringWriter ();
      e.printStackTrace (new java.io.PrintWriter (sw));
      final AbstractNodeBase nb = m_aContext.currentInstruction;
      m_aContext.m_aErrorHandler.fatalError ("External processing failed: " +
                                             sw,
                                             nb.m_sPublicID,
                                             nb.m_sSystemID,
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
    if (m_aInnerProcStack.empty ())
    {
      // initialize all group stx:variables
      m_aTransformNode.initGroupVariables (m_aContext);
      m_aContext.m_aEmitter.startDocument ();
    }
    else
    { // stx:process-document
      m_aInnerProcStack.push (m_aEventStack);
      m_aContext.ancestorStack = m_aEventStack = new Stack<> ();
    }

    m_aEventStack.push (SAXEvent.newRoot ());

    processEvent ();
  }

  @Override
  public void endDocument () throws SAXException
  {
    if (m_aCollectedCharacters.length () != 0)
      processCharacters ();

    if (m_nSkipDepth == 1 && m_aContext.targetHandler != null && dataStack.peek ().lastProcStatus == CSTX.PR_CHILDREN)
    {
      // provisional fix for bug #765301
      // (see comment in endElement below)
      m_nSkipDepth = 0;
      endExternDocument ();
    }

    if (m_nSkipDepth == 0)
    {
      clearProcessSiblings ();
      final Data data = dataStack.pop ();
      m_aContext.currentGroup = data.currentGroup;
      m_aContext.targetGroup = data.targetGroup;
      final short prStatus = data.lastProcStatus;
      if (data.template == null)
      {
        // default action: nothing to do
      }
      else
        if (prStatus == CSTX.PR_CHILDREN || prStatus == CSTX.PR_SELF)
        {
          m_aContext.position = data.contextPosition; // restore position
          m_aContext.localVars = data.localVars;
          AbstractInstruction inst = data.instruction;
          inst = doProcessLoop (inst, m_aEventStack.peek (), true);

          switch (processStatus)
          {
            case CSTX.PR_CHILDREN:
            case CSTX.PR_SELF:
              final AbstractNodeBase start = inst.getNode ();
              m_aContext.m_aErrorHandler.error ("Encountered '" +
                                                start.m_sQName +
                                                "' after stx:process-" +
                                                // prStatus must be either
                                                // PR_CHILDREN
                                                // or PR_SELF, see above
                                                (prStatus == CSTX.PR_CHILDREN ? "children" : "self"),
                                                start.m_sPublicID,
                                                start.m_sSystemID,
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
      m_nSkipDepth--;
      if (m_nSkipDepth == 0 && m_aContext.targetHandler != null)
        endExternDocument ();
    }

    if (m_nSkipDepth == 0)
    {
      // look at the previous process status on the stack
      if (dataStack.peek ().lastProcStatus == CSTX.PR_SELF)
        endDocument (); // recurse (process-self)
      else
      {
        m_aEventStack.pop ();

        if (m_aInnerProcStack.empty ())
        {
          m_aTransformNode.exitRecursionLevel (m_aContext);
          m_aContext.m_aEmitter.endDocument (m_aTransformNode);
        }
        else
          m_aEventStack = m_aContext.ancestorStack = (Stack <SAXEvent>) m_aInnerProcStack.pop ();
      }
    }
    else
      log.error ("skipDepth at document end: " + m_nSkipDepth);
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
        log.debug ("eventStack: " + m_aEventStack);
        log.debug ("dataStack: " + dataStack);
      }

    // look-ahead mechanism
    if (m_aLastElement != null)
      processLastElement (true);

    if (m_aCollectedCharacters.length () != 0)
      processCharacters ();

    if (m_nSkipDepth > 0)
    {
      m_nSkipDepth++;
      if (m_aContext.targetHandler != null)
        m_aContext.targetHandler.startElement (uri, lName, qName, attrs);
      return;
    }

    m_aLastElement = SAXEvent.newElement (uri, lName, qName, attrs, false, m_aInScopeNamespaces);

    if (!nsContextActive)
    {
      m_aNamespaceContext.push (m_aInScopeNamespaces);
      m_aInScopeNamespaces = (Hashtable <String, String>) m_aInScopeNamespaces.clone ();
    }
    nsContextActive = false;
  }

  @Override
  public void endElement (final String uri, final String lName, final String qName) throws SAXException
  {
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
      {
        log.debug (qName + " (skipDepth: " + m_nSkipDepth + ")");
        // log.debug("eventStack: " + eventStack.toString());
        // log.debug("dataStack: " + dataStack.toString());
      }

    if (m_aLastElement != null)
      processLastElement (false);

    if (m_aCollectedCharacters.length () != 0)
      processCharacters ();

    if (m_nSkipDepth == 1 && m_aContext.targetHandler != null && dataStack.peek ().lastProcStatus == CSTX.PR_CHILDREN)
    {
      // provisional fix for bug #765301
      // (This whole external filter stuff needs to be rewritten to
      // enable the functionality for stx:process-siblings. Using
      // skipDepth isn't really a good idea ...)
      m_nSkipDepth = 0;
      endExternDocument ();
    }

    if (m_nSkipDepth == 0)
    {
      clearProcessSiblings ();

      final Data data = dataStack.pop ();
      final short prStatus = data.lastProcStatus;
      m_aContext.currentGroup = data.currentGroup;
      m_aContext.targetGroup = dataStack.peek ().targetGroup;
      if (data.template == null)
      {
        // perform default action?
        if ((data.targetGroup.m_nPassThrough & PASS_THROUGH_ELEMENT) != 0)
          m_aContext.m_aEmitter.endElement (uri, lName, qName, data.targetGroup);
      }
      else
        if (prStatus == CSTX.PR_CHILDREN || prStatus == CSTX.PR_SELF)
        {
          m_aContext.position = data.contextPosition; // restore position
          m_aContext.localVars = data.localVars;
          AbstractInstruction inst = data.instruction;
          inst = doProcessLoop (inst, m_aEventStack.peek (), true);

          if (CSTX.DEBUG)
            if (log.isDebugEnabled ())
              log.debug ("stop " + processStatus);

          switch (processStatus)
          {
            case CSTX.PR_CHILDREN:
            case CSTX.PR_SELF:
            {
              final AbstractNodeBase start = inst.getNode ();
              m_aContext.m_aErrorHandler.error ("Encountered '" +
                                                start.m_sQName +
                                                "' after stx:process-" +
                                                // prStatus must be either
                                                // PR_CHILDREN
                                                // or PR_SELF, see above
                                                (prStatus == CSTX.PR_CHILDREN ? "children" : "self"),
                                                start.m_sPublicID,
                                                start.m_sSystemID,
                                                start.lineNo,
                                                start.colNo);
              throw new SAXException ("Non-recoverable error");
            }

            case CSTX.PR_SIBLINGS:
              dataStack.push (new Data (CSTX.PR_SIBLINGS,
                                        data.template,
                                        inst,
                                        data.passedParams,
                                        m_aContext,
                                        m_aEventStack.peek ()));
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
      m_nSkipDepth--;
      if (m_aContext.targetHandler != null)
      {
        m_aContext.targetHandler.endElement (uri, lName, qName);
        if (m_nSkipDepth == 0)
          endExternDocument ();
      }
    }

    if (m_nSkipDepth == 0)
    {
      // look at the previous process status on the data stack
      if (dataStack.peek ().lastProcStatus == CSTX.PR_SELF)
      {
        endElement (uri, lName, qName); // recurse (process-self)
      }
      else
      {
        m_aEventStack.pop ();
        m_aInScopeNamespaces = m_aNamespaceContext.pop ();
      }
    }
  }

  @Override
  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    if (m_nSkipDepth > 0)
    {
      if (m_aContext.targetHandler != null)
        m_aContext.targetHandler.characters (ch, start, length);
      return;
    }
    m_aCollectedCharacters.append (ch, start, length);
  }

  @Override
  public void ignorableWhitespace (final char [] ch, final int start, final int length) throws SAXException
  {
    characters (ch, start, length);
  }

  @Override
  public void processingInstruction (final String target, final String data) throws SAXException
  {
    if (m_bInsideDTD)
      return;

    if (m_aLastElement != null)
      processLastElement (true);

    if (m_aCollectedCharacters.length () != 0)
      processCharacters ();

    if (m_nSkipDepth > 0)
    {
      if (m_aContext.targetHandler != null)
        m_aContext.targetHandler.processingInstruction (target, data);
      return;
    }

    // don't modify the event stack after process-self
    m_aEventStack.peek ().countPI (target);

    m_aEventStack.push (SAXEvent.newPI (target, data));

    processEvent ();

    m_aEventStack.pop ();
  }

  @Override
  public void startPrefixMapping (final String prefix, final String uri) throws SAXException
  {
    if (m_aLastElement != null)
      processLastElement (true);

    if (m_nSkipDepth > 0)
    {
      if (m_aContext.targetHandler != null)
        m_aContext.targetHandler.startPrefixMapping (prefix, uri);
      return;
    }

    if (!nsContextActive)
    {
      m_aNamespaceContext.push (m_aInScopeNamespaces);
      m_aInScopeNamespaces = (Hashtable <String, String>) m_aInScopeNamespaces.clone ();
      nsContextActive = true;
    }
    if (uri.equals ("")) // undeclare namespace
      m_aInScopeNamespaces.remove (prefix);
    else
      m_aInScopeNamespaces.put (prefix, uri);
  }

  @Override
  public void endPrefixMapping (final String prefix) throws SAXException
  {
    if (m_aContext.targetHandler != null)
      m_aContext.targetHandler.endPrefixMapping (prefix);
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
    m_aContext.locator = locator;
  }

  //
  // from interface LexicalHandler
  //

  public void startDTD (final String name, final String publicId, final String systemId)
  {
    m_bInsideDTD = true;
  }

  public void endDTD ()
  {
    m_bInsideDTD = false;
  }

  public void startEntity (final java.lang.String name) throws SAXException
  {}

  public void endEntity (final java.lang.String name) throws SAXException
  {}

  public void startCDATA () throws SAXException
  {
    if (!m_aContext.targetGroup.m_bRecognizeCdata)
      return;

    if (CSTX.DEBUG)
      log.debug ("");

    if (m_nSkipDepth > 0)
    {
      if (m_aContext.targetHandler != null)
        m_aContext.targetHandler.startCDATA ();
      return;
    }

    if (m_aCollectedCharacters.length () != 0)
    {
      if (m_aLastElement != null)
        processLastElement (true);
      processCharacters ();
      if (m_nSkipDepth > 0)
      {
        if (m_aContext.targetHandler != null)
          m_aContext.targetHandler.startCDATA ();
        return;
      }
    }

    m_bInsideCDATA = true;
  }

  public void endCDATA () throws SAXException
  {
    if (!m_aContext.targetGroup.m_bRecognizeCdata)
      return;

    if (m_nSkipDepth > 0)
    {
      if (m_aContext.targetHandler != null)
        m_aContext.targetHandler.endCDATA ();
      return;
    }

    if (m_aLastElement != null)
      processLastElement (true);

    processCharacters (); // test for emptiness occurs there

    m_bInsideCDATA = false;
  }

  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    if (CSTX.DEBUG)
      if (log.isDebugEnabled ())
        log.debug (new String (ch, start, length));

    if (m_bInsideDTD)
      return;

    if (m_aLastElement != null)
      processLastElement (true);

    if (m_aCollectedCharacters.length () != 0)
      processCharacters ();

    if (m_nSkipDepth > 0)
    {
      if (m_aContext.targetHandler != null)
        m_aContext.targetHandler.comment (ch, start, length);
      return;
    }

    // don't modify the event stack after process-self
    m_aEventStack.peek ().countComment ();

    m_aEventStack.push (SAXEvent.newComment (new String (ch, start, length)));

    processEvent ();

    m_aEventStack.pop ();
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
    return this.m_aEventStack;
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
    return this.m_aContext;
  }

  /**
   * Returns a ref to the registered emitter
   *
   * @return the emitter
   */
  public Emitter getEmitter ()
  {
    return m_aContext.m_aEmitter;
  }

  /**
   * Returns a ref to the last element (look ahead)
   *
   * @return the last element
   */
  protected SAXEvent getLastElement ()
  {
    return this.m_aLastElement;
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
