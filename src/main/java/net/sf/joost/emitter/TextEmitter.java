/*
 * $Id: TextEmitter.java,v 1.4 2007/11/25 14:18:02 obecker Exp $
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
 * Contributor(s): Anatolij Zubow
 */

package net.sf.joost.emitter;

import java.io.IOException;
import java.io.Writer;

import net.sf.joost.OptionalLog;

import org.apache.commons.logging.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class implements an emitter that uses the <code>text</code> output
 * method for byte or character streams.
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:02 $
 * @author Oliver Becker, Anatolij Zubow
 */
public class TextEmitter extends StreamEmitter 
{
   // Log initialization
   private static Log log = OptionalLog.getLog(TextEmitter.class);


   /** Constructor */
   public TextEmitter(Writer writer, String encoding)
   {
      super(writer, encoding);
   }

   /** 
    * Does nothing
    */
   public void startDocument()
   { }

   /**
    * Flushes the output writer 
    */
   public void endDocument() throws SAXException 
   {
      try {
         writer.flush();
      } 
      catch (IOException ex) {
         if (log != null)
            log.error(ex);
         throw new SAXException(ex);
      }
   }

   /**
    * Does nothing
    */
   public void startElement(String uri, String lName, String qName,
                            Attributes attrs)
   { }

   /** 
    * Does nothing
    */
   public void endElement(String uri, String lName, String qName)
   { }

   /**
    * Outputs characters.
    */
   public void characters(char[] ch, int start, int length)
      throws SAXException 
   {
      // Check that the characters can be represented in the current encoding
      for (int i=0; i<length; i++)
         if (!charsetEncoder.canEncode(ch[start+i]))
            throw new SAXException("Cannot output character with code " + 
                                   (int)ch[start+i] + 
                                   " in the encoding '" + encoding + "'");
         
      try {
         writer.write(ch, start, length);
         if (DEBUG)
            log.debug("'" + new String(ch, start, length) + "'");
      } 
      catch (IOException ex) {
         if (log != null)
            log.error(ex);
         throw new SAXException(ex);
      }
   }
}
