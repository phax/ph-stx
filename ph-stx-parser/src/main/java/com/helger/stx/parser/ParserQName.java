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
package com.helger.stx.parser;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.hashcode.IHashCodeGenerator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

public class ParserQName implements Serializable
{
  private final String m_sPrefix;
  private final String m_sLocalName;
  // Status vars
  private transient int m_nHashCode = IHashCodeGenerator.ILLEGAL_HASHCODE;

  public ParserQName (@Nullable final String sPrefix, @Nonnull @Nonempty final String sLocalName)
  {
    ValueEnforcer.notEmpty (sLocalName, "sLocalName");
    m_sPrefix = sPrefix;
    m_sLocalName = sLocalName;
  }

  public boolean hasPrefix ()
  {
    return StringHelper.hasText (m_sPrefix);
  }

  @Nullable
  public String getPrefix ()
  {
    return m_sPrefix;
  }

  @Nonnull
  @Nonempty
  public String getLocalName ()
  {
    return m_sLocalName;
  }

  @Nonnull
  @Nonempty
  public String getAsString ()
  {
    return hasPrefix () ? m_sPrefix + ":" + m_sLocalName : m_sLocalName;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final ParserQName rhs = (ParserQName) o;
    return EqualsHelper.equals (m_sPrefix, rhs.m_sPrefix) && m_sLocalName.equals (rhs.m_sLocalName);
  }

  @Override
  public int hashCode ()
  {
    int ret = m_nHashCode;
    if (ret == IHashCodeGenerator.ILLEGAL_HASHCODE)
      ret = m_nHashCode = new HashCodeGenerator (this).append (m_sPrefix).append (m_sLocalName).getHashCode ();
    return ret;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).appendIfNotNull ("prefix", m_sPrefix)
                                       .append ("localName", m_sLocalName)
                                       .toString ();
  }

  @Nonnull
  public static ParserQName create (@Nonnull @Nonempty final String s1, @Nullable final String s2)
  {
    if (StringHelper.hasText (s2))
      return new ParserQName (s1, s2);
    return new ParserQName (null, s1);
  }
}
