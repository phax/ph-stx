/*
 * $Id: TestTestCases.java,v 1.2 2008/10/06 13:31:41 obecker Exp $
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

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Zubow
 */
public class TestTestCases
{
  @Before
  public void setUp ()
  {
    // set properties
    init ();
  }

  // *****************************************************************************
  // some Tests

  @Test
  public void testRunTests0 ()
  {

    final String xmlId = "data/flat.xml";

    assertTrue (TestCases.runTests0 (xmlId));
  }

  @Test
  public void testRunTests1 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests1 (xmlId, stxId));
  }

  @Test
  public void testRunTests2 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";
    final String outId = "testdata/out.xml";
    final String check = "testdata/resultflat.xml";

    boolean rValue = true;

    TestCases.runTests2 (xmlId, stxId, outId);

    try (final BufferedReader resultStream = new BufferedReader (new FileReader (outId));
        final BufferedReader checkStream = new BufferedReader (new FileReader (check)))
    {
      while (resultStream.ready () && checkStream.ready ())
      {
        if (!(resultStream.readLine ().equals (checkStream.readLine ())))
        {
          rValue = false;
          break;
        }
      }
    }
    catch (final FileNotFoundException fE)
    {
      fE.printStackTrace ();
    }
    catch (final IOException iE)
    {
      iE.printStackTrace ();
    }

    assertTrue (rValue);
  }

  @Test
  public void testRunTests3 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests3 (xmlId, stxId));
  }

  @Test
  public void testRunTests4 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests4 (xmlId, stxId));
  }

  public void testRunTests5 ()
  {

    final String xmlId1 = "data/flat.xml";
    final String xmlId2 = "data/flat2.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests5 (xmlId1, xmlId2, stxId));
  }

  @Test
  public void testRunTests6 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests6 (xmlId, stxId));
  }

  @Test
  public void testRunTests7 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests7 (xmlId, stxId));
  }

  @Test
  public void testRunTests8 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests8 (xmlId, stxId));
  }

  @Test
  public void testRunTests9 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";
    final String stxId2 = "data/indent.stx";

    assertTrue (TestCases.runTests9 (xmlId, stxId1, stxId2));
  }

  @Test
  public void testRunTests10 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests10 (xmlId, stxId));
  }

  @Test
  public void testRunTests11 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests11 (xmlId, stxId));
  }

  @Test
  public void testRunTests12 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests12 (xmlId, stxId));
  }

  @Test
  public void testRunTests13 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests13 (xmlId, stxId));
  }

  // exampleUseAssociated --> not yet implemented in joost public void
  @Test
  @Ignore ("Feature not supported")
  public void testRunTests14 ()
  {
    final String xmlId = "data/flat.xml";
    assertTrue (TestCases.runTests14 (xmlId));
  }

  // @todo : fixing this error
  @Test
  public void testRunTests15 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests15 (xmlId, stxId));
  }

  @Test
  public void testRunTests16 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId = "data/flat.stx";

    assertTrue (TestCases.runTests16 (xmlId, stxId));
  }

  @Test
  public void testRunTests18 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";
    final String stxId2 = "data/indent.stx";
    final String stxId3 = "data/copy.stx";

    assertTrue (TestCases.runTests18 (xmlId, stxId1, stxId2, stxId3));
  }

  // *****************************************************************************
  // some DOM-Tests

  /**
   * WORKS ONLY WITHOUT ENCODINGS
   */
  /*
   * public void testRunTests19() { String xmlId = "data/flat.xml"; String stxId
   * = "data/flat.stx"; //verification result for transformation of flat.xml
   * with flat.stx String VERIFY = "testdata/resultflatnoencoding.xml"; String
   * result = TestCases.runTests19(xmlId, stxId); System.out.println(result);
   * StringTokenizer tokenizer = new StringTokenizer(result, "\n"); boolean rv =
   * true; try { //verify result BufferedReader b = new BufferedReader(new
   * FileReader(VERIFY)); while( b.ready() && tokenizer.hasMoreElements() ) {
   * String x = (String)tokenizer.nextElement(); String y = b.readLine();
   * //System.out.println(x + "=" + y); if ( !(x.equals(y)) ) { rv = false;
   * System.err.println("diff --> " + x + "=" + y); break; } } } catch
   * (FileNotFoundException fE) { fE.printStackTrace(); } catch (IOException iE)
   * { iE.printStackTrace(); } assertTrue(rv); }
   */

  @Test
  public void testRunTests20 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";

    assertTrue (TestCases.runTests20 (xmlId, stxId1));
  }

  @Test
  public void testRunTests21 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";

    assertTrue (TestCases.runTests21 (xmlId, stxId1));
  }

  @Test
  public void testRunTests22 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";

    assertTrue (TestCases.runTests22 (xmlId, stxId1));
  }

  // *****************************************************************************
  // some SAX-Tests

  @Test
  public void testRunTests23 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";

    assertTrue (TestCases.runTests23 (xmlId, stxId1));
  }

  @Test
  public void testRunTests24 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";

    assertTrue (TestCases.runTests24 (xmlId, stxId1));
  }

  @Test
  public void testRunTests25 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";

    assertTrue (TestCases.runTests25 (xmlId, stxId1));
  }

  @Test
  public void testRunTests26 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";

    assertTrue (TestCases.runTests26 (xmlId, stxId1));
  }

  @Test
  public void testRunTests27 ()
  {

    final String xmlId = "data/flat.xml";
    final String stxId1 = "data/flat.stx";

    assertTrue (TestCases.runTests27 (xmlId, stxId1));
  }

  @Test
  public void testRunTests28 ()
  {

    final String xmlId = "data/flat.xml";

    assertTrue (TestCases.runTests28 (xmlId));
  }

  @Test
  public void testRunTests29 ()
  {

    final String xmlId = "data/flat.xml";

    assertTrue (TestCases.runTests29 (xmlId));
  }

  private void init ()
  {

    // log.info("starting TrAX-Tests ... ");

    System.out.println ("setting trax-Props");

    // setting joost as transformer
    final String key = "javax.xml.transform.TransformerFactory";
    final String value = "net.sf.joost.trax.TransformerFactoryImpl";

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
}
