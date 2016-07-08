/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;

public class STXPathExpression extends AbstractSTXValueExpression
{
  private final ESTXPathOperator m_eOperator;
  private final ISTXExpression m_aExpression;

  public STXPathExpression (@Nonnull final ESTXPathOperator eOperator, @Nullable final ISTXExpression aExpr)
  {
    m_eOperator = ValueEnforcer.notNull (eOperator, "Operator");
    m_aExpression = aExpr;
  }

  @Nonnull
  public ESTXPathOperator getOperator ()
  {
    return m_eOperator;
  }

  @Nullable
  public ISTXExpression getExpression ()
  {
    return m_aExpression;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    m_eOperator.writeTo (aWriter);
    if (m_aExpression != null)
      m_aExpression.writeTo (aWriter);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("operator", m_eOperator)
                                       .appendIfNotNull ("expression", m_aExpression)
                                       .toString ();
  }
}
