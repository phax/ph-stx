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
package net.sf.joost.emitter;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import net.sf.joost.CSTX;

/**
 * This class implements an emitter that uses the <code>text</code> output
 * method for byte or character streams.
 *
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:02 $
 * @author Oliver Becker, Anatolij Zubow
 */
public class TextEmitter extends AbstractStreamEmitter
{
  // Logger initialization
  private static Logger log = LoggerFactory.getLogger (TextEmitter.class);

  /** Constructor */
  public TextEmitter (final Writer writer, final String encoding)
  {
    super (writer, encoding);
  }

  /**
   * Does nothing
   */
  public void startDocument ()
  {}

  /**
   * Flushes the output writer
   */
  public void endDocument () throws SAXException
  {
    try
    {
      m_aWriter.flush ();
    }
    catch (final IOException ex)
    {
      log.error ("Exception", ex);
      throw new SAXException (ex);
    }
  }

  /**
   * Does nothing
   */
  public void startElement (final String uri, final String lName, final String qName, final Attributes attrs)
  {}

  /**
   * Does nothing
   */
  public void endElement (final String uri, final String lName, final String qName)
  {}

  /**
   * Outputs characters.
   */
  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    // Check that the characters can be represented in the current encoding
    for (int i = 0; i < length; i++)
      if (!m_aCharsetEncoder.canEncode (ch[start + i]))
        throw new SAXException ("Cannot output character with code " +
                                (int) ch[start + i] +
                                " in the encoding '" +
                                m_sEncoding +
                                "'");

    try
    {
      m_aWriter.write (ch, start, length);
      if (CSTX.DEBUG)
        log.debug ("'" + new String (ch, start, length) + "'");
    }
    catch (final IOException ex)
    {
      log.error ("Exception", ex);
      throw new SAXException (ex);
    }
  }
}
