/*
 * $Id: TransformerHandlerImpl.java,v 1.15 2007/07/15 15:20:41 obecker Exp $
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

package net.sf.joost.trax;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.TransformerHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.DOMEmitter;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.stx.Processor;

/**
 * This class implements the TransformerHandler-Interface for TraX. This class
 * could be used with a SAXResult. So you can simply downcast the
 * TransformerFactory to a SAXTransformerFactory, calling
 * getTransformerHandler() and process the transformation with a Sax-Parser.
 * TransformerHandler acts as a proxy an propagates the Sax-Events to the
 * underlying joost-stx-engine the Processor-class
 *
 * @author Zubow
 */
public class TransformerHandlerImpl implements TransformerHandler
{
  private static final Logger log = LoggerFactory.getLogger (TransformerHandlerImpl.class);

  /**
   * Processor is the joost-stx-engine
   */
  private Processor m_aProcessor;
  private final Transformer m_aTransformer;
  /**
   * Handler for constructing the Resulttype.
   */
  private IStxEmitter m_aSTXEmitter;

  /**
   * Necessary for the document root.
   */
  private String m_sSystemID;

  /**
   * The according Result.
   */
  private Result m_aResult;

  /**
   * Constructor.
   *
   * @param transformer
   */
  protected TransformerHandlerImpl (final Transformer transformer)
  {
    if (CSTX.DEBUG)
      log.debug ("calling constructor");
    // Save the reference to the transformer
    this.m_aTransformer = transformer;
  }

  // *************************************************************************
  // IMPLEMENTATION OF TransformerHandler
  // *************************************************************************

  /**
   * Getter for {@link #m_sSystemID}
   *
   * @return <code>String</code>
   */
  public String getSystemId ()
  {
    return m_sSystemID;
  }

  /**
   * Gets a <code>Transformer</code> object.
   *
   * @return <code>String</code>
   */
  public Transformer getTransformer ()
  {
    return m_aTransformer;
  }

  /**
   * Setter for {@link #m_aResult}
   *
   * @param result
   *        A <code>Result</code>
   * @throws IllegalArgumentException
   */
  public void setResult (final Result result) throws IllegalArgumentException
  {

    if (CSTX.DEBUG)
      log.debug ("setting Result - here SAXResult");

    try
    {
      this.m_aResult = result;
      // init saxresult
      init (result);
    }
    catch (final TransformerException e)
    {
      if (m_aTransformer instanceof TransformerImpl)
      {
        final TransformerConfigurationException tE = new TransformerConfigurationException (e.getMessage (), e);
        try
        {
          m_aTransformer.getErrorListener ().fatalError (tE);
        }
        catch (final TransformerException innerE)
        {
          throw new IllegalArgumentException (innerE.getMessage ());
        }
      }
      else
      {
        log.error ("Exception", e);
        throw new IllegalArgumentException ("result is invalid.");
      }
    }
  }

  /**
   * Setter for {@link #m_sSystemID}
   *
   * @param systemId
   *        the system identifier to set
   */
  public void setSystemId (final String systemId)
  {
    this.m_sSystemID = systemId;
  }

  // *************************************************************************
  // Helper methods
  // *************************************************************************

  /**
   * Helpermethod
   */
  private void init (final Result result) throws TransformerException
  {

    if (CSTX.DEBUG)
      log.debug ("init emitter-class according to result");

    if (this.m_aTransformer instanceof TransformerImpl)
    {
      this.m_aProcessor = ((TransformerImpl) this.m_aTransformer).getStxProcessor ();

      // initialize Emitter --> DOM-, SAX- or StreamEmitter
      m_aSTXEmitter = TrAXHelper.initStxEmitter (result, m_aProcessor, null);
      m_aSTXEmitter.setSystemId (result.getSystemId ());
      // setting Handler
      this.m_aProcessor.setContentHandler (m_aSTXEmitter);
      this.m_aProcessor.setLexicalHandler (m_aSTXEmitter);
    }
  }

  // *************************************************************************
  // IMPLEMENTATION of ContentHandler, LexicalHandler, DTDHandler
  // *************************************************************************

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void setDocumentLocator (final Locator locator)
  {
    m_aProcessor.setDocumentLocator (locator);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void startDocument () throws SAXException
  {
    m_aProcessor.startDocument ();
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void endDocument () throws SAXException
  {
    m_aProcessor.endDocument ();

    // set the constructed DOM-Node on the DOMResult
    if (m_aResult instanceof DOMResult)
    {
      if (CSTX.DEBUG)
        log.debug ("result is a DOMResult");
      final Node nodeResult = ((DOMEmitter) m_aSTXEmitter).getDOMTree ();
      // DOM specific Implementation
      ((DOMResult) m_aResult).setNode (nodeResult);
      return;
    }
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void startPrefixMapping (final String prefix, final String uri) throws SAXException
  {

    m_aProcessor.startPrefixMapping (prefix, uri);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void endPrefixMapping (final String prefix) throws SAXException
  {
    m_aProcessor.endPrefixMapping (prefix);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void startElement (final String namespaceURI,
                            final String localName,
                            final String qName,
                            final Attributes atts) throws SAXException
  {

    m_aProcessor.startElement (namespaceURI, localName, qName, atts);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void endElement (final String namespaceURI, final String localName, final String qName) throws SAXException
  {

    m_aProcessor.endElement (namespaceURI, localName, qName);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {

    m_aProcessor.characters (ch, start, length);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void ignorableWhitespace (final char [] ch, final int start, final int length) throws SAXException
  {

    m_aProcessor.ignorableWhitespace (ch, start, length);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void processingInstruction (final String target, final String data) throws SAXException
  {

    m_aProcessor.processingInstruction (target, data);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void skippedEntity (final String name) throws SAXException
  {
    m_aProcessor.skippedEntity (name);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void startDTD (final String name, final String publicId, final String systemId) throws SAXException
  {

    m_aProcessor.startDTD (name, publicId, systemId);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void endDTD () throws SAXException
  {
    m_aProcessor.endDTD ();
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void startEntity (final String name) throws SAXException
  {
    m_aProcessor.startEntity (name);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void endEntity (final String name) throws SAXException
  {
    m_aProcessor.endEntity (name);
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void startCDATA () throws SAXException
  {
    m_aProcessor.startCDATA ();
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void endCDATA () throws SAXException
  {
    m_aProcessor.endCDATA ();
  }

  /**
   * Propagates the Sax-Event to Joost-Processor.
   */
  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    m_aProcessor.comment (ch, start, length);
  }

  /**
   * Sax-Event - empty
   */
  public void notationDecl (final String name, final String publicId, final String systemId) throws SAXException
  {
    // what do with this ??? no analogon in Processor-class
  }

  /**
   * Sax-Event - empty
   */
  public void unparsedEntityDecl (final String name,
                                  final String publicId,
                                  final String systemId,
                                  final String notationName) throws SAXException
  {
    // what do with this ??? no analogon in Processor-class
  }
}
