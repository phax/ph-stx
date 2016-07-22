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
package net.sf.joost.test.stx.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import org.junit.Test;

import net.sf.joost.trax.TransformerFactoryImpl;

/**
 * @version $Revision: 1.2 $ $Date: 2009/08/21 14:58:41 $
 * @author Oliver Becker
 */
public class ExtensionFunctionTest
{
  private void doTransform (final ExtensionFunctionTarget target, final String stxName) throws TransformerException
  {
    final TransformerFactory factory = new TransformerFactoryImpl ();
    final InputStream inputStream = ExtensionFunctionTest.class.getResourceAsStream (stxName);
    final Source source = new StreamSource (inputStream);
    final Transformer transformer = factory.newTransformer (source);
    transformer.setParameter ("target", target);
    transformer.transform (new StreamSource (new ByteArrayInputStream ("<x/>".getBytes ())),
                           new StreamResult (new OutputStream ()
                           {
                             @Override
                             public void write (final int b) throws IOException
                             {}
                           }));
  }

  @Test
  public void testIntegerValues () throws TransformerException
  {
    final ExtensionFunctionTarget target = new ExtensionFunctionTarget ();
    doTransform (target, "extensionFunctionInt.stx");

    assertEquals (42, target.getIntValue ());
    assertEquals (new Integer (42), target.getIntegerValue ());
  }

  @Test
  public void testEmptyIntegerValues () throws TransformerException
  {
    final ExtensionFunctionTarget target = new ExtensionFunctionTarget ();
    doTransform (target, "extensionFunctionNull.stx");

    assertEquals (0, target.getIntValue ());
    assertNull (target.getIntegerValue ());
  }

  @Test
  public void testBigIntegerValue () throws TransformerException
  {
    final ExtensionFunctionTarget target = new ExtensionFunctionTarget ();
    doTransform (target, "extensionFunctionBigInt.stx");

    assertEquals (42, target.getBigIntegerValue ().intValue ());
  }

  @Test
  public void testException ()
  {
    final ExtensionFunctionTarget target = new ExtensionFunctionTarget ();
    try
    {
      doTransform (target, "extensionFunctionExc.stx");
      fail ();
    }
    catch (final TransformerException ex)
    {
      Throwable t = ex.getCause ();
      while (t != null && !(t instanceof MockExtensionFunctionException))
        t = t.getCause ();
      assertTrue (t instanceof MockExtensionFunctionException);
    }
  }

}
