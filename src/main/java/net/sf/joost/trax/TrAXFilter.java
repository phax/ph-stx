/*
 * $Id: TrAXFilter.java,v 1.10 2007/11/25 15:03:01 obecker Exp $
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

import java.io.IOException;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.logging.Log;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import net.sf.joost.Constants;
import net.sf.joost.OptionalLog;
import net.sf.joost.emitter.SAXEmitter;
import net.sf.joost.emitter.StxEmitter;
import net.sf.joost.stx.Processor;

/**
 * TrAXFilter
 * 
 * @author Zubow
 * @version 1.0
 */
public class TrAXFilter extends XMLFilterImpl implements Constants
{

  // Define a static logger variable so that it references the
  // Logger instance named "TransformerImpl".
  private static Log log = OptionalLog.getLog (TrAXFilter.class);

  private Templates templates = null;
  private Processor processor = null;

  // default ErrorListener
  private ConfigurationErrListener configErrListener;

  /**
   * Constructor
   * 
   * @param templates
   *        A <code>Templates</code>
   */
  protected TrAXFilter (final Templates templates)
  {

    if (DEBUG)
      log.debug ("calling constructor");
    this.templates = templates;
    if (templates instanceof TemplatesImpl)
    {
      configErrListener = ((TemplatesImpl) templates).factory.defaultErrorListener;
    }
  }

  /**
   * Parses the <code>InputSource</code>
   * 
   * @param input
   *        A <code>InputSource</code> object.
   * @throws SAXException
   * @throws IOException
   */
  @Override
  public void parse (final InputSource input) throws SAXException, IOException
  {

    Transformer transformer = null;
    if (DEBUG)
    {
      if (log.isDebugEnabled ())
        log.debug ("parsing InputSource " + input.getSystemId ());
    }

    try
    {
      // get a new Transformer
      transformer = this.templates.newTransformer ();
      if (transformer instanceof TransformerImpl)
      {
        this.processor = ((TransformerImpl) transformer).getStxProcessor ();
      }
      else
      {
        final String msg = "An error is occured, because the given transformer is " +
                           "not an instance of TransformerImpl";
        if (log != null)
          log.fatal (msg);
        else
          System.err.println ("Fatal error - " + msg);

      }
      XMLReader parent = this.getParent ();

      if (parent == null)
      {
        parent = XMLReaderFactory.createXMLReader ();
        setParent (parent);
      }
      ContentHandler handler = this.getContentHandler ();

      if (handler == null)
      {
        handler = parent.getContentHandler ();
      }
      if (handler == null)
      {
        throw new SAXException ("no ContentHandler registered");
      }
      // init StxEmitter
      StxEmitter out = null;

      // SAX specific Implementation
      out = new SAXEmitter (handler);

      if (this.processor != null)
      {
        this.processor.setContentHandler (out);
        this.processor.setLexicalHandler (out);
      }
      else
      {
        throw new SAXException ("Joost-Processor is not correct configured.");
      }
      if (parent == null)
      {
        throw new SAXException ("No parent for filter");
      }
      parent.setContentHandler (this.processor);
      parent.setProperty ("http://xml.org/sax/properties/lexical-handler", this.processor);
      parent.setEntityResolver (this);
      parent.setDTDHandler (this);
      parent.setErrorHandler (this);
      parent.parse (input);

    }
    catch (final TransformerConfigurationException tE)
    {
      try
      {
        configErrListener.fatalError (tE);
      }
      catch (final TransformerConfigurationException innerE)
      {
        throw new SAXException (innerE.getMessage (), innerE);
      }
    }
    catch (final SAXException sE)
    {
      try
      {
        configErrListener.fatalError (new TransformerConfigurationException (sE.getMessage (), sE));
      }
      catch (final TransformerConfigurationException innerE)
      {
        throw new SAXException (innerE.getMessage (), innerE);
      }
    }
    catch (final IOException iE)
    {
      try
      {
        configErrListener.fatalError (new TransformerConfigurationException (iE.getMessage (), iE));
      }
      catch (final TransformerConfigurationException innerE)
      {
        throw new IOException (innerE.getMessage ());
      }
    }
  }
}
