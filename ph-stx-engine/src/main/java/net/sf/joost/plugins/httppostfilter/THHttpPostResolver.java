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
package net.sf.joost.plugins.httppostfilter;

import java.util.Hashtable;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.TransformerHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.joost.CSTX;
import net.sf.joost.ITransformerHandlerResolver;

/**
 * Implementation of HTTP-Post filter. Filter URI:
 * http://www.ietf.org/rfc/rfc2616.txt#POST Example: ...
 * <stx:process-self filter-method="http://www.ietf.org/rfc/rfc2616.txt#POST" >
 * <stx:with-param name="target" select="http://myWebServerIP" />
 * </stx:process-self> ...
 *
 * @version $Revision: 1.5 $ $Date: 2009/09/22 21:13:43 $
 * @author Oliver Becker
 */
public final class THHttpPostResolver implements ITransformerHandlerResolver
{
  /** The URI identifying an STX transformation */
  public static final String HTTP_POST_METHOD = "http://www.ietf.org/rfc/rfc2616.txt#POST";

  /** logging object */
  private static Logger log = LoggerFactory.getLogger (THHttpPostResolver.class);

  /**
   * It return supported URIs, in this case @HTTP_POST_METHOD
   */
  public String [] resolves ()
  {
    final String [] uris = { HTTP_POST_METHOD };
    return uris;
  }

  /**
   * If given method is {@link #HTTP_POST_METHOD} return cached (or new) Trax
   * compatible instance. otherwise return <code>null</code>.
   */
  public TransformerHandler resolve (final String method,
                                     final String href,
                                     final String base,
                                     final URIResolver uriResolver,
                                     final ErrorListener errorListener,
                                     final Hashtable <String, Object> params) throws SAXException
  {
    return _resolve (method, href, null, params);
  }

  /*
   * If given method is {@link #HTTP_POST_METHOD} return cached (or new) Trax
   * compatible instance. otherwise return <code>null</code>.
   */
  public TransformerHandler resolve (final String method,
                                     final XMLReader reader,
                                     final URIResolver uriResolver,
                                     final ErrorListener errorListener,
                                     final Hashtable <String, Object> params) throws SAXException
  {
    return _resolve (method, null, reader, params);
  }

  /*
   * actual business logic related to POST. Used by both resolve methods.
   */
  private TransformerHandler _resolve (final String method,
                                       final String href,
                                       final XMLReader reader,
                                       final Hashtable <String, Object> params) throws SAXException
  {
    if (CSTX.DEBUG)
      log.debug ("hppt-post-filter : resolve '" + method + "'");

    if (!available (method))
      throw new SAXException ("Not supported filter-method!");

    if (reader != null || href != null)
      throw new SAXException ("Attribute 'filter-src' not allowed for method '" + method + "'");

    final String v = String.valueOf (params.get ("target"));
    if (v == null)
      throw new SAXException ("Missing parameter 'target' for filter " + "method '" + method + "'");

    return new HttpPostHandler (v);
  }

  /**
   * Return true if given method is equal to @HTTP_POST_METHOD, otherwise false
   */
  public boolean available (final String method)
  {
    return HTTP_POST_METHOD.equals (method);
  }
}
