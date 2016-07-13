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
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.stx.model.ISTXObject;

public enum ESTXAxis implements IHasID <String>, ISTXObject
{
  // Forward axis
  CHILD ("child", true, ESTXHorizontalDepth.ONE, ESTXVerticalDepth.SELF),
  DESCENDANT ("descendant", true, ESTXHorizontalDepth.UNBOUNDED, ESTXVerticalDepth.SELF),
  ATTRIBUTE ("attribute", true, ESTXHorizontalDepth.SELF, ESTXVerticalDepth.SELF),
  SELF ("self", true, ESTXHorizontalDepth.SELF, ESTXVerticalDepth.SELF),
  DESCENDANT_OR_SELF ("descendant-or-self", true, ESTXHorizontalDepth.UNBOUNDED, ESTXVerticalDepth.SELF),
  FOLLOWING_SIBLING ("following-sibling", true, ESTXHorizontalDepth.SELF, ESTXVerticalDepth.UNBOUNDED),
  FOLLOWING ("following", true, ESTXHorizontalDepth.UNBOUNDED, ESTXVerticalDepth.UNBOUNDED),
  NAMESPACE ("namespace", true, ESTXHorizontalDepth.SELF, ESTXVerticalDepth.SELF),
  // Reverse axis
  PARENT ("parent", false, ESTXHorizontalDepth.ONE, ESTXVerticalDepth.SELF),
  ANCESTOR ("ancestor", false, ESTXHorizontalDepth.UNBOUNDED, ESTXVerticalDepth.SELF),
  PRECEDING_SIBLING ("preceding-sibling", false, ESTXHorizontalDepth.SELF, ESTXVerticalDepth.UNBOUNDED),
  PRECEDING ("preceding", false, ESTXHorizontalDepth.UNBOUNDED, ESTXVerticalDepth.UNBOUNDED),
  ANCESTOR_OR_SELF ("ancestor-or-self", false, ESTXHorizontalDepth.UNBOUNDED, ESTXVerticalDepth.SELF);

  private final String m_sID;
  private final boolean m_bIsForward;
  private ESTXHorizontalDepth m_eHorizontalDepth;
  private ESTXVerticalDepth m_eVerticalDepth;

  private ESTXAxis (@Nonnull @Nonempty final String sID,
                    final boolean bIsForward,
                    @Nonnull final ESTXHorizontalDepth eHorizontalDepth,
                    @Nonnull final ESTXVerticalDepth eVerticalDepth)
  {
    m_sID = sID;
    m_bIsForward = bIsForward;
    m_eHorizontalDepth = eHorizontalDepth;
    m_eVerticalDepth = eVerticalDepth;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  public boolean isForwardAxis ()
  {
    return m_bIsForward;
  }

  public boolean isReverseAxis ()
  {
    return !m_bIsForward;
  }

  @Nonnull
  public ESTXHorizontalDepth getHorizontalDepth ()
  {
    return m_eHorizontalDepth;
  }

  @Nonnull
  public ESTXVerticalDepth getVerticalDepth ()
  {
    return m_eVerticalDepth;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write (m_sID);
    aWriter.write ("::");
  }

  @Nonnull
  public static ESTXAxis getFromIDOrThrow (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrThrow (ESTXAxis.class, sID);
  }
}
