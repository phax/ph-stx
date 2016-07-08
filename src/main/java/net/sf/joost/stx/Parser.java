/*
 * $Id: Parser.java,v 2.23 2007/11/25 19:33:34 obecker Exp $
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
 * Contributor(s): ______________________________________. 
 */

package net.sf.joost.stx;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import net.sf.joost.Constants;
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
import net.sf.joost.instruction.FactoryBase;
import net.sf.joost.instruction.ForEachFactory;
import net.sf.joost.instruction.GroupBase;
import net.sf.joost.instruction.GroupFactory;
import net.sf.joost.instruction.IfFactory;
import net.sf.joost.instruction.IncludeFactory;
import net.sf.joost.instruction.LitElementFactory;
import net.sf.joost.instruction.MatchFactory;
import net.sf.joost.instruction.MessageFactory;
import net.sf.joost.instruction.NSAliasFactory;
import net.sf.joost.instruction.NoMatchFactory;
import net.sf.joost.instruction.NodeBase;
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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.NamespaceSupport;


/** 
 * Creates the tree representation of an STX transformation sheet.
 * The Parser object acts as a SAX ContentHandler.
 * @version $Revision: 2.23 $ $Date: 2007/11/25 19:33:34 $
 * @author Oliver Becker
 */

public class Parser implements Constants, ContentHandler // , ErrorHandler
{
   /** The context object for parsing */
   private ParseContext pContext;

   /** Stack for opened elements, contains Node instances. */
   private Stack openedElements;

   /** The current (last created) Node. */
   private NodeBase currentNode;

   /** Hashtable for STX factory objects, one for each type. */
   private Hashtable stxFactories;

   /** Hashtable for Joost extension factory objects, one for each type. */
   private Hashtable joostFactories;

   /** The factory for literal result elements. */
   private LitElementFactory litFac;

   /** Hashtable: keys = prefixes, values = URI stacks */
   private Hashtable inScopeNamespaces;

   /** Hashtable for newly declared namespaces between literal elements;
       keys = prefixes, values = URIs */
   private Hashtable newNamespaces;

   /** List of nodes that need another call to {@link NodeBase#compile} */
   public Vector compilableNodes = new Vector();

   /** Group which had an <code>stx:include</code>, which in turn created
       this Parser object */
   public GroupBase includingGroup;

   /** An optional ParserListener */
   private ParserListener parserListener;


   //
   // Constructor
   //

   /** Constructs a new Parser instance. */
   public Parser(ParseContext pContext)
   {
      this.pContext = pContext;
      this.parserListener = pContext.parserListener;
      
      // factories for elements from the STX namespace
      FactoryBase[] stxFacs = {
         new TransformFactory(),
         new GroupFactory(),
         new IncludeFactory(),
         new NSAliasFactory(),
         new TemplateFactory(),
         new ProcedureFactory(),
         new CallProcedureFactory(),
         new ParamFactory(),
         new VariableFactory(),
         new AssignFactory(),
         new WithParamFactory(),
         new ValueOfFactory(),
         new PChildrenFactory(),
         new PSelfFactory(),
         new PSiblingsFactory(),
         new PAttributesFactory(),
         new AnalyzeTextFactory(),
         new MatchFactory(),
         new NoMatchFactory(),
         new PDocumentFactory(),
         new ResultDocumentFactory(),
         new BufferFactory(),
         new ResultBufferFactory(),
         new PBufferFactory(),
         new CopyFactory(),
         new TextFactory(),
         new CdataFactory(),
         new AttributeFactory(),
         new ElementFactory(),
         new ElementStartFactory(),
         new ElementEndFactory(),
         new CommentFactory(),
         new PIFactory(),
         new ForEachFactory(),
         new WhileFactory(),
         new IfFactory(),
         new ElseFactory(),
         new ChooseFactory(),
         new WhenFactory(),
         new OtherwiseFactory(),
         new MessageFactory(),
         new DoctypeFactory()
      };
      stxFactories = createFactoryMap(stxFacs);
      
      // factories for elements from the Joost namespace
      FactoryBase[] joostFacs = {
         new ScriptFactory()
      };
      joostFactories = createFactoryMap(joostFacs);

      litFac = new LitElementFactory();
      openedElements = new Stack();
      inScopeNamespaces = new Hashtable();
      newNamespaces = new Hashtable();
   }

   /**
    * creates hashtable and sets its initial content to the given array
    * 
    * @param data to be filled in the map
    * @return the created hashtable
    */
   private Hashtable createFactoryMap(FactoryBase[] data)
   {
      Hashtable map = new Hashtable(data.length);
      for (int i = 0; i < data.length; i++)
         map.put(data[i].getName(), data[i]);
      return map;
   }
      

   /**
    * @return the STX node factories, indexed by local name
    */
   public Map getFactories() {
      return stxFactories;
   }


   /** 
    * @return the root node representing <code>stx:transform</code>. 
    */
   public TransformFactory.Instance getTransformNode()
   {
      return pContext.transformNode;
   }


   /** Buffer for collecting consecutive character data */
   private StringBuffer collectedCharacters = new StringBuffer();

   /** Processes collected character fragments */
   private void processCharacters()
      throws SAXParseException
   {
      String s = collectedCharacters.toString();
      if (currentNode.preserveSpace || s.trim().length() != 0) {
         if (currentNode instanceof GroupBase) {
            if (s.trim().length() != 0)
               throw new SAXParseException(
                  "Text must not occur on group level", pContext.locator);

         }
         else {
            NodeBase textNode = new TextNode(s, currentNode, pContext);
            currentNode.insert(textNode);
            if (parserListener != null)
               parserListener.nodeCreated(textNode);
         }
      }
      collectedCharacters.setLength(0);
   }


   //
   // from interface ContentHandler
   //


   public void setDocumentLocator(Locator locator)
   {
      pContext.locator = locator;
   }


   public void startDocument() throws SAXException
   {
      // declare xml namespace
      startPrefixMapping("xml", NamespaceSupport.XMLNS);
   }


   public void endDocument()
      throws SAXException
   {
      endPrefixMapping("xml");
      if (includingGroup != null)
         return;
      try {
         // call compile() method for those nodes that have requested to
         int pass = 0;
         int size;
         while ((size = compilableNodes.size()) != 0) {
            pass++;
            NodeBase nodes[] = new NodeBase[size];
            compilableNodes.toArray(nodes);
            compilableNodes.clear(); // for the next pass
            for (int i=0; i<size; i++)
               if (nodes[i].compile(pass, pContext)) {
                  // still need another invocation
                  compilableNodes.addElement(nodes[i]);
               }
         }
         compilableNodes = null; // for garbage collection

         if (parserListener != null)
            parserListener.parseFinished();
      }
      catch (SAXParseException ex) {
         pContext.getErrorHandler().error(ex);
      }
   }


   public void startElement(String uri, String lName, String qName,
                            Attributes attrs)
      throws SAXException
   {
      try {
         if (collectedCharacters.length() != 0)
            processCharacters();

         NodeBase newNode;
         pContext.nsSet = getInScopeNamespaces();
         if (STX_NS.equals(uri)) {
            FactoryBase fac = (FactoryBase)stxFactories.get(lName);
            if (fac == null) 
               throw new SAXParseException("Unknown statement '" + qName + 
                                           "'", pContext.locator);
            newNode = fac.createNode(currentNode != null 
                                        ? currentNode : includingGroup, 
                                     qName, attrs, pContext);
            if (pContext.transformNode == null) 
               try {
                  pContext.transformNode = (TransformFactory.Instance)newNode;
               }
               catch (ClassCastException cce) {
                  throw new SAXParseException(
                     "Found '" + qName + "' as root element, " + 
                     "file is not an STX transformation sheet",
                     pContext.locator);
               }
            // if this is an instruction that may create a new namespace,
            // use the full set of namespaces in the next literal element
            if (fac instanceof CopyFactory ||
                fac instanceof ElementFactory ||
                fac instanceof ElementStartFactory)
               newNamespaces = getInScopeNamespaces();
         }
         else if (JOOST_EXT_NS.equals(uri)) {
            FactoryBase fac = (FactoryBase) joostFactories.get(lName);
            if (fac == null) 
               throw new SAXParseException("Unknown statement '" + qName + 
                                           "'", pContext.locator);
            newNode = fac.createNode(currentNode != null 
                                        ? currentNode : includingGroup, 
                                     qName, attrs, pContext);
         }
         else {
            newNode = litFac.createNode(currentNode, uri, lName, qName, attrs,
                                        pContext, newNamespaces);
            // reset these newly declared namespaces
            // newNode "consumes" the old value (without copy)
            newNamespaces = new Hashtable();
         }

         // check xml:space attribute
         int spaceIndex = attrs.getIndex(NamespaceSupport.XMLNS, "space");
         if (spaceIndex != -1) { // attribute present
            String spaceAtt = attrs.getValue(spaceIndex);
            if ("preserve".equals(spaceAtt))
               newNode.preserveSpace = true;
            else if (!"default".equals(spaceAtt))
               throw new SAXParseException(
                  "Value of attribute '" + attrs.getQName(spaceIndex) + 
                  "' must be either 'preserve' or 'default' (found '" +
                  spaceAtt + "')", pContext.locator);
            // "default" means false -> nothing to do
         }
         else if (newNode instanceof TextFactory.Instance ||
                  newNode instanceof CdataFactory.Instance)
            // these elements behave as if xml:space was set to "preserve"
            newNode.preserveSpace = true;
         else if (currentNode != null)
            // inherit from parent
            newNode.preserveSpace = currentNode.preserveSpace;

         if (currentNode != null)
            currentNode.insert(newNode);
         openedElements.push(currentNode);
         currentNode = newNode;

         if (parserListener != null)
            parserListener.nodeCreated(newNode);
      }
      catch (SAXParseException ex) {
         pContext.getErrorHandler().error(ex);
      }
   }


   public void endElement(String uri, String lName, String qName)
      throws SAXException
   {
      try {
         if (collectedCharacters.length() != 0)
            processCharacters();

         currentNode.setEndLocation(pContext);

         if (currentNode instanceof LitElementFactory.Instance)
            // restore the newly declared namespaces from this element
            // (this is a deep copy)
            newNamespaces = 
               ((LitElementFactory.Instance)currentNode).getNamespaces();

         // Don't call compile for an included stx:transform, because
         // the including Parser will call it
         if (!(currentNode == pContext.transformNode && 
               includingGroup != null))
            if (currentNode.compile(0, pContext))
               // need another invocation
               compilableNodes.addElement(currentNode); 
         // add the compilable nodes from an included stx:transform
         if (currentNode instanceof TransformFactory.Instance && 
             currentNode != pContext.transformNode)
            compilableNodes.addAll(
               ((TransformFactory.Instance)currentNode).compilableNodes);
         currentNode = (NodeBase)openedElements.pop();
      }
      catch (SAXParseException ex) {
         pContext.getErrorHandler().error(ex);
      }
   }


   public void characters(char[] ch, int start, int length)
   {
      collectedCharacters.append(ch, start, length);
   }
   

   public void ignorableWhitespace(char[] ch, int start, int length)
   {
      characters(ch, start, length);
   }


   public void processingInstruction(String target, String data)
      throws SAXException 
   {
      try {
         if (collectedCharacters.length() != 0)
            processCharacters();
      }
      catch (SAXParseException ex) {
         pContext.getErrorHandler().error(ex);
      }
   }


   public void startPrefixMapping(String prefix, String uri)
   {
      Stack nsStack = (Stack)inScopeNamespaces.get(prefix);
      if (nsStack == null) {
         nsStack = new Stack();
         inScopeNamespaces.put(prefix, nsStack);
      }
      nsStack.push(uri);
      newNamespaces.put(prefix, uri);
   }


   public void endPrefixMapping(String prefix)
   {
      Stack nsStack = (Stack)inScopeNamespaces.get(prefix);
      nsStack.pop();
      newNamespaces.remove(prefix);
   }


   public void skippedEntity(String name)
   {
   }



//     //
//     // interface ErrorHandler
//     //
//     public void fatalError(SAXParseException e)
//        throws SAXException
//     {
//        throw e;
//     }


//     public void error(SAXParseException e)
//        throws SAXException
//     {
//        throw e;
//     }


//     public void warning(SAXParseException e)
//        throws SAXException
//     {
//        log.warn(e.getMessage());
//     }



   //
   // helper functions
   //

   /**
    * Constructs a hashtable containing a mapping from all namespace
    * prefixes in scope to their URIs.
    */ 
   public Hashtable getInScopeNamespaces()
   {
      Hashtable ret = new Hashtable();
      for (Enumeration e = inScopeNamespaces.keys(); e.hasMoreElements(); ) {
         Object prefix = e.nextElement();
         Stack s = (Stack)inScopeNamespaces.get(prefix);
         if (!s.isEmpty())
            ret.put(prefix, s.peek());
      }
      return ret;
   }
}
