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
package net.sf.joost.test.trax;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.sf.joost.trax.CTrAX;
import net.sf.joost.trax.TransformerFactoryImpl;

public class ExternalErrorListenerTest
{
  private Properties props;

  @Before
  public void setUp () throws Exception
  {
    props = System.getProperties ();
    System.setProperty (CTrAX.KEY_XSLT_FACTORY, "org.apache.xalan.processor.TransformerFactoryImpl");
  }

  @After
  public void tearDown () throws Exception
  {
    System.setProperties (props);
  }

  @Test
  public void testErrorListener () throws Exception
  {
    final String xmlFileString = "errorDocument.xml";
    final String stxFileString = "errorMapping.stx";

    final InputStream xmlStream = ExternalErrorListenerTest.class.getResourceAsStream (xmlFileString);
    final InputStream stxStream = ExternalErrorListenerTest.class.getResourceAsStream (stxFileString);

    final Source xmlSource = new StreamSource (xmlStream);
    final Source xsltSource = new StreamSource (stxStream);

    final TransformerFactory transFact = new TransformerFactoryImpl ();

    final Listener listener1 = new Listener ();
    transFact.setErrorListener (listener1);

    final Transformer trans = transFact.newTransformer (xsltSource);

    final Listener listener2 = new Listener ();
    trans.setErrorListener (listener2);

    try
    {
      trans.transform (xmlSource, new StreamResult (System.out));
      fail ();
    }
    catch (final TransformerException te)
    {
      System.err.println ("transformation aborted: " + te.getMessage ());
    }

    assertFalse (listener1.fatalCalled);
    assertFalse (listener1.errorCalled);
    assertFalse (listener1.warningCalled);

    assertTrue (listener2.fatalCalled);
    assertFalse (listener2.errorCalled); // depends on the actual XSLT impl
    assertFalse (listener2.warningCalled);
  }

  private static class Listener implements ErrorListener
  {
    boolean fatalCalled, errorCalled, warningCalled;

    public void error (final TransformerException exception) throws TransformerException
    {
      errorCalled = true;
      System.err.println ("[ERROR] " + exception.getMessage ());
      throw new TransformerException ("stylesheet aborted: " +
                                      exception.getMessage () +
                                      ": " +
                                      exception.getMessage ());
    }

    public void fatalError (final TransformerException exception) throws TransformerException
    {
      fatalCalled = true;
      System.err.println ("[FATAL] " + exception.getMessage ());
      throw new TransformerException ("stylesheet aborted: " +
                                      exception.getMessage () +
                                      ": " +
                                      exception.getMessage ());
    }

    public void warning (final TransformerException exception)
    {
      warningCalled = true;
      System.out.println ("[WARN ] " + exception.getMessage ());
    }
  }

}
