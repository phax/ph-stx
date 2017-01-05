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
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class CharSet
{

  final static int BITS = 6; // the number of bits to shift (2^6 = 64)
  final static int MOD = (1 << BITS) - 1; // modulus

  long bits[];

  private int numElements;

  public CharSet ()
  {
    bits = new long [1];
  }

  public CharSet (final int initialSize, final int character)
  {
    bits = new long [(initialSize >> BITS) + 1];
    add (character);
  }

  public void add (final int character)
  {
    resize (character);

    if ((bits[character >> BITS] & (1L << (character & MOD))) == 0)
      numElements++;

    bits[character >> BITS] |= (1L << (character & MOD));
  }

  private int nbits2size (final int nbits)
  {
    return ((nbits >> BITS) + 1);
  }

  private void resize (final int nbits)
  {
    final int needed = nbits2size (nbits);

    if (needed < bits.length)
      return;

    final long newbits[] = new long [Math.max (bits.length * 2, needed)];
    System.arraycopy (bits, 0, newbits, 0, bits.length);

    bits = newbits;
  }

  public boolean isElement (final int character)
  {
    final int index = character >> BITS;
    if (index >= bits.length)
      return false;
    return (bits[index] & (1L << (character & MOD))) != 0;
  }

  public CharSetEnumerator characters ()
  {
    return new CharSetEnumerator (this);
  }

  public boolean containsElements ()
  {
    return numElements > 0;
  }

  public int size ()
  {
    return numElements;
  }

  @Override
  public String toString ()
  {
    final CharSetEnumerator set = characters ();

    final StringBuilder result = new StringBuilder ("{");

    if (set.hasMoreElements ())
      result.append ("").append (set.nextElement ());

    while (set.hasMoreElements ())
    {
      final int i = set.nextElement ();
      result.append (", ").append (i);
    }

    result.append ("}");

    return result.toString ();
  }
}
