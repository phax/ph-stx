/*
 * $Id: TemplateThreadSafetyTest.java,v 1.3 2008/12/07 19:14:49 obecker Exp $
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
 * Contributor(s): jason.
 */

package net.sf.joost.test.trax.thread;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

/**
 * This test case demonstrates thread safety issue of the Templates instance The
 * probablilty of the failure grows with number of threads and size of the input
 * xml file. The instance is supposed to thread safe according to the JAXP API:
 * http://java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/Templates.html
 *
 * @author jason
 */
public class TemplateThreadSafetyTest extends TestCase
{
   // raise this number if needed to reproduce the thread-safety problem more
   // reliably
   private static final int MAX_THREADS = 500;

   public TemplateThreadSafetyTest(String testName)
   {
      super(testName);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   public void testTransform() throws IOException,
         TransformerConfigurationException, InterruptedException
   {
      final Exception[] failed = new Exception[1];
      final TransformerFactory factory = new net.sf.joost.trax.TransformerFactoryImpl();
      factory.setURIResolver(new URIResolver() {

         public Source resolve(String href, String base)
               throws TransformerException
         {
            return new StreamSource(
                  TemplateThreadSafetyTest.class.getResourceAsStream(href));
         }
      });
      final StreamSource style = new StreamSource(getClass()
            .getResourceAsStream("style.stx"));
      final Templates templates = factory.newTemplates(style);
      Runnable transformJob = new Runnable() {
         public void run()
         {
            try {
               StreamSource input = new StreamSource(getClass()
                     .getResourceAsStream("input.xml"));
               StringWriter writer = new StringWriter();
               StreamResult result = new StreamResult(writer);
               Transformer transformer = templates.newTransformer();
               System.out.println("Transforming in "
                     + Thread.currentThread().getName());
               transformer.transform(input, result);
               System.out.println("Finished transforming in "
                     + Thread.currentThread().getName());
            }
            catch (TransformerException ex) {
               ex.printStackTrace();
               failed[0] = ex;
            }
            catch (RuntimeException ex) {
               ex.printStackTrace();
               failed[0] = ex;
            }
         }
      };
      Thread[] threads = new Thread[MAX_THREADS];
      for (int i = 0; i < threads.length; i++) {
         threads[i] = new Thread(transformJob, "Thread #" + i);
         threads[i].start();
      }
      for (int i = 0; i < threads.length; i++) {
         threads[i].join();
      }
      assertNull(failed[0]);
   }

}
