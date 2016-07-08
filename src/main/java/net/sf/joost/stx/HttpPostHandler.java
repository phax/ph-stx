/*
 * $Id: HttpPostHandler.java,v 2.3 2008/06/15 08:11:22 obecker Exp $
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

package net.sf.joost.stx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.joost.emitter.XmlEmitter;

/**
 * Implements an HTTP POST connection with a {@link TransformerHandler}
 * interface.
 * 
 * @version $Revision: 2.3 $ $Date: 2008/06/15 08:11:22 $
 * @author Oliver Becker
 */

public class HttpPostHandler extends XmlEmitter implements TransformerHandler
{
  /** event sink for this transformer */
  private SAXResult saxResult;

  /** the character buffer */
  private final StringBuffer buffer;

  /** the target URL for the POST request */
  private final String targetURL;

  // Constructor
  public HttpPostHandler (final String targetURL)
  {
    super (null, DEFAULT_ENCODING, null); // postpone writer initialization
    writer = new StringWriter (); // catch up here
    buffer = ((StringWriter) writer).getBuffer ();
    this.targetURL = targetURL;
  }

  // ---------------------------------------------------------------------

  //
  // from interface DTDHandler (inherited in TransformerHandler)
  // (empty methods)
  //

  public void notationDecl (final String name, final String publicId, final String systemId)
  {}

  public void unparsedEntityDecl (final String name,
                                  final String publicId,
                                  final String systemId,
                                  final String notationName)
  {}

  // ---------------------------------------------------------------------

  // ---------------------------------------------------------------------

  //
  // from interface ContentHandler
  //

  /**
   * Sends the collected XML fragment to the specified target URL and passes the
   * return stream to an {@link XMLReader} object, which is connected to the
   * {@link Result} object of this {@link TransformerHandler}
   */
  @Override
  public void endDocument () throws SAXException
  {
    super.endDocument ();

    if (saxResult == null) // Shouldn't happen
      throw new SAXException ("No result set");

    HttpURLConnection conn = null;
    try
    {
      // create HTTP connection
      final URL url = new URL (targetURL);
      // HttpURLConnection
      conn = (HttpURLConnection) url.openConnection ();
      conn.setRequestMethod ("POST");
      conn.setDoInput (true);
      conn.setDoOutput (true);
      conn.setRequestProperty ("Content-Type", "text/xml");
      conn.connect ();

      final PrintStream ps = new PrintStream (conn.getOutputStream (), false, "UTF-8");
      ps.print (buffer.toString ());
      ps.close ();

      final XMLReader parser = Processor.createXMLReader ();
      parser.setContentHandler (saxResult.getHandler ());
      try
      {
        parser.setProperty ("http://xml.org/sax/properties/lexical-handler", saxResult.getLexicalHandler ());
      }
      catch (final SAXException ex)
      {}

      parser.parse (new InputSource (conn.getInputStream ()));
    }
    catch (final IOException ex)
    {
      System.err.println (ex);
      final InputStream is = conn.getErrorStream ();
      final BufferedReader br = new BufferedReader (new InputStreamReader (is));
      try
      {
        String line = br.readLine ();
        while (line != null)
        {
          System.err.println (line);
          line = br.readLine ();
        }
      }
      catch (final IOException ex2)
      {}

      // try {
      // InputStreamReader err = new InputStreamReader(conn.getInputStream());
      // OutputStreamWriter o = new OutputStreamWriter(System.err);
      // int c;
      // System.err.println(">>>");
      // while ((c = err.read()) != -1)
      // o.write(c);
      // err.close();
      // o.close();
      // System.err.println("<<<");
      // }
      // catch (IOException ex2) { }
      throw new SAXException (ex.toString ());
    }
  }

  // ---------------------------------------------------------------------

  //
  // from interface TransformerHandler
  //

  public void setResult (final Result result)
  {
    if (result instanceof SAXResult)
      saxResult = (SAXResult) result;
    else
    {
      // this will not happen in Joost
      throw new IllegalArgumentException ("result must be a SAXResult");
    }
  }

  // Never invoked by Joost
  @Override
  public void setSystemId (final String id)
  {}

  @Override
  public String getSystemId ()
  {
    return null;
  }

  public Transformer getTransformer ()
  {
    return null;
  }
}
