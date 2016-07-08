/*
 * $Id: IncludeFactory.java,v 2.14 2008/06/15 08:11:23 obecker Exp $
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

package net.sf.joost.instruction;

import java.net.URL;
import java.util.HashSet;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Parser;
import net.sf.joost.stx.Processor;
import net.sf.joost.trax.TrAXHelper;

/**
 * Factory for <code>include</code> elements, which will be replaced by groups
 * for the included transformation sheet
 * 
 * @version $Revision: 2.14 $ $Date: 2008/06/15 08:11:23 $
 * @author Oliver Becker
 */

final public class IncludeFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  // Constructor
  public IncludeFactory ()
  {
    attrNames = new HashSet ();
    attrNames.add ("href");
  }

  /** @return <code>"include"</code> */
  @Override
  public String getName ()
  {
    return "include";
  }

  /** Returns an instance of {@link TransformFactory.Instance} */
  @Override
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext pContext) throws SAXException
  {
    // check parent
    if (parent == null)
      throw new SAXParseException ("'" + qName + "' not allowed as root element", pContext.locator);
    if (!(parent instanceof GroupBase))
      throw new SAXParseException ("'" + qName + "' not allowed as child of '" + parent.qName + "'", pContext.locator);

    final String hrefAtt = getRequiredAttribute (qName, attrs, "href", pContext);

    checkAttributes (qName, attrs, attrNames, pContext);

    final Parser stxParser = new Parser (new ParseContext (pContext));
    stxParser.includingGroup = (GroupBase) parent;

    XMLReader reader = null;
    InputSource iSource;
    try
    {
      Source source;
      if (pContext.uriResolver != null &&
          (source = pContext.uriResolver.resolve (hrefAtt, pContext.locator.getSystemId ())) != null)
      {
        final SAXSource saxSource = TrAXHelper.getSAXSource (source, null);
        reader = saxSource.getXMLReader ();
        iSource = saxSource.getInputSource ();
      }
      else
      {
        iSource = new InputSource (new URL (new URL (pContext.locator.getSystemId ()), hrefAtt).toExternalForm ());
      }
      if (reader == null)
        reader = Processor.createXMLReader ();
      reader.setContentHandler (stxParser);
      reader.setErrorHandler (pContext.getErrorHandler ());
      reader.parse (iSource);
    }
    catch (final java.io.IOException ex)
    {
      // TODO: better error handling
      throw new SAXParseException (ex.toString (), pContext.locator);
    }
    catch (final SAXParseException ex)
    {
      // propagate
      throw ex;
    }
    catch (final SAXException ex)
    {
      if (ex.getException () instanceof TransformerConfigurationException)
        throw ex;
      else
      {
        // will this ever happen?
        // add locator information
        throw new SAXParseException (ex.getMessage (), pContext.locator);
      }
    }
    catch (final TransformerException te)
    {
      throw new SAXException (te);
    }

    final TransformFactory.Instance tfi = stxParser.getTransformNode ();
    // transfer compilable nodes to the calling Parser object
    tfi.compilableNodes = stxParser.compilableNodes;
    tfi.qName = qName; // replace name for error reporting
    return tfi;
  }
}
