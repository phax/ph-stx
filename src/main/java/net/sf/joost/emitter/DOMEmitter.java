/*
 * $Id: DOMEmitter.java,v 1.8 2008/10/12 16:45:02 obecker Exp $
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
 * The Initial Developer of the Original Code is Anatolij Zubow.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.emitter;

import net.sf.joost.OptionalLog;

import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * This class implements the common interface <code>StxEmitter</code>. Is is
 * designed for using <code>DOMResult</code>. So it generates a DOM-tree,
 * which can be exported with the method {@link #getDOMTree()}.
 *
 * @author Anatolij Zubow, Oliver Becker
 */
public class DOMEmitter extends StxEmitterBase
{

   // Define a static logger variable so that it references the
   // Logger instance named "DOMEmitter".
   private static Log log = OptionalLog.getLog(DOMEmitter.class);

   private Document document = null;
   private Node nextSiblingOfRootNodes = null;
   private Stack stack = new Stack();
   private boolean insideCDATA = false;


   /**
    * DefaultConstructor
    *
    * @throws ParserConfigurationException
    *                 if an error occurs while creating
    *                 {@link javax.xml.parsers.DocumentBuilder}
    *                 DOM-DocumentBuilder
    */
   public DOMEmitter(DOMResult result) throws ParserConfigurationException
   {
      if (DEBUG)
         log.debug("init DOMEmitter");

      Node rootNode = result.getNode();
      nextSiblingOfRootNodes = result.getNextSibling();
      if (rootNode != null) {
         // use the document of the provided node
         if (rootNode instanceof Document)
            document = (Document) rootNode;
         else
            document = rootNode.getOwnerDocument();

         stack.push(rootNode);
      }
      else {
         // create a new document
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = factory.newDocumentBuilder();
         document = docBuilder.newDocument();

         stack.push(document);
      }
   }


   private void insertNode(Node newNode)
   {
      Node lastNode = (Node) stack.peek();
      if (stack.size() == 1 && nextSiblingOfRootNodes != null) {
         lastNode.insertBefore(newNode, nextSiblingOfRootNodes);
      }
      else {
         lastNode.appendChild(newNode);
      }
   }


   /**
    * After transformation you can call this method to get the document node.
    *
    * @return A {@link org.w3c.dom.Node} object
    */
   public Node getDOMTree()
   {
      return (Node) stack.get(0);
   }


   /**
    * SAX2-Callback - Creates a {@link org.w3c.dom.Document}
    */
   public void startDocument() throws SAXException
   {
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void endDocument() throws SAXException
   {
   }


   /**
    * SAX2-Callback - Creates a DOM-element-node and memorizes it for the
    * {@link #endElement(String ,String ,String)} method by putting it onto the
    * top of this stack.
    */
   public void startElement(String uri, String local, String raw,
         Attributes attrs) throws SAXException
   {
      // create new element : iterate over all attribute-values
      Element elem = document.createElementNS(uri, raw);
      int nattrs = attrs.getLength();
      for (int i = 0; i < nattrs; i++) {
         String namespaceuri = attrs.getURI(i);
         String value = attrs.getValue(i);
         String qName = attrs.getQName(i);
         if ((namespaceuri == null) || (namespaceuri.equals(""))) {
            elem.setAttribute(qName, value);
         }
         else {
            elem.setAttributeNS(namespaceuri, qName, value);
         }
      }

      // append this new node onto current stack node
      insertNode(elem);
      // push this node into the global stack
      stack.push(elem);
   }


   /**
    * SAX2-Callback - Removes the last element at the the top of the stack.
    */
   public void endElement(String uri, String local, String raw)
         throws SAXException
   {
      stack.pop();
   }


   /**
    * SAX2-Callback - Creates a DOM-text-node and looks at the element at the
    * top of the stack without removing it from the stack.
    */
   public void characters(char[] ch, int start, int length) throws SAXException
   {
      String str = new String(ch, start, length);
      if (insideCDATA) {
         // create CDATASection
         insertNode(document.createCDATASection(str));
      }
      else {
         insertNode(document.createTextNode(str));
      }
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void startPrefixMapping(String prefix, String uri)
   {
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void endPrefixMapping(String prefix)
   {
   }


   /**
    * SAX2-Callback
    */
   public void processingInstruction(String target, String data)
   {
      insertNode(document.createProcessingInstruction(target, data));
   }


   /**
    * SAX2-Callback
    */
   public void comment(char[] ch, int start, int length) throws SAXException
   {
      insertNode(document.createComment(new String(ch, start, length)));
   }


   /**
    * SAX2-Callback
    */
   public void endCDATA() throws SAXException
   {
      insideCDATA = false;
   }


   /**
    * SAX2-Callback
    */
   public void startCDATA() throws SAXException
   {
      insideCDATA = true;
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void endEntity(String name) throws org.xml.sax.SAXException
   {
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void startEntity(String name) throws org.xml.sax.SAXException
   {
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void endDTD() throws org.xml.sax.SAXException
   {
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void startDTD(String name, String publicId, String systemId)
         throws org.xml.sax.SAXException
   {
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void skippedEntity(String value) throws SAXException
   {
   }


   /**
    * SAX2-Callback
    */
   public void ignorableWhitespace(char[] ch, int start, int length)
         throws SAXException
   {
      // shouldn't be called; anyway, treat it like characters ...
      characters(ch, start, length);
   }


   /**
    * SAX2-Callback - Is empty
    */
   public void setDocumentLocator(Locator locator)
   {
   }

}
