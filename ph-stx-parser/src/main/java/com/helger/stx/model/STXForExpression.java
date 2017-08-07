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
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.string.ToStringGenerator;

/**
 * For expression.<br>
 * <code>"for" "$" VarName "in" ExprSingle ("," "$" VarName "in" ExprSingle)* "return" ExprSingle</code>
 *
 * @author Philip Helger
 */
public class STXForExpression extends AbstractSTXExpression
{
  private final ICommonsList <STXVarNameAndExpression> m_aForClauses;
  private final ISTXExpression m_aReturnExpression;

  public STXForExpression (@Nonnull final Iterable <? extends STXVarNameAndExpression> aForClauses,
                           @Nonnull final ISTXExpression aReturnExpression)
  {
    ValueEnforcer.notNull (aForClauses, "ForClauses");
    ValueEnforcer.notNull (aReturnExpression, "ReturnExpression");
    m_aForClauses = new CommonsArrayList<> (aForClauses);
    m_aReturnExpression = aReturnExpression;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write ("for ");
    boolean bFirst = true;
    for (final STXVarNameAndExpression aForClause : m_aForClauses)
    {
      if (bFirst)
        bFirst = false;
      else
        aWriter.write (", ");
      aForClause.writeTo (aWriter);
    }
    aWriter.write (" return ");
    m_aReturnExpression.writeTo (aWriter);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <STXVarNameAndExpression> getAllForClauses ()
  {
    return m_aForClauses.getClone ();
  }

  @Nonnull
  public ISTXExpression getReturnExpression ()
  {
    return m_aReturnExpression;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("forClauses", m_aForClauses)
                                       .append ("returnExpression", m_aReturnExpression)
                                       .getToString ();
  }
}
