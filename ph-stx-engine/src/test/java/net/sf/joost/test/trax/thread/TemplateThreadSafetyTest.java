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
package net.sf.joost.test.trax.thread;

import static org.junit.Assert.assertNull;

import java.io.StringWriter;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

/**
 * This test case demonstrates thread safety issue of the Templates instance The
 * probablilty of the failure grows with number of threads and size of the input
 * xml file. The instance is supposed to thread safe according to the JAXP API:
 * http://java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/Templates.html
 *
 * @author jason
 */
public class TemplateThreadSafetyTest
{
  // raise this number if needed to reproduce the thread-safety problem more
  // reliably
  private static final int MAX_THREADS = 500;

  @Test
  public void testTransform () throws TransformerConfigurationException, InterruptedException
  {
    final Exception [] failed = new Exception [1];
    final TransformerFactory factory = new net.sf.joost.trax.TransformerFactoryImpl ();
    factory.setURIResolver ( (href,
                              base) -> new StreamSource (TemplateThreadSafetyTest.class.getResourceAsStream (href)));
    final StreamSource style = new StreamSource (getClass ().getResourceAsStream ("style.stx"));
    final Templates templates = factory.newTemplates (style);
    final Runnable transformJob = () -> {
      try
      {
        final StreamSource input = new StreamSource (getClass ().getResourceAsStream ("input.xml"));
        final StringWriter writer = new StringWriter ();
        final StreamResult result = new StreamResult (writer);
        final Transformer transformer = templates.newTransformer ();
        System.out.println ("Transforming in " + Thread.currentThread ().getName ());
        transformer.transform (input, result);
        System.out.println ("Finished transforming in " + Thread.currentThread ().getName ());
      }
      catch (final TransformerException ex1)
      {
        ex1.printStackTrace ();
        failed[0] = ex1;
      }
      catch (final RuntimeException ex2)
      {
        ex2.printStackTrace ();
        failed[0] = ex2;
      }
    };
    final Thread [] threads = new Thread [MAX_THREADS];
    for (int i = 0; i < threads.length; i++)
    {
      threads[i] = new Thread (transformJob, "Thread #" + i);
      threads[i].start ();
    }
    for (final Thread thread : threads)
    {
      thread.join ();
    }
    assertNull (failed[0]);
  }

}
