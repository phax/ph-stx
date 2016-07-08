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
package com.helger.stx.model.types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.hashcode.HashCodeGenerator;

public class XPathType
{
  private final QName m_aName;
  private final XPathType m_aParentType;

  public XPathType (@Nonnull final QName aName, @Nullable final XPathType aParentType)
  {
    m_aName = ValueEnforcer.notNull (aName, "Name");
    m_aParentType = aParentType;
  }

  public boolean isBuiltIn ()
  {
    return false;
  }

  @Nonnull
  public QName getName ()
  {
    return m_aName;
  }

  public boolean isRootType ()
  {
    return m_aParentType == null;
  }

  public boolean hasParentType ()
  {
    return m_aParentType != null;
  }

  @Nullable
  public XPathType getParentType ()
  {
    return m_aParentType;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final XPathType rhs = (XPathType) o;
    // parent type identity check is fine!
    return m_aName.equals (rhs.m_aName) && m_aParentType == rhs.m_aParentType;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aName)
                                       .append (m_aParentType == null ? null : m_aParentType.getName ())
                                       .getHashCode ();
  }
}
