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
package net.sf.joost.trax;

import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.joost.CSTX;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Parser;
import net.sf.joost.stx.Processor;
import net.sf.joost.trace.DebugProcessor;

/**
 * This class implements the Templates-Interface for TraX. Templates are
 * thread-safe, so create one templates and call newTransformer() to get a new
 * Transformer-Object.
 *
 * @author Zubow
 */
public class TemplatesImpl implements Templates
{

  // Define a static logger variable so that it references the
  // Logger instance named "TemplatesImpl".
  private static final Logger log = LoggerFactory.getLogger (TemplatesImpl.class);

  /**
   * Holding a reference on a <code>TransformerFactoryImpl</code> should be
   * visible for {@link TrAXFilter TrAXFilter}
   */
  protected TransformerFactoryImpl m_aFactory;

  /**
   * Holding a reference on the Joost-STX-Processor <code>Processor</code>
   */
  private Processor m_aProcessor;

  /**
   * Synch object to guard against setting values from the TrAX interface or
   * reentry while the transform is going on.
   */
  private final Object reentryGuard = new Object ();

  /**
   * Constructor used by {@link net.sf.joost.trax.TemplatesHandlerImpl}
   *
   * @param stxParser
   *        A parsed stylesheet in form of <code>Parser</code>
   */
  protected TemplatesImpl (final Parser stxParser,
                           final TransformerFactoryImpl factory) throws TransformerConfigurationException
  {

    if (CSTX.DEBUG)
      log.debug ("calling constructor with existing Parser");
    this.m_aFactory = factory;
    try
    {
      // configure the template
      init (stxParser);
    }
    catch (final TransformerConfigurationException tE)
    {
      log.error ("Exception", tE);
      throw tE;
    }
  }

  /**
   * Constructor.
   *
   * @param reader
   *        The <code>XMLReader</code> for parsing the stylesheet
   * @param isource
   *        The <code>InputSource</code> of the stylesheet
   * @param factory
   *        A reference on a <code>TransformerFactoryImpl</code>
   * @throws TransformerConfigurationException
   *         When an error occurs.
   */
  protected TemplatesImpl (final XMLReader reader,
                           final InputSource isource,
                           final TransformerFactoryImpl factory) throws TransformerConfigurationException
  {

    if (CSTX.DEBUG)
      log.debug ("calling constructor with SystemId " + isource.getSystemId ());
    this.m_aFactory = factory;
    try
    {
      // configure template
      init (reader, isource);
    }
    catch (final TransformerConfigurationException tE)
    {
      factory.m_aDefaultErrorListener.fatalError (tE);
    }
  }

  /**
   * Configures the <code>Templates</code> - initializing with a completed
   * <code>Parser</code> object.
   *
   * @param stxParser
   *        A <code>Parser</code>
   * @throws TransformerConfigurationException
   *         When an error occurs while initializing the <code>Templates</code>.
   */
  private void init (final Parser stxParser) throws TransformerConfigurationException
  {

    if (CSTX.DEBUG)
      log.debug ("init without InputSource ");
    try
    {
      // check if transformerfactory is in debug mode
      final boolean debugmode = ((Boolean) this.m_aFactory.getAttribute (CTrAX.DEBUG_FEATURE)).booleanValue ();

      if (debugmode)
      {
        log.info ("init transformer in debug mode");
        m_aProcessor = new DebugProcessor (stxParser);
      }
      else
      {
        m_aProcessor = new Processor (stxParser);
      }
      m_aProcessor.setTransformerHandlerResolver (m_aFactory.m_aTHResolver);
      m_aProcessor.setOutputURIResolver (m_aFactory.m_aOutputUriResolver);
    }
    catch (final org.xml.sax.SAXException sE)
    {
      log.error ("Exception", sE);
      throw new TransformerConfigurationException (sE.getMessage ());
    }
    catch (final java.lang.NullPointerException nE)
    {
      log.error ("Exception", nE);
      throw new TransformerConfigurationException ("Could not found value for property javax.xml.parsers.SAXParser " +
                                                   nE.getMessage ());
    }
  }

  /**
   * Configures the <code>Templates</code> - initializing by parsing the
   * stylesheet.
   *
   * @param reader
   *        The <code>XMLReader</code> for parsing the stylesheet
   * @param isource
   *        The <code>InputSource</code> of the stylesheet
   * @throws TransformerConfigurationException
   *         When an error occurs while initializing the <code>Templates</code>.
   */
  private void init (final XMLReader reader, final InputSource isource) throws TransformerConfigurationException
  {

    if (CSTX.DEBUG)
      log.debug ("init with InputSource " + isource.getSystemId ());
    try
    {
      /**
       * Register ErrorListener from
       * {@link TransformerFactoryImpl#getErrorListener()} if available.
       */
      // check if transformerfactory is in debug mode
      final boolean debugmode = ((Boolean) this.m_aFactory.getAttribute (CTrAX.DEBUG_FEATURE)).booleanValue ();

      final ParseContext pContext = new ParseContext ();
      pContext.allowExternalFunctions = m_aFactory.m_bAllowExternalFunctions;
      pContext.setErrorListener (m_aFactory.getErrorListener ());
      pContext.uriResolver = m_aFactory.getURIResolver ();
      if (debugmode)
      {
        if (CSTX.DEBUG)
          log.info ("init transformer in debug mode");
        pContext.parserListener = m_aFactory.getParserListenerMgr ();
        m_aProcessor = new DebugProcessor (reader, isource, pContext, m_aFactory.getMessageEmitter ());
      }
      else
      {
        m_aProcessor = new Processor (reader, isource, pContext);
      }
      m_aProcessor.setTransformerHandlerResolver (m_aFactory.m_aTHResolver);
      m_aProcessor.setOutputURIResolver (m_aFactory.m_aOutputUriResolver);
    }
    catch (final java.io.IOException iE)
    {
      if (CSTX.DEBUG)
        log.debug ("Exception", iE);
      throw new TransformerConfigurationException (iE.getMessage (), iE);
    }
    catch (final org.xml.sax.SAXException sE)
    {
      final Exception emb = sE.getException ();
      if (emb instanceof TransformerConfigurationException)
        throw (TransformerConfigurationException) emb;
      if (CSTX.DEBUG)
        log.debug ("Exception", sE);
      throw new TransformerConfigurationException (sE.getMessage (), sE);
    }
    catch (final java.lang.NullPointerException nE)
    {
      if (CSTX.DEBUG)
        log.debug ("Exception", nE);
      nE.printStackTrace (System.err);
      throw new TransformerConfigurationException ("could not found value for property javax.xml.parsers.SAXParser ",
                                                   nE);
    }
  }

  /**
   * Method returns a Transformer-instance for transformation-process
   *
   * @return A <code>Transformer</code> object.
   * @throws TransformerConfigurationException
   */
  public Transformer newTransformer () throws TransformerConfigurationException
  {

    synchronized (reentryGuard)
    {
      if (CSTX.DEBUG)
        log.debug ("calling newTransformer to get a " + "Transformer object for Transformation");
      try
      {
        // register the processor
        final Transformer transformer = new TransformerImpl (m_aProcessor.copy ());
        if (m_aFactory.getURIResolver () != null)
          transformer.setURIResolver (m_aFactory.getURIResolver ());
        return transformer;
      }
      catch (final SAXException e)
      {
        log.error ("Exception", e);
        throw new TransformerConfigurationException (e.getMessage ());
      }
    }
  }

  /**
   * Gets the static properties for stx:output.
   *
   * @return Properties according to JAXP-Spec or null if an error is occured.
   */
  public Properties getOutputProperties ()
  {

    try
    {
      final Transformer transformer = newTransformer ();
      return transformer.getOutputProperties ();
    }
    catch (final TransformerConfigurationException tE)
    {
      try
      {
        m_aFactory.m_aDefaultErrorListener.fatalError (tE);
      }
      catch (final TransformerConfigurationException e)
      {}
      return null;
    }
  }
}
