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
import com.helger.stx.parser.ParserQName;

public class STXVariableReference extends AbstractSTXPrimaryExpression
{
  private final ParserQName m_aVarName;

  public STXVariableReference (@Nonnull final ParserQName aVarName)
  {
    m_aVarName = ValueEnforcer.notNull (aVarName, "VarName");
  }

  @Nonnull
  public ParserQName getVarName ()
  {
    return m_aVarName;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write ('$');
    aWriter.write (m_aVarName.getAsString ());
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("varName", m_aVarName).toString ();
  }
}
