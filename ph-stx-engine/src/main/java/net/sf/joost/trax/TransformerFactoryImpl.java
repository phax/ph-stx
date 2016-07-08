/*
 * $Id: TransformerFactoryImpl.java,v 1.26 2008/06/15 08:11:22 obecker Exp $
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
 * Contributor(s): Oliver Becker.
 */

package net.sf.joost.trax;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import net.sf.joost.OptionalLog;
import net.sf.joost.OutputURIResolver;
import net.sf.joost.TransformerHandlerResolver;
import net.sf.joost.emitter.StreamEmitter;
import net.sf.joost.emitter.StxEmitter;
import net.sf.joost.stx.Processor;
import net.sf.joost.trace.ParserListenerMgr;

/**
 * This class implements the TransformerFactory-Interface for TraX. With the
 * help of this factory you can get a templates-object or directly a
 * transformer-object for the transformation process. If you use a SAXResult you
 * can simply downcast to SAXTransformerFactory and use it like a Sax-Parser.
 * 
 * @author Zubow
 */
public class TransformerFactoryImpl extends SAXTransformerFactory implements TrAXConstants
{

  // Define a static logger variable so that it references the
  // Logger instance named "TransformerFactoryImpl".
  private static Log log = OptionalLog.getLog (TransformerFactoryImpl.class);

  // Member
  private URIResolver uriResolver = null;
  private ErrorListener errorListener = null;
  protected TransformerHandlerResolver thResolver = null;
  protected OutputURIResolver outputUriResolver = null;
  protected boolean allowExternalFunctions = true;

  // init default errorlistener
  // visible for TemplatesImpl
  protected ConfigurationErrListener defaultErrorListener = new ConfigurationErrListener ();

  // indicates if the transformer is working in debug mode
  private boolean debugmode = false;

  // indicates which Emitter class for stx:message output should be used
  private StxEmitter msgEmitter;

  // Synch object to guard against setting values from the TrAX interface
  // or reentry while the transform is going on.
  private final Boolean reentryGuard = new Boolean (true);

  /**
   * The parserlistener manager for tracing purpose.
   */
  private final ParserListenerMgr parserListenerMgr = new ParserListenerMgr ();

  /**
   * The default constructor.
   */
  public TransformerFactoryImpl ()
  {
    try
    {
      // initialize default messageEmitter
      msgEmitter = StreamEmitter.newEmitter (System.err, null);
      ((StreamEmitter) msgEmitter).setOmitXmlDeclaration (true);
    }
    catch (final UnsupportedEncodingException e)
    {
      // must not and cannot happen since outputProperties was null
      throw new TransformerFactoryConfigurationError (e);
    }
  }

  // *************************************************************************
  // IMPLEMENTATION OF TransformerFactory
  // *************************************************************************

  /**
   * Returns the <code>Source</code> of the stylesheet associated with the
   * xml-document. Feature is not supported.
   * 
   * @param source
   *        The <code>Source</code> of the xml-document.
   * @param media
   *        Matching media-type.
   * @param title
   *        Matching title-type.
   * @param charset
   *        Matching charset-type.
   * @return A <code>Source</code> of the stylesheet.
   * @throws TransformerConfigurationException
   */
  @Override
  public Source getAssociatedStylesheet (final Source source,
                                         final String media,
                                         final String title,
                                         final String charset) throws TransformerConfigurationException
  {

    final TransformerConfigurationException tE = new TransformerConfigurationException ("Feature not supported");

    defaultErrorListener.fatalError (tE);
    return null;
  }

  /**
   * Allows the user to retrieve specific attributes of the underlying
   * implementation.
   * 
   * @param name
   *        The attribute name.
   * @return An object according to the attribute-name
   * @throws IllegalArgumentException
   *         When such a attribute does not exists.
   */
  @Override
  public Object getAttribute (final String name) throws IllegalArgumentException
  {

    if (KEY_TH_RESOLVER.equals (name))
    {
      return thResolver;
    }
    else
      if (KEY_OUTPUT_URI_RESOLVER.equals (name))
      {
        return outputUriResolver;
      }
      else
        if (MESSAGE_EMITTER_CLASS.equals (name))
        {
          return msgEmitter;
        }
        else
          if (KEY_XSLT_FACTORY.equals (name))
          {
            return System.getProperty (KEY_XSLT_FACTORY);
          }
          else
            if (ALLOW_EXTERNAL_FUNCTIONS.equals (name))
            {
              return new Boolean (allowExternalFunctions);
            }
            else
              if (DEBUG_FEATURE.equals (name))
              {
                return new Boolean (debugmode);
              }
              else
              {
                if (log != null)
                  log.warn ("Feature not supported: " + name);
                throw new IllegalArgumentException ("Feature not supported: " + name);
              }
  }

  /**
   * Allows the user to set specific attributes on the underlying
   * implementation. An attribute in this context is defined to be an option
   * that the implementation provides.
   * 
   * @param name
   *        Name of the attribute (key)
   * @param value
   *        Value of the attribute.
   * @throws IllegalArgumentException
   */
  @Override
  public void setAttribute (final String name, final Object value) throws IllegalArgumentException
  {

    if (KEY_TH_RESOLVER.equals (name))
    {
      thResolver = (TransformerHandlerResolver) value;
    }
    else
      if (KEY_OUTPUT_URI_RESOLVER.equals (name))
      {
        outputUriResolver = (OutputURIResolver) value;
      }
      else
        if (MESSAGE_EMITTER_CLASS.equals (name))
        {
          // object is of type string, so use reflection
          if (value instanceof String)
          {
            try
            {
              msgEmitter = buildMessageEmitter ((String) value);
            }
            catch (final TransformerConfigurationException e)
            {
              if (log != null)
                log.fatal (e.getMessage (), e);
              throw new IllegalArgumentException (e.getMessage ());
            }
          }
          else
            if (value instanceof StxEmitter)
            { // already instantiated
              msgEmitter = (StxEmitter) value;
            }
            else
            {
              throw new IllegalArgumentException ("Emitter is of wrong type," +
                                                  "should be either a String or a StxEmitter");
            }
        }
        else
          if (KEY_XSLT_FACTORY.equals (name))
          {
            System.setProperty (KEY_XSLT_FACTORY, (String) value);
          }
          else
            if (ALLOW_EXTERNAL_FUNCTIONS.equals (name))
            {
              this.allowExternalFunctions = ((Boolean) value).booleanValue ();
            }
            else
              if (DEBUG_FEATURE.equals (name))
              {
                this.debugmode = ((Boolean) value).booleanValue ();
              }
              else
              {
                if (log != null)
                  log.warn ("Feature not supported: " + name);
                throw new IllegalArgumentException ("Feature not supported: " + name);
              }
  }

  /**
   * Getter for {@link #errorListener}
   * 
   * @return The registered <code>ErrorListener</code>
   */
  @Override
  public ErrorListener getErrorListener ()
  {
    return errorListener;
  }

  /**
   * Setter for {@link #errorListener}
   * 
   * @param errorListener
   *        The <code>ErrorListener</code> object.
   * @throws IllegalArgumentException
   */
  @Override
  public void setErrorListener (final ErrorListener errorListener) throws IllegalArgumentException
  {

    synchronized (reentryGuard)
    {
      if (DEBUG)
        log.debug ("setting ErrorListener");
      if (errorListener == null)
      {
        throw new IllegalArgumentException ("ErrorListener is null");
      }
      this.errorListener = errorListener;
      defaultErrorListener.setUserErrorListener (errorListener);
    }
  }

  /**
   * Getter for {@link #uriResolver}
   * 
   * @return The registered <code>URIResolver</code>
   */
  @Override
  public URIResolver getURIResolver ()
  {
    return uriResolver;
  }

  /**
   * Setter for {@link #uriResolver}
   * 
   * @param resolver
   *        The <code>URIResolver</code> object.
   */
  @Override
  public void setURIResolver (final URIResolver resolver)
  {

    synchronized (reentryGuard)
    {
      this.uriResolver = resolver;
    }
  }

  /**
   * see
   * {@link javax.xml.transform.TransformerFactory#setFeature(java.lang.String, boolean)}
   */
  @Override
  public void setFeature (final String name, final boolean value) throws TransformerConfigurationException
  {
    // TODO compare with xalan/saxon
    throw new IllegalArgumentException ("Not yet implemented");
  }

  /**
   * Supplied features.
   * 
   * @param name
   *        Name of the feature.
   * @return true if feature is supported.
   */
  @Override
  public boolean getFeature (final String name)
  {

    if (name.equals (SAXSource.FEATURE))
    {
      return true;
    }
    if (name.equals (SAXResult.FEATURE))
    {
      return true;
    }
    if (name.equals (DOMSource.FEATURE))
    {
      return true;
    }
    if (name.equals (DOMResult.FEATURE))
    {
      return true;
    }
    if (name.equals (StreamSource.FEATURE))
    {
      return true;
    }
    if (name.equals (StreamResult.FEATURE))
    {
      return true;
    }
    if (name.equals (SAXTransformerFactory.FEATURE))
    {
      return true;
    }
    if (name.equals (SAXTransformerFactory.FEATURE_XMLFILTER))
    {
      return true;
    }

    final String errMsg = "Unknown feature " + name;
    final TransformerConfigurationException tE = new TransformerConfigurationException (errMsg);

    try
    {
      defaultErrorListener.error (tE);
      return false;
    }
    catch (final TransformerException e)
    {
      throw new IllegalArgumentException (errMsg);
    }
  }

  /**
   * Creates a new Templates for Transformations.
   * 
   * @param source
   *        The <code>Source</code> of the stylesheet.
   * @return A <code>Templates</code> object or <code>null</code> when an error
   *         occured (no user defined ErrorListener)
   * @throws TransformerConfigurationException
   */
  @Override
  public Templates newTemplates (final Source source) throws TransformerConfigurationException
  {

    synchronized (reentryGuard)
    {
      if (DEBUG)
      {
        if (log.isDebugEnabled ())
          log.debug ("get a Templates-instance from Source " + source.getSystemId ());
      }
      try
      {
        final SAXSource saxSource = TrAXHelper.getSAXSource (source, errorListener);
        final Templates template = new TemplatesImpl (saxSource.getXMLReader (), saxSource.getInputSource (), this);
        return template;
      }
      catch (final TransformerException tE)
      {
        defaultErrorListener.fatalError (tE);
        return null;
      }
    }
  }

  /**
   * Creates a new Transformer object that performs a copy of the source to the
   * result.
   * 
   * @return A <code>Transformer</code> object for an identical transformation.
   * @throws TransformerConfigurationException
   */
  @Override
  public Transformer newTransformer () throws TransformerConfigurationException
  {

    synchronized (reentryGuard)
    {
      final StreamSource streamSrc = new StreamSource (new StringReader (IDENTITY_TRANSFORM));
      return newTransformer (streamSrc);
    }
  }

  /**
   * Gets a new Transformer object for transformation.
   * 
   * @param source
   *        The <code>Source</code> of the stylesheet.
   * @return A <code>Transformer</code> object according to the
   *         <code>Templates</code> object.
   * @throws TransformerConfigurationException
   */
  @Override
  public Transformer newTransformer (final Source source) throws TransformerConfigurationException
  {

    synchronized (reentryGuard)
    {
      if (DEBUG)
        log.debug ("get a Transformer-instance");
      final Templates templates = newTemplates (source);
      final Transformer transformer = templates.newTransformer ();
      return (transformer);
    }
  }

  // *************************************************************************
  // IMPLEMENTATION OF SAXTransformerFactory
  // *************************************************************************

  /**
   * Gets a <code>TemplatesHandler</code> object that can process SAX
   * ContentHandler events into a <code>Templates</code> object. Implementation
   * of the {@link SAXTransformerFactory}
   * 
   * @see SAXTransformerFactory
   * @return {@link TemplatesHandler} ready to parse a stylesheet.
   * @throws TransformerConfigurationException
   */
  @Override
  public TemplatesHandler newTemplatesHandler () throws TransformerConfigurationException
  {

    synchronized (reentryGuard)
    {
      if (DEBUG)
        log.debug ("create a TemplatesHandler-instance");
      final TemplatesHandlerImpl thandler = new TemplatesHandlerImpl (this);
      return thandler;
    }
  }

  /**
   * Gets a <code>TransformerHandler</code> object that can process SAX
   * ContentHandler events into a Result. The transformation is defined as an
   * identity (or copy) transformation, for example to copy a series of SAX
   * parse events into a DOM tree. Implementation of the
   * {@link SAXTransformerFactory}
   * 
   * @return {@link TransformerHandler} ready to transform SAX events.
   * @throws TransformerConfigurationException
   */
  @Override
  public TransformerHandler newTransformerHandler () throws TransformerConfigurationException
  {

    synchronized (reentryGuard)
    {
      if (DEBUG)
        log.debug ("get a TransformerHandler " + "(identity transformation or copy)");
      final StreamSource streamSrc = new StreamSource (new StringReader (IDENTITY_TRANSFORM));
      return newTransformerHandler (streamSrc);
    }
  }

  /**
   * Gets a <code>TransformerHandler</code> object that can process SAX
   * ContentHandler events into a Result, based on the transformation
   * instructions specified by the argument. Implementation of the
   * {@link SAXTransformerFactory}
   * 
   * @param src
   *        The Source of the transformation instructions
   * @return {@link TransformerHandler} ready to transform SAX events.
   * @throws TransformerConfigurationException
   */
  @Override
  public TransformerHandler newTransformerHandler (final Source src) throws TransformerConfigurationException
  {

    synchronized (reentryGuard)
    {
      if (DEBUG)
        if (log.isDebugEnabled ())
          log.debug ("get a TransformerHandler-instance from Source " + src.getSystemId ());
      final Templates templates = newTemplates (src);
      return newTransformerHandler (templates);
    }
  }

  /**
   * Gets a <code>TransformerHandler</code> object that can process SAX
   * ContentHandler events into a Result, based on the Templates argument.
   * Implementation of the {@link SAXTransformerFactory}
   * 
   * @param templates
   *        - The compiled transformation instructions.
   * @return {@link TransformerHandler} ready to transform SAX events.
   * @throws TransformerConfigurationException
   */
  @Override
  public TransformerHandler newTransformerHandler (final Templates templates) throws TransformerConfigurationException
  {

    synchronized (reentryGuard)
    {
      if (DEBUG)
        log.debug ("get a TransformerHandler-instance from Templates");
      final Transformer internal = templates.newTransformer ();
      final TransformerHandlerImpl thandler = new TransformerHandlerImpl (internal);
      return thandler;
    }
  }

  /**
   * Creates an <code>XMLFilter</code> that uses the given <code>Source</code>
   * as the transformation instructions. Implementation of the
   * {@link SAXTransformerFactory}
   * 
   * @param src
   *        - The Source of the transformation instructions.
   * @return An {@link XMLFilter} object, or <code>null</code> if this feature
   *         is not supported.
   * @throws TransformerConfigurationException
   */
  @Override
  public XMLFilter newXMLFilter (final Source src) throws TransformerConfigurationException
  {

    if (DEBUG)
      if (log.isDebugEnabled ())
        log.debug ("getting SAXTransformerFactory.FEATURE_XMLFILTER " + "from Source " + src.getSystemId ());
    XMLFilter xFilter = null;
    try
    {
      final Templates templates = newTemplates (src);
      // get a XMLReader
      final XMLReader parser = Processor.createXMLReader ();
      xFilter = newXMLFilter (templates);
      xFilter.setParent (parser);
      return xFilter;
    }
    catch (final SAXException ex)
    {
      final TransformerConfigurationException tE = new TransformerConfigurationException (ex.getMessage (), ex);
      defaultErrorListener.fatalError (tE);
      return null;
    }
  }

  /**
   * Creates an XMLFilter, based on the Templates argument. Implementation of
   * the {@link SAXTransformerFactory}
   * 
   * @param templates
   *        - The compiled transformation instructions.
   * @return An {@link XMLFilter} object, or null if this feature is not
   *         supported.
   */
  @Override
  public XMLFilter newXMLFilter (final Templates templates)
  {

    if (DEBUG)
      log.debug ("getting SAXTransformerFactory.FEATURE_XMLFILTER " + "from Templates");

    // Implementation
    return new TrAXFilter (templates);
  }

  /** returns the value of {@link #parserListenerMgr} */
  public ParserListenerMgr getParserListenerMgr ()
  {
    return parserListenerMgr;
  }

  /** returns the value of {@link #msgEmitter} */
  public StxEmitter getMessageEmitter ()
  {
    return msgEmitter;
  }

  /**
   * Method creates a new Emitter for stx:message output
   * 
   * @param emitterClass
   *        the name of the emitter class
   * @return a <code>StxEmitter</code>
   * @throws TransformerConfigurationException
   *         in case of errors
   */
  public StxEmitter buildMessageEmitter (final String emitterClass) throws TransformerConfigurationException
  {

    Object emitter = null;
    try
    {
      emitter = loadClass (emitterClass).newInstance ();
      if (!(emitter instanceof StxEmitter))
      {
        throw new TransformerConfigurationException (emitterClass + " is not an StxEmitter");
      }
    }
    catch (final InstantiationException ie)
    {
      throw new TransformerConfigurationException (ie.getMessage (), ie);
    }
    catch (final IllegalAccessException ile)
    {
      throw new TransformerConfigurationException (ile.getMessage (), ile);
    }

    return (StxEmitter) emitter;
  }

  // classloader helper
  private Class loadClass (final String className) throws TransformerConfigurationException
  {
    try
    {
      final ClassLoader loader = Thread.currentThread ().getContextClassLoader ();
      if (loader != null)
      {
        try
        {
          return loader.loadClass (className);
        }
        catch (final Exception ex)
        {
          return Class.forName (className);
        }
      }
      else
      {
        return Class.forName (className);
      }
    }
    catch (final Exception e)
    {
      throw new TransformerConfigurationException ("Failed to load " + className, e);
    }
  }
}
