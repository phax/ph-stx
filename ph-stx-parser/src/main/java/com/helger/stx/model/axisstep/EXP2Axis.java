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

public enum EXP2Axis implements IHasID <String>, ISTXObject
{
  // Forward axis
  CHILD ("child", true, EXP2HorizontalDepth.ONE, EXP2VerticalDepth.SELF),
  DESCENDANT ("descendant", true, EXP2HorizontalDepth.UNBOUNDED, EXP2VerticalDepth.SELF),
  ATTRIBUTE ("attribute", true, EXP2HorizontalDepth.SELF, EXP2VerticalDepth.SELF),
  SELF ("self", true, EXP2HorizontalDepth.SELF, EXP2VerticalDepth.SELF),
  DESCENDANT_OR_SELF ("descendant-or-self", true, EXP2HorizontalDepth.UNBOUNDED, EXP2VerticalDepth.SELF),
  FOLLOWING_SIBLING ("following-sibling", true, EXP2HorizontalDepth.SELF, EXP2VerticalDepth.UNBOUNDED),
  FOLLOWING ("following", true, EXP2HorizontalDepth.UNBOUNDED, EXP2VerticalDepth.UNBOUNDED),
  NAMESPACE ("namespace", true, EXP2HorizontalDepth.SELF, EXP2VerticalDepth.SELF),
  // Reverse axis
  PARENT ("parent", false, EXP2HorizontalDepth.ONE, EXP2VerticalDepth.SELF),
  ANCESTOR ("ancestor", false, EXP2HorizontalDepth.UNBOUNDED, EXP2VerticalDepth.SELF),
  PRECEDING_SIBLING ("preceding-sibling", false, EXP2HorizontalDepth.SELF, EXP2VerticalDepth.UNBOUNDED),
  PRECEDING ("preceding", false, EXP2HorizontalDepth.UNBOUNDED, EXP2VerticalDepth.UNBOUNDED),
  ANCESTOR_OR_SELF ("ancestor-or-self", false, EXP2HorizontalDepth.UNBOUNDED, EXP2VerticalDepth.SELF);

  private final String m_sID;
  private final boolean m_bIsForward;
  private EXP2HorizontalDepth m_eHorizontalDepth;
  private EXP2VerticalDepth m_eVerticalDepth;

  private EXP2Axis (@Nonnull @Nonempty final String sID,
                    final boolean bIsForward,
                    @Nonnull final EXP2HorizontalDepth eHorizontalDepth,
                    @Nonnull final EXP2VerticalDepth eVerticalDepth)
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
  public EXP2HorizontalDepth getHorizontalDepth ()
  {
    return m_eHorizontalDepth;
  }

  @Nonnull
  public EXP2VerticalDepth getVerticalDepth ()
  {
    return m_eVerticalDepth;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write (m_sID);
    aWriter.write ("::");
  }

  @Nonnull
  public static EXP2Axis getFromIDOrThrow (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrThrow (EXP2Axis.class, sID);
  }
}
