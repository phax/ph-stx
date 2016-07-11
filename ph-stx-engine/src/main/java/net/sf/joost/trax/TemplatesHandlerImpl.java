/*
 * $Id: TemplatesHandlerImpl.java,v 1.12 2004/12/27 18:52:46 obecker Exp $
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
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.trax;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TemplatesHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import net.sf.joost.CSTX;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Parser;

/**
 * A SAX ContentHandler that may be used to process SAX parse events (parsing
 * transformation instructions) into a Templates object. TemplatesHandlerImpl
 * acts as a proxy to {@link net.sf.joost.stx.Parser}
 *
 * @author Zubow
 */
public class TemplatesHandlerImpl implements TemplatesHandler
{

  // Define a static logger variable so that it references the
  // Logger instance named "TemplatesHandlerImpl".
  private static Logger log;
  static
  {
    if (CSTX.DEBUG)
      log = LoggerFactory.getLogger (TemplatesHandlerImpl.class);
  }

  // member fields
  private Parser stxparser = null;
  private String systemId = null;
  private TransformerFactoryImpl tfactory = null;

  /**
   * Constructor
   *
   * @param tfactory
   *        A Reference to <code>TransformerFactoryImpl</code>
   */
  protected TemplatesHandlerImpl (final TransformerFactoryImpl tfactory)
  {

    if (CSTX.DEBUG)
      log.debug ("calling constructor");
    this.tfactory = tfactory;
    // construct a tree representation of an STX stylesheet
    final ParseContext pContext = new ParseContext ();
    pContext.allowExternalFunctions = tfactory.allowExternalFunctions;
    stxparser = new Parser (pContext);
  }

  // *************************************************************************
  // IMPLEMENTATION OF TemplatesHandler
  // *************************************************************************

  /**
   * Get the base ID (URI or system ID) from where relative URLs will be
   * resolved
   *
   * @return The systemID that was set with {link setSystemId(String)}
   */
  public String getSystemId ()
  {
    return this.systemId;
  }

  /**
   * When a TemplatesHandler object is used as a ContentHandler for the parsing
   * of transformation instructions, it creates a Templates object, which the
   * caller can get once the SAX events have been completed.
   *
   * @return {@link Templates} The Templates object that was created during the
   *         SAX event process, or <code>null</code> if no Templates object has
   *         been created.
   */
  public Templates getTemplates ()
  {

    if (CSTX.DEBUG)
      log.debug ("calling getTemplates()");
    Templates templates = null;
    try
    {
      // construct TrAX-representation of an compiled STX stylesheet
      templates = new TemplatesImpl (stxparser, tfactory);
    }
    catch (final TransformerConfigurationException tE)
    {
      try
      {
        tfactory.defaultErrorListener.fatalError (tE);
      }
      catch (final TransformerConfigurationException e)
      {
        return null;
      }
    }
    return templates;
  }

  /**
   * Set the base ID (URI or system ID) from the Templates object created by
   * this builder. This must be set in order to resolve relative URIs in the
   * stylesheet. This must be called before the startDocument event.
   *
   * @param systemId
   *        Necessary for document root.
   */
  public void setSystemId (final String systemId)
  {
    this.systemId = systemId;
  }

  // *************************************************************************
  // IMPLEMENTATION OF ContentHandler
  // *************************************************************************

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void setDocumentLocator (final Locator locator)
  {
    stxparser.setDocumentLocator (locator);
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void startDocument () throws org.xml.sax.SAXException
  {
    stxparser.startDocument ();
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void endDocument () throws org.xml.sax.SAXException
  {
    stxparser.endDocument ();
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void startPrefixMapping (final String parm1, final String parm2) throws org.xml.sax.SAXException
  {
    stxparser.startPrefixMapping (parm1, parm2);
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void endPrefixMapping (final String parm) throws org.xml.sax.SAXException
  {
    stxparser.endPrefixMapping (parm);
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void startElement (final String parm1,
                            final String parm2,
                            final String parm3,
                            final Attributes parm4) throws org.xml.sax.SAXException
  {
    stxparser.startElement (parm1, parm2, parm3, parm4);
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void endElement (final String parm1, final String parm2, final String parm3) throws org.xml.sax.SAXException
  {
    stxparser.endElement (parm1, parm2, parm3);
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void characters (final char [] parm1, final int parm2, final int parm3) throws org.xml.sax.SAXException
  {
    stxparser.characters (parm1, parm2, parm3);
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void ignorableWhitespace (final char [] parm1,
                                   final int parm2,
                                   final int parm3) throws org.xml.sax.SAXException
  {
    stxparser.ignorableWhitespace (parm1, parm2, parm3);
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void processingInstruction (final String parm1, final String parm2) throws org.xml.sax.SAXException
  {
    stxparser.processingInstruction (parm1, parm2);
  }

  /**
   * SAX2-Callback - Simply propagates the Call to the registered
   * {@link net.sf.joost.stx.Parser} - here the {@link #stxparser}
   */
  public void skippedEntity (final String parm1) throws org.xml.sax.SAXException
  {
    stxparser.skippedEntity (parm1);
  }
}
