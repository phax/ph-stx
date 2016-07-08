/*
 * $Id: ExtensionFunctionTest.java,v 1.2 2009/08/21 14:58:41 obecker Exp $
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
package net.sf.joost.test.stx.function;

import net.sf.joost.trax.TransformerFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.2 $ $Date: 2009/08/21 14:58:41 $
 * @author Oliver Becker
 */
public class ExtensionFunctionTest extends TestCase
{
   private void doTransform(ExtensionFunctionTarget target, String stxName)
         throws TransformerException
   {
      TransformerFactory factory = new TransformerFactoryImpl();
      InputStream inputStream = ExtensionFunctionTest.class
            .getResourceAsStream(stxName);
      Source source = new StreamSource(inputStream);
      Transformer transformer = factory.newTransformer(source);
      transformer.setParameter("target", target);
      transformer.transform(
            new StreamSource(new ByteArrayInputStream("<x/>".getBytes())),
            new StreamResult(new OutputStream() {
               public void write(int b) throws IOException
               { }
            }));
   }

   public void testIntegerValues() throws TransformerException
   {
      ExtensionFunctionTarget target = new ExtensionFunctionTarget();
      doTransform(target, "extensionFunctionInt.stx");

      assertEquals(42, target.getIntValue());
      assertEquals(new Integer(42), target.getIntegerValue());
   }

   public void testEmptyIntegerValues() throws TransformerException
   {
      ExtensionFunctionTarget target = new ExtensionFunctionTarget();
      doTransform(target, "extensionFunctionNull.stx");

      assertEquals(0, target.getIntValue());
      assertNull(target.getIntegerValue());
   }

   public void testBigIntegerValue() throws TransformerException
   {
      ExtensionFunctionTarget target = new ExtensionFunctionTarget();
      doTransform(target, "extensionFunctionBigInt.stx");

      assertEquals(42, target.getBigIntegerValue().intValue());
   }

   public void testException()
   {
      ExtensionFunctionTarget target = new ExtensionFunctionTarget();
      try {
         doTransform(target, "extensionFunctionExc.stx");
         fail();
      }
      catch (TransformerException ex) {
         Throwable t = ex.getCause();
         while (t != null && !(t instanceof ExtensionFunctionException))
            t = t.getCause();
         assertTrue(t instanceof ExtensionFunctionException);
      }
   }

}
