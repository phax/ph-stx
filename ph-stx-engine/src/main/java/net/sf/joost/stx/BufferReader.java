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
package net.sf.joost.stx;

import java.util.Hashtable;
import java.util.Stack;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.LocatorImpl;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.BufferEmitter;
import net.sf.joost.instruction.AbstractGroupBase;

/**
 * An XMLReader object that uses the events from a buffer.
 *
 * @version $Revision: 1.7 $ $Date: 2008/03/29 12:12:57 $
 * @author Oliver Becker
 */

public class BufferReader implements XMLReader
{
  /** the lexical handler object */
  private LexicalHandler m_aLexH;

  /** the content handler object */
  private ContentHandler m_aContH;

  /** the array of events to be feed into the external SAX processor */
  private final SAXEvent [] m_aEvents;

  private final String m_sPublicID, m_sSystemID;

  /**
   * Constructs a new <code>BufferReader</code> object.
   *
   * @param context
   *        the current context
   * @param bufExpName
   *        the internal expanded name
   * @param groupScope
   *        the scope of the buffer
   * @param publicId
   *        the public identifier to be used for the buffer
   * @param systemId
   *        the system identifier to be used for the buffer
   * @exception SAXException
   *            if there's no such buffer
   */
  public BufferReader (final Context context,
                       final String bufExpName,
                       final AbstractGroupBase groupScope,
                       final String publicId,
                       final String systemId) throws SAXException
  {
    final Emitter emitter = (Emitter) ((groupScope == null) ? context.localVars.get (bufExpName)
                                                            : ((Hashtable) ((Stack) context.groupVars.get (groupScope)).peek ()).get (bufExpName));
    // endDocument() doesn't add a event to the buffer.
    // However, it checks that the buffer contents is well-formed
    emitter.endDocument (context.currentInstruction);
    this.m_aEvents = ((BufferEmitter) emitter.m_aContH).getEvents ();
    this.m_sPublicID = publicId;
    this.m_sSystemID = systemId;
  }

  public void setFeature (final String name, final boolean state) throws SAXNotRecognizedException,
                                                                  SAXNotSupportedException
  {
    if (name.equals (CSTX.FEAT_NS))
    {
      if (!state)
        throw new SAXNotSupportedException ("Cannot switch off namespace support (attempt setting " +
                                            name +
                                            " to " +
                                            state +
                                            ")");
    }
    else
      if (name.equals (CSTX.FEAT_NSPREFIX))
      {
        if (state)
          throw new SAXNotSupportedException ("Cannot report namespace declarations as attributes " +
                                              "(attempt setting " +
                                              name +
                                              " to " +
                                              state +
                                              ")");
      }
      else
        throw new SAXNotRecognizedException (name);
  }

  public boolean getFeature (final String name) throws SAXNotRecognizedException
  {
    if (name.equals (CSTX.FEAT_NS))
      return true;
    if (name.equals (CSTX.FEAT_NSPREFIX))
      return false;
    throw new SAXNotRecognizedException (name);
  }

  public void setProperty (final String name, final Object value) throws SAXNotRecognizedException,
                                                                  SAXNotSupportedException
  {
    if (name.equals ("http://xml.org/sax/properties/lexical-handler"))
      m_aLexH = (LexicalHandler) value;
    else
      throw new SAXNotRecognizedException (name);
  }

  public Object getProperty (final String name) throws SAXNotRecognizedException
  {
    if (name.equals ("http://xml.org/sax/properties/lexical-handler"))
      return m_aLexH;
    throw new SAXNotRecognizedException (name);
  }

  /** does nothing */
  public void setEntityResolver (final EntityResolver resolver)
  {}

  /** @return <code>null</code> */
  public EntityResolver getEntityResolver ()
  {
    return null;
  }

  /** does nothing */
  public void setDTDHandler (final DTDHandler handler)
  {}

  /** @return <code>null</code> */
  public DTDHandler getDTDHandler ()
  {
    return null;
  }

  public void setContentHandler (final ContentHandler handler)
  {
    m_aContH = handler;
  }

  public ContentHandler getContentHandler ()
  {
    return m_aContH;
  }

  /** does nothing */
  public void setErrorHandler (final ErrorHandler handler)
  {}

  public ErrorHandler getErrorHandler ()
  {
    return null;
  }

  public void parse (final InputSource dummy) throws SAXException
  {
    if (m_aContH == null)
    { // shouldn't happen
      throw new SAXException ("Missing ContentHandler for buffer processing");
    }
    if (m_aLexH == null)
    {
      if (m_aContH instanceof LexicalHandler)
        m_aLexH = (LexicalHandler) m_aContH;
    }
    final LocatorImpl locator = new LocatorImpl ();
    locator.setPublicId (m_sPublicID);
    locator.setSystemId (m_sSystemID);
    m_aContH.setDocumentLocator (locator);
    // Note: call startDocument() and endDocument() only for external
    // processing (when parse() is invoked by someone else)
    m_aContH.startDocument ();
    parse (m_aContH, m_aLexH);
    m_aContH.endDocument ();
  }

  public void parse (final String dummy) throws SAXException
  {
    // seems that nobody calls this method in my scenario, anyway ...
    parse ((InputSource) null);
  }

  /**
   * Do the real work: emit SAX events to the handler objects.
   */
  public void parse (final ContentHandler contH, final LexicalHandler lexH) throws SAXException
  {
    // generate events
    for (final SAXEvent ev : m_aEvents)
    {
      switch (ev.m_nType)
      {
        case SAXEvent.ELEMENT:
          contH.startElement (ev.m_sURI, ev.m_sLocalName, ev.m_sQName, ev.m_aAttrs);
          break;
        case SAXEvent.ELEMENT_END:
          contH.endElement (ev.m_sURI, ev.m_sLocalName, ev.m_sQName);
          break;
        case SAXEvent.TEXT:
          contH.characters (ev.m_sValue.toCharArray (), 0, ev.m_sValue.length ());
          break;
        case SAXEvent.CDATA:
          if (lexH != null)
          {
            lexH.startCDATA ();
            contH.characters (ev.m_sValue.toCharArray (), 0, ev.m_sValue.length ());
            lexH.endCDATA ();
          }
          else
            contH.characters (ev.m_sValue.toCharArray (), 0, ev.m_sValue.length ());
          break;
        case SAXEvent.PI:
          contH.processingInstruction (ev.m_sQName, ev.m_sValue);
          break;
        case SAXEvent.COMMENT:
          if (lexH != null)
            lexH.comment (ev.m_sValue.toCharArray (), 0, ev.m_sValue.length ());
          break;
        case SAXEvent.MAPPING:
          contH.startPrefixMapping (ev.m_sQName, ev.m_sValue);
          break;
        case SAXEvent.MAPPING_END:
          contH.endPrefixMapping (ev.m_sQName);
          break;
      }
    }
  }
}
