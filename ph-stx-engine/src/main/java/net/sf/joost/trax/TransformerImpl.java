/*
 * $Id: TransformerImpl.java,v 1.32 2009/03/15 14:01:22 obecker Exp $
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.joost.CSTX;
import net.sf.joost.OptionalLog;
import net.sf.joost.emitter.DOMEmitter;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.Processor;
import net.sf.joost.trace.DebugEmitter;
import net.sf.joost.trace.DebugProcessor;
import net.sf.joost.trace.TraceManager;

/**
 * This class implements the Transformer-Interface for TraX. With a
 * Transformer-object you can proceed transformations, but be careful, because a
 * Transformer-object is not thread- safe. For threads you should use Templates.
 *
 * @author Zubow
 */
public class TransformerImpl extends Transformer implements TrAXConstants
{

  // Define a static logger variable so that it references the
  // Logger instance named "TransformerImpl".
  private static Logger log = OptionalLog.getLog (TransformerImpl.class);

  private Processor m_aProcessor = null; // Bugfix

  private URIResolver uriRes = null;

  // init with default errorlistener
  private ErrorListener errorListener = new TransformationErrListener ();

  // output properties
  private final Set <String> supportedProperties = new HashSet<> ();
  private final Set <String> ignoredProperties = new HashSet<> ();

  /**
   * Synch object to gaurd against setting values from the TrAX interface or
   * reentry while the transform is going on.
   */
  private final Boolean reentryGuard = Boolean.TRUE;

  /**
   * This is a compile-time flag to enable or disable calling of trace
   * listeners. For optimization purposes this flag must be set to false.
   */
  public static boolean DEBUG_MODE = false;

  /**
   * This is a run-time flag (only used when {@link #DEBUG_MODE} is true). If
   * the value is <code>true</code>, the transformation will be cancelled at the
   * next possible execution step.
   */
  public boolean cancelTransformation = false;

  /**
   * The trace manager.
   */
  private final TraceManager traceManager = new TraceManager ();

  /**
   * Constructor
   *
   * @param processor
   *        A <code>Processor</code> object.
   */
  protected TransformerImpl (final Processor processor)
  {
    this.m_aProcessor = processor;

    // set tracing manager on processor object
    if (processor instanceof DebugProcessor)
    {
      final DebugProcessor dbp = (DebugProcessor) processor;
      dbp.setTraceManager (traceManager);
      dbp.setTransformer (this);

      final Emitter emitter = processor.getEmitter ();
      if (emitter instanceof DebugEmitter)
      {
        ((DebugEmitter) emitter).setTraceManager (traceManager);
      }
    }
    supportedProperties.add (OutputKeys.ENCODING);
    supportedProperties.add (OutputKeys.MEDIA_TYPE);
    supportedProperties.add (OutputKeys.METHOD);
    supportedProperties.add (OutputKeys.OMIT_XML_DECLARATION);
    supportedProperties.add (OutputKeys.STANDALONE);
    supportedProperties.add (OutputKeys.VERSION);
    supportedProperties.add (TrAXConstants.OUTPUT_KEY_SUPPORT_DISABLE_OUTPUT_ESCAPING);

    ignoredProperties.add (OutputKeys.CDATA_SECTION_ELEMENTS);
    ignoredProperties.add (OutputKeys.DOCTYPE_PUBLIC);
    ignoredProperties.add (OutputKeys.DOCTYPE_SYSTEM);
    ignoredProperties.add (OutputKeys.INDENT);
  }

  /**
   * Get an instance of the tracemanager for this transformation. This object
   * can be used to set tracelisteners on various events during the
   * transformation.
   *
   * @return A reference to a tracemanager
   */
  public TraceManager getTraceManager ()
  {
    return traceManager;
  }

  /**
   * Transforms a xml-source : SAXSource, DOMSource, StreamSource to SAXResult,
   * DOMResult and StreamResult
   *
   * @param xmlSource
   *        A <code>Source</code>
   * @param result
   *        A <code>Result</code>
   * @throws TransformerException
   */
  @Override
  public void transform (final Source xmlSource, final Result result) throws TransformerException
  {

    IStxEmitter out = null;
    SAXSource saxSource = null;

    // should be synchronized
    synchronized (reentryGuard)
    {
      if (CSTX.DEBUG)
        log.debug ("perform transformation from " +
                   "xml-source(SAXSource, DOMSource, StreamSource) " +
                   "to SAXResult, DOMResult or StreamResult");
      try
      {

        // init StxEmitter
        out = TrAXHelper.initStxEmitter (result, m_aProcessor, null);
        out.setSystemId (result.getSystemId ());

        this.m_aProcessor.setContentHandler (out);
        this.m_aProcessor.setLexicalHandler (out);

        // register ErrorListener
        if (this.errorListener != null)
        {
          this.m_aProcessor.setErrorListener (errorListener);
        }

        // construct from source a SAXSource
        saxSource = TrAXHelper.getSAXSource (xmlSource, errorListener);

        final InputSource isource = saxSource.getInputSource ();

        if (isource != null)
        {
          if (CSTX.DEBUG)
            log.debug ("perform transformation");

          if (saxSource.getXMLReader () != null)
          {
            // should not be an DOMSource
            if (xmlSource instanceof SAXSource)
            {

              final XMLReader xmlReader = ((SAXSource) xmlSource).getXMLReader ();

              /**
               * URIs for Identifying Feature Flags and Properties : There is no
               * fixed set of features or properties available for SAX2, except
               * for two features that all XML parsers must support.
               * Implementors are free to define new features and properties as
               * needed, using URIs to identify them. All XML readers are
               * required to recognize the
               * "http://xml.org/sax/features/namespaces" and the
               * "http://xml.org/sax/features/namespace-prefixes" features (at
               * least to get the feature values, if not set them) and to
               * support a true value for the namespaces property and a false
               * value for the namespace-prefixes property. These requirements
               * ensure that all SAX2 XML readers can provide the minimal
               * required Namespace support for higher-level specs such as RDF,
               * XSL, XML Schemas, and XLink. XML readers are not required to
               * recognize or support any other features or any properties. For
               * the complete list of standard SAX2 features and properties, see
               * the {@link org.xml.sax} Package Description.
               */
              if (xmlReader != null)
              {
                try
                {
                  // set the required
                  // "http://xml.org/sax/features/namespaces" Feature
                  xmlReader.setFeature (CSTX.FEAT_NS, true);
                  // set the required
                  // "http://xml.org/sax/features/namespace-prefixes"
                  // Feature
                  xmlReader.setFeature (CSTX.FEAT_NSPREFIX, false);
                  // maybe there would be other features
                }
                catch (final SAXException sE)
                {
                  getErrorListener ().warning (new TransformerException (sE.getMessage (), sE));
                }
              }
            }
            // set the the SAXSource as the parent of the STX-Processor
            this.m_aProcessor.setParent (saxSource.getXMLReader ());
          }

          // perform transformation
          this.m_aProcessor.parse (isource);
        }
        else
        {
          final TransformerException tE = new TransformerException ("InputSource is null - could not perform transformation");
          getErrorListener ().fatalError (tE);
        }
        // perform result
        performResults (result, out);
      }
      catch (final SAXException ex)
      {
        TransformerException tE;
        final Exception emb = ex.getException ();
        if (emb instanceof TransformerException)
        {
          tE = (TransformerException) emb;
        }
        else
        {
          tE = new TransformerException (ex.getMessage (), ex);
        }
        getErrorListener ().fatalError (tE);
      }
      catch (final IOException ex)
      {
        // will this ever happen?
        getErrorListener ().fatalError (new TransformerException (ex.getMessage (), ex));
      }
    }
  }

  /**
   * Performs the <code>Result</code>.
   *
   * @param result
   *        A <code>Result</code>
   * @param out
   *        <code>StxEmitter</code>.
   */
  private void performResults (final Result result, final IStxEmitter out)
  {

    if (CSTX.DEBUG)
      log.debug ("perform result");
    // DOMResult
    if (result instanceof DOMResult)
    {
      if (CSTX.DEBUG)
        log.debug ("result is a DOMResult");
      final Node nodeResult = ((DOMEmitter) out).getDOMTree ();
      // DOM specific Implementation
      ((DOMResult) result).setNode (nodeResult);
      return;
    }
    // StreamResult
    if (result instanceof StreamResult)
    {
      if (CSTX.DEBUG)
        log.debug ("result is a StreamResult");
      return;
    }
    // SAXResult
    if (result instanceof SAXResult)
    {
      if (CSTX.DEBUG)
        log.debug ("result is a SAXResult");
      return;
    }
  }

  /**
   * Getter for an output property.
   *
   * @param name
   *        The key of the output property.
   * @return The value for that property, <code>null</code> if not set.
   * @throws IllegalArgumentException
   */
  @Override
  public String getOutputProperty (final String name) throws IllegalArgumentException
  {

    if (supportedProperties.contains (name))
      return m_aProcessor.outputProperties.getProperty (name);
    if (ignoredProperties.contains (name))
      return null;
    final IllegalArgumentException iE = new IllegalArgumentException ("Unsupported property " + name);
    log.error (iE.getMessage (), iE);
    throw iE;
  }

  /**
   * Setter for an output property.
   *
   * @param name
   *        The key of the outputProperty.
   * @param value
   *        The value of the outputProperty.
   * @throws IllegalArgumentException
   */
  @Override
  public void setOutputProperty (final String name, final String value) throws IllegalArgumentException
  {

    IllegalArgumentException iE;
    if (supportedProperties.contains (name))
    {
      if (OutputKeys.METHOD.equals (name) && !isValidOutputMethod (value))
      {
        iE = new IllegalArgumentException ("Unsupported output method " + value);
        log.error (iE.getMessage (), iE);
        throw iE;
      }
      m_aProcessor.outputProperties.setProperty (name, value);
    }
    else
      if (ignoredProperties.contains (name))
      {
        log.warn ("Output property '" + name + "' is not supported and will be ignored");
      }
      else
      {
        iE = new IllegalArgumentException ("Invalid output property '" + name + "'");
        log.error (iE.getMessage (), iE);
        throw iE;
      }
  }

  /**
   * Getter for {@link Processor#outputProperties}
   *
   * @return a copy of the current output properties
   */
  @Override
  public Properties getOutputProperties ()
  {
    return (Properties) m_aProcessor.outputProperties.clone ();
  }

  /**
   * Setter for {@link Processor#outputProperties}
   *
   * @param oformat
   *        A <code>Properties</code> object, that replaces the current set of
   *        output properties.
   * @throws IllegalArgumentException
   */
  @Override
  public void setOutputProperties (final Properties oformat) throws IllegalArgumentException
  {
    if (oformat == null)
    {
      m_aProcessor.initOutputProperties (); // re-initialize
    }
    else
    {
      IllegalArgumentException iE;
      // check properties in oformat
      for (final Enumeration e = oformat.keys (); e.hasMoreElements ();)
      {
        final Object propKey = e.nextElement ();
        if (ignoredProperties.contains (propKey))
        {
          log.warn ("Output property '" + propKey + "' is not supported and will be ignored");
          continue;
        }
        if (!supportedProperties.contains (propKey))
        {
          iE = new IllegalArgumentException ("Invalid output property '" + propKey + "'");
          log.error ("Exception", iE);
          throw iE;
        }
        final String propVal = oformat.getProperty ((String) propKey);
        if (OutputKeys.METHOD.equals (propKey) && !isValidOutputMethod (propVal))
        {
          iE = new IllegalArgumentException ("Unsupported output method " + oformat.getProperty ((String) propKey));
          log.error ("Exception", iE);
          throw iE;
        }
      }
      m_aProcessor.outputProperties = (Properties) oformat.clone ();
    }
  }

  /**
   * @return <code>true</code> if <code>value</code> is a valid output method
   */
  private boolean isValidOutputMethod (final String value)
  {
    return value.startsWith ("{") // qualified name
           || "xml".equals (value) || "text".equals (value);
  }

  /**
   * Getter for {@link #uriRes}
   *
   * @return <code>URIResolver</code>
   */
  @Override
  public URIResolver getURIResolver ()
  {
    return uriRes;
  }

  /**
   * Setter for {@link #uriRes}
   *
   * @param resolver
   *        A <code>URIResolver</code> object.
   */
  @Override
  public void setURIResolver (final URIResolver resolver)
  {
    synchronized (reentryGuard)
    {
      uriRes = resolver;
      m_aProcessor.setURIResolver (resolver);
    }
  }

  /**
   * Clears all parameters
   */
  @Override
  public void clearParameters ()
  {
    m_aProcessor.clearParameters ();
  }

  /**
   * Setter for parameters.
   *
   * @param name
   *        The key of the parameter.
   * @param value
   *        The value of the parameter.
   */
  @Override
  public void setParameter (final String name, final Object value)
  {
    m_aProcessor.setParameter (name, value);
  }

  /**
   * Getter for parameters.
   *
   * @param name
   *        The key-value of the parameter.
   * @return An <code>Object</code> according to the key-value or null.
   */
  @Override
  public Object getParameter (final String name)
  {
    return m_aProcessor.getParameter (name);
  }

  /**
   * @param listener
   * @throws IllegalArgumentException
   */
  @Override
  public void setErrorListener (final ErrorListener listener) throws IllegalArgumentException
  {

    synchronized (reentryGuard)
    {
      errorListener = listener;
    }
  }

  /**
   * Setter for {@link #errorListener}
   *
   * @return A <code>ErrorListener</code>
   */
  @Override
  public ErrorListener getErrorListener ()
  {
    return errorListener;
  }

  /**
   * Getter for {@link #m_aProcessor}
   *
   * @return A <code>Processor</code> object.
   */
  public Processor getStxProcessor ()
  {
    // Processor tempProcessor = new Processor(processor);
    return m_aProcessor;
  }
}
