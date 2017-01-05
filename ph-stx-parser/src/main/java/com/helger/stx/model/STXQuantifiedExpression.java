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
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.string.ToStringGenerator;

/**
 * Quantified expression.<br>
 * <code>("some" | "every") "$" VarName "in" ExprSingle ("," "$" VarName "in" ExprSingle)* "satisfies" ExprSingle</code>
 *
 * @author Philip Helger
 */
public class STXQuantifiedExpression extends AbstractSTXExpression
{
  private final ESTXQuantifiedExpressionType m_eType;
  private final ICommonsList <STXVarNameAndExpression> m_aClauses;
  private final ISTXExpression m_aSatisfyExpression;

  public STXQuantifiedExpression (@Nonnull final ESTXQuantifiedExpressionType eType,
                                  @Nonnull final Iterable <? extends STXVarNameAndExpression> aClauses,
                                  @Nonnull final ISTXExpression aSatisfyExpression)
  {
    ValueEnforcer.notNull (aClauses, "Clauses");
    m_eType = ValueEnforcer.notNull (eType, "Type");
    m_aClauses = new CommonsArrayList <> (aClauses);
    m_aSatisfyExpression = ValueEnforcer.notNull (aSatisfyExpression, "SatisfyExpression");
  }

  @Nonnull
  public ESTXQuantifiedExpressionType getType ()
  {
    return m_eType;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <STXVarNameAndExpression> getAllClauses ()
  {
    return m_aClauses.getClone ();
  }

  @Nonnull
  public ISTXExpression getSatisfyExpression ()
  {
    return m_aSatisfyExpression;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    m_eType.writeTo (aWriter);
    boolean bFirst = true;
    for (final STXVarNameAndExpression aClause : m_aClauses)
    {
      if (bFirst)
        bFirst = false;
      else
        aWriter.write (", ");
      aClause.writeTo (aWriter);
    }
    aWriter.write (" satisfies ");
    m_aSatisfyExpression.writeTo (aWriter);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("type", m_eType)
                                       .append ("clauses", m_aClauses)
                                       .append ("satisfyExpression", m_aSatisfyExpression)
                                       .toString ();
  }

}
