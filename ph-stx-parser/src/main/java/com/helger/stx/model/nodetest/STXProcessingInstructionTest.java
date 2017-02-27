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
package com.helger.stx.model.nodetest;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.string.ToStringGenerator;
import com.helger.stx.model.ISTXObject;

public class STXProcessingInstructionTest extends AbstractSTXKindTest
{
  private final ISTXObject m_aPITarget;

  public STXProcessingInstructionTest ()
  {
    this (null);
  }

  public STXProcessingInstructionTest (@Nullable final ISTXObject aPITarget)
  {
    m_aPITarget = aPITarget;
  }

  @Nullable
  public ISTXObject getPITarget ()
  {
    return m_aPITarget;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write ("processing-instruction(");
    if (m_aPITarget != null)
      m_aPITarget.writeTo (aWriter);
    aWriter.write (')');
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).appendIfNotNull ("PITarget", m_aPITarget).getToString ();
  }
}
