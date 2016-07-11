/*
 * $Id: RegexTranslator.java,v 1.2 2007/11/25 14:18:02 obecker Exp $
 *
 * Copied from Michael Kay's Saxon 8.9
 * Local changes (excluding package declarations and imports) marked as // OB
 */

package net.sf.joost.util.regex;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import net.sf.joost.util.om.IntHashSet;
import net.sf.joost.util.om.Whitespace;
import net.sf.joost.util.om.XMLChar;

/**
 * Abstract superclass for the various regex translators, which differ according
 * to the target platform.
 */
public abstract class AbstractRegexTranslator
{

  protected CharSequence m_aRegExp;
  protected boolean isXPath;
  protected boolean ignoreWhitespace;
  protected boolean inCharClassExpr;
  protected boolean caseBlind;
  protected int pos = 0;
  protected int m_nLength;
  protected char curChar;
  protected boolean eos = false;
  protected int currentCapture = 0;
  protected IntHashSet captures = new IntHashSet ();
  protected final StringBuilder result = new StringBuilder (32);

  protected void translateTop () throws RegexSyntaxException
  {
    translateRegExp ();
    if (!eos)
    {
      throw makeException ("expected end of string");
    }
  }

  protected void translateRegExp () throws RegexSyntaxException
  {
    translateBranch ();
    while (curChar == '|')
    {
      copyCurChar ();
      translateBranch ();
    }
  }

  protected void translateBranch () throws RegexSyntaxException
  {
    while (translateAtom ())
      translateQuantifier ();
  }

  protected abstract boolean translateAtom () throws RegexSyntaxException;

  protected void translateQuantifier () throws RegexSyntaxException
  {
    switch (curChar)
    {
      case '*':
      case '?':
      case '+':
        copyCurChar ();
        break;
      case '{':
        copyCurChar ();
        translateQuantity ();
        expect ('}');
        copyCurChar ();
        break;
      default:
        return;
    }
    if (curChar == '?' && isXPath)
    {
      copyCurChar ();
    }
  }

  protected void translateQuantity () throws RegexSyntaxException
  {
    final String lower = parseQuantExact ().toString ();
    int lowerValue = -1;
    try
    {
      lowerValue = Integer.parseInt (lower);
      result.append (lower);
    }
    catch (final NumberFormatException e)
    {
      // JDK 1.4 cannot handle ranges bigger than this
      result.append ("" + Integer.MAX_VALUE);
    }
    if (curChar == ',')
    {
      copyCurChar ();
      if (curChar != '}')
      {
        final String upper = parseQuantExact ().toString ();
        try
        {
          final int upperValue = Integer.parseInt (upper);
          result.append (upper);
          if (lowerValue < 0 || upperValue < lowerValue)
            throw makeException ("invalid range in quantifier");
        }
        catch (final NumberFormatException e)
        {
          result.append ("" + Integer.MAX_VALUE);
          if (lowerValue < 0 && new BigDecimal (lower).compareTo (new BigDecimal (upper)) > 0)
            throw makeException ("invalid range in quantifier");
        }
      }
    }
  }

  protected CharSequence parseQuantExact () throws RegexSyntaxException
  {
    final StringBuilder buf = new StringBuilder (10);
    do
    {
      if ("0123456789".indexOf (curChar) < 0)
        throw makeException ("expected digit in quantifier");
      buf.append (curChar);
      advance ();
    } while (curChar != ',' && curChar != '}');
    return buf;
  }

  protected void copyCurChar ()
  {
    result.append (curChar);
    advance ();
  }

  public static final int NONE = -1;
  public static final int SOME = 0;
  public static final int ALL = 1;

  public static final String SURROGATES1_CLASS = "[\uD800-\uDBFF]";
  public static final String SURROGATES2_CLASS = "[\uDC00-\uDFFF]";
  public static final String NOT_ALLOWED_CLASS = "[\u0000&&[^\u0000]]";

  public static final class Range implements Comparable <Range>
  {
    private final int min;
    private final int max;

    public Range (final int min, final int max)
    {
      this.min = min;
      this.max = max;
    }

    public int getMin ()
    {
      return min;
    }

    public int getMax ()
    {
      return max;
    }

    public int compareTo (final Range o)
    {
      final Range other = o;
      if (this.min < other.min)
        return -1;
      if (this.min > other.min)
        return 1;
      if (this.max > other.max)
        return -1;
      if (this.max < other.max)
        return 1;
      return 0;
    }
  }

  protected void advance ()
  {
    if (pos < m_nLength)
    {
      curChar = m_aRegExp.charAt (pos++);
      if (ignoreWhitespace && !inCharClassExpr)
      {
        while (Whitespace.isWhitespace (curChar))
        {
          advance ();
        }
      }
    }
    else
    {
      pos++;
      curChar = RegexData.EOS;
      eos = true;
    }
  }

  protected int absorbSurrogatePair () throws RegexSyntaxException
  {
    if (XMLChar.isSurrogate (curChar))
    {
      if (!XMLChar.isHighSurrogate (curChar))
        throw makeException ("invalid surrogate pair");
      final char c1 = curChar;
      advance ();
      if (!XMLChar.isLowSurrogate (curChar))
        throw makeException ("invalid surrogate pair");
      return XMLChar.supplemental (c1, curChar);
    }
    else
    {
      return curChar;
    }
  }

  protected void recede ()
  {
    // The caller must ensure we don't fall off the start of the expression
    if (eos)
    {
      curChar = m_aRegExp.charAt (m_nLength - 1);
      pos = m_nLength;
      eos = false;
    }
    else
    {
      curChar = m_aRegExp.charAt ((--pos) - 1);
    }
    if (ignoreWhitespace && !inCharClassExpr)
    {
      while (Whitespace.isWhitespace (curChar))
      {
        recede ();
      }
    }
  }

  protected void expect (final char c) throws RegexSyntaxException
  {
    if (curChar != c)
    {
      throw makeException ("expected", new String (new char [] { c }));
    }
  }

  protected RegexSyntaxException makeException (final String key)
  {
    // OB: removed usage of Err.wrap()
    // return new RegexSyntaxException("Error at character " + (pos - 1) +
    // " in regular expression " + Err.wrap(regExp, Err.VALUE) + ": " + key);
    return new RegexSyntaxException ("Error at character " +
                                     (pos - 1) +
                                     " in regular expression '" +
                                     m_aRegExp +
                                     "' : " +
                                     key);
  }

  protected RegexSyntaxException makeException (final String key, final String arg)
  {
    // OB: removed usage of Err.wrap()
    // return new RegexSyntaxException("Error at character " + (pos - 1) +
    // " in regular expression " + Err.wrap(regExp, Err.VALUE) + ": " + key +
    // " (" + arg + ')');
    return new RegexSyntaxException ("Error at character " +
                                     (pos - 1) +
                                     " in regular expression '" +
                                     m_aRegExp +
                                     "': " +
                                     key +
                                     " (" +
                                     arg +
                                     ')');
  }

  protected static boolean isJavaMetaChar (final int c)
  {
    switch (c)
    {
      case '\\':
      case '^':
      case '?':
      case '*':
      case '+':
      case '(':
      case ')':
      case '{':
      case '}':
      case '|':
      case '[':
      case ']':
      case '-':
      case '&':
      case '$':
      case '.':
        return true;
    }
    return false;
  }

  protected static String highSurrogateRanges (final List ranges)
  {
    final StringBuilder highRanges = new StringBuilder (ranges.size () * 2);
    for (int i = 0, len = ranges.size (); i < len; i++)
    {
      final Range r = (Range) ranges.get (i);
      char min1 = XMLChar.highSurrogate (r.getMin ());
      final char min2 = XMLChar.lowSurrogate (r.getMin ());
      char max1 = XMLChar.highSurrogate (r.getMax ());
      final char max2 = XMLChar.lowSurrogate (r.getMax ());
      if (min2 != RegexData.SURROGATE2_MIN)
      {
        min1++;
      }
      if (max2 != RegexData.SURROGATE2_MAX)
      {
        max1--;
      }
      if (max1 >= min1)
      {
        highRanges.append (min1);
        highRanges.append (max1);
      }
    }
    return highRanges.toString ();
  }

  protected static String lowSurrogateRanges (final List ranges)
  {
    final StringBuilder lowRanges = new StringBuilder (ranges.size () * 2);
    for (int i = 0, len = ranges.size (); i < len; i++)
    {
      final Range r = (Range) ranges.get (i);
      final char min1 = XMLChar.highSurrogate (r.getMin ());
      final char min2 = XMLChar.lowSurrogate (r.getMin ());
      final char max1 = XMLChar.highSurrogate (r.getMax ());
      final char max2 = XMLChar.lowSurrogate (r.getMax ());
      if (min1 == max1)
      {
        if (min2 != RegexData.SURROGATE2_MIN || max2 != RegexData.SURROGATE2_MAX)
        {
          lowRanges.append (min1);
          lowRanges.append (min2);
          lowRanges.append (max2);
        }
      }
      else
      {
        if (min2 != RegexData.SURROGATE2_MIN)
        {
          lowRanges.append (min1);
          lowRanges.append (min2);
          lowRanges.append (RegexData.SURROGATE2_MAX);
        }
        if (max2 != RegexData.SURROGATE2_MAX)
        {
          lowRanges.append (max1);
          lowRanges.append (RegexData.SURROGATE2_MIN);
          lowRanges.append (max2);
        }
      }
    }
    return lowRanges.toString ();
  }

  protected static void sortRangeList (final List ranges)
  {
    Collections.sort (ranges);
    int toIndex = 0;
    int fromIndex = 0;
    int len = ranges.size ();
    while (fromIndex < len)
    {
      Range r = (Range) ranges.get (fromIndex);
      final int min = r.getMin ();
      int max = r.getMax ();
      while (++fromIndex < len)
      {
        final Range r2 = (Range) ranges.get (fromIndex);
        if (r2.getMin () > max + 1)
          break;
        if (r2.getMax () > max)
          max = r2.getMax ();
      }
      if (max != r.getMax ())
        r = new Range (min, max);
      ranges.set (toIndex++, r);
    }
    while (len > toIndex)
      ranges.remove (--len);
  }

  protected static boolean isBlock (final String name)
  {
    for (final String blockName : RegexData.blockNames)
    {
      if (name.equals (blockName))
      {
        return true;
      }
    }
    return false;
  }

  protected static boolean isAsciiAlnum (final char c)
  {
    if ('a' <= c && c <= 'z')
    {
      return true;
    }
    if ('A' <= c && c <= 'Z')
    {
      return true;
    }
    if ('0' <= c && c <= '9')
    {
      return true;
    }
    return false;
  }

}

//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Contributor(s):
//
