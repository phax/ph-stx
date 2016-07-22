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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.plugins.saxfilter;

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
 * Implementation of SAX as Trax filter. Filter URI: http://xml.org/sax Example:
 * ... <stx:process-self filter-method="http://xml.org/sax" filter-src=
 * "url('your-file.stx')" /> ...
 *
 * @version $Revision: 1.4 $ $Date: 2009/09/22 21:13:44 $
 * @author Oliver Becker, Nikolay Fiykov
 */

public final class THSAXResolver implements ITransformerHandlerResolver
{
  /** The URI identifying an STX transformation */
  public static final String SAX_METHOD = "http://xml.org/sax";

  /** logging object */
  private static final Logger log = LoggerFactory.getLogger (THSAXResolver.class);

  /**
   * It return supported URIs, in this case @SAX_METHOD
   */
  public String [] resolves ()
  {
    final String [] uris = { SAX_METHOD };
    return uris;
  }

  /**
   * If given method is {@link #SAX_METHOD} return cached (or new) Trax
   * compatible instance. otherwise return <code>null</code>.
   */
  public TransformerHandler resolve (final String method,
                                     final String href,
                                     final String base,
                                     final URIResolver uriResolver,
                                     final ErrorListener errorListener,
                                     final Hashtable <String, Object> params) throws SAXException
  {
    return _resolve (method, href, null);
  }

  /**
   * If given method is {@link #SAX_METHOD} return cached (or new) Trax
   * compatible instance. otherwise return <code>null</code>.
   */
  public TransformerHandler resolve (final String method,
                                     final XMLReader reader,
                                     final URIResolver uriResolver,
                                     final ErrorListener errorListener,
                                     final Hashtable <String, Object> params) throws SAXException
  {
    return _resolve (method, null, reader);
  }

  /**
   * If given method is @SAX_METHOD return cached (or new) Trax compatible
   * instance. otherwise return null.
   */
  private TransformerHandler _resolve (final String method,
                                       final String href,
                                       final XMLReader reader) throws SAXException
  {
    if (CSTX.DEBUG)
      log.debug ("sax-filter : resolve '" + method + "'");

    if (!available (method))
      throw new SAXException ("Not supported filter-method!");

    if ((reader != null) || (href != null))
      throw new SAXException ("Attribute 'filter-src' not allowed for method '" + method + "'");

    return new SAXWrapperHandler ();
  }

  /**
   * Return true if given method is equal to @SAX_METHOD, otherwise false
   */
  public boolean available (final String method)
  {
    return SAX_METHOD.equals (method);
  }
}
