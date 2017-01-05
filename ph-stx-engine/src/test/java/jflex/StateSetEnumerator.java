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
 * Enumerates the states of a StateSet.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class StateSetEnumerator
{

  private final static boolean DEBUG = false;

  private int index;
  private int offset;
  private long mask;

  private long [] bits;

  /**
   * creates a new StateSetEnumerator that is not yet associated with a
   * StateSet. hasMoreElements() and nextElement() will throw
   * NullPointerException when used before reset()
   */
  public StateSetEnumerator ()
  {}

  public StateSetEnumerator (final StateSet states)
  {
    reset (states);
  }

  public void reset (final StateSet states)
  {
    bits = states.bits;
    index = 0;
    offset = 0;
    mask = 1;
    while (index < bits.length && bits[index] == 0)
      index++;

    if (index >= bits.length)
      return;

    while (offset <= StateSet.MASK && ((bits[index] & mask) == 0))
    {
      mask <<= 1;
      offset++;
    }
  }

  private void advance ()
  {

    if (DEBUG)
      Out.dump ("Advancing, at start, index = " + index + ", offset = " + offset); //$NON-NLS-1$ //$NON-NLS-2$

    // cache fields in local variable for faster access
    int _index = this.index;
    int _offset = this.offset;
    long _mask = this.mask;
    final long [] _bits = this.bits;

    long bi = _bits[_index];

    do
    {
      _offset++;
      _mask <<= 1;
    } while (_offset <= StateSet.MASK && ((bi & _mask) == 0));

    if (_offset > StateSet.MASK)
    {
      final int length = _bits.length;

      do
        _index++;
      while (_index < length && _bits[_index] == 0);

      if (_index >= length)
      {
        this.index = length; // indicates "no more elements"
        return;
      }

      _offset = 0;
      _mask = 1;
      bi = _bits[_index];

      // terminates, because bi != 0
      while ((bi & _mask) == 0)
      {
        _mask <<= 1;
        _offset++;
      }
    }

    // write back cached values
    this.index = _index;
    this.mask = _mask;
    this.offset = _offset;
  }

  public boolean hasMoreElements ()
  {
    if (DEBUG)
      Out.dump ("hasMoreElements, index = " + index + ", offset = " + offset); //$NON-NLS-1$ //$NON-NLS-2$
    return index < bits.length;
  }

  public int nextElement ()
  {
    if (DEBUG)
      Out.dump ("nextElement, index = " + index + ", offset = " + offset); //$NON-NLS-1$ //$NON-NLS-2$
    final int x = (index << StateSet.BITS) + offset;
    advance ();
    return x;
  }

}
