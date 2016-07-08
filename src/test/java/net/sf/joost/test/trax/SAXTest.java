/*
 * $Id: SAXTest.java,v 1.1 2007/07/15 15:32:28 obecker Exp $
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

package net.sf.joost.test.trax;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Zubow
 */
public class SAXTest extends XMLFilterImpl {

    // Define a static logger variable so that it references the
    // Logger instance named "RunTests".
    static Logger log = Logger.getLogger(SAXTest.class);

    private static String log4jprop = "conf/log4j.properties";

    static {
        //calling once
        PropertyConfigurator.configure(log4jprop);
    }

    public static void main(String[] args) {

        String xmlId = "test/flat.xml";
        String stxId = "test/flat.stx";

        System.setProperty("javax.xml.transform.TransformerFactory",
                     "net.sf.joost.trax.TransformerFactoryImpl");

        try {

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer =
                factory.newTransformer(new StreamSource(stxId));

            transformer.transform(
                new SAXSource(new SAXTest(xmlId), new InputSource()),
                new StreamResult(System.out));

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    // *********************************************************************

    private String data;

    public SAXTest(String data) {
        // init somehow
        this.data = data;
    }

    public void parse(InputSource dummy)
        throws SAXException {

        ContentHandler h = getContentHandler();
        h.startDocument();
        h.startElement("", "flat", "flat", new AttributesImpl());

        for (int i=0; i < 14; i++) {
            h.startElement("", "entry", "entry", new AttributesImpl());
            String data = "" + new Integer((123 + i));
            h.characters(data.toCharArray(), 0, data.length());
            h.endElement("", "entry", "entry");
        }

        h.endElement("", "flat", "flat");
        h.endDocument();
    }
}

