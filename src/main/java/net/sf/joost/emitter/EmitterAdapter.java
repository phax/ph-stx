/*
 * $Id: EmitterAdapter.java,v 1.3 2009/03/15 13:21:48 obecker Exp $
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

import net.sf.joost.instruction.NodeBase;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.helpers.MutableAttributes;
import net.sf.joost.stx.helpers.MutableAttributesImpl;

import java.util.Hashtable;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;


/**
 * Adapter that passes events from <code>ContentHandler</code> and
 * <code>LexicalHandler</code> to {@link Emitter}. Such an intermediate
 * object is needed because {@link Emitter} itself doesn't implement
 * these interfaces.
 * @version $Revision: 1.3 $ $Date: 2009/03/15 13:21:48 $
 * @author Oliver Becker
 */

public class EmitterAdapter implements ContentHandler, LexicalHandler
{
   private Emitter emitter;
   private Hashtable nsTable = new Hashtable();

   private NodeBase instruction;

   public EmitterAdapter(Emitter emitter, NodeBase instruction)
   {
      this.emitter = emitter;
      this.instruction = instruction;
   }


   //
   // from interface ContentHandler
   //

   public void setDocumentLocator(Locator locator)
   { } // ignore

   public void startDocument()
   { } // ignore

   public void endDocument()
   { } // ignore

   public void startPrefixMapping(String prefix, String uri)
   {
      nsTable.put(prefix, uri);
   }

   public void endPrefixMapping(String prefix)
   { } // nothing to do

   public void startElement(String uri, String lName, String qName,
                            Attributes atts)
      throws SAXException
   {
      // remove namespace declarations that might appear in the attributes
      MutableAttributes filteredAtts =
         new MutableAttributesImpl(null, 0);
      for (int i=0; i< atts.getLength(); i++) {
         if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(atts.getURI(i)))
               filteredAtts.addAttribute(atts.getURI(i), atts.getLocalName(i),
                                         atts.getQName(i), atts.getType(i),
                                         atts.getValue(i));
      }

      emitter.startElement(uri, lName, qName, filteredAtts, nsTable,
                           instruction);
      nsTable.clear();
   }

   public void endElement(String uri, String lName, String qName)
      throws SAXException
   {
      emitter.endElement(uri, lName, qName, instruction);
   }

   public void characters(char[] ch, int start, int length)
      throws SAXException
   {
      emitter.characters(ch, start, length, instruction);
   }

   public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException
   {
      emitter.characters(ch, start, length, instruction);
   }

   public void processingInstruction(String target, String data)
      throws SAXException
   {
      emitter.processingInstruction(target, data, instruction);
   }

   public void skippedEntity(String name)
   { } // ignore


   //
   // from interface LexicalHandler
   //

   public void startDTD(String name, String pubId, String sysId)
   { } // ignore

   public void endDTD()
   { } // ignore

   public void startEntity(String name)
   { } // ignore

   public void endEntity(String name)
   { } // ignore

   public void startCDATA()
      throws SAXException
   {
      emitter.startCDATA(instruction);
   }

   public void endCDATA()
      throws SAXException
   {
      emitter.endCDATA();
   }

   public void comment(char[] ch, int start, int length)
      throws SAXException
   {
      emitter.comment(ch, start, length, instruction);
   }
}
