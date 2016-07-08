/*
 * $Id: ProfilerTestCases.java,v 1.1 2007/07/15 15:32:29 obecker Exp $
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

package net.sf.joost.test.trax.profiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import net.sf.joost.emitter.StreamEmitter;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Processor;

/**
 * @author Zubow
 */
public class ProfilerTestCases
{

  // Logger instance named "ProfilerTestCases".
  static Logger log = Logger.getLogger (ProfilerTestCases.class);

  // *****************************************************************************
  // some Tests

  private final String xmlId = "test/middle2.xml";

  // small
  // private static int count = 42650;
  // middle
  // private static int count = 218248;
  // big
  // private static int count = 419327;
  // extrem
  private static int count = 4193270;

  public ProfilerTestCases ()
  {
    init ();
  }

  // ****************** X2StreamResult ************************

  /**
   * Show the Identity-transformation with StreamSource and StreamResult without
   * TrAX.
   */
  @Test
  public void testRunTests0 ()
  {

    long delta = exampleWithoutTrAX (xmlId);
    log.info ("1. Stream2Stream Transformation without TrAX length : " + delta + " ms");

    delta = exampleWithoutTrAX (xmlId);
    log.info ("2. Stream2Stream Transformation without TrAX length : " + delta + " ms");

  }

  /**
   * Show the Identity-transformation with StreamSource and StreamResult
   */
  @Test
  public void testRunTests1 ()
  {
    long delta = exampleStreamSourceAndResult (xmlId);
    log.info ("1. Stream2Stream Transformation length : " + delta + " ms");

    delta = exampleStreamSourceAndResult (xmlId);
    log.info ("2. Stream2Stream Transformation length : " + delta + " ms");

  }

  /**
   * Show the Identity-transformation with SaxSource and StreamResult
   */
  @Test
  public void testRunTests2 ()
  {
    long delta = exampleSAXSourceAndStreamResult ();

    log.info ("1. SAX2Stream Transformation length : " + delta + " ms");

    delta = exampleSAXSourceAndStreamResult ();
    log.info ("2. SAX2Stream Transformation length : " + delta + " ms");
  }

  /**
   * Show the Identity-transformation with DOMSource and StreamResult
   */
  @Test
  public void testRunTests3 ()
  {
    long delta = exampleDOMSourceAndStreamResult (xmlId);

    log.info ("1. DOM2Stream Transformation length : " + delta + " ms");

    delta = exampleDOMSourceAndStreamResult (xmlId);
    log.info ("2. DOM2Stream Transformation length : " + delta + " ms");
  }

  // ****************** X2SAXResult ************************

  /**
   * Show the Identity-transformation with StreamSource and SAXResult
   */
  @Test
  public void testRunTests4 ()
  {
    long delta = exampleStreamSourceAndSAXResult (xmlId);

    log.info ("1. Stream2SAX Transformation length : " + delta + " ms");

    delta = exampleStreamSourceAndSAXResult (xmlId);
    log.info ("2. Stream2SAX Transformation length : " + delta + " ms");
  }

  /**
   * Show the Identity-transformation with SAXSource and SAXResult
   */
  @Test
  public void testRunTests5 ()
  {
    long delta = exampleSAXSourceAndSAXResult ();

    log.info ("1. SAX2SAX Transformation length : " + delta + " ms");

    delta = exampleSAXSourceAndSAXResult ();
    log.info ("2. SAX2SAX Transformation length : " + delta + " ms");
  }

  /**
   * Show the Identity-transformation with DOMSource and SAXResult
   */
  @Test
  public void testRunTests6 ()
  {
    long delta = exampleDOMSourceAndSAXResult (xmlId);

    log.info ("1. DOM2SAX Transformation length : " + delta + " ms");

    delta = exampleDOMSourceAndSAXResult (xmlId);
    log.info ("2. DOM2SAX Transformation length : " + delta + " ms");
  }

  // ****************** X2DOMResult ************************

  /**
   * Show the Identity-transformation with StreamSource and DOMResult
   */
  @Test
  public void testRunTests7 ()
  {
    long delta = exampleStreamSourceAndDOMResult (xmlId);

    log.info ("1. Stream2DOM Transformation length : " + delta + " ms");

    delta = exampleStreamSourceAndDOMResult (xmlId);
    log.info ("2. Stream2DOM Transformation length : " + delta + " ms");
  }

  /**
   * Show the Identity-transformation with SAXSource and DOMResult
   */
  @Test
  public void testRunTests8 ()
  {
    long delta = exampleSAXSourceAndDOMResult ();

    log.info ("1. SAX2DOM Transformation length : " + delta + " ms");

    delta = exampleSAXSourceAndDOMResult ();
    log.info ("2. SAX2DOM Transformation length : " + delta + " ms");
  }

  /**
   * Show the Identity-transformation with DOMSource and DOMResult
   */
  @Test
  public void testRunTests9 ()
  {
    long delta = exampleDOMSourceAndDOMResult (xmlId);

    log.info ("1. DOM2SAX Transformation length : " + delta + " ms");

    delta = exampleDOMSourceAndDOMResult (xmlId);
    log.info ("2. DOM2SAX Transformation length : " + delta + " ms");
  }

  // ***********************************************
  private void init ()
  {

    // log.info("starting TrAX-Tests ... ");

    System.out.println ("setting trax-Props");

    // setting joost as transformer
    final String key = "javax.xml.transform.TransformerFactory";
    final String value = "net.sf.joost.trax.TransformerFactoryImpl";

    // test with Apache Xalan
    // value = "org.apache.xalan.processor.TransformerFactoryImpl";

    // log.debug("Setting key " + key + " to " + value);

    // setting xerces as parser
    final String key2 = "javax.xml.parsers.SAXParser";
    final String value2 = "org.apache.xerces.parsers.SAXParser";

    final String key3 = "org.xml.sax.driver";
    final String value3 = "org.apache.xerces.parsers.SAXParser";

    // log.debug("Setting key " + key2 + " to " + value2);

    final Properties props = System.getProperties ();

    props.put (key, value);
    props.put (key2, value2);
    props.put (key3, value3);

    System.setProperties (props);
  }

  // *******************************************************
  // Testimplementierungen

  public static long exampleWithoutTrAX (final String sourceID)
  {

    final String stxFile = "test/nomatch1.stx";

    // Create a new STX Processor object
    long delta = 0;
    try
    {
      final Processor pr = new Processor (new InputSource (stxFile), new ParseContext ());
      final StreamEmitter em = StreamEmitter.newXMLEmitter (new BufferedWriter (new FileWriter ("testdata/profiler/0.xml")));

      pr.setContentHandler (em);

      pr.setLexicalHandler (em);

      final long start = System.currentTimeMillis ();

      pr.parse (sourceID);

      delta = System.currentTimeMillis () - start;

    }
    catch (final IOException e)
    {
      e.printStackTrace (); // To change body of catch statement use Options |
                            // File Templates.
    }
    catch (final SAXException e)
    {
      e.printStackTrace (); // To change body of catch statement use Options |
                            // File Templates.
    }

    return delta;
  }

  /**
   * Show the Identity-transformation with StreamSource and StreamResult
   */
  public static long exampleStreamSourceAndResult (final String sourceID)
  {

    long delta = 0;

    try
    {
      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final BufferedReader reader = new BufferedReader (new FileReader (sourceID));
      final BufferedWriter writer = new BufferedWriter (new FileWriter ("testdata/profiler/1.xml"));

      final long start = System.currentTimeMillis ();

      transformer.transform (new StreamSource (reader), new StreamResult (writer));
      delta = System.currentTimeMillis () - start;

    }
    catch (final Exception e)
    {
      return 0;
    }
    return delta;
  }

  /**
   * Show the Identity-transformation with SAXSource and StreamResult
   */
  public static long exampleSAXSourceAndStreamResult ()
  {

    long delta = 0;

    try
    {
      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final BufferedWriter writer = new BufferedWriter (new FileWriter ("testdata/profiler/2.xml"));

      final long start = System.currentTimeMillis ();

      transformer.transform (new SAXSource (new MyXMLFilter (count), new InputSource ()), new StreamResult (writer));

      delta = System.currentTimeMillis () - start;

    }
    catch (final Exception e)
    {
      return 0;
    }
    return delta;
  }

  /**
   * Show the Identity-transformation with DOMSource and StreamResult
   */
  public static long exampleDOMSourceAndStreamResult (final String xmlId)
  {

    long delta = 0;

    try
    {

      // prepare
      final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();
      // Note you must always setNamespaceAware when building .xsl stylesheets
      dfactory.setNamespaceAware (true);
      final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();
      final Node doc = docBuilder.parse (new InputSource (xmlId));
      // fix errors
      final DOMSource domInSource = new DOMSource (doc);
      domInSource.setSystemId (xmlId);

      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final BufferedWriter writer = new BufferedWriter (new FileWriter ("testdata/profiler/3.xml"));

      final long start = System.currentTimeMillis ();

      transformer.transform (domInSource, new StreamResult (writer));

      delta = System.currentTimeMillis () - start;

    }
    catch (final Exception e)
    {
      return 0;
    }
    return delta;
  }

  /**
   * Show the Identity-transformation with StreamSource and SAXResult
   */
  public static long exampleStreamSourceAndSAXResult (final String sourceID)
  {

    long delta = 0;

    try
    {
      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final BufferedReader reader = new BufferedReader (new FileReader (sourceID));

      final long start = System.currentTimeMillis ();

      transformer.transform (new StreamSource (reader), new SAXResult (new MyContentHandler ()));
      delta = System.currentTimeMillis () - start;

    }
    catch (final Exception e)
    {
      return 0;
    }
    return delta;
  }

  /**
   * Show the Identity-transformation with SAXSource and SAXResult
   */
  public static long exampleSAXSourceAndSAXResult ()
  {

    long delta = 0;

    try
    {
      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final XMLFilter myFilter = new MyXMLFilter (count);
      // @todo : fixing
      // myFilter.setFeature("namespace.uri", true);

      final long start = System.currentTimeMillis ();

      transformer.transform (new SAXSource (myFilter, new InputSource ()), new SAXResult (new MyContentHandler ()));

      delta = System.currentTimeMillis () - start;

    }
    catch (final Exception e)
    {
      e.printStackTrace ();
      return 0;
    }
    return delta;
  }

  /**
   * Show the Identity-transformation with DOMSource and SAXResult
   */
  public static long exampleDOMSourceAndSAXResult (final String sourceID)
  {

    long delta = 0;

    try
    {

      // prepare
      final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();
      // Note you must always setNamespaceAware when building .xsl stylesheets
      dfactory.setNamespaceAware (true);
      final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();
      final Node doc = docBuilder.parse (new InputSource (sourceID));
      // fix errors
      final DOMSource domInSource = new DOMSource (doc);
      domInSource.setSystemId (sourceID);

      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final long start = System.currentTimeMillis ();

      transformer.transform (domInSource, new SAXResult (new MyContentHandler ()));
      delta = System.currentTimeMillis () - start;

    }
    catch (final Exception e)
    {
      return 0;
    }
    return delta;
  }

  /**
   * Show the Identity-transformation with StreamSource and DOMResult
   */
  public static long exampleStreamSourceAndDOMResult (final String sourceID)
  {

    long delta = 0;

    try
    {

      // prepare
      final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();
      // Note you must always setNamespaceAware when building .xsl stylesheets
      dfactory.setNamespaceAware (true);
      final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();
      final org.w3c.dom.Document outNode = docBuilder.newDocument ();

      final DOMResult result = new DOMResult (outNode);

      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final BufferedReader reader = new BufferedReader (new FileReader (sourceID));

      final long start = System.currentTimeMillis ();

      transformer.transform (new StreamSource (reader), result);
      delta = System.currentTimeMillis () - start;
      /*
       * Node nodeResult = result.getNode(); if(nodeResult != null) {
       * XMLSerializer serial = new XMLSerializer(); String resultString =
       * serial.writeToString(nodeResult);
       * log.info("*** print out DOM-document - DOMResult ***");
       * log.info(resultString); }
       */

    }
    catch (final Exception e)
    {
      return 0;
    }
    return delta;
  }

  /**
   * Show the Identity-transformation with SAXSource and DOMResult
   */
  public static long exampleSAXSourceAndDOMResult ()
  {

    long delta = 0;

    try
    {

      // prepare
      final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();
      // Note you must always setNamespaceAware when building .xsl stylesheets
      dfactory.setNamespaceAware (true);
      final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();
      final org.w3c.dom.Document outNode = docBuilder.newDocument ();

      final DOMResult result = new DOMResult (outNode);

      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final long start = System.currentTimeMillis ();

      transformer.transform (new SAXSource (new MyXMLFilter (count), new InputSource ()), result);

      delta = System.currentTimeMillis () - start;

      /*
       * Node nodeResult = result.getNode(); if(nodeResult != null) {
       * XMLSerializer serial = new XMLSerializer(); String resultString =
       * serial.writeToString(nodeResult);
       * log.info("*** print out DOM-document - DOMResult ***");
       * log.info(resultString); }
       */

    }
    catch (final Exception e)
    {
      return 0;
    }
    return delta;
  }

  /**
   * Show the Identity-transformation with DOMSource and DOMResult
   */
  public static long exampleDOMSourceAndDOMResult (final String sourceID)
  {

    long delta = 0;

    try
    {

      // prepare
      final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance ();
      // Note you must always setNamespaceAware when building .xsl stylesheets
      dfactory.setNamespaceAware (true);
      final DocumentBuilder docBuilder = dfactory.newDocumentBuilder ();
      final Node doc = docBuilder.parse (new InputSource (sourceID));
      // fix errors
      final DOMSource domInSource = new DOMSource (doc);
      domInSource.setSystemId (sourceID);

      final org.w3c.dom.Document outNode = docBuilder.newDocument ();
      final DOMResult result = new DOMResult (outNode);

      // register own ErrorListener for the TransformerFactory
      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      // Create a transformer for the stylesheet.
      final Transformer transformer = tfactory.newTransformer ();

      final long start = System.currentTimeMillis ();

      transformer.transform (domInSource, result);
      delta = System.currentTimeMillis () - start;

      /*
       * Node nodeResult = result.getNode(); if(nodeResult != null) {
       * XMLSerializer serial = new XMLSerializer(); String resultString =
       * serial.writeToString(nodeResult);
       * log.info("*** print out DOM-document - DOMResult ***");
       * log.info(resultString); }
       */

    }
    catch (final Exception e)
    {
      return 0;
    }
    return delta;
  }
}
