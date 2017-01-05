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
 *  are Copyright (C) 2016-2017 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.test.trax;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * TestContentHandler for transformation over TraX with joost acts as SAXResult
 *
 * @author Zubow
 */
public class ContentHandlerTest implements ContentHandler
{

  // Define a static logger variable so that it references the
  // Logger instance named "RunTests".
  private static final Logger log = LoggerFactory.getLogger (ContentHandlerTest.class);

  public void setDocumentLocator (final Locator locator)
  {
    log.info ("setDocumentLocator");
  }

  public void startDocument () throws SAXException
  {
    log.info ("ExampleContentHandler - startDocument");
  }

  public void endDocument () throws SAXException
  {
    log.info ("endDocument");
  }

  public void startPrefixMapping (final String prefix, final String uri) throws SAXException
  {
    log.info ("startPrefixMapping: " + prefix + ", " + uri);
  }

  public void endPrefixMapping (final String prefix) throws SAXException
  {
    log.info ("endPrefixMapping: " + prefix);
  }

  public void startElement (final String namespaceURI,
                            final String localName,
                            final String qName,
                            final Attributes atts) throws SAXException
  {

    log.info ("startElement: " + namespaceURI + ", " + localName + ", " + qName);

    final int n = atts.getLength ();

    for (int i = 0; i < n; i++)
    {
      log.info (", " + atts.getQName (i) + "='" + atts.getValue (i) + "'");
    }

    log.info ("");
  }

  public void endElement (final String namespaceURI, final String localName, final String qName) throws SAXException
  {

    log.info ("endElement: " + namespaceURI + ", " + localName + ", " + qName);
  }

  public void characters (final char ch[], final int start, final int length) throws SAXException
  {

    final String s = new String (ch, start, (length > 30) ? 30 : length);

    if (length > 30)
    {
      log.info ("characters: \"" + s + "\"...");
    }
    else
    {
      log.info ("characters: \"" + s + "\"");
    }
  }

  public void ignorableWhitespace (final char ch[], final int start, final int length) throws SAXException
  {
    log.info ("ignorableWhitespace");
  }

  public void processingInstruction (final String target, final String data) throws SAXException
  {
    log.info ("processingInstruction: " + target + ", " + data);
  }

  public void skippedEntity (final String name) throws SAXException
  {
    log.info ("skippedEntity: " + name);
  }

  @Test
  public void testBasic () throws SAXException, ParserConfigurationException, IOException
  {
    final org.xml.sax.XMLReader parser = javax.xml.parsers.SAXParserFactory.newInstance ()
                                                                           .newSAXParser ()
                                                                           .getXMLReader ();

    log.error ("Parser: " + parser.getClass ());

    parser.setContentHandler (new ContentHandlerTest ());

    final String sURL = getClass ().getResource ("errorDocument.xml").toExternalForm ();
    log.info ("Parsing " + sURL);
    parser.parse (sURL);
  }
}
