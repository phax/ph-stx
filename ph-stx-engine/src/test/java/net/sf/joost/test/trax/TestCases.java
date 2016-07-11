/*
 * $Id: TestCases.java,v 1.4 2009/03/15 14:01:19 obecker Exp $
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

package net.sf.joost.test.trax;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.DOMSerializerImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import junit.framework.Assert;
import net.sf.joost.IOutputURIResolver;
import net.sf.joost.trax.CTrAX;

/**
 * @author Zubow
 */
public class TestCases
{

  // Define a static logger variable so that it references the
  // Logger instance named "TemplatesImpl".
  static Logger log = Logger.getLogger (TestCases.class);

  // DEFAULTSETTINGS
  private final static String DEFXML = "data/flat.xml";

  private final static String DEFXML2 = "data/flat2.xml";

  private final static String DEFSTX1 = "data/flat.stx";

  private final static String DEFSTX2 = "data/indent.stx";

  private final static String DEFSTX3 = "data/copy.stx";

  private final static String DEFDEST = "result/result.xml";

  private static InputStream getStream (final String id)
  {
    return TestCases.class.getResourceAsStream (id);
  }

  private static Source getSource (final String id)
  {
    return new StreamSource (getStream (id));
  }

  private static String getSystemId (final String id)
  {
    return TestCases.class.getResource (id).toExternalForm ();
  }

  private static InputSource getInputSource (final String id)
  {
    return new InputSource (getStream (id));
  }

  /**
   * @todo : Identity-transformation
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests0 (String xmlsrc)
  {

    if (xmlsrc == null)
    {
      xmlsrc = DEFXML;
    }

    log.debug ("\n\n==== exampleIdentity ====");
    try
    {

      return exampleIdentity (xmlsrc);

    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests1 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleSimple ====");
    try
    {

      return exampleSimple1 (xmlsrc, stx);

    }
    catch (final Exception ex)
    {
      handleException (ex);
      // ex.printStackTrace();
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   * @param dest
   */
  public static void runTests2 (String xmlsrc, String stx, String dest)
  {

    if ((xmlsrc == null) || (stx == null) || (dest == null))
    {

      xmlsrc = DEFXML;
      stx = DEFSTX1;
      dest = DEFDEST;
    }

    log.debug ("\n\n==== exampleSimple2 (see " + dest + " ) ====");

    try
    {

      exampleSimple2 (xmlsrc, stx, dest);

    }
    catch (final Exception ex)
    {
      handleException (ex);
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests3 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {

      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleFromStream ====");

    try
    {

      return (exampleFromStream (xmlsrc, stx));

    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests4 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {

      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleFromReader ====");

    try
    {

      return (exampleFromReader (xmlsrc, stx));

    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc1
   * @param xmlsrc2
   * @param stx
   */
  public static boolean runTests5 (String xmlsrc1, String xmlsrc2, String stx)
  {

    if ((xmlsrc1 == null) || (xmlsrc2 == null) || (stx == null))
    {
      xmlsrc1 = DEFXML;
      xmlsrc2 = DEFXML2;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleUseTemplatesObj ====");

    try
    {

      return (exampleUseTemplatesObj (xmlsrc1, xmlsrc2, stx));

    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests6 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleContentHandlerToContentHandler ====");

    try
    {

      return (exampleContentHandlerToContentHandler (xmlsrc, stx));

    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests7 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleXMLReader ====");

    try
    {
      return (exampleXMLReader (xmlsrc, stx));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests8 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleXMLFilter ====");

    try
    {
      return (exampleXMLFilter (xmlsrc, stx));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx1
   * @param stx2
   */
  public static boolean runTests9 (String xmlsrc, String stx1, String stx2)
  {

    if ((xmlsrc == null) || (stx1 == null) || (stx2 == null))
    {
      xmlsrc = DEFXML;
      stx1 = DEFSTX1;
      stx2 = DEFSTX2;
    }

    log.debug ("\n\n==== exampleXMLFilterChain ====");

    try
    {
      return (exampleXMLFilterChain (xmlsrc, stx1, stx2));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests10 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleDOM2DOM ====");

    try
    {

      // print DOMResult
      final Node nodeResult = exampleDOM2DOM (xmlsrc, stx);

      if (nodeResult != null)
      {

        final String result = serializeDOM2String (nodeResult);

        log.info ("*** print out DOM-document - DOMResult ***");
        log.info (result);
      }
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
    return true;
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests11 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleParam ====");

    try
    {
      return (exampleParam (xmlsrc, stx));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests12 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleTransformerReuse ====");

    try
    {
      return (exampleTransformerReuse (xmlsrc, stx));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests13 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleOutputProperties ====");

    try
    {
      return (exampleOutputProperties (xmlsrc, stx));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   */
  public static boolean runTests14 (String xmlsrc)
  {
    if (xmlsrc == null)
    {
      xmlsrc = DEFXML;
    }

    log.debug ("\n\n==== exampleUseAssociated ====");

    try
    {
      return (exampleUseAssociated (xmlsrc));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests15 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleContentHandler2DOM ====");

    try
    {
      return (exampleContentHandler2DOM (xmlsrc, stx));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : only a simple copy
   * @param xmlsrc
   * @param stx
   */
  public static boolean runTests16 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleAsSerializer ====");

    try
    {
      return (exampleAsSerializer (xmlsrc, stx));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * @todo : description
   * @param xmlsrc
   * @param stx1
   * @param stx2
   * @param stx3
   * @return
   */
  public static boolean runTests18 (String xmlsrc, String stx1, String stx2, String stx3)
  {

    if ((xmlsrc == null) || (stx1 == null) || (stx2 == null) || (stx3 == null))
    {
      xmlsrc = DEFXML;
      stx1 = DEFSTX1;
      stx2 = DEFSTX2;
      stx3 = DEFSTX3;
    }

    log.debug ("\n\n==== exampleXMLFilterBigChain ====");

    try
    {
      return (exampleXMLFilterBigChain (xmlsrc, stx1, stx2, stx3));
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * description : StreamSource to DomResult
   *
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static String runTests19 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleStreamSourceToDomResult ====");

    try
    {
      return exampleStreamSourceToDomResult (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return null;
    }
  }

  /**
   * description : stylesheet-input as DOMSource
   *
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static boolean runTests20 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleDomSourceStylesheetToStreamResult ====");

    try
    {
      return exampleDomSourceStylesheetToStreamResult (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * description : stylesheet-input and xml-input as DOMSource
   *
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static boolean runTests21 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleDomSourceForBoth ====");

    try
    {
      return exampleDomSourceForBoth (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * description : using only DOMSource and DOMResult
   *
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static boolean runTests22 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleDOMSourceAndDomResult ====");

    try
    {
      return exampleDOMSourceAndDomResult (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * using SaxSource only
   *
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static boolean runTests23 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleSaxSourceForBoth ====");

    try
    {
      return exampleSaxSourceForBoth (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * using SaxSource and SaxResult only
   *
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static boolean runTests24 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    log.debug ("\n\n==== exampleSaxSourceAndSaxResult ====");

    try
    {
      return exampleSaxSourceAndSaxResult (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * using SaxResult with ContentHandler
   *
   * @todo : find out why we must specify this key : "org.xml.sax.driver"
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static boolean runTests25 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    // set env-values
    // setting new
    final String key = "org.xml.sax.driver";
    final String value = "org.apache.xerces.parsers.SAXParser";

    log.debug ("Setting key " + key + " to " + value);

    final Properties props = System.getProperties ();
    props.put (key, value);

    System.setProperties (props);

    log.debug ("\n\n==== exampleSaxResultWithContentHandler ====");

    try
    {
      return exampleSaxResultWithContentHandler (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * using {@link TemplatesHandler}
   *
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static boolean runTests26 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    // set env-values
    // setting new
    final String key = "org.xml.sax.driver";
    final String value = "org.apache.xerces.parsers.SAXParser";

    log.debug ("Setting key " + key + " to " + value);

    final Properties props = System.getProperties ();
    props.put (key, value);

    System.setProperties (props);

    log.debug ("\n\n==== exampleTemplatesHandler ====");

    try
    {
      return exampleTemplatesHandler (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  /**
   * using {@link TemplatesHandler}
   *
   * @param xmlsrc
   * @param stx
   * @return
   */
  public static boolean runTests27 (String xmlsrc, String stx)
  {

    if ((xmlsrc == null) || (stx == null))
    {
      xmlsrc = DEFXML;
      stx = DEFSTX1;
    }

    // set env-values
    // setting new
    final String key = "org.xml.sax.driver";
    final String value = "org.apache.xerces.parsers.SAXParser";

    log.debug ("Setting key " + key + " to " + value);

    final Properties props = System.getProperties ();
    props.put (key, value);

    System.setProperties (props);

    log.debug ("\n\n==== exampleSAXTransformerFactory ====");

    try
    {
      return exampleSAXTransformerFactory (xmlsrc, stx);
    }
    catch (final Exception ex)
    {
      handleException (ex);
      return false;
    }
  }

  public static boolean runTests28 (String xmlsrc)
  {
    if (xmlsrc == null)
      xmlsrc = DEFXML;

    try
    {
      return exampleOutputURIResolver (xmlsrc);
    }
    catch (final Exception e)
    {
      handleException (e);
      return false;
    }
  }

  public static boolean runTests29 (String xmlsrc)
  {
    if (xmlsrc == null)
      xmlsrc = DEFXML;

    try
    {
      return exampleDisableOutputEscaping (xmlsrc);
    }
    catch (final Exception e)
    {
      handleException (e);
      return false;
    }
  }

  /**
   * Show the Identity-transformation
   */
  public static boolean exampleIdentity (final String sourceID) throws TransformerException,
                                                                TransformerConfigurationException
  {

    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance ();
    // register own ErrorListener for the TransformerFactory
    final ErrorListener fListener = new ErrorListenerImpl ("TransformerFactory");
    tfactory.setErrorListener (fListener);

    // register own ErrorListener for the TransformerFactory
    final ErrorListener tListener = new ErrorListenerImpl ("Transformer");
    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer ();
    transformer.setErrorListener (tListener);

    // Transform the source XML to System.out.
    transformer.transform (getSource (sourceID), new StreamResult (System.out));

    return true;
  }

  /**
   * Show the simplest possible transformation from system id to output stream.
   */
  public static boolean exampleSimple1 (final String sourceID, final String stxID) throws TransformerException,
                                                                                   TransformerConfigurationException
  {

    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // register own ErrorListener for the TransformerFactory
    final ErrorListener fListener = new ErrorListenerImpl ("TransformerFactory");
    tfactory.setErrorListener (fListener);

    final ErrorListener tListener = new ErrorListenerImpl ("Transformer");
    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer (getSource (stxID));
    transformer.setErrorListener (tListener);

    // Transform the source XML to System.out.
    transformer.transform (getSource (sourceID), new StreamResult (System.out));

    return true;
  }

  /**
   * Show the simplest possible transformation from File to a File.
   */

  public static void exampleSimple2 (final String sourceID,
                                     final String stxID,
                                     final String dest) throws TransformerException, TransformerConfigurationException
  {

    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer (getSource (stxID));

    // Transform the source XML to flat.out.
    transformer.transform (new StreamSource (new File (sourceID)), new StreamResult (new File (dest)));

  }

  /**
   * Show simple transformation from input stream to output stream.
   */

  public static boolean exampleFromStream (final String sourceID, final String stxID) throws TransformerException,
                                                                                      TransformerConfigurationException,
                                                                                      FileNotFoundException
  {

    // Create a transform factory instance.

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    final InputStream stxIS = new BufferedInputStream (getStream (stxID));

    final StreamSource stxSource = new StreamSource (stxIS);

    // Note that if we don't do this, relative URLs can not be resolved
    // correctly!
    stxSource.setSystemId (stxID);

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer (stxSource);

    final InputStream xmlIS = new BufferedInputStream (getStream (sourceID));

    final StreamSource xmlSource = new StreamSource (xmlIS);

    // Note that if we don't do this, relative URLs can not be resolved
    // correctly!
    xmlSource.setSystemId (sourceID);

    // Transform the source XML to System.out.
    transformer.transform (xmlSource, new StreamResult (System.out));

    return true;
  }

  /**
   * Show simple transformation from reader to output stream. In general this
   * use case is discouraged, since the XML encoding can not be processed.
   */
  public static boolean exampleFromReader (final String sourceID, final String stxID) throws TransformerException,
                                                                                      TransformerConfigurationException,
                                                                                      FileNotFoundException
  {

    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Note that in this case the XML encoding can not be processed!
    final Reader stxReader = new BufferedReader (new InputStreamReader (getStream (stxID)));

    final StreamSource stxSource = new StreamSource (stxReader);

    // Note that if we don't do this, relative URLs can not be resolved
    // correctly!
    stxSource.setSystemId (stxID);

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer (stxSource);

    // Note that in this case the XML encoding can not be processed!
    final Reader xmlReader = new BufferedReader (new InputStreamReader (getStream (sourceID)));

    final StreamSource xmlSource = new StreamSource (xmlReader);

    // Note that if we don't do this, relative URLs can not be resolved
    // correctly!
    xmlSource.setSystemId (sourceID);

    // Transform the source XML to System.out.
    transformer.transform (xmlSource, new StreamResult (System.out));

    return true;
  }

  /**
   * Show the simplest possible transformation from system id to output stream.
   */
  public static boolean exampleUseTemplatesObj (final String sourceID1,
                                                final String sourceID2,
                                                final String stxID) throws TransformerException,
                                                                    TransformerConfigurationException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Create a templates object, which is the processed,
    // thread-safe representation of the stylesheet.
    final Templates templates = tfactory.newTemplates (getSource (stxID));

    // Illustrate the fact that you can make multiple transformers
    // from the same template.
    final Transformer transformer1 = templates.newTransformer ();

    final Transformer transformer2 = templates.newTransformer ();

    log.debug ("\n\n----- transform of " + sourceID1 + " -----");

    transformer1.transform (getSource (sourceID1), new StreamResult (System.out));

    log.debug ("\n\n----- transform of " + sourceID2 + " -----");

    transformer2.transform (getSource (sourceID2), new StreamResult (System.out));

    return true;
  }

  /**
   * Show the Transformer using SAX events in and SAX events out.
   */
  public static boolean exampleContentHandlerToContentHandler (final String sourceID,
                                                               final String stxID) throws TransformerException,
                                                                                   TransformerConfigurationException,
                                                                                   SAXException,
                                                                                   IOException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Does this factory support SAX features?
    if (tfactory.getFeature (SAXSource.FEATURE))
    {

      // If so, we can safely cast.
      final SAXTransformerFactory stfactory = ((SAXTransformerFactory) tfactory);

      // A TransformerHandler is a ContentHandler that will listen for
      // SAX events, and transform them to the result.
      final TransformerHandler handler = stfactory.newTransformerHandler (getSource (stxID));

      // Set the result handling to be a serialization to System.out.
      final Result result = new SAXResult (new ExampleContentHandler ());

      handler.setResult (result);

      // Create a reader, and set it's content handler to be the
      // TransformerHandler.
      XMLReader reader = null;

      // Use JAXP1.1 ( if possible )

      try
      {

        final javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance ();

        factory.setNamespaceAware (true);

        final javax.xml.parsers.SAXParser jaxpParser =

                                                     factory.newSAXParser ();

        reader = jaxpParser.getXMLReader ();

      }
      catch (final javax.xml.parsers.ParserConfigurationException ex)
      {
        throw new org.xml.sax.SAXException (ex);
      }
      catch (final javax.xml.parsers.FactoryConfigurationError ex1)
      {
        throw new org.xml.sax.SAXException (ex1.toString ());
      }
      catch (final NoSuchMethodError ex2)
      {}

      if (reader == null)
        reader = XMLReaderFactory.createXMLReader ();

      reader.setContentHandler (handler);

      // It's a good idea for the parser to send lexical events.
      // The TransformerHandler is also a LexicalHandler.
      reader.setProperty ("http://xml.org/sax/properties/lexical-handler", handler);

      // Parse the source XML, and send the parse events to the
      // TransformerHandler.
      reader.parse (getInputSource (sourceID));
    }
    else
    {

      log.error ("Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
      return false;
    }

    return true;
  }

  /**
   * Show the Transformer as a SAX2 XMLReader. An XMLFilter obtained from
   * newXMLFilter should act as a transforming XMLReader if setParent is not
   * called. Internally, an XMLReader is created as the parent for the
   * XMLFilter.
   */
  public static boolean exampleXMLReader (final String sourceID, final String stxID) throws TransformerException,
                                                                                     TransformerConfigurationException,
                                                                                     SAXException,
                                                                                     IOException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    if (tfactory.getFeature (SAXSource.FEATURE))
    {

      final XMLReader reader = ((SAXTransformerFactory) tfactory).newXMLFilter (getSource (stxID));

      reader.setContentHandler (new ExampleContentHandler ());

      reader.parse (getInputSource (sourceID));
    }
    else
    {
      log.error ("tfactory does not support SAX features!");
      return false;
    }
    return true;
  }

  /**
   * Show the Transformer as a simple XMLFilter. This is pretty similar to
   * exampleXMLReader, except that here the parent XMLReader is created by the
   * caller, instead of automatically within the XMLFilter. This gives the
   * caller more direct control over the parent reader.
   */
  public static boolean exampleXMLFilter (final String sourceID, final String stxID) throws TransformerException,
                                                                                     TransformerConfigurationException,
                                                                                     SAXException,
                                                                                     IOException // ,
                                                                                                 // ParserConfigurationException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    XMLReader reader = null;

    // Use JAXP1.1 ( if possible )

    try
    {

      final javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance ();

      factory.setNamespaceAware (true);

      final javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser ();

      reader = jaxpParser.getXMLReader ();

    }
    catch (final javax.xml.parsers.ParserConfigurationException ex)
    {
      throw new org.xml.sax.SAXException (ex);
    }
    catch (final javax.xml.parsers.FactoryConfigurationError ex1)
    {
      throw new org.xml.sax.SAXException (ex1.toString ());
    }
    catch (final NoSuchMethodError ex2)
    {}

    if (reader == null)
      reader = XMLReaderFactory.createXMLReader ();

    // The transformer will use a SAX parser as it's reader.
    reader.setContentHandler (new ExampleContentHandler ());
    try
    {
      reader.setFeature ("http://xml.org/sax/features/namespace-prefixes", true);
      reader.setFeature ("http://apache.org/xml/features/validation/dynamic", true);
    }
    catch (final SAXException se)
    {
      se.printStackTrace ();
    }

    final XMLFilter filter = ((SAXTransformerFactory) tfactory).newXMLFilter (getSource (stxID));

    filter.setParent (reader);

    // Now, when you call transformer.parse, it will set itself as
    // the content handler for the parser object (it's "parent"), and
    // will then call the parse method on the parser.
    filter.parse (getInputSource (sourceID));

    return true;
  }

  /**
   * This example shows how to chain events from one Transformer to another
   * transformer, using the Transformer as a SAX2 XMLFilter/XMLReader.
   */
  public static boolean exampleXMLFilterChain (final String sourceID,
                                               final String stxID_1,
                                               final String stxID_2) throws TransformerException,
                                                                     TransformerConfigurationException,
                                                                     SAXException,
                                                                     IOException
  {
    // register own ErrorListener for the TransformerFactory
    final ErrorListener fListener = new ErrorListenerImpl ("TransformerFactory");

    // register own ErrorListener for the Transformer
    // ErrorListener tListener = new ErrorListenerImpl("Transformer");

    final TransformerFactory tfactory = TransformerFactory.newInstance ();
    tfactory.setErrorListener (fListener);

    // Templates stylesheet1 = tfactory.newTemplates(new
    // StreamSource(stxID_1));

    // Transformer transformer1 = stylesheet1.newTransformer();

    // If one success, assume all will succeed.

    if (tfactory.getFeature (SAXSource.FEATURE))
    {

      final SAXTransformerFactory stf = (SAXTransformerFactory) tfactory;

      final XMLReader reader = XMLReaderFactory.createXMLReader ();

      // init transformation-filter
      final XMLFilter filter1 = stf.newXMLFilter (getSource (stxID_1));

      final XMLFilter filter2 = stf.newXMLFilter (getSource (stxID_2));

      if (null != filter1) // If one success, assume all were success.
      {

        // transformer1 will use a SAX parser as it's reader.
        filter1.setParent (reader);
        // filter1.setContentHandler(new ExampleContentHandler());
        // filter1.parse(new InputSource(sourceID));

        // transformer2 will use transformer1 as it's reader.
        filter2.setParent (filter1);

        filter2.setContentHandler (new ExampleContentHandler ());

        // filter2.setContentHandler(new
        // org.xml.sax.helpers.DefaultHandler());

        // Now, when you call transformer3 to parse, it will set

        // itself as the ContentHandler for transform2, and

        // call transform2.parse, which will set itself as the

        // content handler for transform1, and call transform1.parse,

        // which will set itself as the content listener for the

        // SAX parser, and call parser.parse(new
        // InputSource("xml/flat.xml")).

        filter2.parse (getInputSource (sourceID));

      }
      else
      {
        log.error ("Can't do exampleXMLFilter because " + "tfactory doesn't support asXMLFilter()");
        return false;
      }
    }
    else
    {
      log.error ("Can't do exampleXMLFilter because " + "tfactory is not a SAXTransformerFactory");
      return false;
    }
    return true;
  }

  /**
   * This example shows how to chain events from one Transformer to another
   * transformer, using the Transformer as a SAX2 XMLFilter/XMLReader. The chain
   * consists of 3 steps.
   */
  public static boolean exampleXMLFilterBigChain (final String sourceID,
                                                  final String stxID_1,
                                                  final String stxID_2,
                                                  final String stxID_3) throws TransformerException,
                                                                        TransformerConfigurationException,
                                                                        SAXException,
                                                                        IOException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // If one success, assume all will succeed.

    if (tfactory.getFeature (SAXSource.FEATURE))
    {

      final SAXTransformerFactory stf = (SAXTransformerFactory) tfactory;

      XMLReader reader = null;

      // Use JAXP1.1 ( if possible )

      try
      {

        final javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance ();

        factory.setNamespaceAware (true);

        final javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser ();

        reader = jaxpParser.getXMLReader ();

      }
      catch (final javax.xml.parsers.ParserConfigurationException ex)
      {
        throw new org.xml.sax.SAXException (ex);
      }
      catch (final javax.xml.parsers.FactoryConfigurationError ex1)
      {
        throw new org.xml.sax.SAXException (ex1.toString ());
      }
      catch (final NoSuchMethodError ex2)
      {}

      if (reader == null)
        reader = XMLReaderFactory.createXMLReader ();

      final XMLFilter filter1 = stf.newXMLFilter (getSource (stxID_1));

      final XMLFilter filter2 = stf.newXMLFilter (getSource (stxID_2));

      final XMLFilter filter3 = stf.newXMLFilter (getSource (stxID_3));

      if (null != filter1) // If one success, assume all were success.
      {

        // transformer1 will use a SAX parser as it's reader.
        filter1.setParent (reader);

        // transformer2 will use transformer1 as it's reader.
        filter2.setParent (filter1);

        filter3.setParent (filter2);
        filter3.setContentHandler (new ExampleContentHandler ());

        // filter3.setContentHandler(new
        // org.xml.sax.helpers.DefaultHandler());

        // Now, when you call transformer3 to parse, it will set

        // itself as the ContentHandler for transform2, and

        // call transform2.parse, which will set itself as the

        // content handler for transform1, and call transform1.parse,

        // which will set itself as the content listener for the

        // SAX parser, and call parser.parse(new
        // InputSource("xml/flat.xml")).

        filter3.parse (getInputSource (sourceID));
      }
      else
      {
        log.error ("Can't do exampleXMLFilter because " + "tfactory doesn't support asXMLFilter()");
        return false;
      }
    }
    else
    {
      log.error ("Can't do exampleXMLFilter because " + "tfactory is not a SAXTransformerFactory");
      return false;
    }
    return true;
  }

  /**
   * Show how to transform a DOM tree into another DOM tree. This uses the
   * javax.xml.parsers to parse an XML file into a DOM, and create an output
   * DOM.
   */
  public static Node exampleDOM2DOM (final String sourceID,
                                     final String stxID) throws TransformerException,
                                                         TransformerConfigurationException,
                                                         SAXException,
                                                         IOException,
                                                         ParserConfigurationException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    if (tfactory.getFeature (DOMSource.FEATURE))
    {

      Templates templates;

      {

        final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();

        dfactory.setNamespaceAware (true);

        final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();

        // i think there is an error, so fix

        final InputSource isource = getInputSource (stxID);

        final Node doc = docBuilder.parse (isource);

        final DOMSource dsource = new DOMSource (doc);

        // If we don't do this, the transformer won't know how to
        // resolve relative URLs in the stylesheet.
        dsource.setSystemId (isource.getSystemId ());

        templates = tfactory.newTemplates (dsource);
      }

      final Transformer transformer = templates.newTransformer ();

      final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();

      // Note you must always setNamespaceAware when building .xsl
      // stylesheets

      dfactory.setNamespaceAware (true);

      final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();

      final org.w3c.dom.Document outNode = docBuilder.newDocument ();

      final Node doc = docBuilder.parse (getInputSource (sourceID));

      // fix errors
      final DOMSource domInSource = new DOMSource (doc);
      domInSource.setSystemId (getSystemId (sourceID));

      final DOMResult domresult = new DOMResult (outNode);
      transformer.transform (domInSource, domresult);

      return domresult.getNode ();

    }
    else
    {
      throw new org.xml.sax.SAXNotSupportedException ("DOM node processing not supported!");
    }
  }

  /**
   * This shows how to set a parameter for use by the templates. Use two
   * transformers to show that different parameters may be set on different
   * transformers.
   */
  public static boolean exampleParam (final String sourceID, final String stxID) throws TransformerException,
                                                                                 TransformerConfigurationException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    final Templates templates = tfactory.newTemplates (getSource (stxID));

    final Transformer transformer1 = templates.newTransformer ();

    final Transformer transformer2 = templates.newTransformer ();

    transformer1.setParameter ("a-param", "hello to you!");

    transformer1.transform (getSource (sourceID), new StreamResult (System.out));

    log.debug ("\n=========");

    // transformer2.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer2.transform (getSource (sourceID), new StreamResult (System.out));

    return true;
  }

  /**
   * Show the that a transformer can be reused, and show resetting a parameter
   * on the transformer.
   */
  public static boolean exampleTransformerReuse (final String sourceID,
                                                 final String stxID) throws TransformerException,
                                                                     TransformerConfigurationException
  {

    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer (getSource (stxID));

    transformer.setParameter ("a-param", "hello to you!");

    // Transform the source XML to System.out.
    transformer.transform (getSource (sourceID), new StreamResult (System.out));

    log.debug ("\n=========\n");

    transformer.setParameter ("a-param", "hello to me!");

    // transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    // Transform the source XML to System.out.
    transformer.transform (getSource (sourceID), new StreamResult (System.out));

    return true;
  }

  /**
   * Show how to override output properties.
   */
  public static boolean exampleOutputProperties (final String sourceID,
                                                 final String stxID) throws TransformerException,
                                                                     TransformerConfigurationException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    final Templates templates = tfactory.newTemplates (getSource (stxID));

    final Properties oprops = templates.getOutputProperties ();

    oprops.put (OutputKeys.METHOD, "text");

    final Transformer transformer = templates.newTransformer ();

    transformer.setOutputProperties (oprops);

    transformer.transform (getSource (sourceID), new StreamResult (System.out));

    // to be ignored
    transformer.setOutputProperty (OutputKeys.INDENT, "yes");

    try
    {
      transformer.setOutputProperty ("foobar", "yes");
      return false;
    }
    catch (final IllegalArgumentException expectedException)
    {}

    return true;
  }

  /**
   * Show how to get stylesheets that are associated with a given xml document
   * via the xml-stylesheet PI (see http://www.w3.org/TR/xml-stylesheet/).
   */
  public static boolean exampleUseAssociated (final String sourceID) throws TransformerException,
                                                                     TransformerConfigurationException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // The DOM tfactory will have it's own way, based on DOM2,
    // of getting associated stylesheets.
    if (tfactory instanceof SAXTransformerFactory)
    {

      final SAXTransformerFactory stf = ((SAXTransformerFactory) tfactory);

      final Source sources = stf.getAssociatedStylesheet (getSource (sourceID), null, null, null);

      if (null != sources)
      {

        final Transformer transformer = tfactory.newTransformer (sources);

        transformer.transform (getSource (sourceID), new StreamResult (System.out));
      }
      else
      {
        log.debug ("Can't find the associated stylesheet!");
        return false;
      }
    }
    return true;
  }

  /**
   * Show the Transformer using SAX events in and DOM nodes out.
   */
  public static boolean exampleContentHandler2DOM (final String sourceID,
                                                   final String stxID) throws TransformerException,
                                                                       TransformerConfigurationException,
                                                                       SAXException,
                                                                       IOException,
                                                                       ParserConfigurationException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Make sure the transformer factory we obtained supports both
    // DOM and SAX.
    if (tfactory.getFeature (SAXSource.FEATURE) && tfactory.getFeature (DOMSource.FEATURE))
    {

      // We can now safely cast to a SAXTransformerFactory.
      final SAXTransformerFactory sfactory = (SAXTransformerFactory) tfactory;

      // Create an Document node as the root for the output.
      final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();

      final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();

      final org.w3c.dom.Document outNode = docBuilder.newDocument ();

      // Create a ContentHandler that can liston to SAX events
      // and transform the output to DOM nodes.
      final TransformerHandler handler = sfactory.newTransformerHandler (getSource (stxID));

      final DOMResult domresult = new DOMResult (outNode);

      handler.setResult (domresult);

      // Create a reader and set it's ContentHandler to be the
      // transformer.
      XMLReader reader = null;

      // Use JAXP1.1 ( if possible )
      try
      {

        final javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance ();

        factory.setNamespaceAware (true);

        final javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser ();

        reader = jaxpParser.getXMLReader ();

      }
      catch (final javax.xml.parsers.ParserConfigurationException ex)
      {
        throw new org.xml.sax.SAXException (ex);
      }
      catch (final javax.xml.parsers.FactoryConfigurationError ex1)
      {
        throw new org.xml.sax.SAXException (ex1.toString ());
      }
      catch (final NoSuchMethodError ex2)
      {}

      if (reader == null)
        reader = XMLReaderFactory.createXMLReader ();

      reader.setContentHandler (handler);

      reader.setProperty ("http://xml.org/sax/properties/lexical-handler", handler);

      // Send the SAX events from the parser to the transformer,
      // and thus to the DOM tree.
      reader.parse (getInputSource (sourceID));

      // Serialize the node for diagnosis.
      exampleSerializeNode (domresult);
    }
    else
    {
      log.error ("Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
      return false;
    }
    return true;
  }

  /**
   * Serialize a node to System.out.
   */

  public static void exampleSerializeNode (final DOMResult result) throws TransformerException,
                                                                   TransformerConfigurationException,
                                                                   SAXException,
                                                                   IOException,
                                                                   ParserConfigurationException
  {
    if (result != null)
    {
      final String str = serializeDOM2String (result.getNode ());
      log.info ("*** print out DOM-document - DOMResult ***");
      log.info (str);
    }
  }

  /**
   * A fuller example showing how the TrAX interface can be used to serialize a
   * DOM tree.
   */
  public static boolean exampleAsSerializer (final String sourceID,
                                             final String stxID) throws TransformerException,
                                                                 TransformerConfigurationException,
                                                                 SAXException,
                                                                 IOException,
                                                                 ParserConfigurationException
  {

    final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();

    final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();

    final Node doc = docBuilder.parse (getInputSource (sourceID));

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // This creates a transformer that does a simple identity transform,
    // and thus can be used for all intents and purposes as a serializer.
    final Transformer serializer = tfactory.newTransformer ();

    // serializer.transform(new StreamSource(sourceID), new
    // StreamResult(System.out));

    final Properties oprops = new Properties ();

    oprops.put ("method", "html");

    oprops.put ("indent-amount", "2");

    // serializer.setOutputProperties(oprops);

    final DOMSource domInSource = new DOMSource (doc);
    domInSource.setSystemId (getSystemId (sourceID));

    serializer.transform (domInSource, new StreamResult (System.out));

    return true;
  }

  /**
   * using DOMResult
   *
   * @throws Exception
   */
  public static String exampleStreamSourceToDomResult (final String xmlId, final String stxId) throws Exception
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Create a transformer for the stylesheet.
    final Templates templates = tfactory.newTemplates (getSource (stxId));

    final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();

    // Note you must always setNamespaceAware when building .stx stylesheets
    dfactory.setNamespaceAware (true);

    final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();

    final org.w3c.dom.Document outNode = docBuilder.newDocument ();

    // Node doc = docBuilder.parse(new InputSource(xmlId));

    final Transformer transformer = templates.newTransformer ();

    final DOMResult myDomResult = new DOMResult (outNode);

    transformer.transform (getSource (xmlId), myDomResult);

    // print DOMResult

    final Node nodeResult = myDomResult.getNode ();

    if (nodeResult != null)
    {

      final String result = serializeDOM2String (nodeResult);

      log.info ("*** print out DOM-document - DOMResultt ***");
      log.info (result);

      return "";
    }
    else
    {
      return null;
    }
  }

  /**
   * using DOMSource for stylesheet (stx)
   *
   * @throws Exception
   */
  public static boolean exampleDomSourceStylesheetToStreamResult (final String xmlId,
                                                                  final String stxId) throws Exception
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();

    // Note you must always setNamespaceAware when building .stx stylesheets
    dfactory.setNamespaceAware (true);

    final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();

    // test
    final InputSource is = getInputSource (stxId);

    final Node doc = docBuilder.parse (is);

    // check
    if (doc != null)
    {

      final String result = serializeDOM2String (doc);

      log.debug ("*** Result ***");
      log.debug (result);
    }
    else
    {
      return false;
    }

    final DOMSource domSource = new DOMSource (doc);

    domSource.setSystemId (is.getSystemId ());

    log.debug ("id = " + domSource.getSystemId ());

    // Create a transformer for the stylesheet.
    final Templates templates = tfactory.newTemplates (domSource);

    final Transformer transformer = templates.newTransformer ();
    // DOMResult myDomResult = new DOMResult(outNode);
    transformer.transform (getSource (xmlId), new StreamResult (System.out));

    return true;
  }

  /**
   * using DOMSource for both inputs (xml and stx)
   *
   * @throws Exception
   */
  public static boolean exampleDomSourceForBoth (final String xmlId, final String stxId) throws Exception
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();

    // Note you must always setNamespaceAware when building .stx stylesheets

    dfactory.setNamespaceAware (true);

    final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();

    // test
    final InputSource is = getInputSource (stxId);

    final Node doc = docBuilder.parse (is);

    // check
    if (doc != null)
    {

      final String result = serializeDOM2String (doc);

      log.debug ("*** STX-sheet ***");
      log.debug (result);

    }
    else
    {
      return false;
    }

    final DOMSource domSource = new DOMSource (doc);

    domSource.setSystemId (getSystemId (stxId));

    log.debug ("id = " + domSource.getSystemId ());

    // Create a transformer for the stylesheet.
    final Templates templates = tfactory.newTemplates (domSource);

    final Transformer transformer = templates.newTransformer ();
    // DOMResult myDomResult = new DOMResult(outNode);

    final DOMSource docInSource = new DOMSource (doc);

    docInSource.setSystemId (getSystemId (xmlId));

    transformer.transform (docInSource, new StreamResult (System.out));

    return true;
  }

  /**
   * using DOMSource and DOMResult only
   *
   * @throws Exception
   */
  public static boolean exampleDOMSourceAndDomResult (final String xmlId, final String stxId) throws Exception
  {

    final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();
    // Note you must always setNamespaceAware when building .stx stylesheets
    dfactory.setNamespaceAware (true);

    DocumentBuilder docBuilder;
    Document doc;

    // create the transformer from a DOMSource

    docBuilder = dfactory.newDocumentBuilder ();

    doc = docBuilder.parse (getInputSource (stxId));
    final DOMSource domSource = new DOMSource (doc);
    domSource.setSystemId (getSystemId (stxId));

    log.debug ("id = " + domSource.getSystemId ());

    // Create a transformer for the stylesheet.
    final TransformerFactory tfactory = TransformerFactory.newInstance ();
    final Templates templates = tfactory.newTemplates (domSource);
    final Transformer transformer = templates.newTransformer ();

    // get DOMSource for xml -instance

    docBuilder = dfactory.newDocumentBuilder ();

    doc = docBuilder.parse (getInputSource (xmlId));
    final DOMSource docInSource = new DOMSource (doc);
    docInSource.setSystemId (getSystemId (xmlId));

    // construct a DOMResult

    final Document newDoc = docBuilder.newDocument ();
    final DOMResult myDomResult = new DOMResult (newDoc);

    transformer.transform (docInSource, myDomResult);
    Assert.assertEquals (newDoc, myDomResult.getNode ());

    // print DOMResult
    final Node nodeResult = myDomResult.getNode ();

    if (nodeResult != null)
    {

      final String result = serializeDOM2String (nodeResult);

      log.info ("*** print out DOM-document - DOMResult ***");
      log.info (result);

      return true;
    }
    return false;
  }

  /**
   * using SAXSource for both inputs
   *
   * @throws Exception
   */
  public static boolean exampleSaxSourceForBoth (final String xmlId, final String stxId) throws Exception
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    final InputSource isStx = getInputSource (stxId);

    final SAXSource saxSourceStx = new SAXSource (isStx);

    log.debug ("id = " + saxSourceStx.getSystemId ());

    // Create a transformer for the stylesheet.
    final Templates templates = tfactory.newTemplates (saxSourceStx);

    final Transformer transformer = templates.newTransformer ();

    final InputSource isXmlIn = getInputSource (xmlId);

    final SAXSource saxSourceXmlIn = new SAXSource (isXmlIn);

    transformer.transform (saxSourceXmlIn, new StreamResult (System.out));

    return true;
  }

  /**
   * using SAXSource und SAXResult only
   *
   * @throws Exception
   */
  public static boolean exampleSaxSourceAndSaxResult (final String xmlId, final String stxId) throws Exception
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    final InputSource isStx = getInputSource (stxId);

    final SAXSource saxSourceStx = new SAXSource (isStx);

    log.debug ("id = " + saxSourceStx.getSystemId ());

    // Create a transformer for the stylesheet.
    final Templates templates = tfactory.newTemplates (saxSourceStx);

    final Transformer transformer = templates.newTransformer ();

    final InputSource isXmlIn = getInputSource (xmlId);

    final SAXSource saxSourceXmlIn = new SAXSource (isXmlIn);

    // Set the result handling to be a serialization to System.out.

    final SAXResult saxResultXml = new SAXResult (new ExampleContentHandler ());

    transformer.transform (saxSourceXmlIn, saxResultXml);

    return true;
  }

  /**
   * using SAXResult with ExampleContentHandler
   *
   * @throws Exception
   */
  public static boolean exampleSaxResultWithContentHandler (final String xmlId, final String stxId) throws Exception
  {

    log.debug ("using SAXResult with ExampleContentHandler");

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Does this factory support SAX features?
    if (tfactory.getFeature (SAXSource.FEATURE))
    {

      // If so, we can safely cast.
      final SAXTransformerFactory stfactory = ((SAXTransformerFactory) tfactory);

      // A TransformerHandler is a ContentHandler that will listen for
      // SAX events, and transform them to the result.

      final TransformerHandler handler = stfactory.newTransformerHandler (getSource (stxId));

      // Set the result handling to be a serialization to System.out.

      final Result result = new SAXResult (new ExampleContentHandler ());
      handler.setResult (result);

      // Create a reader, and set it's content handler to be the
      // TransformerHandler.
      final XMLReader reader = XMLReaderFactory.createXMLReader ();

      reader.setContentHandler (handler);

      // It's a good idea for the parser to send lexical events.
      // The TransformerHandler is also a LexicalHandler.
      reader.setProperty ("http://xml.org/sax/properties/lexical-handler", handler);

      // Parse the source XML, and send the parse events to the
      // TransformerHandler.

      reader.parse (getInputSource (xmlId));

    }
    else
    {

      log.error ("Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
      return false;
    }
    return true;
  }

  /**
   * Show the Transformer using TemplatesHandler
   */
  public static boolean exampleTemplatesHandler (final String sourceID, final String stxID) throws TransformerException,
                                                                                            TransformerConfigurationException,
                                                                                            SAXException,
                                                                                            IOException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Does this factory support SAX features?
    if (tfactory.getFeature (SAXSource.FEATURE))
    {

      // If so, we can safely cast.
      final SAXTransformerFactory stfactory = ((SAXTransformerFactory) tfactory);

      // hole TemplatesHandler
      final TemplatesHandler tempHandler = stfactory.newTemplatesHandler ();

      // Create a reader, and set it's content handler to be the
      // TransformerHandler.
      XMLReader reader = null;

      // Use JAXP1.1 ( if possible )

      try
      {
        final javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance ();
        factory.setNamespaceAware (true);
        final javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser ();
        reader = jaxpParser.getXMLReader ();
      }
      catch (final javax.xml.parsers.ParserConfigurationException ex)
      {
        throw new org.xml.sax.SAXException (ex);
      }
      catch (final javax.xml.parsers.FactoryConfigurationError ex1)
      {
        throw new org.xml.sax.SAXException (ex1.toString ());
      }
      catch (final NoSuchMethodError ex2)
      {}

      if (reader == null)
        reader = XMLReaderFactory.createXMLReader ();

      reader.setContentHandler (tempHandler);

      // Parse the stylesheet
      reader.parse (getInputSource (stxID));

      // hole das Template from TemplatesHandler
      final Templates templates = tempHandler.getTemplates ();

      // hole den TransformerHandler
      final TransformerHandler transHandler = stfactory.newTransformerHandler (templates);

      // Set the result handling to be a serialization to System.out.
      final Result result = new SAXResult (new ExampleContentHandler ());

      // setting Emitter
      transHandler.setResult (result);

      reader.setContentHandler (transHandler);

      // It's a good idea for the parser to send lexical events.
      // The TransformerHandler is also a LexicalHandler.
      reader.setProperty ("http://xml.org/sax/properties/lexical-handler", transHandler);

      // Parse the source XML, and send the parse events to the
      // TransformerHandler.
      reader.parse (getInputSource (sourceID));

    }
    else
    {

      log.error ("Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
      return false;
    }

    return true;
  }

  /**
   * Show the Transformer using TemplatesHandler
   */
  public static boolean exampleSAXTransformerFactory (final String sourceID,
                                                      final String stxID) throws TransformerException,
                                                                          TransformerConfigurationException,
                                                                          SAXException,
                                                                          IOException
  {

    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    // Does this factory support SAX features?
    if (tfactory.getFeature (SAXSource.FEATURE))
    {

      // If so, we can safely cast.
      final SAXTransformerFactory stfactory = ((SAXTransformerFactory) tfactory);

      final TemplatesHandler handler = stfactory.newTemplatesHandler ();
      handler.setSystemId (getSystemId (stxID));

      final XMLReader reader = XMLReaderFactory.createXMLReader ();
      reader.setContentHandler (handler);

      reader.parse (getInputSource (stxID));

      final Templates templates = handler.getTemplates ();

      // transform
      final Transformer transformer = templates.newTransformer ();
      transformer.transform (getSource (sourceID), new StreamResult (System.out));

    }

    return true;
  }

  public static boolean exampleOutputURIResolver (final String sourceID) throws TransformerException,
                                                                         UnsupportedEncodingException
  {

    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance ();

    final ByteArrayOutputStream baoStream = new ByteArrayOutputStream ();
    final Map <StreamResult, ByteArrayOutputStream> resultMap = new HashMap<> ();
    final IOutputURIResolver resolver = new IOutputURIResolver ()
    {
      public Result resolve (final String href,
                             final String base,
                             final Properties outputProperties,
                             final boolean append) throws TransformerException
      {
        System.out.println ("href: " + href);
        System.out.println ("base: " + base);
        System.out.println ("props: " + outputProperties);
        System.out.println ("append: " + append);
        if ("foo.xml".equals (href))
        {
          final StreamResult result = new StreamResult (baoStream);
          resultMap.put (result, baoStream);
          return result;
        }
        return null;
      }

      public void close (final Result result) throws TransformerException
      {
        final OutputStream stream = resultMap.get (result);
        try
        {
          if (stream != null)
            stream.close ();
        }
        catch (final IOException e)
        {
          throw new TransformerException (e);
        }
      }
    };

    tfactory.setAttribute (CTrAX.KEY_OUTPUT_URI_RESOLVER, resolver);

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer (getSource ("data/result-doc.stx"));

    // Transform the source XML to flat.out.
    transformer.transform (getSource (sourceID), new StreamResult (System.out));

    final String string = baoStream.toString ("iso-8859-1");
    System.out.println ("Result:");
    System.out.println (string);

    return "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<bar baz=\"ä\" />\n".equals (string);
  }

  public static boolean exampleDisableOutputEscaping (final String sourceId) throws TransformerException,
                                                                             UnsupportedEncodingException
  {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();

    final TransformerFactory tfactory = TransformerFactory.newInstance ();
    final Templates templates = tfactory.newTemplates (getSource ("data/doe.stx"));

    Transformer transformer = templates.newTransformer ();
    transformer.setOutputProperty (CTrAX.OUTPUT_KEY_SUPPORT_DISABLE_OUTPUT_ESCAPING, "yes");
    transformer.transform (getSource (sourceId), new StreamResult (baos));

    String result = new String (baos.toByteArray (), "UTF-8");
    Assert.assertTrue (result.contains ("<>"));
    Assert.assertFalse (result.contains (Result.PI_DISABLE_OUTPUT_ESCAPING));
    Assert.assertFalse (result.contains (Result.PI_ENABLE_OUTPUT_ESCAPING));

    baos.reset ();

    transformer = templates.newTransformer ();
    transformer.transform (getSource (sourceId), new StreamResult (baos));

    result = new String (baos.toByteArray (), "UTF-8");
    Assert.assertFalse (result.contains ("<>"));
    Assert.assertTrue (result.contains (Result.PI_DISABLE_OUTPUT_ESCAPING));
    Assert.assertTrue (result.contains (Result.PI_ENABLE_OUTPUT_ESCAPING));

    return true; // TODO rewrite these strange test architecture
  }

  /**
   * Helpermethod to serialize a DOM-Node into a string.
   *
   * @param node
   *        a DOM-node
   * @return Serialized DOM-Document to String
   * @throws IOException
   */
  public static String serializeDOM2String (final Node node) throws IOException
  {

    final DOMSerializerImpl serializer = new DOMSerializerImpl ();

    return serializer.writeToString (node);
  }

  /**
   * Exceptionhandling
   *
   * @param ex
   */
  private static void handleException (final Exception ex)
  {

    log.error ("EXCEPTION: ");

    ex.printStackTrace ();

    if (ex instanceof TransformerConfigurationException)
    {

      final Throwable ex1 = ((TransformerConfigurationException) ex).getException ();

      log.error ("Internal exception: ", ex1);

      if (ex1 instanceof SAXException)
      {

        final Exception ex2 = ((SAXException) ex1).getException ();

        log.error ("Internal sub-exception: ", ex2);
      }
    }
  }
}
