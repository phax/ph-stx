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
package net.sf.joost.emitter;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Result;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class implements an emitter for html code.
 *
 * @version $Revision: 1.6 $ $Date: 2008/10/06 13:31:41 $
 * @author Thomas Behrends
 */
public class HtmlEmitter extends AbstractStreamEmitter
{
  /** output property: omit-xml-declaration */
  private boolean propOmitXmlDeclaration = false;
  private boolean insideCDATA = false;
  private boolean supportDisableOutputEscaping = false;
  private boolean disabledOutputEscaping = false;
  /**
   * Empty HTML 4.01 elements according to
   * http://www.w3.org/TR/1999/REC-html401-19991224/index/elements.html
   */
  private static final Set <String> emptyHTMLElements;
  static
  {
    emptyHTMLElements = new HashSet<> ();
    emptyHTMLElements.add ("AREA");
    emptyHTMLElements.add ("BASE");
    emptyHTMLElements.add ("BASEFONT");
    emptyHTMLElements.add ("BR");
    emptyHTMLElements.add ("COL");
    emptyHTMLElements.add ("FRAME");
    emptyHTMLElements.add ("HR");
    emptyHTMLElements.add ("IMG");
    emptyHTMLElements.add ("INPUT");
    emptyHTMLElements.add ("ISINDEX");
    emptyHTMLElements.add ("LINK");
    emptyHTMLElements.add ("META");
    emptyHTMLElements.add ("PARAM");
  }

  /** Constructor */
  public HtmlEmitter (final Writer writer, final String encoding)
  {
    super (writer, encoding);
  }

  /**
   * Defines whether the XML declaration should be omitted, default is
   * <code>false</code>.
   *
   * @param flag
   *        <code>true</code>: the XML declaration will be omitted;
   *        <code>false</code>: the XML declaration will be output
   */
  @Override
  public void setOmitXmlDeclaration (final boolean flag)
  {
    propOmitXmlDeclaration = flag;
  }

  @Override
  public void setSupportDisableOutputEscaping (final boolean flag)
  {
    supportDisableOutputEscaping = flag;
  }

  /**
   * SAX2-Callback - Outputs XML-Deklaration with encoding.
   */
  public void startDocument () throws SAXException
  {
    if (propOmitXmlDeclaration)
      return;

    try
    {
      m_aWriter.write ("<!DOCTYPE HTML PUBLIC " + "\"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
    }
    catch (final IOException ex)
    {
      throw new SAXException (ex);
    }
  }

  /**
   * SAX2-Callback - Closing OutputStream.
   */
  public void endDocument () throws SAXException
  {
    try
    {
      m_aWriter.write ("\n");
      m_aWriter.flush ();
    }
    catch (final IOException ex)
    {
      throw new SAXException (ex);
    }
  }

  /**
   * SAX2-Callback
   */
  public void startElement (final String uri,
                            final String lName,
                            final String qName,
                            final Attributes attrs) throws SAXException
  {
    final StringBuffer out = new StringBuffer ("<");
    out.append (qName);

    final int length = attrs.getLength ();
    for (int i = 0; i < length; i++)
    {
      out.append (' ').append (attrs.getQName (i)).append ("=\"");
      final char [] attChars = attrs.getValue (i).toCharArray ();

      // perform output escaping
      for (int j = 0; j < attChars.length; j++)
        switch (attChars[j])
        {
          case '&':
            out.append ("&amp;");
            break;
          case '<':
            out.append ("&lt;");
            break;
          case '>':
            out.append ("&gt;");
            break;
          case '\"':
            out.append ("&quot;");
            break;
          case '\t':
            out.append ("&#x9;");
            break;
          case '\n':
            out.append ("&#xA;");
            break;
          case '\r':
            out.append ("&#xD;");
            break;
          case 160:
            out.append ("&nbsp;");
            break;
          default:
            j = encodeCharacters (attChars, j, out);
        }
      out.append ('\"');
    }

    out.append (">");

    try
    {
      m_aWriter.write (out.toString ());
    }
    catch (final IOException ex)
    {
      throw new SAXException (ex);
    }
  }

  /**
   * SAX2-Callback - Outputs the element-tag.
   */
  public void endElement (final String uri, final String lName, final String qName) throws SAXException
  {
    // output end tag only if it is not an empty element in HTML
    if (!emptyHTMLElements.contains (qName.toUpperCase ()))
    {
      try
      {
        m_aWriter.write ("</");
        m_aWriter.write (qName);
        m_aWriter.write (">");
      }
      catch (final IOException ex)
      {
        throw new SAXException (ex);
      }
    }
  }

  /**
   * SAX2-Callback - Constructs characters.
   */
  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    try
    {
      if (insideCDATA || disabledOutputEscaping)
      {
        // Check that the characters can be represented in the current
        // encoding
        for (int i = 0; i < length; i++)
          if (!m_aCharsetEncoder.canEncode (ch[start + i]))
            throw new SAXException ("Cannot output character with code " +
                                    (int) ch[start + i] +
                                    " in the encoding '" +
                                    m_sEncoding +
                                    "'");
        m_aWriter.write (ch, start, length);
      }
      else
      {
        final StringBuffer out = new StringBuffer ((int) (length * 1.3f));
        // perform output escaping
        for (int i = 0; i < length; i++)
          switch (ch[start + i])
          {
            case '&':
              out.append ("&amp;");
              break;
            case '<':
              out.append ("&lt;");
              break;
            case '>':
              out.append ("&gt;");
              break;
            case 160:
              out.append ("&nbsp;");
              break;
            default:
              i = encodeCharacters (ch, start + i, out) - start;
          }
        m_aWriter.write (out.toString ());
      }
    }
    catch (final IOException ex)
    {
      throw new SAXException (ex);
    }
  }

  /**
   * SAX2-Callback - Outputs a comment
   */
  @Override
  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    try
    {
      m_aWriter.write ("<!--");
      m_aWriter.write (ch, start, length);
      m_aWriter.write ("-->");
    }
    catch (final IOException ex)
    {
      throw new SAXException (ex);
    }
  }

  /**
   * SAX2-Callback - Handles a PI (cares about disable-output-escaping)
   */
  @Override
  public void processingInstruction (final String target, final String data) throws SAXException
  {
    if (supportDisableOutputEscaping)
    {
      if (Result.PI_DISABLE_OUTPUT_ESCAPING.equals (target))
      {
        disabledOutputEscaping = true;
      }
      else
        if (Result.PI_ENABLE_OUTPUT_ESCAPING.equals (target))
        {
          disabledOutputEscaping = false;
        }
    }
  }

  /**
   * CDATA sections will be handled like "disable-output-escaping" in HTML
   * (which is of course a kind of a "hack" ...)
   */
  @Override
  public void startCDATA () throws SAXException
  {
    insideCDATA = true;
  }

  @Override
  public void endCDATA () throws SAXException
  {
    insideCDATA = false;
  }
}
