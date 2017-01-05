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

import java.util.List;
//SAX2
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import net.sf.joost.stx.SAXEvent;

/**
 * This class implements a buffer for storing SAX events.
 *
 * @version $Revision: 1.5 $ $Date: 2005/11/06 21:22:21 $
 * @author Oliver Becker
 */

public class BufferEmitter extends AbstractStxEmitterBase
{
  /** the event buffer */
  private final List <SAXEvent> buffer = new Vector<> ();

  /**
   * the event array, the old contents remains valid until this buffer is
   * completely new filled
   */
  private SAXEvent [] eventArray = new SAXEvent [0]; // initial: empty

  /** CDATA flag */
  private boolean insideCDATA = false;

  /** characters flag, needed for detecting empty CDATA sections */
  private boolean charsEmitted = false;

  /** @return an array of the events stored in this buffer */
  public SAXEvent [] getEvents ()
  {
    return eventArray;
  }

  /** Clears the event buffer */
  public void clear ()
  {
    buffer.clear ();
  }

  /**
   * Signals that the buffer is completely filled; makes its contents available
   * to {@link #getEvents}
   */
  public void filled ()
  {
    eventArray = new SAXEvent [buffer.size ()];
    buffer.toArray (eventArray);
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

  public void startPrefixMapping (final String prefix, final String uri) throws SAXException
  {
    buffer.add (SAXEvent.newMapping (prefix, uri));
  }

  public void endPrefixMapping (final String prefix) throws SAXException
  {
    buffer.add (SAXEvent.newMapping (prefix, null));
  }

  public void startElement (final String namespaceURI,
                            final String localName,
                            final String qName,
                            final Attributes atts) throws SAXException
  {
    buffer.add (SAXEvent.newElement (namespaceURI, localName, qName, atts, true, null));
  }

  public void endElement (final String namespaceURI, final String localName, final String qName) throws SAXException
  {
    buffer.add (SAXEvent.newElement (namespaceURI, localName, qName, null, true, null));
  }

  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    if (insideCDATA)
    {
      buffer.add (SAXEvent.newCDATA (new String (ch, start, length)));
      charsEmitted = true;
    }
    else
      buffer.add (SAXEvent.newText (new String (ch, start, length)));
  }

  /** not used */
  public void ignorableWhitespace (final char [] ch, final int start, final int length) throws SAXException
  {
    characters (ch, start, length); // just to be sure ...
  }

  public void processingInstruction (final String target, final String data) throws SAXException
  {
    buffer.add (SAXEvent.newPI (target, data));
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

  public void startCDATA () throws SAXException
  {
    insideCDATA = true;
    charsEmitted = false;
  }

  public void endCDATA () throws SAXException
  {
    insideCDATA = false;
    if (!charsEmitted) // no characters event: empty CDATA section
      buffer.add (SAXEvent.newCDATA (""));
  }

  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    buffer.add (SAXEvent.newComment (new String (ch, start, length)));
  }
}
