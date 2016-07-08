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
package com.helger.stx.model.axisstep;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;
import com.helger.stx.model.nodetest.ISTXNodeTest;

public class XP2SingleStep extends AbstractXP2SingleStep
{
  private final EXP2Axis m_eAxis;
  private final ISTXNodeTest m_aNodeTest;

  public XP2SingleStep (@Nonnull final EXP2Axis eAxis, @Nonnull final ISTXNodeTest aNodeTest)
  {
    m_eAxis = ValueEnforcer.notNull (eAxis, "Axis");
    m_aNodeTest = ValueEnforcer.notNull (aNodeTest, "NodeTest");
  }

  @Nonnull
  public EXP2Axis getAxis ()
  {
    return m_eAxis;
  }

  @Nonnull
  public ISTXNodeTest getNodeTest ()
  {
    return m_aNodeTest;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    m_eAxis.writeTo (aWriter);
    m_aNodeTest.writeTo (aWriter);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("axis", m_eAxis).append ("nodeTest", m_aNodeTest).toString ();
  }
}
