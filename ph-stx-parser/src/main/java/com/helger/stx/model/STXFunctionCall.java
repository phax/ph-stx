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
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.string.ToStringGenerator;
import com.helger.stx.parser.ParserQName;

public class STXFunctionCall extends AbstractSTXPrimaryExpression
{
  private final ParserQName m_aFunctionName;
  private final ICommonsList <ISTXExpression> m_aExpressions;

  public STXFunctionCall (@Nonnull final ParserQName aFunctionName,
                          @Nullable final Iterable <? extends ISTXExpression> aExpressionList)
  {
    m_aFunctionName = ValueEnforcer.notNull (aFunctionName, "FunctionName");
    m_aExpressions = new CommonsArrayList<> (aExpressionList);
  }

  @Nonnull
  public ParserQName getFunctionName ()
  {
    return m_aFunctionName;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <ISTXExpression> getAllExpressions ()
  {
    return m_aExpressions.getClone ();
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write (m_aFunctionName.getAsString ());
    aWriter.write ('(');
    boolean bFirst = true;
    for (final ISTXExpression aExpression : m_aExpressions)
    {
      if (bFirst)
        bFirst = false;
      else
        aWriter.write (", ");
      aExpression.writeTo (aWriter);
    }
    aWriter.write (')');
  }

  @Override
  @Nonnull
  public String toString ()
  {
    return new ToStringGenerator (this).append ("functionName", m_aFunctionName)
                                       .append ("expressions", m_aExpressions)
                                       .getToString ();
  }
}
