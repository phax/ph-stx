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
package com.helger.stx.model;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

public class STXStringLiteral extends AbstractSTXLiteralExpression
{
  private static final char [] OLD_SQ = new char [] { '\'' };
  private static final char [][] NEW_SQ = new char [] [] { new char [] { '\'', '\'' } };

  private final String m_sValue;

  public STXStringLiteral (@Nonnull final String sValue)
  {
    m_sValue = ValueEnforcer.notNull (sValue, "Value");
  }

  @Nonnull
  public String getValue ()
  {
    return m_sValue;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    final boolean bNeedsEscaping = m_sValue.indexOf ('\'') >= 0;
    aWriter.write ('\'');
    if (bNeedsEscaping)
      StringHelper.replaceMultipleTo (m_sValue, OLD_SQ, NEW_SQ, aWriter);
    else
      aWriter.write (m_sValue);
    aWriter.write ('\'');
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("value", m_sValue).toString ();
  }
}
