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
package jflex;

/**
 * Stores an interval of characters together with the character class A
 * character belongs to an interval, if its Unicode value is greater than or
 * equal to the Unicode value of <CODE>start</code> and smaller than or euqal to
 * the Unicode value of <CODE>end</code>. All characters of the interval must
 * belong to the same character class.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class CharClassInterval
{

  /**
   * The first character of the interval
   */
  int start;

  /**
   * The last character of the interval
   */
  int end;

  /**
   * The code of the class all characters of this interval belong to.
   */
  int charClass;

  /**
   * Creates a new CharClassInterval from <CODE>start</code> to <CODE>end</code>
   * that belongs to character class <CODE>charClass</code>.
   *
   * @param start
   *        The first character of the interval
   * @param end
   *        The last character of the interval
   * @param charClass
   *        The code of the class all characters of this interval belong to.
   */
  public CharClassInterval (final int start, final int end, final int charClass)
  {
    this.start = start;
    this.end = end;
    this.charClass = charClass;
  }

  /**
   * returns string representation of this class interval
   */
  @Override
  public String toString ()
  {
    return "[" + start + "-" + end + "=" + charClass + "]";
  }
}
