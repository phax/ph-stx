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
package net.sf.joost.emitter;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This class implements an emitter that collects characters events
 *
 * @version $Revision: 1.9 $ $Date: 2007/11/25 14:18:02 $
 * @author Oliver Becker
 */

public final class StringEmitter extends AbstractStxEmitterBase
{
  /** the string buffer */
  private final StringBuffer m_aBuffer;

  /**
   * additional info for error messages, <code>null</code> means: don't report
   * errors
   */
  private final String m_sErrorInfo;

  public StringEmitter (final StringBuffer buffer, final String errorInfo)
  {
    m_aBuffer = buffer;
    m_sErrorInfo = errorInfo;
  }

  /** @return the string buffer for this emitter */
  public StringBuffer getBuffer ()
  {
    return m_aBuffer;
  }

  //
  // SAX ContentHandler interface
  //

  /** not used */
  public void setDocumentLocator (final Locator locator)
  {}

  /** do nothing */
  public void startDocument () throws SAXException
  {}

  /** do nothing */
  public void endDocument () throws SAXException
  {}

  /** do nothing */
  public void startPrefixMapping (final String prefix, final String uri) throws SAXException
  {}

  /** do nothing */
  public void endPrefixMapping (final String prefix) throws SAXException
  {}

  /** not allowed */
  public void startElement (final String namespaceURI,
                            final String localName,
                            final String qName,
                            final Attributes atts) throws SAXException
  {
    if (m_sErrorInfo != null)
      throw new SAXException ("Can't create element '" + qName + "' here " + m_sErrorInfo);
  }

  /** not allowed */
  public void endElement (final String namespaceURI, final String localName, final String qName) throws SAXException
  {
    // no exception thrown here, because there must have been be a
    // startElement event before
  }

  /** Add the characters to the internal buffer */
  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    m_aBuffer.append (ch, start, length);
  }

  /** not used */
  public void ignorableWhitespace (final char [] ch, final int start, final int length) throws SAXException
  {
    characters (ch, start, length); // just to be sure ...
  }

  /** not allowed */
  public void processingInstruction (final String target, final String data) throws SAXException
  {
    if (m_sErrorInfo != null)
      throw new SAXException ("Can't create processing instruction '" + target + "' here " + m_sErrorInfo);
  }

  /** not used */
  public void skippedEntity (final String name) throws SAXException
  {}

  //
  // SAX LexicalHandler interface
  //

  /** not used */
  public void startDTD (final String name, final String publicId, final String systemId) throws SAXException
  {}

  /** not used */
  public void endDTD () throws SAXException
  {}

  /** not used */
  public void startEntity (final String name) throws SAXException
  {}

  /** not used */
  public void endEntity (final String name) throws SAXException
  {}

  /** not allowed */
  public void startCDATA () throws SAXException
  {
    if (m_sErrorInfo != null)
      throw new SAXException ("Can't create CDATA section here " + m_sErrorInfo);
  }

  /** not allowed */
  public void endCDATA () throws SAXException
  {
    // no exception thrown here, because there must have been be a
    // startElement event before
  }

  /** not allowed */
  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    if (m_sErrorInfo != null)
      throw new SAXException ("Can't create comment here " + m_sErrorInfo);
  }
}
