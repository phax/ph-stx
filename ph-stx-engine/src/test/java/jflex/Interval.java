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
 * An interval of characters with basic operations.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class Interval
{

  /* start and end of the interval */
  public int start, end;

  /**
   * Constuct a new interval from <code>start</code> to <code>end</code>.
   *
   * @param start
   *        first character the interval should contain
   * @param end
   *        last character the interval should contain
   */
  public Interval (final int start, final int end)
  {
    this.start = start;
    this.end = end;
  }

  /**
   * Copy constructor
   */
  public Interval (final Interval other)
  {
    this.start = other.start;
    this.end = other.end;
  }

  /**
   * Return <code>true</code> iff <code>point</code> is contained in this
   * interval.
   *
   * @param point
   *        the character to check
   */
  public boolean contains (final int point)
  {
    return start <= point && end >= point;
  }

  /**
   * Return <code>true</code> iff this interval completely contains the other
   * one.
   *
   * @param other
   *        the other interval
   */
  public boolean contains (final Interval other)
  {
    return this.start <= other.start && this.end >= other.end;
  }

  /**
   * Return <code>true</code> if <code>o</code> is an interval with the same
   * borders.
   *
   * @param o
   *        the object to check equality with
   */
  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!(o instanceof Interval))
      return false;

    final Interval other = (Interval) o;
    return other.start == this.start && other.end == this.end;
  }

  /**
   * Set a new last character
   *
   * @param end
   *        the new last character of this interval
   */
  public void setEnd (final int end)
  {
    this.end = end;
  }

  /**
   * Set a new first character
   *
   * @param start
   *        the new first character of this interval
   */
  public void setStart (final int start)
  {
    this.start = start;
  }

  /**
   * Check whether a character is printable.
   *
   * @param c
   *        the character to check
   */
  private static boolean isPrintable (final int c)
  {
    // fixme: should make unicode test here
    return c > 31 && c < 127;
  }

  /**
   * Get a String representation of this interval.
   *
   * @return a string <code>"[start-end]"</code> or <code>"[start]"</code> (if
   *         there is only one character in the interval) where
   *         <code>start</code> and <code>end</code> are either a number (the
   *         character code) or something of the from <code>'a'</code>.
   */
  @Override
  public String toString ()
  {
    final StringBuilder result = new StringBuilder ("[");

    if (isPrintable (start))
      result.append ("'").append ((char) start).append ("'");
    else
      result.append (start);

    if (start != end)
    {
      result.append ("-");

      if (isPrintable (end))
        result.append ("'").append ((char) end).append ("'");
      else
        result.append (end);
    }

    result.append ("]");
    return result.toString ();
  }

  /**
   * Make a copy of this interval.
   *
   * @return the copy
   */
  public Interval copy ()
  {
    return new Interval (start, end);
  }
}
