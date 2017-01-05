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
import com.helger.commons.string.ToStringGenerator;

/**
 * If expression.<br>
 * <code>"if" "(" Expr ")" "then" ExprSingle "else" ExprSingle</code>
 *
 * @author Philip Helger
 */
public class STXIfExpression extends AbstractSTXExpression
{
  private final STXExpressionList m_aTestExprs;
  private final ISTXExpression m_aThenExpr;
  private final ISTXExpression m_aElseExpr;

  public STXIfExpression (@Nonnull final STXExpressionList aTestExprs,
                          @Nonnull final ISTXExpression aThenExpr,
                          @Nonnull final ISTXExpression aElseExpr)
  {
    m_aTestExprs = ValueEnforcer.notNull (aTestExprs, "TestExprs");
    m_aThenExpr = ValueEnforcer.notNull (aThenExpr, "ThenExpr");
    m_aElseExpr = ValueEnforcer.notNull (aElseExpr, "ElseExpr");
  }

  @Nonnull
  public STXExpressionList getTestExpressionList ()
  {
    return m_aTestExprs;
  }

  @Nonnull
  public ISTXExpression getThenExpression ()
  {
    return m_aThenExpr;
  }

  @Nonnull
  public ISTXExpression getElseExpression ()
  {
    return m_aElseExpr;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write ("if (");
    m_aTestExprs.writeTo (aWriter);
    aWriter.write (") then ");
    m_aThenExpr.writeTo (aWriter);
    aWriter.write (" else ");
    m_aElseExpr.writeTo (aWriter);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("testExprs", m_aTestExprs)
                                       .append ("thenExpr", m_aThenExpr)
                                       .append ("elseExpr", m_aElseExpr)
                                       .toString ();
  }
}
