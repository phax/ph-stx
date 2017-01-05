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
package net.sf.joost.stx;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Wraps a SAX parser ({@link XMLReader}) in a {@link TransformerHandler}
 * object.
 * <p>
 * Collects all character data reported by {@link #characters characters} and
 * parses them afterwards with a SAX parser (which produces the result of this
 * transformation). Other input events will be ignored.
 * 
 * @version $Revision: 1.2 $ $Date: 2008/06/15 08:11:22 $
 * @author Oliver Becker
 */

public class SAXWrapperHandler extends DefaultHandler implements TransformerHandler
{
  /** event sink for this transformer */
  private SAXResult saxResult;

  /** the wrapped SAX parser */
  private XMLReader parser;

  /** the character buffer */
  private StringBuffer buffer;

  //
  // from interface LexicalHandler
  // (not implemented by DefaultHandler; empty implementations)
  //

  public void startDTD (final String name, final String publicId, final String systemId)
  {}

  public void endDTD ()
  {}

  public void startEntity (final String name)
  {}

  public void endEntity (final String name)
  {}

  public void startCDATA ()
  {}

  public void endCDATA ()
  {}

  public void comment (final char [] ch, final int start, final int length)
  {}

  // ---------------------------------------------------------------------

  //
  // from interface ContentHandler
  // (only relevant methods are overridden from DefaultHandler)
  //

  /** initialize parser and character buffer */
  @Override
  public void startDocument () throws SAXException
  {
    if (saxResult == null) // Shouldn't happen
      throw new SAXException ("No result set");

    parser = Processor.createXMLReader ();
    parser.setContentHandler (saxResult.getHandler ());
    try
    {
      parser.setProperty ("http://xml.org/sax/properties/lexical-handler", saxResult.getLexicalHandler ());
    }
    catch (final SAXException ex)
    {}

    buffer = new StringBuffer ();
  }

  /** collect characters */
  @Override
  public void characters (final char [] ch, final int start, final int length)
  {
    buffer.append (ch, start, length);
  }

  /** collect characters */
  @Override
  public void ignorableWhitespace (final char [] ch, final int start, final int length)
  {
    buffer.append (ch, start, length);
  }

  /** parse the collected characters */
  @Override
  public void endDocument () throws SAXException
  {
    try
    {
      parser.parse (new InputSource (new StringReader (buffer.toString ())));
    }
    catch (final IOException ex)
    {
      // shouldn't happen
      throw new SAXException (ex);
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
