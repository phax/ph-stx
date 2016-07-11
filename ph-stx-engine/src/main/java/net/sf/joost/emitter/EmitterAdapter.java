/*
 * $Id: EmitterAdapter.java,v 1.3 2009/03/15 13:21:48 obecker Exp $
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

package net.sf.joost.emitter;

import java.util.Hashtable;
import java.util.Map;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.helpers.IMutableAttributes;
import net.sf.joost.stx.helpers.MutableAttributesImpl;

/**
 * Adapter that passes events from <code>ContentHandler</code> and
 * <code>LexicalHandler</code> to {@link Emitter}. Such an intermediate object
 * is needed because {@link Emitter} itself doesn't implement these interfaces.
 *
 * @version $Revision: 1.3 $ $Date: 2009/03/15 13:21:48 $
 * @author Oliver Becker
 */

public class EmitterAdapter implements ContentHandler, LexicalHandler
{
  private final Emitter m_aEmitter;
  private final Map <String, String> nsTable = new Hashtable<> ();
  private final AbstractNodeBase m_aInstruction;

  public EmitterAdapter (final Emitter emitter, final AbstractNodeBase instruction)
  {
    m_aEmitter = emitter;
    m_aInstruction = instruction;
  }

  //
  // from interface ContentHandler
  //

  public void setDocumentLocator (final Locator locator)
  {} // ignore

  public void startDocument ()
  {} // ignore

  public void endDocument ()
  {} // ignore

  public void startPrefixMapping (final String prefix, final String uri)
  {
    nsTable.put (prefix, uri);
  }

  public void endPrefixMapping (final String prefix)
  {} // nothing to do

  public void startElement (final String uri,
                            final String lName,
                            final String qName,
                            final Attributes atts) throws SAXException
  {
    // remove namespace declarations that might appear in the attributes
    final IMutableAttributes filteredAtts = new MutableAttributesImpl (null, 0);
    for (int i = 0; i < atts.getLength (); i++)
    {
      if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals (atts.getURI (i)))
        filteredAtts.addAttribute (atts.getURI (i),
                                   atts.getLocalName (i),
                                   atts.getQName (i),
                                   atts.getType (i),
                                   atts.getValue (i));
    }

    m_aEmitter.startElement (uri, lName, qName, filteredAtts, nsTable, m_aInstruction);
    nsTable.clear ();
  }

  public void endElement (final String uri, final String lName, final String qName) throws SAXException
  {
    m_aEmitter.endElement (uri, lName, qName, m_aInstruction);
  }

  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    m_aEmitter.characters (ch, start, length, m_aInstruction);
  }

  public void ignorableWhitespace (final char [] ch, final int start, final int length) throws SAXException
  {
    m_aEmitter.characters (ch, start, length, m_aInstruction);
  }

  public void processingInstruction (final String target, final String data) throws SAXException
  {
    m_aEmitter.processingInstruction (target, data, m_aInstruction);
  }

  public void skippedEntity (final String name)
  {} // ignore

  //
  // from interface LexicalHandler
  //

  public void startDTD (final String name, final String pubId, final String sysId)
  {} // ignore

  public void endDTD ()
  {} // ignore

  public void startEntity (final String name)
  {} // ignore

  public void endEntity (final String name)
  {} // ignore

  public void startCDATA () throws SAXException
  {
    m_aEmitter.startCDATA (m_aInstruction);
  }

  public void endCDATA () throws SAXException
  {
    m_aEmitter.endCDATA ();
  }

  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    m_aEmitter.comment (ch, start, length, m_aInstruction);
  }
}
