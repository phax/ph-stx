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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author Zubow
 */
public class SAXTest extends XMLFilterImpl
{
  private static final Logger log = LoggerFactory.getLogger (SAXTest.class);

  public static void main (final String [] args)
  {

    final String xmlId = "test/flat.xml";
    final String stxId = "test/flat.stx";

    System.setProperty ("javax.xml.transform.TransformerFactory", "net.sf.joost.trax.TransformerFactoryImpl");

    try
    {

      final TransformerFactory factory = TransformerFactory.newInstance ();
      final Transformer transformer = factory.newTransformer (new StreamSource (stxId));

      transformer.transform (new SAXSource (new SAXTest (xmlId), new InputSource ()), new StreamResult (System.out));

    }
    catch (final Exception e)
    {
      System.err.println (e.toString ());
    }
  }

  // *********************************************************************

  private final String data;

  public SAXTest (final String data)
  {
    // init somehow
    this.data = data;
  }

  @Override
  public void parse (final InputSource dummy) throws SAXException
  {

    final ContentHandler h = getContentHandler ();
    h.startDocument ();
    h.startElement ("", "flat", "flat", new AttributesImpl ());

    for (int i = 0; i < 14; i++)
    {
      h.startElement ("", "entry", "entry", new AttributesImpl ());
      final String data = "" + new Integer ((123 + i));
      h.characters (data.toCharArray (), 0, data.length ());
      h.endElement ("", "entry", "entry");
    }

    h.endElement ("", "flat", "flat");
    h.endDocument ();
  }
}
