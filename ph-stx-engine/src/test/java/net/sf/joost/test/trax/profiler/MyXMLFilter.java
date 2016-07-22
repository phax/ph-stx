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

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author Zubow
 */
public class MyXMLFilter extends XMLFilterImpl
{
  private int count = 0;

  public MyXMLFilter (final int count)
  {
    this.count = count;
  }

  @Override
  public void parse (final InputSource dummy) throws SAXException
  {
    final String data = "" + new Integer ((123));

    final ContentHandler h = getContentHandler ();
    h.startDocument ();
    h.startElement ("", "flat", "flat", new AttributesImpl ());

    for (int i = 0; i < count; i++)
    {
      h.startElement ("", "entry", "entry", new AttributesImpl ());
      h.characters (data.toCharArray (), 0, data.length ());
      h.endElement ("", "entry", "entry");
    }

    h.endElement ("", "flat", "flat");
    h.endDocument ();
  }
}
