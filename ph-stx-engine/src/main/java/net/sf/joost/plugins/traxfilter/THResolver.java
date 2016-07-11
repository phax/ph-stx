/*
 * $Id: THResolver.java,v 1.11 2009/09/22 21:13:44 obecker Exp $
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
 * Contributor(s): Nikolay Fiykov
 */
package net.sf.joost.plugins.traxfilter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.joost.CSTX;
import net.sf.joost.OptionalLog;
import net.sf.joost.TransformerHandlerResolver;
import net.sf.joost.plugins.attributes.Attribute;
import net.sf.joost.plugins.attributes.BooleanAttribute;
import net.sf.joost.plugins.attributes.StringAttribute;
import net.sf.joost.stx.Value;
import net.sf.joost.trax.CTrAX;
import net.sf.joost.trax.TransformerFactoryImpl;

/**
 * Implementation of TrAX XSLT and STX filters. Filter URIs:
 * http://www.w3.org/1999/XSL/Transform http://stx.sourceforge.net/2002/ns It
 * works by instantiating a TrAX SAX TransformerHandler and delegating the
 * execution to it. Particual TrAX transformer can be specified by system
 * property javax.xml.transform.TransformerFactory. Examples: ...
 * <stx:process-self filter-method="http://www.w3.org/1999/XSL/Transform"
 * filter-src="url('your-file.xsl')" /> ...
 * <stx:process-self filter-method="http://stx.sourceforge.net/2002/ns"
 * filter-src="url('your-file.stx')" /> ...
 * <p>
 * This filter supports following properties. Each one of them can be specified
 * as system property (
 * -Dhttp://stx.sourceforge.net/2002/ns/trax-filter:REUSE-TH-URL=true ) or
 * passed as parameter in your main STX template ( &lt;stx:with-param
 * name="http://stx.sourceforge.net/2002/ns/trax-filter:REUSE-TH-URL"
 * select="'true'" /&gr; )
 * <li>http://stx.sourceforge.net/2002/ns/trax-filter:REUSE-TH-URL If set to
 * true it will cache instantiated transformer objects and will reuse them each
 * time same HREF is asked to be resolved. This implied mainly for
 * filter-src=url(...). Possible values are true or false, false by
 * default.</li>
 * <li>http://stx.sourceforge.net/2002/ns/trax-filter:REUSE-TH-BUFFER
 * Essentially same as REUSE-TH-URL meaning but works for filter-src=buffer(...)
 * It caches all buffers into one object, so use it only if you're going to have
 * only one buffer() in your transformation. Possible values are true or false,
 * false by default.</li>
 * <li>http://stx.sourceforge.net/2002/ns/trax-filter:FACTORY Specifies what
 * TrAX factory is to be used. This is necessary when you want to specify
 * factory different than build-in ones such as Xalan's XTLTC for instance.
 * Possible values are fully classified java class name, by default not
 * specified.</li>
 * <li>http://stx.sourceforge.net/2002/ns/trax-filter:THREAT-URL-AS-SYSTEM_ID
 * Indicate that what is passed in filter-src=url(...) is in fact TrAX SYSTEM_ID
 * instead of an actual URL. This is required when having using custom TrAX
 * factories like Xalan's XSLTC which expects complied XSLT class name unstead
 * of valid file URL.</li>
 * <p>
 * The namespace http://stx.sourceforge.net/2002/ns/trax-filter/attribute
 * designates attributes passed to underlying TrAX factory. This is useful when
 * one desires to instrument in the factory a particular way. For example
 * setting Xalan's incremental parsing feature is done:
 * -Dhttp://stx.sourceforge.net/2002/ns/trax-filter/attribute:http://apache.org/xalan/features/incremental=true
 * or &lt;stx:with-param
 * name="http://stx.sourceforge.net/2002/ns/trax-filter/attribute:http://apache.org/xalan/features/incremental"
 * select="'true'" /&gt;
 *
 * @version $Revision: 1.11 $ $Date: 2009/09/22 21:13:44 $
 * @author fikin
 */
public class THResolver implements TransformerHandlerResolver
{

  /** supported methods */
  public static final String STX_METHOD = CSTX.STX_NS;
  public static final String XSLT_METHOD = "http://www.w3.org/1999/XSL/Transform";
  public static final String TRAX_METHOD = "http://java.sun.com/xml/jaxp";

  /* supported parameter prefixes */
  /** namespace for filter's own attributes */
  public static final String FILTER_ATTR_NS = CSTX.STX_NS + "/trax-filter";
  /** namespace for attributes provided to underlying TrAX object */
  public static final String TRAX_ATTR_NS = CSTX.STX_NS + "/trax-filter/attribute";

  /** internal representation of parameters namespaces */
  private static final String tmp_FILTER_ATTR_NS = "{" + FILTER_ATTR_NS + "}";
  private static final String tmp_TRAX_ATTR_NS = "{" + TRAX_ATTR_NS + "}";

  /** supported filter attributes */
  private static Hashtable <String, Attribute> attrs = new Hashtable<> ();

  /** indicate if to cache TrAX TH and reuse them across calls */
  public static final BooleanAttribute REUSE_TH_URL = new BooleanAttribute ("REUSE-TH-URL",
                                                                            System.getProperty (FILTER_ATTR_NS +
                                                                                                ":REUSE-TH-URL",
                                                                                                "false"),
                                                                            attrs);

  /**
   * force caching XMLReader TH, joost sends new XMLReader each time it calls
   * resolve() even if it is one and same buffer this flag is meant to "force"
   * buffer-based th caching
   */
  public static final BooleanAttribute REUSE_TH_BUFFER = new BooleanAttribute ("REUSE-TH-BUFFER",
                                                                               System.getProperty (FILTER_ATTR_NS +
                                                                                                   ":REUSE-TH-BUFFER",
                                                                                                   "false"),
                                                                               attrs);

  /**
   * if specified this class will be used as TransformerFactory for creating TH
   */
  public static final StringAttribute FACTORY = new StringAttribute ("FACTORY",
                                                                     System.getProperty (FILTER_ATTR_NS +
                                                                                         ":FACTORY",
                                                                                         ""),
                                                                     attrs);

  /**
   * if set to true specifies that url(...) is in fact systemId(...) rather than
   * url() to a file/resource
   */
  public static final BooleanAttribute HREF_IS_SYSTEM_ID = new BooleanAttribute ("THREAT-URL-AS-SYSTEM_ID",
                                                                                 System.getProperty (FILTER_ATTR_NS +
                                                                                                     ":THREAT-URL-AS-SYSTEM_ID",
                                                                                                     "false"),
                                                                                 attrs);

  /** all XMLReader-based TH are reused under this hashtable key */
  private static final String XMLREADER_KEY = "_XMLREADER";

  /** cached TrAX TH */
  private static Hashtable <String, TransformerHandler> cachedTH = new Hashtable<> (5);

  /** supported URI methods */
  private static final String [] METHODS = { STX_METHOD, XSLT_METHOD, TRAX_METHOD };

  private static Object SYNCHRONIZE_GUARD = new Object ();

  /** logging object */
  private static Logger log = OptionalLog.getLog (THResolver.class);

  /*
   * (non-Javadoc)
   * @see net.sf.joost.plugins.HandlerPlugin#resolves()
   */
  public String [] resolves ()
  {
    if (CSTX.DEBUG)
      log.debug ("resolves()");

    return METHODS;
  }

  /**
   * @see net.sf.joost.TransformerHandlerResolver#resolve(java.lang.String,
   *      java.lang.String, java.lang.String, javax.xml.transform.URIResolver,
   *      javax.xml.transform.ErrorListener, java.util.Hashtable)
   */
  public TransformerHandler resolve (final String method,
                                     final String href,
                                     final String base,
                                     final URIResolver uriResolver,
                                     final ErrorListener errorListener,
                                     final Hashtable <String, Value> params) throws SAXException
  {
    if (!available (method))
      throw new SAXException ("Not supported filter-method:" + method);

    if (CSTX.DEBUG)
      log.debug ("resolve(url): href=" + href + ", base=" + base);

    if (href == null)
      throw new SAXException ("method-src must be url() or buffer()");

    setFilterAttributes (params);

    TransformerHandler th = null;

    // reuse th if available
    th = getReusableHrefTH (method, href);

    // new transformer if non available
    if (th == null)
    {
      // prepare the source
      Source source = null;
      try
      {
        // use custom URIResolver if present
        if (uriResolver != null)
        {
          source = uriResolver.resolve (href, base);
        }
        if (source == null)
        {
          if (HREF_IS_SYSTEM_ID.booleanValue ())
          {
            // systemId
            if (CSTX.DEBUG)
              log.debug ("resolve(url): new source out of systemId='" + href + "'");
            source = new StreamSource (href);
          }
          else
          {
            // file
            final String url = new URL (new URL (base), href).toExternalForm ();
            if (CSTX.DEBUG)
              log.debug ("resolve(url): new source out of file='" + url + "'");
            source = new StreamSource (url);
          }
        }
      }
      catch (final MalformedURLException muex)
      {
        throw new SAXException (muex);
      }
      catch (final TransformerException tex)
      {
        throw new SAXException (tex);
      }

      th = newTHOutOfTrAX (method, source, params, errorListener, uriResolver);

      // cache the instance if required
      cacheHrefTH (method, href, th);
    }

    prepareTh (th, params);
    return th;
  }

  /**
   * @see net.sf.joost.TransformerHandlerResolver#resolve(java.lang.String,
   *      org.xml.sax.XMLReader, javax.xml.transform.URIResolver,
   *      javax.xml.transform.ErrorListener, java.util.Hashtable)
   */
  public TransformerHandler resolve (final String method,
                                     final XMLReader reader,
                                     final URIResolver uriResolver,
                                     final ErrorListener errorListener,
                                     final Hashtable <String, Value> params) throws SAXException
  {
    if (!available (method))
      throw new SAXException ("Not supported filter-method:" + method);

    if (CSTX.DEBUG)
      log.debug ("resolve(buffer)");

    if (reader == null)
      throw new SAXException ("method-src must be url() or buffer()");

    setFilterAttributes (params);

    TransformerHandler th = null;

    // reuse th if available
    th = getReusableXmlReaderTH (method);

    // new transformer if non available
    if (th == null)
    {
      // prepare the source
      if (CSTX.DEBUG)
        log.debug ("resolve(buffer): new source out of buffer");
      final Source source = new SAXSource (reader, new InputSource ());

      th = newTHOutOfTrAX (method, source, params, errorListener, uriResolver);

      // cache the instance if required
      cacheBufferTH (method, th);
    }

    prepareTh (th, params);
    return th;
  }

  /**
   * @see net.sf.joost.TransformerHandlerResolver#available(java.lang.String)
   */
  public boolean available (final String method)
  {
    if (CSTX.DEBUG)
      log.debug ("available(): method=" + method);

    return (STX_METHOD.equals (method) || XSLT_METHOD.equals (method) || TRAX_METHOD.equals (method));
  }

  /**
   * Lookup in the hashtable for cached TH instance based on href. Takes into
   * account if caching flag is on.
   *
   * @param method
   * @param href
   * @return TH or null
   */
  protected TransformerHandler getReusableHrefTH (final String method, final String href)
  {
    if (CSTX.DEBUG)
      log.debug ("getReusableHrefTH(): href=" + href);

    if (REUSE_TH_URL.booleanValue ())
      return cachedTH.get (method + href);
    return null;
  }

  /**
   * cache this TH instance if flags says so
   *
   * @param th
   */
  protected void cacheHrefTH (final String method, final String href, final TransformerHandler th)
  {
    if (CSTX.DEBUG)
      log.debug ("cacheHrefTH()");

    if (REUSE_TH_URL.booleanValue ())
      cachedTH.put (method + href, th);
  }

  /**
   * Lookup in the hashtable for cached TH instance based on XmlReader. Takes
   * into account if caching flag is on.
   *
   * @param method
   * @return TH or null
   */
  protected TransformerHandler getReusableXmlReaderTH (final String method)
  {
    if (CSTX.DEBUG)
      log.debug ("getReusableXmlReaderTH()");

    if (REUSE_TH_BUFFER.booleanValue ())
      return cachedTH.get (method + XMLREADER_KEY);
    return null;
  }

  /**
   * cache this TH instance if flags says so
   *
   * @param th
   */
  protected void cacheBufferTH (final String method, final TransformerHandler th)
  {
    if (CSTX.DEBUG)
      log.debug ("cacheBufferTH()");

    if (REUSE_TH_BUFFER.booleanValue ())
      cachedTH.put (method + XMLREADER_KEY, th);
  }

  /**
   * Creates new TH instance out of TrAX factory
   *
   * @param method
   * @param source
   * @return TH
   */
  protected TransformerHandler newTHOutOfTrAX (final String method,
                                               final Source source,
                                               final Hashtable <String, Value> params,
                                               final ErrorListener errorListener,
                                               final URIResolver uriResolver) throws SAXException
  {
    if (CSTX.DEBUG)
      log.debug ("newTHOutOfTrAX()");

    SAXTransformerFactory saxtf;

    if (FACTORY.getValueStr ().length () > 0)
    {
      // create factory as asked by the client
      try
      {
        saxtf = (SAXTransformerFactory) (Class.forName (FACTORY.getValueStr ())).newInstance ();
        if (CSTX.DEBUG)
          log.debug ("newTHOutOfTrAX(): use custom TrAX factory " + FACTORY.getValueStr ());
      }
      catch (final InstantiationException e)
      {
        throw new SAXException (e);
      }
      catch (final ClassNotFoundException e)
      {
        throw new SAXException (e);
      }
      catch (final IllegalAccessException e)
      {
        throw new SAXException (e);
      }

    }
    else
      if (STX_METHOD.equals (method))
      {
        saxtf = new TransformerFactoryImpl ();
        if (CSTX.DEBUG)
          log.debug ("newTHOutOfTrAX(): use default Joost factory " + saxtf.getClass ().toString ());
      }
      else
      {
        final String TFPROP = "javax.xml.transform.TransformerFactory";
        final String STXIMP = "net.sf.joost.trax.TransformerFactoryImpl";

        synchronized (SYNCHRONIZE_GUARD)
        {

          final String propVal = System.getProperty (TFPROP);
          boolean propChanged = false;

          final String xsltFac = System.getProperty (CTrAX.KEY_XSLT_FACTORY);
          if (xsltFac != null || STXIMP.equals (propVal))
          {
            // change this property,
            // otherwise we wouldn't get an XSLT transformer
            if (xsltFac != null)
              System.setProperty (TFPROP, xsltFac);
            else
            {
              final Properties props = System.getProperties ();
              props.remove (TFPROP);
              System.setProperties (props);
            }
            propChanged = true;
          }

          saxtf = (SAXTransformerFactory) TransformerFactory.newInstance ();

          if (propChanged)
          {
            // reset property
            if (propVal != null)
              System.setProperty (TFPROP, propVal);
            else
            {
              final Properties props = System.getProperties ();
              props.remove (TFPROP);
              System.setProperties (props);
            }
          }
        }

        if (CSTX.DEBUG)
          log.debug ("newTHOutOfTrAX(): use default TrAX factory " + saxtf.getClass ().toString ());
      }

    // set factory attributes
    setTraxFactoryAttributes (saxtf, params);
    setupTransformerFactory (saxtf, errorListener, uriResolver);

    try
    {
      if (CSTX.DEBUG)
        log.debug ("newTHOutOfTrAX(): creating factory's reusable TH");
      // TrAX way to create TH
      final TransformerHandler th = saxtf.newTransformerHandler (source);
      setupTransformer (th.getTransformer (), errorListener, uriResolver);
      return th;
    }
    catch (final TransformerConfigurationException ex)
    {
      throw new SAXException (ex);
    }

  }

  private void setupTransformerFactory (final TransformerFactory factory,
                                        final ErrorListener errorListener,
                                        final URIResolver uriResolver)
  {
    if (errorListener != null)
      factory.setErrorListener (errorListener);
    if (uriResolver != null)
      factory.setURIResolver (uriResolver);
  }

  private void setupTransformer (final Transformer transformer,
                                 final ErrorListener errorListener,
                                 final URIResolver uriResolver)
  {
    if (errorListener != null)
      transformer.setErrorListener (errorListener);
    if (uriResolver != null)
      transformer.setURIResolver (uriResolver);
  }

  /**
   * Set to the SAX TrAX Factory attributes by inspecting the given parameters
   * for those which are from TrAX namespace
   */
  protected void setTraxFactoryAttributes (final SAXTransformerFactory saxtf, final Hashtable <String, Value> params)
  {
    // loop over all parameters
    final Enumeration <String> e = params.keys ();
    while (e.hasMoreElements ())
    {
      final String key = e.nextElement ();

      // is this one from TrAX namespace?
      if (key.startsWith (tmp_TRAX_ATTR_NS))
      {

        // it is, remove the namespace prefix and set it to the factory
        final String name = key.substring (tmp_TRAX_ATTR_NS.length ()).toLowerCase ();
        saxtf.setAttribute (name, params.get (key));
        if (CSTX.DEBUG)
          log.debug ("newTHOutOfTrAX(): set factory attribute " + name + "=" + params.get (key));
      }
    }

  }

  /**
   * Prepare TH instance for work This involves setting TrAX parameters and all
   * other stuff if needed
   *
   * @param th
   * @param params
   */
  protected void prepareTh (final TransformerHandler th, final Hashtable <String, Value> params)
  {
    if (CSTX.DEBUG)
      log.debug ("prepareTh()");

    final Transformer tr = th.getTransformer ();

    // make sure old parameters are cleaned
    tr.clearParameters ();

    // set transformation parameters
    if (!params.isEmpty ())
    {
      for (final Enumeration <String> e = params.keys (); e.hasMoreElements ();)
      {
        final String key = e.nextElement ();
        if (CSTX.DEBUG)
          log.debug ("prepareTh(): set parameter " + key + "=" + params.get (key));

        if (!key.startsWith (tmp_TRAX_ATTR_NS) && !key.startsWith (tmp_FILTER_ATTR_NS))
        {
          // ordinary parameter, set it to the TrAX object
          tr.setParameter (key, params.get (key));
        }
      }
    }
  }

  /**
   * Find in the given list of parameters filter's own one and set their state
   *
   * @param params
   */
  protected void setFilterAttributes (final Hashtable <String, Value> params)
  {
    if (CSTX.DEBUG)
      log.debug ("setFilterAttributes()");

    // loop over all coming parameters
    final Enumeration <String> e = params.keys ();
    while (e.hasMoreElements ())
    {
      final String key = e.nextElement ();

      // is this a parameter from filter's namespace?
      if (key.startsWith (tmp_FILTER_ATTR_NS))
      {

        // it is, extract the name of the attribute and set its value
        final String name = key.substring (tmp_FILTER_ATTR_NS.length ()).toLowerCase ();
        final Attribute a = (attrs.get (name));
        if (a == null)
          throw new IllegalArgumentException ("setFilterAttributes() : " + name + " not supported");

        a.setValue (String.valueOf (params.get (key)));
        if (CSTX.DEBUG)
          log.debug ("setFilterAttributes(): set attribute " + name + "=" + params.get (key));
      }
    }
  }
}
