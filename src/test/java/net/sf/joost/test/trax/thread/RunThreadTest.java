/*
 * $Id: RunThreadTest.java,v 1.1 2007/07/15 15:32:35 obecker Exp $
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

package net.sf.joost.test.trax.thread;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Thread-safety test
 * 
 * @author Zubow
 */
public class RunThreadTest
{

  private static String xmlId = "test/flat.xml";
  private static String stxId = "test/flat.stx";
  private static String outId = "testdata/out.html";

  public static void main (final String args[])
  {

    try
    {

      settingProps ();

      // Create a transform factory instance.
      final String tProp = System.getProperty ("javax.xml.transform.TransformerFactory");

      final TransformerFactory tfactory = TransformerFactory.newInstance ();

      final InputStream stxIS = new BufferedInputStream (new FileInputStream (stxId));
      final StreamSource stxSource = new StreamSource (stxIS);
      stxSource.setSystemId (stxId);

      final Templates templates = tfactory.newTemplates (stxSource);

      final Transformer transformer = templates.newTransformer ();

      // init threads - sharing Transformer
      final TransformerThread firstThread = new TransformerThread (transformer, "first");
      final TransformerThread secondThread = new TransformerThread (transformer, "second");

      // init threads - sharing Templates
      // TransformerThread firstThread = new TransformerThread(templates,
      // "first");
      // TransformerThread secondThread = new TransformerThread(templates,
      // "second");

      // init threads - sharing transformerfactory
      // TransformerThread firstThread = new TransformerThread(tfactory,
      // "first");
      // TransformerThread secondThread = new TransformerThread(tfactory,
      // "second");

      // starting
      firstThread.start ();
      secondThread.start ();

    }
    catch (final FileNotFoundException fE)
    {
      fE.printStackTrace ();
    }
    catch (final TransformerConfigurationException tE)
    {
      tE.printStackTrace ();
    }

  }

  private static void settingProps ()
  {

    // setting joost as transformer
    final String key = "javax.xml.transform.TransformerFactory";
    final String value = "net.sf.joost.trax.TransformerFactoryImpl";

    // setting xerces as parser
    final String key2 = "javax.xml.parsers.SAXParser";
    final String value2 = "org.apache.xerces.parsers.SAXParser";

    // setting new
    final String key3 = "org.xml.sax.driver";
    final String value3 = "org.apache.xerces.parsers.SAXParser";

    final Properties props = System.getProperties ();
    props.put (key, value);
    props.put (key2, value2);
    props.put (key3, value3);

    System.setProperties (props);
  }
}
