/**
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.1 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is: this file
 *
 *  The Initial Developer of the Original Code is Oliver Becker.
 *
 *  Portions created by Philip Helger
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.stx;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.NamespaceSupport;

import net.sf.joost.CSTX;
import net.sf.joost.instruction.AbstractFactoryBase;
import net.sf.joost.instruction.AbstractGroupBase;
import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.instruction.AnalyzeTextFactory;
import net.sf.joost.instruction.AssignFactory;
import net.sf.joost.instruction.AttributeFactory;
import net.sf.joost.instruction.BufferFactory;
import net.sf.joost.instruction.CallProcedureFactory;
import net.sf.joost.instruction.CdataFactory;
import net.sf.joost.instruction.ChooseFactory;
import net.sf.joost.instruction.CommentFactory;
import net.sf.joost.instruction.CopyFactory;
import net.sf.joost.instruction.DoctypeFactory;
import net.sf.joost.instruction.ElementEndFactory;
import net.sf.joost.instruction.ElementFactory;
import net.sf.joost.instruction.ElementStartFactory;
import net.sf.joost.instruction.ElseFactory;
import net.sf.joost.instruction.ForEachFactory;
import net.sf.joost.instruction.GroupFactory;
import net.sf.joost.instruction.IfFactory;
import net.sf.joost.instruction.IncludeFactory;
import net.sf.joost.instruction.LitElementFactory;
import net.sf.joost.instruction.MatchFactory;
import net.sf.joost.instruction.MessageFactory;
import net.sf.joost.instruction.NSAliasFactory;
import net.sf.joost.instruction.NoMatchFactory;
import net.sf.joost.instruction.OtherwiseFactory;
import net.sf.joost.instruction.PAttributesFactory;
import net.sf.joost.instruction.PBufferFactory;
import net.sf.joost.instruction.PChildrenFactory;
import net.sf.joost.instruction.PDocumentFactory;
import net.sf.joost.instruction.PIFactory;
import net.sf.joost.instruction.PSelfFactory;
import net.sf.joost.instruction.PSiblingsFactory;
import net.sf.joost.instruction.ParamFactory;
import net.sf.joost.instruction.ProcedureFactory;
import net.sf.joost.instruction.ResultBufferFactory;
import net.sf.joost.instruction.ResultDocumentFactory;
import net.sf.joost.instruction.ScriptFactory;
import net.sf.joost.instruction.TemplateFactory;
import net.sf.joost.instruction.TextFactory;
import net.sf.joost.instruction.TextNode;
import net.sf.joost.instruction.TransformFactory;
import net.sf.joost.instruction.ValueOfFactory;
import net.sf.joost.instruction.VariableFactory;
import net.sf.joost.instruction.WhenFactory;
import net.sf.joost.instruction.WhileFactory;
import net.sf.joost.instruction.WithParamFactory;

/**
 * Creates the tree representation of an STX transformation sheet. The Parser
 * object acts as a SAX ContentHandler.
 *
 * @version $Revision: 2.23 $ $Date: 2007/11/25 19:33:34 $
 * @author Oliver Becker
 */

public class Parser implements ContentHandler // , ErrorHandler
{
  /** The context object for parsing */
  private final ParseContext m_aPContext;

  /** Stack for opened elements, contains Node instances. */
  private final Stack <AbstractNodeBase> openedElements;

  /** The current (last created) Node. */
  private AbstractNodeBase currentNode;

  /** Hashtable for STX factory objects, one for each type. */
  private final Map <String, AbstractFactoryBase> stxFactories;

  /** Hashtable for Joost extension factory objects, one for each type. */
  private final Map <String, AbstractFactoryBase> joostFactories;

  /** The factory for literal result elements. */
  private final LitElementFactory litFac;

  /** Hashtable: keys = prefixes, values = URI stacks */
  private final Map <String, Stack <String>> inScopeNamespaces;

  /**
   * Hashtable for newly declared namespaces between literal elements; keys =
   * prefixes, values = URIs
   */
  private Hashtable <String, String> newNamespaces;

  /**
   * List of nodes that need another call to {@link AbstractNodeBase#compile}
   */
  public List <AbstractNodeBase> compilableNodes = new Vector<> ();

  /**
   * Group which had an <code>stx:include</code>, which in turn created this
   * Parser object
   */
  public AbstractGroupBase includingGroup;

  /** An optional ParserListener */
  private final IParserListener m_aParserListener;

  //
  // Constructor
  //

  /** Constructs a new Parser instance. */
  public Parser (final ParseContext pContext)
  {
    this.m_aPContext = pContext;
    this.m_aParserListener = pContext.parserListener;

    // factories for elements from the STX namespace
    final AbstractFactoryBase [] stxFacs = { new TransformFactory (),
                                             new GroupFactory (),
                                             new IncludeFactory (),
                                             new NSAliasFactory (),
                                             new TemplateFactory (),
                                             new ProcedureFactory (),
                                             new CallProcedureFactory (),
                                             new ParamFactory (),
                                             new VariableFactory (),
                                             new AssignFactory (),
                                             new WithParamFactory (),
                                             new ValueOfFactory (),
                                             new PChildrenFactory (),
                                             new PSelfFactory (),
                                             new PSiblingsFactory (),
                                             new PAttributesFactory (),
                                             new AnalyzeTextFactory (),
                                             new MatchFactory (),
                                             new NoMatchFactory (),
                                             new PDocumentFactory (),
                                             new ResultDocumentFactory (),
                                             new BufferFactory (),
                                             new ResultBufferFactory (),
                                             new PBufferFactory (),
                                             new CopyFactory (),
                                             new TextFactory (),
                                             new CdataFactory (),
                                             new AttributeFactory (),
                                             new ElementFactory (),
                                             new ElementStartFactory (),
                                             new ElementEndFactory (),
                                             new CommentFactory (),
                                             new PIFactory (),
                                             new ForEachFactory (),
                                             new WhileFactory (),
                                             new IfFactory (),
                                             new ElseFactory (),
                                             new ChooseFactory (),
                                             new WhenFactory (),
                                             new OtherwiseFactory (),
                                             new MessageFactory (),
                                             new DoctypeFactory () };
    stxFactories = createFactoryMap (stxFacs);

    // factories for elements from the Joost namespace
    final AbstractFactoryBase [] joostFacs = { new ScriptFactory () };
    joostFactories = createFactoryMap (joostFacs);

    litFac = new LitElementFactory ();
    openedElements = new Stack<> ();
    inScopeNamespaces = new Hashtable<> ();
    newNamespaces = new Hashtable<> ();
  }

  /**
   * creates hashtable and sets its initial content to the given array
   *
   * @param data
   *        to be filled in the map
   * @return the created hashtable
   */
  private Hashtable <String, AbstractFactoryBase> createFactoryMap (final AbstractFactoryBase [] data)
  {
    final Hashtable <String, AbstractFactoryBase> map = new Hashtable<> (data.length);
    for (final AbstractFactoryBase element : data)
      map.put (element.getName (), element);
    return map;
  }

  /**
   * @return the STX node factories, indexed by local name
   */
  public Map <String, AbstractFactoryBase> getFactories ()
  {
    return stxFactories;
  }

  /**
   * @return the root node representing <code>stx:transform</code>.
   */
  public TransformFactory.Instance getTransformNode ()
  {
    return m_aPContext.transformNode;
  }

  /** Buffer for collecting consecutive character data */
  private final StringBuffer collectedCharacters = new StringBuffer ();

  /** Processes collected character fragments */
  private void processCharacters () throws SAXParseException
  {
    final String s = collectedCharacters.toString ();
    if (currentNode.m_bPreserveSpace || s.trim ().length () != 0)
    {
      if (currentNode instanceof AbstractGroupBase)
      {
        if (s.trim ().length () != 0)
          throw new SAXParseException ("Text must not occur on group level", m_aPContext.locator);
      }
      else
      {
        final AbstractNodeBase textNode = new TextNode (s, currentNode, m_aPContext);
        currentNode.insert (textNode);
        if (m_aParserListener != null)
          m_aParserListener.nodeCreated (textNode);
      }
    }
    collectedCharacters.setLength (0);
  }

  //
  // from interface ContentHandler
  //

  public void setDocumentLocator (final Locator locator)
  {
    m_aPContext.locator = locator;
  }

  public void startDocument () throws SAXException
  {
    // declare xml namespace
    startPrefixMapping ("xml", NamespaceSupport.XMLNS);
  }

  public void endDocument () throws SAXException
  {
    endPrefixMapping ("xml");
    if (includingGroup != null)
      return;
    try
    {
      // call compile() method for those nodes that have requested to
      int pass = 0;
      int size;
      while ((size = compilableNodes.size ()) != 0)
      {
        pass++;
        final AbstractNodeBase nodes[] = new AbstractNodeBase [size];
        compilableNodes.toArray (nodes);
        compilableNodes.clear (); // for the next pass
        for (int i = 0; i < size; i++)
          if (nodes[i].compile (pass, m_aPContext))
          {
            // still need another invocation
            compilableNodes.add (nodes[i]);
          }
      }
      compilableNodes = null; // for garbage collection

      if (m_aParserListener != null)
        m_aParserListener.parseFinished ();
    }
    catch (final SAXParseException ex)
    {
      m_aPContext.getErrorHandler ().error (ex);
    }
  }

  public void startElement (final String uri,
                            final String lName,
                            final String qName,
                            final Attributes attrs) throws SAXException
  {
    try
    {
      if (collectedCharacters.length () != 0)
        processCharacters ();

      AbstractNodeBase newNode;
      m_aPContext.nsSet = getInScopeNamespaces ();
      if (CSTX.STX_NS.equals (uri))
      {
        final AbstractFactoryBase fac = stxFactories.get (lName);
        if (fac == null)
          throw new SAXParseException ("Unknown statement '" + qName + "'", m_aPContext.locator);
        newNode = fac.createNode (currentNode != null ? currentNode : includingGroup, qName, attrs, m_aPContext);
        if (m_aPContext.transformNode == null)
          try
          {
            m_aPContext.transformNode = (TransformFactory.Instance) newNode;
          }
          catch (final ClassCastException cce)
          {
            throw new SAXParseException ("Found '" +
                                         qName +
                                         "' as root element, " +
                                         "file is not an STX transformation sheet",
                                         m_aPContext.locator);
          }
        // if this is an instruction that may create a new namespace,
        // use the full set of namespaces in the next literal element
        if (fac instanceof CopyFactory || fac instanceof ElementFactory || fac instanceof ElementStartFactory)
          newNamespaces = getInScopeNamespaces ();
      }
      else
        if (CSTX.JOOST_EXT_NS.equals (uri))
        {
          final AbstractFactoryBase fac = joostFactories.get (lName);
          if (fac == null)
            throw new SAXParseException ("Unknown statement '" + qName + "'", m_aPContext.locator);
          newNode = fac.createNode (currentNode != null ? currentNode : includingGroup, qName, attrs, m_aPContext);
        }
        else
        {
          newNode = litFac.createNode (currentNode, uri, lName, qName, attrs, m_aPContext, newNamespaces);
          // reset these newly declared namespaces
          // newNode "consumes" the old value (without copy)
          newNamespaces = new Hashtable<> ();
        }

      // check xml:space attribute
      final int spaceIndex = attrs.getIndex (NamespaceSupport.XMLNS, "space");
      if (spaceIndex != -1)
      { // attribute present
        final String spaceAtt = attrs.getValue (spaceIndex);
        if ("preserve".equals (spaceAtt))
          newNode.m_bPreserveSpace = true;
        else
          if (!"default".equals (spaceAtt))
            throw new SAXParseException ("Value of attribute '" +
                                         attrs.getQName (spaceIndex) +
                                         "' must be either 'preserve' or 'default' (found '" +
                                         spaceAtt +
                                         "')",
                                         m_aPContext.locator);
        // "default" means false -> nothing to do
      }
      else
        if (newNode instanceof TextFactory.Instance || newNode instanceof CdataFactory.Instance)
          // these elements behave as
          // if xml:space was set to
          // "preserve"
          newNode.m_bPreserveSpace = true;
        else
          if (currentNode != null)
            // inherit from parent
            newNode.m_bPreserveSpace = currentNode.m_bPreserveSpace;

      if (currentNode != null)
        currentNode.insert (newNode);
      openedElements.push (currentNode);
      currentNode = newNode;

      if (m_aParserListener != null)
        m_aParserListener.nodeCreated (newNode);
    }
    catch (final SAXParseException ex)
    {
      m_aPContext.getErrorHandler ().error (ex);
    }
  }

  public void endElement (final String uri, final String lName, final String qName) throws SAXException
  {
    try
    {
      if (collectedCharacters.length () != 0)
        processCharacters ();

      currentNode.setEndLocation (m_aPContext);

      if (currentNode instanceof LitElementFactory.Instance)
      {
        // restore the newly declared namespaces from this element
        // (this is a deep copy)
        newNamespaces = ((LitElementFactory.Instance) currentNode).getNamespaces ();
      }

      // Don't call compile for an included stx:transform, because
      // the including Parser will call it
      if (!(currentNode == m_aPContext.transformNode && includingGroup != null))
        if (currentNode.compile (0, m_aPContext))
          // need another invocation
          compilableNodes.add (currentNode);
      // add the compilable nodes from an included stx:transform
      if (currentNode instanceof TransformFactory.Instance && currentNode != m_aPContext.transformNode)
        compilableNodes.addAll (((TransformFactory.Instance) currentNode).m_aCompilableNodes);
      currentNode = openedElements.pop ();
    }
    catch (final SAXParseException ex)
    {
      m_aPContext.getErrorHandler ().error (ex);
    }
  }

  public void characters (final char [] ch, final int start, final int length)
  {
    collectedCharacters.append (ch, start, length);
  }

  public void ignorableWhitespace (final char [] ch, final int start, final int length)
  {
    characters (ch, start, length);
  }

  public void processingInstruction (final String target, final String data) throws SAXException
  {
    try
    {
      if (collectedCharacters.length () != 0)
        processCharacters ();
    }
    catch (final SAXParseException ex)
    {
      m_aPContext.getErrorHandler ().error (ex);
    }
  }

  public void startPrefixMapping (final String prefix, final String uri)
  {
    final Stack <String> nsStack = inScopeNamespaces.computeIfAbsent (prefix, k -> new Stack<> ());
    nsStack.push (uri);
    newNamespaces.put (prefix, uri);
  }

  public void endPrefixMapping (final String prefix)
  {
    final Stack <String> nsStack = inScopeNamespaces.get (prefix);
    nsStack.pop ();
    newNamespaces.remove (prefix);
  }

  public void skippedEntity (final String name)
  {}

  // //
  // // interface ErrorHandler
  // //
  // public void fatalError(SAXParseException e)
  // throws SAXException
  // {
  // throw e;
  // }

  // public void error(SAXParseException e)
  // throws SAXException
  // {
  // throw e;
  // }

  // public void warning(SAXParseException e)
  // throws SAXException
  // {
  // log.warn(e.getMessage());
  // }

  //
  // helper functions
  //

  /**
   * Constructs a hashtable containing a mapping from all namespace prefixes in
   * scope to their URIs.
   */
  public Hashtable <String, String> getInScopeNamespaces ()
  {
    final Hashtable <String, String> ret = new Hashtable<> ();
    for (final Map.Entry <String, Stack <String>> e : inScopeNamespaces.entrySet ())
      if (!e.getValue ().isEmpty ())
        ret.put (e.getKey (), e.getValue ().peek ());
    return ret;
  }
}
