/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.stx.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.junit.Test;

import com.helger.commons.io.stream.NonBlockingStringWriter;
import com.helger.stx.model.STXPath;

/**
 * Test class for class {@link STXReader}.
 *
 * @author Philip Helger
 */
public final class STXReaderTest
{
  private void _testOK (@Nonnull final String sData) throws IOException
  {
    _testOK (sData, sData);
  }

  private void _testOK (@Nonnull final String sData, @Nonnull final String sRecheck) throws IOException
  {
    final STXPath aParsed = STXReader.readFromString (sData);
    assertNotNull ("Failed to parse:\n" + sData, aParsed);

    // Only if no comment is contained
    final NonBlockingStringWriter aSW = new NonBlockingStringWriter ();
    aParsed.writeTo (aSW);
    assertEquals (sRecheck, aSW.getAsString ());
  }

  private void _testNotOK (@Nonnull final String sData)
  {
    assertNull (sData, STXReader.readFromString (sData));
  }

  @Test
  public void testOKFromSpecs () throws IOException
  {
    // From the specs
    // 2.5
    _testOK ("item");
    _testOK ("list/item");
    _testOK ("chapter//list/item");
    _testOK ("/root/list/*");
    _testOK ("pre:list[@id = 5]/pre:item");
    _testOK ("*[sf:position() = 1]");
    _testOK ("node()");
    _testOK ("text()");
    _testOK ("cdata()");
    _testOK ("processing-instruction()");
  }

  @Test
  public void testNotOKArbitrary ()
  {
    _testNotOK ("bla ge 0");
  }
}
