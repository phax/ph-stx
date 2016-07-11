/*
 * $Id: NSFilter.java,v 1.2 2009/09/22 21:13:44 obecker Exp $
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
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.samples;

import java.util.Hashtable;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import net.sf.joost.TransformerHandlerResolver;
import net.sf.joost.trax.CTrAX;

/**
 * Example class that demonstrates the usage of external filters in Joost.
 * <p>
 * For simplicity this example comprises three tasks within one class:
 * <ul>
 * <li>starting the application in the main method, registering an object as a
 * resolver for TransformerHandler objects</li>
 * <li>acting as a TransformerHandlerResolver, that returns itself</li>
 * <li>acting as a TransformerHandler, that removes all elements in a given
 * namespace (passed as a parameter)</li>
 * </ul>
 *
 * @version $Revision: 1.2 $ $Date: 2009/09/22 21:13:44 $
 * @author Oliver Becker
 */

public class NSFilter extends XMLFilterImpl implements TransformerHandler, TransformerHandlerResolver
{
  public static void main (final String [] args)
  {
    // example transformation
    final String testSTX = "NSFilter.stx";

    // use Joost as transformation engine
    System.setProperty ("javax.xml.transform.TransformerFactory", "net.sf.joost.trax.TransformerFactoryImpl");

    // The object that is the filter (TransformerHandler) as well as
    // a resolver for that filter (TransformerHandlerResolver)
    final NSFilter filter = new NSFilter ();

    try
    {
      final TransformerFactory factory = TransformerFactory.newInstance ();

      // register the resolver
      factory.setAttribute (CTrAX.KEY_TH_RESOLVER, filter);

      final Transformer transformer = factory.newTransformer (new StreamSource (NSFilter.class.getResourceAsStream (testSTX)));

      transformer.transform (new StreamSource (NSFilter.class.getResourceAsStream (testSTX)),
                             new StreamResult (System.out));
    }
    catch (final TransformerException e)
    {
      final SourceLocator sloc = e.getLocator ();
      if (sloc != null)
        System.err.println (sloc.getSystemId () +
                            ":" +
                            sloc.getLineNumber () +
                            ":" +
                            sloc.getColumnNumber () +
                            ": " +
                            e.getMessage ());
      else
        System.err.println (e.getMessage ());
    }
  }

  // ---------------------------------------------------------------------

  //
  // from interface TransformerHandlerResolver
  //

  /**
   * The filter-method attribute value to be used in the STX transformation
   * sheet
   */
  private static final String METHOD = "http://joost.sf.net/samples/NSFilter";

  public TransformerHandler resolve (final String method,
                                     final String href,
                                     final String base,
                                     final URIResolver uriResolver,
                                     final ErrorListener errorListener,
                                     final Hashtable params) throws SAXException
  {
    if (METHOD.equals (method))
    {
      if (href != null)
        throw new SAXException ("Specification of an external source '" + href + "' not allowed for " + method);
      skipUri = String.valueOf (params.get ("uri"));
      return this;
    }
    return null;
  }

  public TransformerHandler resolve (final String method,
                                     final XMLReader reader,
                                     final URIResolver uriResolver,
                                     final ErrorListener errorListener,
                                     final Hashtable params) throws SAXException
  {
    if (METHOD.equals (method))
      throw new SAXException ("Provision of internal code not allowed for " + method);
    return null;
  }

  public boolean available (final String method)
  {
    return METHOD.equals (method);
  }

  public String [] resolves ()
  {
    return new String [] { METHOD };
  }

  // ---------------------------------------------------------------------

  /** This filter removes all elements in this namespace, set in resolve */
  private String skipUri;

  private int skipDepth = 0;

  // ---------------------------------------------------------------------

  //
  // from interface ContentHandler
  //

  @Override
  public void startElement (final String uri,
                            final String lName,
                            final String qName,
                            final Attributes attrs) throws SAXException
  {
    if (skipDepth > 0 || uri.equals (skipUri))
    {
      skipDepth++;
    }
    else
      super.startElement (uri, lName, qName, attrs);
  }

  @Override
  public void endElement (final String uri, final String lName, final String qName) throws SAXException
  {
    if (skipDepth > 0)
    {
      skipDepth--;
    }
    else
      super.endElement (uri, lName, qName);
  }

  @Override
  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    if (skipDepth == 0)
      super.characters (ch, start, length);
  }

  // ---------------------------------------------------------------------

  //
  // from interface LexicalHandler (not implemented by XMLFilterImpl)
  //

  private LexicalHandler lexH;

  public void startDTD (final String name, final String pubId, final String sysId)
  {} // not used

  public void endDTD ()
  {} // not used

  public void startEntity (final String name)
  {} // not used

  public void endEntity (final String name)
  {} // not used

  public void startCDATA () throws SAXException
  {
    if (skipDepth == 0 && lexH != null)
      lexH.startCDATA ();
  }

  public void endCDATA () throws SAXException
  {
    if (skipDepth == 0 && lexH != null)
      lexH.endCDATA ();
  }

  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    if (skipDepth == 0 && lexH != null)
      lexH.comment (ch, start, length);
  }

  // ---------------------------------------------------------------------

  //
  // from interface TransformerHandler
  //

  public void setResult (final Result result)
  {
    if (result instanceof SAXResult)
    {
      final SAXResult sresult = (SAXResult) result;
      // to be used by XMLFilterImpl
      setContentHandler (sresult.getHandler ());
      lexH = sresult.getLexicalHandler ();
    }
    else
    {
      // this will not happen in Joost
    }
  }

  // Never invoked by Joost
  public void setSystemId (final String id)
  {}

  public String getSystemId ()
  {
    return null;
  }

  public Transformer getTransformer ()
  {
    return null;
  }
}
