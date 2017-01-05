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

import java.io.File;

/**
 * Performs simple semantic analysis on regular expressions.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class SemCheck
{

  // stored globally since they are used as constants in all checks
  private static Macros macros;

  /**
   * Performs semantic analysis for all expressions. Currently checks for empty
   * expressions only.
   *
   * @param rs
   *        the reg exps to be checked
   * @param m
   *        the macro table (in expanded form)
   * @param f
   *        the spec file containing the rules
   */
  public static void check (final RegExps rs, final Macros m, final File f)
  {
    macros = m;
    final int num = rs.getNum ();
    for (int i = 0; i < num; i++)
    {
      final RegExp r = rs.getRegExp (i);
      final RegExp l = rs.getLookAhead (i);
      final Action a = rs.getAction (i);

      if (r != null && maybeEmtpy (r))
      {
        if (l != null)
        {
          if (a == null)
            Out.error (ErrorMessages.EMPTY_MATCH_LOOK);
          else
            Out.error (f, ErrorMessages.EMPTY_MATCH_LOOK, a.priority - 1, -1);
        }
        else
        {
          if (a == null)
            Out.warning (ErrorMessages.EMPTY_MATCH);
          else
            Out.warning (f, ErrorMessages.EMPTY_MATCH, a.priority - 1, -1);
        }
      }
    }
  }

  /**
   * Checks if the expression potentially matches the empty string.
   */
  public static boolean maybeEmtpy (final RegExp re)
  {
    RegExp2 r;

    switch (re.type)
    {

      case sym.BAR:
      {
        r = (RegExp2) re;
        return maybeEmtpy (r.r1) || maybeEmtpy (r.r2);
      }

      case sym.CONCAT:
      {
        r = (RegExp2) re;
        return maybeEmtpy (r.r1) && maybeEmtpy (r.r2);
      }

      case sym.STAR:
      case sym.QUESTION:
        return true;

      case sym.PLUS:
      {
        final RegExp1 r1 = (RegExp1) re;
        return maybeEmtpy ((RegExp) r1.content);
      }

      case sym.CCLASS:
      case sym.CCLASSNOT:
      case sym.CHAR:
      case sym.CHAR_I:
        return false;

      case sym.STRING:
      case sym.STRING_I:
      {
        final String content = (String) ((RegExp1) re).content;
        return content.length () == 0;
      }

      case sym.TILDE:
        return false;

      case sym.BANG:
      {
        final RegExp1 r1 = (RegExp1) re;
        return !maybeEmtpy ((RegExp) r1.content);
      }

      case sym.MACROUSE:
        return maybeEmtpy (macros.getDefinition ((String) ((RegExp1) re).content));
    }

    throw new Error ("Unknown expression type " + re.type + " in " + re); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns length if expression has fixed length, -1 otherwise. Negation
   * operators are treated as always variable length.
   */
  public static int length (final RegExp re)
  {
    RegExp2 r;

    switch (re.type)
    {

      case sym.BAR:
      {
        r = (RegExp2) re;
        final int l1 = length (r.r1);
        if (l1 < 0)
          return -1;
        final int l2 = length (r.r2);

        if (l1 == l2)
          return l1;
        else
          return -1;
      }

      case sym.CONCAT:
      {
        r = (RegExp2) re;
        final int l1 = length (r.r1);
        if (l1 < 0)
          return -1;
        final int l2 = length (r.r2);
        if (l2 < 0)
          return -1;
        return l1 + l2;
      }

      case sym.STAR:
      case sym.PLUS:
      case sym.QUESTION:
        return -1;

      case sym.CCLASS:
      case sym.CCLASSNOT:
      case sym.CHAR:
      case sym.CHAR_I:
        return 1;

      case sym.STRING:
      case sym.STRING_I:
      {
        final String content = (String) ((RegExp1) re).content;
        return content.length ();
      }

      case sym.TILDE:
      case sym.BANG:
        // too hard to calculate at this level, use safe approx
        return -1;

      case sym.MACROUSE:
        return length (macros.getDefinition ((String) ((RegExp1) re).content));
    }

    throw new Error ("Unknown expression type " + re.type + " in " + re); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns true iff the expression is a finite choice of fixed length
   * expressions. Negation operators are treated as always variable length.
   */
  public static boolean isFiniteChoice (final RegExp re)
  {
    RegExp2 r;

    switch (re.type)
    {

      case sym.BAR:
      {
        r = (RegExp2) re;
        return isFiniteChoice (r.r1) && isFiniteChoice (r.r2);
      }

      case sym.CONCAT:
      {
        r = (RegExp2) re;
        final int l1 = length (r.r1);
        if (l1 < 0)
          return false;
        final int l2 = length (r.r2);
        return l2 >= 0;
      }

      case sym.STAR:
      case sym.PLUS:
      case sym.QUESTION:
        return false;

      case sym.CCLASS:
      case sym.CCLASSNOT:
      case sym.CHAR:
      case sym.CHAR_I:
        return true;

      case sym.STRING:
      case sym.STRING_I:
      {
        return true;
      }

      case sym.TILDE:
      case sym.BANG:
        return false;

      case sym.MACROUSE:
        return isFiniteChoice (macros.getDefinition ((String) ((RegExp1) re).content));
    }

    throw new Error ("Unknown expression type " + re.type + " in " + re); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
