/**
 * Copyright (C) 2016 Philip Helger (www.helger.com)
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
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;

public class STXNumericLiteral extends AbstractSTXLiteralExpression
{
  private final BigDecimal m_aValue;

  public STXNumericLiteral (@Nonnull final BigInteger aValue)
  {
    this (new BigDecimal (aValue));
  }

  public STXNumericLiteral (@Nonnull final BigDecimal aValue)
  {
    m_aValue = ValueEnforcer.notNull (aValue, "Value");
  }

  @Nonnull
  public BigDecimal getValue ()
  {
    return m_aValue;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write (m_aValue.toString ());
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("value", m_aValue).toString ();
  }
}
