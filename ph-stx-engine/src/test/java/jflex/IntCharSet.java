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

import java.util.ArrayList;
import java.util.List;

import jflex.unicode.UnicodeProperties;

/**
 * CharSet implemented with intervals [fixme: optimizations possible]
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class IntCharSet
{

  private final static boolean DEBUG = false;

  /* invariant: all intervals are disjoint, ordered */
  private final List <Interval> intervals;
  private int pos;

  public IntCharSet ()
  {
    this.intervals = new ArrayList <> ();
  }

  public IntCharSet (final int c)
  {
    this (new Interval (c, c));
  }

  public IntCharSet (final Interval interval)
  {
    this ();
    intervals.add (interval);
  }

  public IntCharSet (final List <Interval> chars)
  {
    final int size = chars.size ();
    intervals = new ArrayList <> (size);

    for (final Interval interval : chars)
      add (interval);
  }

  /**
   * returns the index of the interval that contains the character c, -1 if
   * there is no such interval
   *
   * @prec: true
   * @post: -1 <= return < intervals.size() && (return > -1 -->
   *        intervals[return].contains(c))
   * @param c
   *        the character
   * @return the index of the enclosing interval, -1 if no such interval
   */
  private int indexOf (final int c)
  {
    int start = 0;
    int end = intervals.size () - 1;

    while (start <= end)
    {
      final int check = (start + end) / 2;
      final Interval i = intervals.get (check);

      if (start == end)
        return i.contains (c) ? start : -1;

      if (c < i.start)
      {
        end = check - 1;
        continue;
      }

      if (c > i.end)
      {
        start = check + 1;
        continue;
      }

      return check;
    }

    return -1;
  }

  public IntCharSet add (final IntCharSet set)
  {
    for (final Interval interval : set.intervals)
      add (interval);
    return this;
  }

  public void add (final Interval interval)
  {

    int size = intervals.size ();

    for (int i = 0; i < size; i++)
    {
      final Interval elem = intervals.get (i);

      if (elem.end + 1 < interval.start)
        continue;

      if (elem.contains (interval))
        return;

      if (elem.start > interval.end + 1)
      {
        intervals.add (i, new Interval (interval));
        return;
      }

      if (interval.start < elem.start)
        elem.start = interval.start;

      if (interval.end <= elem.end)
        return;

      elem.end = interval.end;

      i++;
      // delete all x with x.contains( interval.end )
      while (i < size)
      {
        final Interval x = intervals.get (i);
        if (x.start > elem.end + 1)
          return;

        if (x.end > elem.end)
        {
          elem.end = x.end;
        }
        intervals.remove (i);
        size--;
      }
      return;
    }

    intervals.add (new Interval (interval));
  }

  public void add (final int c)
  {
    final int size = intervals.size ();

    for (int i = 0; i < size; i++)
    {
      final Interval elem = intervals.get (i);
      if (elem.end + 1 < c)
        continue;

      if (elem.contains (c))
        return; // already there, nothing to do

      // assert(elem.end+1 >= c && (elem.start > c || elem.end < c));

      if (elem.start > c + 1)
      {
        intervals.add (i, new Interval (c, c));
        return;
      }

      // assert(elem.end+1 >= c && elem.start <= c+1 && (elem.start > c ||
      // elem.end < c));

      if (c + 1 == elem.start)
      {
        elem.start = c;
        return;
      }

      // assert(elem.end+1 == c);
      elem.end = c;

      // merge with next interval if it contains c
      if (i + 1 >= size)
        return;
      final Interval x = intervals.get (i + 1);
      if (x.start <= c + 1)
      {
        elem.end = x.end;
        intervals.remove (i + 1);
      }
      return;
    }

    // end reached but nothing found -> append at end
    intervals.add (new Interval (c, c));
  }

  public boolean contains (final int singleChar)
  {
    return indexOf (singleChar) >= 0;
  }

  /**
   * o instanceof Interval
   */
  @Override
  public boolean equals (final Object o)
  {
    final IntCharSet set = (IntCharSet) o;

    return intervals.equals (set.intervals);
  }

  private int min (final int a, final int b)
  {
    return a <= b ? a : b;
  }

  private int max (final int a, final int b)
  {
    return a >= b ? a : b;
  }

  /* intersection */
  public IntCharSet and (final IntCharSet set)
  {
    if (DEBUG)
    {
      Out.dump ("intersection");
      Out.dump ("this  : " + this);
      Out.dump ("other : " + set);
    }

    final IntCharSet result = new IntCharSet ();

    int i = 0; // index in this.intervals
    int j = 0; // index in set.intervals

    final int size = intervals.size ();
    final int setSize = set.intervals.size ();

    while (i < size && j < setSize)
    {
      final Interval x = this.intervals.get (i);
      final Interval y = set.intervals.get (j);

      if (x.end < y.start)
      {
        i++;
        continue;
      }

      if (y.end < x.start)
      {
        j++;
        continue;
      }

      result.intervals.add (new Interval (max (x.start, y.start), min (x.end, y.end)));

      if (x.end >= y.end)
        j++;
      if (y.end >= x.end)
        i++;
    }

    if (DEBUG)
    {
      Out.dump ("result: " + result);
    }

    return result;
  }

  /* complement */
  /* prec: this.contains(set), set != null */
  public void sub (final IntCharSet set)
  {
    if (DEBUG)
    {
      Out.dump ("complement");
      Out.dump ("this  : " + this);
      Out.dump ("other : " + set);
    }

    int i = 0; // index in this.intervals
    int j = 0; // index in set.intervals

    final int setSize = set.intervals.size ();

    while (i < intervals.size () && j < setSize)
    {
      final Interval x = this.intervals.get (i);
      final Interval y = set.intervals.get (j);

      if (DEBUG)
      {
        Out.dump ("this      : " + this);
        Out.dump ("this  [" + i + "] : " + x);
        Out.dump ("other [" + j + "] : " + y);
      }

      if (x.end < y.start)
      {
        i++;
        continue;
      }

      if (y.end < x.start)
      {
        j++;
        continue;
      }

      // x.end >= y.start && y.end >= x.start ->
      // x.end <= y.end && x.start >= y.start (prec)

      if (x.start == y.start && x.end == y.end)
      {
        intervals.remove (i);
        j++;
        continue;
      }

      // x.end <= y.end && x.start >= y.start &&
      // (x.end < y.end || x.start > y.start) ->
      // x.start < x.end

      if (x.start == y.start)
      {
        x.start = y.end + 1;
        j++;
        continue;
      }

      if (x.end == y.end)
      {
        x.end = y.start - 1;
        i++;
        j++;
        continue;
      }

      intervals.add (i, new Interval (x.start, y.start - 1));
      x.start = y.end + 1;

      i++;
      j++;
    }

    if (DEBUG)
    {
      Out.dump ("result: " + this);
    }
  }

  public boolean containsElements ()
  {
    return intervals.size () > 0;
  }

  public int numIntervals ()
  {
    return intervals.size ();
  }

  public List <Interval> getIntervals ()
  {
    return intervals;
  }

  // beware: depends on caller protocol, single user only
  public Interval getNext ()
  {
    if (pos == intervals.size ())
      pos = 0;
    return intervals.get (pos++);
  }

  /**
   * Create a caseless version of this charset.
   * <p>
   * The caseless version contains all characters of this char set, and
   * additionally all lower/upper/title case variants of the characters in this
   * set.
   *
   * @param unicodeProperties
   *        The Unicode Properties to use when generating caseless equivalence
   *        classes.
   * @return a caseless copy of this set
   */
  public IntCharSet getCaseless (final UnicodeProperties unicodeProperties)
  {
    final IntCharSet n = copy ();

    final int size = intervals.size ();
    for (int i = 0; i < size; i++)
    {
      final Interval elem = intervals.get (i);
      for (int c = elem.start; c <= elem.end; c++)
      {
        final IntCharSet equivalenceClass = unicodeProperties.getCaselessMatches (c);
        if (null != equivalenceClass)
          n.add (equivalenceClass);
      }
    }
    return n;
  }

  /**
   * Make a string representation of this char set.
   *
   * @return a string representing this char set.
   */
  @Override
  public String toString ()
  {
    final StringBuilder result = new StringBuilder ("{ ");

    for (final Interval interval : intervals)
      result.append (interval);

    result.append (" }");

    return result.toString ();
  }

  /**
   * Return a (deep) copy of this char set
   *
   * @return the copy
   */
  public IntCharSet copy ()
  {
    final IntCharSet result = new IntCharSet ();
    for (final Interval interval : intervals)
      result.intervals.add (interval.copy ());
    return result;
  }
}
