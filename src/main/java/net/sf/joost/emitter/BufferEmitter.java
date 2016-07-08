/*
 * $Id: BufferEmitter.java,v 1.5 2005/11/06 21:22:21 obecker Exp $
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

package net.sf.joost.emitter;

//SAX2
import java.util.Vector;

import net.sf.joost.stx.SAXEvent;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * This class implements a buffer for storing SAX events.
 * @version $Revision: 1.5 $ $Date: 2005/11/06 21:22:21 $
 * @author Oliver Becker
 */

public class BufferEmitter extends StxEmitterBase {

   /** the event buffer */
   private Vector buffer = new Vector();

   /** the event array, the old contents remains valid until this buffer 
       is completely new filled */
   private SAXEvent[] eventArray = new SAXEvent[0];  // initial: empty

   /** CDATA flag */
   private boolean insideCDATA = false;

   /** characters flag, needed for detecting empty CDATA sections */
   private boolean charsEmitted = false;


   /** @return an array of the events stored in this buffer */
   public SAXEvent[] getEvents()
   {
      return eventArray;
   }

   /** Clears the event buffer */
   public void clear()
   {
      buffer.clear();
   }

   /** Signals that the buffer is completely filled; makes its contents
       available to {@link #getEvents} */
   public void filled()
   {
      eventArray = new SAXEvent[buffer.size()];
      buffer.toArray(eventArray);
   }


   //
   // SAX ContentHandler interface
   //

   /** not used */
   public void setDocumentLocator(Locator locator)
   { }

   /** do nothing */
   public void startDocument()
      throws SAXException
   { }

   /** do nothing */
   public void endDocument()
      throws SAXException
   { }

   public void startPrefixMapping(String prefix, String uri)
      throws SAXException
   {
      buffer.addElement(SAXEvent.newMapping(prefix, uri));
   }

   public void endPrefixMapping(String prefix)
      throws SAXException
   {
      buffer.addElement(SAXEvent.newMapping(prefix, null)); 
   }

   public void startElement(String namespaceURI, String localName,
                            String qName, Attributes atts)
      throws SAXException
   {
      buffer.addElement(SAXEvent.newElement(namespaceURI, localName,
                                            qName, atts, true, null));
   }

   public void endElement(String namespaceURI, String localName,
                          String qName)
      throws SAXException
   {
      buffer.addElement(SAXEvent.newElement(namespaceURI, localName,
                                            qName, null, true, null));
   }

   public void characters(char[] ch, int start, int length)
      throws SAXException
   {
      if (insideCDATA) {
         buffer.addElement(SAXEvent.newCDATA(new String(ch, start, length)));
         charsEmitted = true;
      }
      else
         buffer.addElement(SAXEvent.newText(new String(ch, start, length)));
   }

   /** not used */
   public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException
   {
      characters(ch, start, length); // just to be sure ...
   }

   public void processingInstruction(String target, String data)
      throws SAXException
   {
      buffer.addElement(SAXEvent.newPI(target, data));
   }

   /** not used */
   public void skippedEntity(String name)
      throws SAXException
   { }


   //
   // SAX LexicalHandler interface
   //

   /** not used */
   public void startDTD(String name, String publicId, String systemId)
      throws SAXException
   { }

   /** not used */
   public void endDTD()
      throws SAXException
   { }

   /** not used */
   public void startEntity(String name)
      throws SAXException
   { }

   /** not used */
   public void endEntity(String name)
      throws SAXException
   { }

   public void startCDATA()
      throws SAXException
   {
      insideCDATA = true;
      charsEmitted = false;
   }

   public void endCDATA()
      throws SAXException
   {
      insideCDATA = false;
      if (!charsEmitted) // no characters event: empty CDATA section
         buffer.addElement(SAXEvent.newCDATA(""));
   }

   public void comment(char[] ch, int start, int length)
      throws SAXException
   {
      buffer.addElement(SAXEvent.newComment(new String(ch, start, length)));
   }
}
