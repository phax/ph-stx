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
 * Simple pair of integers. Used in NFA to represent a partial NFA by its start
 * and end state.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
final class IntPair
{

  int start;
  int end;

  IntPair (final int start, final int end)
  {
    this.start = start;
    this.end = end;
  }

  @Override
  public int hashCode ()
  {
    return end + (start << 8);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o instanceof IntPair)
    {
      final IntPair p = (IntPair) o;
      return start == p.start && end == p.end;
    }
    return false;
  }

  @Override
  public String toString ()
  {
    return "(" + start + "," + end + ")";
  }
}
