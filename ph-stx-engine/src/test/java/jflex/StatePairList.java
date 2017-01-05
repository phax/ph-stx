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
 * A list of pairs of states. Used in DFA minimization.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class StatePairList
{

  // implemented as two arrays of integers.
  // java.util classes proved too inefficient.

  int p[];
  int q[];

  int num;

  public StatePairList ()
  {
    p = new int [8];
    q = new int [8];
    num = 0;
  }

  public void addPair (final int i, final int j)
  {
    for (int x = 0; x < num; x++)
      if (p[x] == i && q[x] == j)
        return;

    if (num >= p.length)
      increaseSize (num);

    p[num] = i;
    q[num] = j;

    num++;
  }

  public void markAll (final StatePairList [] [] list, final boolean [] [] equiv)
  {
    for (int x = 0; x < num; x++)
    {
      final int i = p[x];
      final int j = q[x];

      if (equiv[i][j])
      {
        equiv[i][j] = false;
        if (list[i][j] != null)
          list[i][j].markAll (list, equiv);
      }
    }
  }

  private void increaseSize (int length)
  {
    length = Math.max (length + 1, 4 * p.length);
    Out.debug ("increasing length to " + length); //$NON-NLS-1$

    final int pn[] = new int [length];
    final int qn[] = new int [length];

    System.arraycopy (p, 0, pn, 0, p.length);
    System.arraycopy (q, 0, qn, 0, q.length);

    p = pn;
    q = qn;
  }
}
