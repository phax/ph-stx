/**
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.1 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is: this file
 *
 *  The Initial Developer of the Original Code is Oliver Becker.
 *
 *  Portions created by Philip Helger
 *  are Copyright (C) 2016-2017 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.stx.helpers;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.xml.sax.Attributes;

/**
 * Mutable attributes implementation.
 */
public final class MutableAttributesImpl implements IMutableAttributes
{
  private int m_nLength;
  private int m_nMax;
  private String [] m_aUris;
  private String [] m_aLocalNames;
  private String [] m_aQNames;
  private String [] m_aValues;
  private String [] m_aTypes;

  public MutableAttributesImpl (@Nonnull final Attributes attributes)
  {
    this (attributes, attributes.getLength ());
  }

  public MutableAttributesImpl (final Attributes attributes, @Nonnegative final int length)
  {
    m_nLength = length;
    m_nMax = m_nLength + 2; // _max is initially at least 2
    m_aUris = new String [m_nMax];
    m_aLocalNames = new String [m_nMax];
    m_aQNames = new String [m_nMax];
    m_aValues = new String [m_nMax];
    m_aTypes = new String [m_nMax];

    for (int n = m_nLength; n-- > 0;)
    {
      m_aUris[n] = attributes.getURI (n);
      m_aLocalNames[n] = attributes.getLocalName (n);
      m_aQNames[n] = attributes.getQName (n);
      m_aValues[n] = attributes.getValue (n);
      m_aTypes[n] = attributes.getType (n);
    }
  }

  @CheckForSigned
  public int getIndex (final String uri, final String localName)
  {
    for (int n = m_nLength; n-- > 0;)
      if (localName.equals (m_aLocalNames[n]) && uri.equals (m_aUris[n]))
        return n;
    return -1;
  }

  @Nonnegative
  public int getLength ()
  {
    return m_nLength;
  }

  public String getLocalName (final int index)
  {
    return m_aLocalNames[index];
  }

  public String getQName (final int index)
  {
    return m_aQNames[index];
  }

  public String getType (final int index)
  {
    return m_aTypes[index];
  }

  @Nullable
  public String getType (final String qName)
  {
    for (int n = m_nLength; n-- > 0;)
      if (qName.equals (m_aQNames[n]))
        return m_aTypes[n];
    return null;
  }

  @Nullable
  public String getType (final String uri, final String localName)
  {
    for (int n = m_nLength; n-- > 0;)
    {
      if (localName.equals (m_aLocalNames[n]) && m_aUris[n].equals (uri))
        return m_aTypes[n];
    }
    return null;
  }

  public String getURI (final int index)
  {
    return m_aUris[index];
  }

  public String getValue (final int index)
  {
    return m_aValues[index];
  }

  @Nullable
  public String getValue (final String qName)
  {
    for (int n = m_nLength; n-- > 0;)
    {
      if (qName.equals (m_aQNames[n]))
        return m_aValues[n];
    }
    return null;
  }

  @Nullable
  public String getValue (final String uri, final String localName)
  {
    for (int n = m_nLength; n-- > 0;)
    {
      if (m_aLocalNames[n].equals (localName) && m_aUris[n].equals (uri))
        return m_aValues[n];
    }
    return null;
  }

  @CheckForSigned
  public int getIndex (final String qName)
  {
    for (int n = m_nLength; n-- > 0;)
    {
      if (qName.equals (m_aQNames[n]))
        return n;
    }
    return -1;
  }

  public void setValue (final int index, final String value)
  {
    m_aValues[index] = value;
  }

  public void addAttribute (final String uri,
                            final String lName,
                            final String qName,
                            final String type,
                            final String value)
  {
    if (m_nLength == m_nMax)
    {
      m_nMax *= 2;

      final String [] uris = new String [m_nMax + 1];
      final String [] lNames = new String [m_nMax + 1];
      final String [] qNames = new String [m_nMax + 1];
      final String [] values = new String [m_nMax + 1];
      final String [] types = new String [m_nMax + 1];

      System.arraycopy (m_aUris, 0, uris, 0, m_nLength);
      System.arraycopy (m_aLocalNames, 0, lNames, 0, m_nLength);
      System.arraycopy (m_aQNames, 0, qNames, 0, m_nLength);
      System.arraycopy (m_aValues, 0, values, 0, m_nLength);
      System.arraycopy (m_aTypes, 0, types, 0, m_nLength);

      m_aUris = uris;
      m_aLocalNames = lNames;
      m_aQNames = qNames;
      m_aValues = values;
      m_aTypes = types;
    }

    m_aUris[m_nLength] = uri;
    m_aLocalNames[m_nLength] = lName;
    m_aQNames[m_nLength] = qName;
    m_aValues[m_nLength] = value;
    m_aTypes[m_nLength] = type;

    m_nLength++;
  }
}
