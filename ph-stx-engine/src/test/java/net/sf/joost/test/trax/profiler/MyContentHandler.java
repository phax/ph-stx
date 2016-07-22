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
package net.sf.joost.test.trax.profiler;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * TestContentHandler for transformation over TraX with joost acts as SAXResult
 *
 * @author Zubow
 */
public class MyContentHandler implements ContentHandler
{
  public void setDocumentLocator (final Locator locator)
  {}

  public void startDocument () throws SAXException
  {
    // System.out.println("startDocument");
  }

  public void endDocument () throws SAXException
  {
    // System.out.println("endDocument");
  }

  public void startPrefixMapping (final String prefix, final String uri) throws SAXException
  {}

  public void endPrefixMapping (final String prefix) throws SAXException
  {}

  public void startElement (final String namespaceURI,
                            final String localName,
                            final String qName,
                            final Attributes atts) throws SAXException
  {}

  public void endElement (final String namespaceURI, final String localName, final String qName) throws SAXException
  {}

  public void characters (final char ch[], final int start, final int length) throws SAXException
  {}

  public void ignorableWhitespace (final char ch[], final int start, final int length) throws SAXException
  {}

  public void processingInstruction (final String target, final String data) throws SAXException
  {}

  public void skippedEntity (final String name) throws SAXException
  {}

  public static void main (final String [] args) throws Exception
  {}
}
