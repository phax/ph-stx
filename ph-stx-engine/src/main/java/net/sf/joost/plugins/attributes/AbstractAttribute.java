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
package net.sf.joost.plugins.attributes;

import java.util.Hashtable;

import com.helger.commons.hashcode.HashCodeGenerator;

/**
 * This class encapsulates base Attribute interface created on Mar 9, 2005
 *
 * @author fiykov
 * @version $Revision: 1.1 $
 * @since
 */
public abstract class AbstractAttribute
{
  /** attribute name */
  final String m_sName;

  /** attribute value */
  Object m_aValue;

  /** list of valid values if any */
  final String [] m_aValidValues;

  /**
   * Default constructor
   *
   * @param name
   * @param validValues
   */
  public AbstractAttribute (final String name,
                            final String [] validValues,
                            final String defVal,
                            final Hashtable <String, AbstractAttribute> col)
  {
    this.m_sName = name;
    this.m_aValidValues = validValues;
    col.put (name.toLowerCase (), this);
    setValue (defVal);
  }

  /**
   * Set value interface
   *
   * @param value
   */
  public void setValue (final String value) throws IllegalArgumentException
  {
    final String v = value.toLowerCase ();
    boolean flg = true;
    for (final String validValue : m_aValidValues)
    {
      flg = false;
      if (validValue.equals (v))
      {
        flg = true;
        break;
      }
    }
    if (flg)
      this.m_aValue = newValue (value);
    else
      throw new IllegalArgumentException ("setValue(" + value + "): not valid value!");
  }

  /**
   * Sub-classes implement this one
   *
   * @param value
   * @return
   */
  public abstract Object newValue (String value);

  /**
   * @return the value as string
   */
  public String getValueStr ()
  {
    return m_aValue.toString ();
  }

  /**
   * @return the value
   */
  public Object getValue ()
  {
    return m_aValue;
  }

  /**
   * Perform generic compare based on name
   */
  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final AbstractAttribute rhs = (AbstractAttribute) o;
    return m_sName.equals (rhs.m_sName);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sName).getHashCode ();
  }

  /**
   * Default printing based on name
   */
  @Override
  public String toString ()
  {
    return m_sName;
  }
}
