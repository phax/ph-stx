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
package com.helger.stx.parser;

import java.io.Serializable;
import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.string.ToStringGenerator;
import com.helger.stx.STXSourceArea;
import com.helger.stx.STXSourceLocation;

/**
 * This class represents a simple node in the tree built by jjtree. It's a
 * customized version of the default JJTree Node.
 *
 * @author Philip Helger
 */
public class STXNode implements Node, Iterable <STXNode>, Serializable
{
  private final int m_nType;
  private STXNode m_aParent;
  private STXNode [] m_aChildren;
  private Object m_aValue;
  private String m_sText;
  private Token m_aFirstToken;
  private Token m_aLastToken;

  public STXNode (final int nType)
  {
    m_nType = nType;
  }

  public int getId ()
  {
    return m_nType;
  }

  public void jjtOpen ()
  {}

  public void jjtClose ()
  {}

  public void jjtSetParent (@Nullable final Node aNode)
  {
    m_aParent = (STXNode) aNode;
  }

  @Nullable
  public Node jjtGetParent ()
  {
    return m_aParent;
  }

  /**
   * Called from the highest index to the lowest index!
   */
  public void jjtAddChild (final Node aNode, final int nIndex)
  {
    if (m_aChildren == null)
      m_aChildren = new STXNode [nIndex + 1];
    else
      if (nIndex >= m_aChildren.length)
      {
        // Does not really occur here
        final STXNode [] aTmpArray = new STXNode [nIndex + 1];
        System.arraycopy (m_aChildren, 0, aTmpArray, 0, m_aChildren.length);
        m_aChildren = aTmpArray;
      }
    m_aChildren[nIndex] = (STXNode) aNode;
  }

  public STXNode jjtGetChild (final int nIndex)
  {
    return m_aChildren[nIndex];
  }

  @Nonnegative
  public int jjtGetNumChildren ()
  {
    return m_aChildren == null ? 0 : m_aChildren.length;
  }

  // The following 4 methods are required for JJTree option TRACK_TOKENS=true

  @Nullable
  public Token jjtGetFirstToken ()
  {
    return m_aFirstToken;
  }

  public void jjtSetFirstToken (@Nonnull final Token aFirstToken)
  {
    m_aFirstToken = aFirstToken;
  }

  @Nullable
  public Token jjtGetLastToken ()
  {
    return m_aLastToken;
  }

  public void jjtSetLastToken (@Nonnull final Token aLastToken)
  {
    m_aLastToken = aLastToken;
  }

  public void setValue (@Nullable final Object aValue)
  {
    m_aValue = aValue;
  }

  @Nullable
  public Object getValue ()
  {
    return m_aValue;
  }

  public void setText (@Nullable final String sText)
  {
    m_sText = sText;
  }

  public void appendText (@Nonnull final String sText)
  {
    if (m_sText == null)
      m_sText = sText;
    else
      m_sText += sText;
  }

  @Nullable
  public String getText ()
  {
    return m_sText;
  }

  public boolean hasText ()
  {
    return m_sText != null;
  }

  public int getNodeType ()
  {
    return m_nType;
  }

  @Nonnull
  public Iterator <STXNode> iterator ()
  {
    final ICommonsList <STXNode> aChildren = new CommonsArrayList<> (jjtGetNumChildren ());
    if (m_aChildren != null)
      for (final STXNode aChildNode : m_aChildren)
        if (aChildNode != null)
          aChildren.add (aChildNode);
    return aChildren.iterator ();
  }

  /**
   * @return The source location of this node. May be <code>null</code> if
   *         neither begin token nor end token is present.
   */
  @Nullable
  public STXSourceLocation getSourceLocation ()
  {
    final STXSourceArea aFirstTokenArea = m_aFirstToken == null ? null
                                                                : new STXSourceArea (m_aFirstToken.beginLine,
                                                                                     m_aFirstToken.beginColumn,
                                                                                     m_aFirstToken.endLine,
                                                                                     m_aFirstToken.endColumn);
    final STXSourceArea aLastTokenArea = m_aLastToken == null ? null
                                                              : new STXSourceArea (m_aLastToken.beginLine,
                                                                                   m_aLastToken.beginColumn,
                                                                                   m_aLastToken.endLine,
                                                                                   m_aLastToken.endColumn);
    if (aFirstTokenArea == null && aLastTokenArea == null)
      return null;
    return new STXSourceLocation (aFirstTokenArea, aLastTokenArea);
  }

  public void dump (final String prefix)
  {
    System.out.println (prefix + toString ());
    if (m_aChildren != null)
      for (final STXNode aChild : m_aChildren)
        if (aChild != null)
          aChild.dump (prefix + " ");
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("type", ParserSTXTreeConstants.jjtNodeName[m_nType])
                                       .appendIfNotNull ("parentType",
                                                         m_aParent == null ? null
                                                                           : ParserSTXTreeConstants.jjtNodeName[m_aParent.m_nType])
                                       .appendIfNotNull ("value", m_aValue)
                                       .appendIfNotNull ("text", m_sText)
                                       .append ("childCound", m_aChildren == null ? 0 : m_aChildren.length)
                                       .appendIfNotNull ("firstToken", m_aFirstToken)
                                       .appendIfNotNull ("lastToken", m_aLastToken)
                                       .getToString ();
  }
}
