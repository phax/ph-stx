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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package jflex;

/**
 * Enumerator for the elements of a CharSet. Does not implement
 * java.util.Enumeration, but supports the same protocol.
 * 
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class CharSetEnumerator
{

  private int index;
  private int offset;
  private long mask = 1;

  private CharSet set;

  public CharSetEnumerator (final CharSet characters)
  {
    set = characters;

    while (index < set.bits.length && set.bits[index] == 0)
      index++;

    if (index >= set.bits.length)
      return;

    while (offset <= CharSet.MOD && ((set.bits[index] & mask) == 0))
    {
      mask <<= 1;
      offset++;
    }
  }

  private void advance ()
  {
    do
    {
      offset++;
      mask <<= 1;
    } while (offset <= CharSet.MOD && ((set.bits[index] & mask) == 0));

    if (offset > CharSet.MOD)
    {
      do
        index++;
      while (index < set.bits.length && set.bits[index] == 0);

      if (index >= set.bits.length)
        return;

      offset = 0;
      mask = 1;

      while (offset <= CharSet.MOD && ((set.bits[index] & mask) == 0))
      {
        mask <<= 1;
        offset++;
      }
    }
  }

  public boolean hasMoreElements ()
  {
    return index < set.bits.length;
  }

  public int nextElement ()
  {
    final int x = (index << CharSet.BITS) + offset;
    advance ();
    return x;
  }

}
