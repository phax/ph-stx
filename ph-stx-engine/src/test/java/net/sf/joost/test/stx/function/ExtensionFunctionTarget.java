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
package net.sf.joost.test.stx.function;

import java.math.BigInteger;

/**
 * @version $Revision: 1.2 $ $Date: 2009/08/21 14:58:41 $
 * @author Oliver Becker
 */
public class ExtensionFunctionTarget
{
  private int m_nIntValue = Integer.MIN_VALUE;
  private Integer m_aIntegerValue = Integer.valueOf (Integer.MIN_VALUE);
  private BigInteger m_aBigIntegerValue = BigInteger.ZERO;

  public int getIntValue ()
  {
    return m_nIntValue;
  }

  public void setIntValue (final int intValue)
  {
    this.m_nIntValue = intValue;
  }

  public Integer getIntegerValue ()
  {
    return m_aIntegerValue;
  }

  public void setIntegerValue (final Integer integerValue)
  {
    this.m_aIntegerValue = integerValue;
  }

  public BigInteger getBigIntegerValue ()
  {
    return m_aBigIntegerValue;
  }

  public void setBigIntegerValue (final BigInteger bigIntegerValue)
  {
    this.m_aBigIntegerValue = bigIntegerValue;
  }

  public static BigInteger parseBigInt (final long value)
  {
    return BigInteger.valueOf (value);
  }

  public void exc ()
  {
    throw new MockExtensionFunctionException ();
  }
}
