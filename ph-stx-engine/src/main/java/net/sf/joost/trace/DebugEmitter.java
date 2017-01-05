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
package net.sf.joost.trace;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.ErrorHandlerImpl;
import net.sf.joost.stx.SAXEvent;

/**
 * Extends the {@link net.sf.joost.stx.Emitter} with debug features.
 *
 * @version $Revision: 1.12 $ $Date: 2006/02/03 19:04:46 $
 * @author Zubow
 */
public class DebugEmitter extends Emitter
{
  /** logger */
  private static final Logger log = LoggerFactory.getLogger (DebugEmitter.class);

  /** for dynamic tracing */
  private TraceManager m_aTraceMgr;

  /** handle locator information */
  private final LocatorImpl m_aLocator = new LocatorImpl ();

  public DebugWriter m_aWriter;

  /**
   * constructor see {@link Emitter#Emitter(ErrorHandlerImpl)}
   */
  public DebugEmitter (final ErrorHandlerImpl errorHandler)
  {
    super (errorHandler);
  }

  /**
   * Called from {@link #pushEmitter(IStxEmitter)}
   *
   * @param prev
   *        the previous emitter
   * @param handler
   *        the new content handler
   */
  private DebugEmitter (final DebugEmitter prev, final IStxEmitter handler)
  {
    super (prev, handler);
    m_aTraceMgr = prev.m_aTraceMgr;
    m_aWriter = prev.m_aWriter;
  }

  /*
   * (non-Javadoc)
   * @see net.sf.joost.stx.Emitter#pushEmitter(net.sf.joost.emitter.StxEmitter)
   */
  @Override
  public Emitter pushEmitter (final IStxEmitter handler)
  {
    return new DebugEmitter (this, handler);
  }

  /**
   * setter for {@link #m_aTraceMgr} property
   */
  public void setTraceManager (final TraceManager tmgr)
  {
    m_aTraceMgr = tmgr;
  }

  /**
   * getter for {@link #m_aTraceMgr} property
   */
  public TraceManager getTraceManager ()
  {
    return m_aTraceMgr;
  }

  public Locator getEmitterLocator ()
  {
    return m_aLocator;
  }

  /**
   * overloaded method for debug support see {@link Emitter#getResultWriter}
   */
  @Override
  public Writer getResultWriter (final String href,
                                 final String encoding,
                                 final String publicId,
                                 final String systemId,
                                 final int lineNo,
                                 final int colNo,
                                 final boolean append) throws java.io.IOException, SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("requesting writer for " + href);
    return m_aWriter = new DebugWriter (href);
  }

  // ------------------------------------------------------------------
  // Sax-callback methods
  // ------------------------------------------------------------------

  /**
   * overloaded method for debug information
   */
  @Override
  public void startDocument () throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("start resultdocument");
    // update locator
    updateLocator (null, null, -1, -1);
    m_aTraceMgr.fireStartResultDocument ();
  }

  /**
   * overloaded method for debug information
   */
  @Override
  public void endDocument (final AbstractInstruction instruction) throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("end resultdocument");
    super.endDocument (instruction);
    // update locator
    updateLocator (instruction.getNode ().m_sPublicID,
                   instruction.getNode ().m_sSystemID,
                   instruction.lineNo,
                   instruction.colNo);
    m_aTraceMgr.fireEndResultDocument ();
  }

  /**
   * overloaded method for debug information
   */
  @Override
  public void startElement (final String uri,
                            final String lName,
                            final String qName,
                            final Attributes attrs,
                            final Map <String, String> namespaces,
                            final AbstractNodeBase instruction) throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("start element in resultdoc");
    SAXEvent saxevent;
    saxevent = SAXEvent.newElement (uri, lName, qName, attrs, true, namespaces);

    super.startElement (uri, lName, qName, attrs, namespaces, instruction);
    // update locator
    updateLocator (instruction.m_sPublicID, instruction.m_sSystemID, instruction.lineNo, instruction.colNo);
    m_aTraceMgr.fireStartResultElement (saxevent);
  }

  /**
   * overloaded method for debug information
   */
  @Override
  public void endElement (final String uri,
                          final String lName,
                          final String qName,
                          final AbstractInstruction instruction) throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("end element in resultdoc");
    SAXEvent saxevent;
    // todo - namespace support - remove null value
    saxevent = SAXEvent.newElement (uri, lName, qName, null, true, null);
    // update locator
    updateLocator (instruction.getNode ().m_sPublicID,
                   instruction.getNode ().m_sSystemID,
                   instruction.lineNo,
                   instruction.colNo);
    super.endElement (uri, lName, qName, instruction);
    m_aTraceMgr.fireEndResultElement (saxevent);
  }

  /**
   * overloaded method for debug information
   */
  @Override
  public void characters (final char [] ch,
                          final int start,
                          final int length,
                          final AbstractNodeBase instruction) throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("characters in resultdoc");
    SAXEvent saxevent;
    saxevent = SAXEvent.newText (new String (ch, start, length));
    super.characters (ch, start, length, instruction);
    // update locator
    updateLocator (instruction.m_sPublicID, instruction.m_sSystemID, instruction.lineNo, instruction.colNo);
    m_aTraceMgr.fireResultText (saxevent);
  }

  /**
   * overloaded method for debug information
   */
  @Override
  public void processingInstruction (final String target,
                                     final String data,
                                     final AbstractNodeBase instruction) throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("processingInstruction in resultdoc");
    SAXEvent saxevent;
    saxevent = SAXEvent.newPI (target, data);
    super.processingInstruction (target, data, instruction);
    // update locator
    updateLocator (instruction.m_sPublicID, instruction.m_sSystemID, instruction.lineNo, instruction.colNo);
    m_aTraceMgr.fireResultPI (saxevent);
  }

  /**
   * overloaded method for debug information
   */
  @Override
  public void comment (final char [] ch,
                       final int start,
                       final int length,
                       final AbstractNodeBase instruction) throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("comment in resultdoc");
    SAXEvent saxevent;
    saxevent = SAXEvent.newComment (new String (ch, start, length));
    super.comment (ch, start, length, instruction);
    // update locator
    updateLocator (instruction.m_sPublicID, instruction.m_sSystemID, instruction.lineNo, instruction.colNo);
    m_aTraceMgr.fireResultComment (saxevent);
  }

  /**
   * overloaded method for debug information
   */
  @Override
  public void startCDATA (final AbstractNodeBase instruction) throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("start CDATA in resultdoc");
    super.startCDATA (instruction);
    // update locator
    updateLocator (instruction.m_sPublicID, instruction.m_sSystemID, instruction.lineNo, instruction.colNo);
    m_aTraceMgr.fireStartResultCDATA ();
  }

  /**
   * overloaded method for debug information
   */
  @Override
  public void endCDATA () throws SAXException
  {
    if (log.isDebugEnabled ())
      log.debug ("end CDATA in resultdoc");
    super.endCDATA ();
    // update locator
    updateLocator (null, null, -1, -1);
    m_aTraceMgr.fireEndResultCDATA ();
  }

  // ------------------------------------------------------------------------
  // helper methods
  // ------------------------------------------------------------------------
  private void updateLocator (final String publicId, final String systemId, final int lineNo, final int colNo)
  {
    if (log.isDebugEnabled ())
      log.debug ("update emitterlocator " + publicId + " " + systemId + " " + lineNo + "," + colNo);
    m_aLocator.setPublicId (publicId);
    m_aLocator.setSystemId (systemId);
    m_aLocator.setLineNumber (lineNo);
    m_aLocator.setColumnNumber (colNo);
  }

  // ------------------------------------------------------------------------
  // Inner classes
  // ------------------------------------------------------------------------

  public static final class DebugWriter extends StringWriter
  {
    private final String m_sHref;

    public DebugWriter (final String href)
    {
      m_sHref = href;
    }

    public String getHref ()
    {
      return m_sHref;
    }
  }
}
