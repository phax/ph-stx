/*
 * $Id: StxTest.java,v 1.3 2008/10/06 13:31:42 obecker Exp $
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
package net.sf.joost.test.stx;

import net.sf.joost.trax.TrAXConstants;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @version $Revision: 1.3 $ $Date: 2008/10/06 13:31:42 $
 * @author Oliver Becker
 */
public class StxTest extends TestCase
{
   private static TransformerFactory factory;
   static {
      System.setProperty("javax.xml.transform.TransformerFactory",
                         "net.sf.joost.trax.TransformerFactoryImpl");
      factory = TransformerFactory.newInstance();
      // The default Xalan of Java 1.4 doesn't work
      // Requires Saxon to be in the classpath
      factory.setAttribute(net.sf.joost.trax.TrAXConstants.KEY_XSLT_FACTORY,
                           "net.sf.saxon.TransformerFactoryImpl");
   }

   public StxTest(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return new TestSuite(StxTest.class);
   }

   public void testExamplesWithTrax()
   {
      System.out.println("Start testExamplesWithTrax");
      boolean testResult = true;

      File testDir = new File("../test");
      String[] resultFiles = testDir.list(
         new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
               return name.endsWith(".res");
            }
         }
      );
      for (int i = 0; i < resultFiles.length; i++) {
         String fName = resultFiles[i];
         String baseName = fName.substring(0, fName.indexOf(".res"));
         System.out.println(baseName);

         File resFile = new File(testDir, fName);
         File stxFile = new File(testDir, baseName + ".stx");
         File parFile = new File(testDir, baseName + ".par");
         File xmlFile = new File(testDir, baseName + ".xml");
         if (!xmlFile.exists()) {
            int hyphen = fName.indexOf('-');
            if (hyphen != -1)
               xmlFile = new File(testDir,
                                  fName.substring(0, hyphen) + ".xml");
         }

         try {
            Transformer t = factory.newTransformer(new StreamSource(stxFile));

            if (parFile.exists()) {
               String params =
                  (new BufferedReader(new FileReader(parFile))).readLine();
               StringTokenizer st1 = new StringTokenizer(params);
               while (st1.hasMoreTokens()) {
                  String parSpec = st1.nextToken();
                  int equalsIndex = parSpec.indexOf('=');
                  if (equalsIndex > 0)
                     t.setParameter(parSpec.substring(0, equalsIndex),
                                    parSpec.substring(equalsIndex+1));
                  else if ("-doe".equals(parSpec)) {
                     t.setOutputProperty(
                        TrAXConstants.OUTPUT_KEY_SUPPORT_DISABLE_OUTPUT_ESCAPING,
                        "yes");
                  }
               }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            t.transform(new StreamSource(xmlFile),
                        new StreamResult(baos));
            BufferedReader brExpected =
               new BufferedReader(new FileReader(resFile));
            BufferedReader brReceived =
               new BufferedReader(new StringReader(baos.toString()));

            String lineExpected = brExpected.readLine();
            String lineReceived = brReceived.readLine();
            int lineno = 1;
            while (lineExpected != null && lineReceived != null) {
               if (!lineExpected.equals(lineReceived)) {
                  System.err.println("In line " + lineno);
                  System.err.println("Exp:  " + lineExpected);
                  System.err.println("Rec:  " + lineReceived);
                  testResult = false;
                  break;
               }
               lineExpected = brExpected.readLine();
               lineReceived = brReceived.readLine();
               lineno++;
            }
            if (lineExpected == null && lineReceived != null) {
               System.err.println("Expected result has extra lines");
               testResult = false;
            }
            else if (lineExpected != null && lineReceived == null) {
               System.err.println("Received result has extra lines");
               testResult = false;
            }
         }
         catch (TransformerException e) {
            System.err.println(e);
            testResult = false;
         }
         catch (IOException e) {
            System.err.println(e);
            testResult = false;
         }
      }
      assertTrue(testResult);
      System.out.println("Done testExamplesWithTrax");
   }
}
