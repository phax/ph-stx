/*
 * $Id: TransformerHandlerResolver.java,v 1.8 2009/09/22 21:13:44 obecker Exp $
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
 * Contributor(s): fikin_________________________________.
 */

package net.sf.joost;

import java.util.Hashtable;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.joost.stx.Value;

/**
 * Basic interface for resolving external {@link TransformerHandler} objects.
 * <p>
 * An object that implements this interface can be called by the STX processor
 * if it encounters a request to hand over the processing to an external
 * {@link TransformerHandler} object.
 * <p>
 * A <code><strong>TransformerHandlerResolver</strong></code> can be registered
 * by using the Joost specific
 * {@link net.sf.joost.stx.Processor#setTransformerHandlerResolver} method, or
 * (using JAXP) by calling
 * {@link javax.xml.transform.TransformerFactory#setAttribute} with the string
 * {@link net.sf.joost.trax.CTrAX#KEY_TH_RESOLVER} as its first argument.
 * <p>
 * Also <code><strong>TransformerHandlerResolver</strong></code> can be
 * registered using <strong>Java1.3 services plugin mechanism</strong>. This is
 * achieved by specifying the particular handler implementation in
 * META-INF/services/net.sf.joost.TransformerHandlerResolver file. Joost is
 * using Jakarta's Discovery library to locate all available plugins which
 * provides some additional configuration options one can employ as well.
 * <p>
 * The {@link javax.xml.transform.sax.TransformerHandler} object returned by
 * each of the <code>resolve</code> methods is required to accept a
 * {@link javax.xml.transform.sax.SAXResult} as parameter in the
 * {@link TransformerHandler#setResult} method. The other methods
 * {@link TransformerHandler#setSystemId},
 * {@link TransformerHandler#getSystemId}, and
 * {@link TransformerHandler#getTransformer} won't be called by Joost.
 * Especially potential parameters for the transformation will be provided
 * already as the third argument in each of the <code>resolve</code> methods, so
 * there's no need to implement a {@link javax.xml.transform.Transformer} dummy
 * solely as means to the end of enabling
 * {@link javax.xml.transform.Transformer#setParameter}.
 *
 * @version $Revision: 1.8 $ $Date: 2009/09/22 21:13:44 $
 * @author Oliver Becker
 */

public interface ITransformerHandlerResolver
{
  /**
   * Resolves a {@link TransformerHandler} object for an external
   * transformation. This method will be called if the <code>filter-src</code>
   * attribute contains an URL, or if this attribute is missing at all.
   *
   * @param method
   *        an URI string provided in the <code>filter-method</code> attribute,
   *        identifying the type of the requested filter
   * @param href
   *        the location of the source for the filter provided in the
   *        <code>filter-src</code> attribute (as pseudo-argument of the
   *        <code>url(...)</code> notation); <code>null</code> if the
   *        <code>filter-src</code> attribute is missing
   * @param base
   *        the base URI of the transformation sheet
   * @param uriResolver
   *        the optional URIResolver configured for Joost
   * @param errorListener
   *        the optional ErrorListener configured for Joost
   * @param params
   *        the set of parameters specified using <code>stx:with-param</code>
   *        elements, all values are {@link String}s
   * @return a {@link TransformerHandler} object that transforms a SAX stream,
   *         or <code>null</code> if the STX processor should try to resolve the
   *         handler itself
   * @exception SAXException
   *            if an error occurs during the creation or initialization
   */
  TransformerHandler resolve (String method,
                              String href,
                              String base,
                              URIResolver uriResolver,
                              ErrorListener errorListener,
                              Hashtable <String, Value> params) throws SAXException;

  /**
   * Resolves a {@link TransformerHandler} object for an external
   * transformation. This method will be called if the <code>filter-src</code>
   * attribute contains a buffer specification.
   *
   * @param method
   *        an URI string provided in the <code>filter-method</code> attribute,
   *        identifying the type of the requested filter
   * @param reader
   *        an {@link XMLReader} object that provides the source for the
   *        transformation as a stream of SAX events (the contents of an
   *        <code>stx:buffer</code>). Either <code>parse</code> method may be
   *        used, the required parameters <code>systemId</code> or
   *        <code>input</code> respectively will be ignored by this reader.
   * @param uriResolver
   *        the optional URIResolver configured for Joost
   * @param errorListener
   *        the optional ErrorListener configured for Joost
   * @param params
   *        the set of parameters specified using <code>stx:with-param</code>
   *        elements, all values are {@link String}s
   * @return a {@link TransformerHandler} object that transforms a SAX stream,
   *         or <code>null</code> if the STX processor should try to resolve the
   *         handler itself
   * @exception SAXException
   *            if an error occurs during the creation or initialization
   */
  TransformerHandler resolve (String method,
                              XMLReader reader,
                              URIResolver uriResolver,
                              ErrorListener errorListener,
                              Hashtable <String, Value> params) throws SAXException;

  /**
   * Determines whether a requested filter is available or not, used by the STX
   * function <code>filter-available</code>.
   *
   * @param method
   *        an URI string identifying the type of the requested filter
   * @return <code>true</code> if this resolver will return a
   *         {@link TransformerHandler} object for this filter
   */
  boolean available (String method);

  /**
   * Return all supported filter-method URIs Each one must return
   * <code>true</code> when checked against {@link #available(String)}.
   *
   * @return array of supported URIs
   */
  String [] resolves ();
}
