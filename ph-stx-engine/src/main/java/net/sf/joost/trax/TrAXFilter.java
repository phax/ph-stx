/*
 * $Id: TrAXFilter.java,v 1.10 2007/11/25 15:03:01 obecker Exp $
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

import java.io.IOException;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.emitter.SAXEmitter;
import net.sf.joost.stx.Processor;

/**
 * TrAXFilter
 *
 * @author Zubow
 * @version 1.0
 */
public class TrAXFilter extends XMLFilterImpl
{
  private static final Logger log = LoggerFactory.getLogger (TrAXFilter.class);

  private final Templates m_aTemplates;
  private Processor m_aProcessor;

  // default ErrorListener
  private ConfigurationErrListener m_aConfigErrListener;

  /**
   * Constructor
   *
   * @param templates
   *        A <code>Templates</code>
   */
  protected TrAXFilter (final Templates templates)
  {

    if (CSTX.DEBUG)
      log.debug ("calling constructor");
    this.m_aTemplates = templates;
    if (templates instanceof TemplatesImpl)
    {
      m_aConfigErrListener = ((TemplatesImpl) templates).m_aFactory.m_aDefaultErrorListener;
    }
  }

  /**
   * Parses the <code>InputSource</code>
   *
   * @param input
   *        A <code>InputSource</code> object.
   * @throws SAXException
   * @throws IOException
   */
  @Override
  public void parse (final InputSource input) throws SAXException, IOException
  {

    Transformer transformer = null;
    if (CSTX.DEBUG)
    {
      if (log.isDebugEnabled ())
        log.debug ("parsing InputSource " + input.getSystemId ());
    }

    try
    {
      // get a new Transformer
      transformer = this.m_aTemplates.newTransformer ();
      if (transformer instanceof TransformerImpl)
      {
        this.m_aProcessor = ((TransformerImpl) transformer).getStxProcessor ();
      }
      else
      {
        final String msg = "An error is occured, because the given transformer is " +
                           "not an instance of TransformerImpl";
        log.error (msg);

      }
      XMLReader parent = this.getParent ();
      if (parent == null)
      {
        parent = XMLReaderFactory.createXMLReader ();
        setParent (parent);
      }

      ContentHandler handler = this.getContentHandler ();
      if (handler == null)
      {
        handler = parent.getContentHandler ();
      }
      if (handler == null)
        throw new SAXException ("no ContentHandler registered");

      // SAX specific Implementation
      final IStxEmitter out = new SAXEmitter (handler);

      if (m_aProcessor != null)
      {
        m_aProcessor.setContentHandler (out);
        m_aProcessor.setLexicalHandler (out);
      }
      else
      {
        throw new SAXException ("Joost-Processor is not correct configured.");
      }

      if (parent == null)
        throw new SAXException ("No parent for filter");
      parent.setContentHandler (this.m_aProcessor);
      parent.setProperty ("http://xml.org/sax/properties/lexical-handler", this.m_aProcessor);
      parent.setEntityResolver (this);
      parent.setDTDHandler (this);
      parent.setErrorHandler (this);
      parent.parse (input);
    }
    catch (final TransformerConfigurationException tE)
    {
      try
      {
        m_aConfigErrListener.fatalError (tE);
      }
      catch (final TransformerConfigurationException innerE)
      {
        throw new SAXException (innerE.getMessage (), innerE);
      }
    }
    catch (final SAXException sE)
    {
      try
      {
        m_aConfigErrListener.fatalError (new TransformerConfigurationException (sE.getMessage (), sE));
      }
      catch (final TransformerConfigurationException innerE)
      {
        throw new SAXException (innerE.getMessage (), innerE);
      }
    }
    catch (final IOException iE)
    {
      try
      {
        m_aConfigErrListener.fatalError (new TransformerConfigurationException (iE.getMessage (), iE));
      }
      catch (final TransformerConfigurationException innerE)
      {
        throw new IOException (innerE.getMessage ());
      }
    }
  }
}
