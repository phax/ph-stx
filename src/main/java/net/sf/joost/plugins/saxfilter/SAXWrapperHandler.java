/*
 * $Id: SAXWrapperHandler.java,v 1.2 2008/06/15 08:11:23 obecker Exp $
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

package net.sf.joost.plugins.saxfilter;

import net.sf.joost.stx.Processor;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Wraps a SAX parser XMLReader in a TransformerHandler object.
 * <p> Collects all character data reported by {@link #characters characters}
 * and parses them afterwards with a SAX parser (which produces the result
 * of this transformation). Other input events will be ignored.
 * @version $Revision: 1.2 $ $Date: 2008/06/15 08:11:23 $
 * @author Oliver Becker
 */

public class SAXWrapperHandler
   extends DefaultHandler implements TransformerHandler
{
   /** event sink for this transformer */
   private SAXResult saxResult;

   /** the wrapped SAX parser */
   private XMLReader parser;

   /** the character buffer */
   private StringBuffer buffer;

   //
   // from interface LexicalHandler
   // (not implemented by DefaultHandler; empty implementations)
   //

   public void startDTD(String name, String publicId, String systemId)
   { }

   public void endDTD()
   { }

   public void startEntity(String name)
   { }

   public void endEntity(String name)
   { }

   public void startCDATA()
   { }

   public void endCDATA()
   { }

   public void comment(char[] ch, int start, int length)
   { }


   // ---------------------------------------------------------------------

   //
   // from interface ContentHandler
   // (only relevant methods are overridden from DefaultHandler)
   //

   /** initialize parser and character buffer */
   public void startDocument()
      throws SAXException
   {
      if (saxResult == null) // Shouldn't happen
         throw new SAXException("No result set");

      parser = Processor.createXMLReader();
      parser.setContentHandler(saxResult.getHandler());
      try {
         parser.setProperty("http://xml.org/sax/properties/lexical-handler",
                            saxResult.getLexicalHandler());
      }
      catch (SAXException ex) { }

      buffer = new StringBuffer();
   }

   /** collect characters */
   public void characters(char[] ch, int start, int length)
   {
      buffer.append(ch, start, length);
   }

   /** collect characters */
   public void ignorableWhitespace(char[] ch, int start, int length)
   {
      buffer.append(ch, start, length);
   }

   /** parse the collected characters */
   public void endDocument()
      throws SAXException
   {
      try {
         parser.parse(new InputSource(new StringReader(buffer.toString())));
      }
      catch (IOException ex) {
         // shouldn't happen
         throw new SAXException(ex);
      }
   }


   // ---------------------------------------------------------------------

   //
   // from interface TransformerHandler
   //

   public void setResult(Result result)
   {
      if (result instanceof SAXResult)
         saxResult = (SAXResult)result;
      else {
         // this will not happen in Joost
         throw new IllegalArgumentException("result must be a SAXResult");
      }
   }

   // Never invoked by Joost
   public void setSystemId(String id)
   { }

   public String getSystemId()
   {
      return null;
   }

   public Transformer getTransformer()
   {
      return null;
   }
}
