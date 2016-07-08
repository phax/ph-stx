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

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;
import com.helger.stx.parser.ParserQName;

public class STXSingleType implements ISTXObject
{
  private final ParserQName m_aAtomicType;
  private final boolean m_bEmptySequenceAllowed;

  public STXSingleType (@Nonnull final ParserQName aAtomicType, final boolean bEmptySequenceAllowed)
  {
    m_aAtomicType = ValueEnforcer.notNull (aAtomicType, "AtomicType");
    m_bEmptySequenceAllowed = bEmptySequenceAllowed;
  }

  @Nonnull
  public ParserQName getAtomicType ()
  {
    return m_aAtomicType;
  }

  public boolean isEmptySequenceAllowed ()
  {
    return m_bEmptySequenceAllowed;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write (m_aAtomicType.getAsString ());
    if (m_bEmptySequenceAllowed)
      aWriter.write ('?');
  }

  @Override
  @Nonnull
  public String toString ()
  {
    return new ToStringGenerator (this).append ("atomicType", m_aAtomicType)
                                       .append ("emptySequenceAllowed", m_bEmptySequenceAllowed)
                                       .toString ();
  }
}
