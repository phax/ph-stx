/*
 * $Id: MyContentHandler.java,v 1.1 2007/07/15 15:32:28 obecker Exp $
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

package net.sf.joost.test.trax.profiler;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * TestContentHandler for transformation over TraX with joost
 * acts as SAXResult
 *
 * @author Zubow
 */
public class MyContentHandler implements ContentHandler {


    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
        //System.out.println("startDocument");
    }

    public void endDocument() throws SAXException {
        //System.out.println("endDocument");
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement( String namespaceURI, String localName,
        String qName, Attributes atts)
        throws SAXException {
    }

    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
    }

    public void characters(char ch[], int start, int length)
        throws SAXException {
    }

    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException {
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public static void main(String[] args) throws Exception {
    }
}


