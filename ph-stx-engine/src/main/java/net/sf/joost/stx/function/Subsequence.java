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
package net.sf.joost.stx.function;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>subsequence</code> function.<br>
 * Returns the subsequence from the first parameter, beginning at a position
 * given by the second parameter with a length given by an optional third
 * parameter.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-subsequence"> fn:subsequence
 *      in "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class Subsequence implements IInstance
{
  /** @return 2 **/
  public int getMinParCount ()
  {
    return 2;
  }

  /** @return 3 **/
  public int getMaxParCount ()
  {
    return 3;
  }

  /** @return "subsequence" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "subsequence";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException,
                                                                                        EvalException
  {
    Value seq;
    long begin, end;
    if (args.m_aLeft.getType () == AbstractTree.LIST)
    { // three parameters
      seq = args.m_aLeft.m_aLeft.evaluate (context, top);
      final double arg2 = args.m_aLeft.m_aRight.evaluate (context, top).getNumberValue ();
      final double arg3 = args.m_aRight.evaluate (context, top).getNumberValue ();

      // extra test, because round(NaN) gives 0
      if (seq.type == Value.EMPTY || Double.isNaN (arg2) || Double.isNaN (arg2 + arg3))
        return Value.VAL_EMPTY;

      // the first item is at position 1
      begin = Math.round (arg2 - 1.0);
      end = begin + Math.round (arg3);
      if (begin < 0)
        begin = 0;
      if (end <= begin)
        return Value.VAL_EMPTY;
    }
    else
    { // two parameters
      seq = args.m_aLeft.evaluate (context, top);
      final double arg2 = args.m_aRight.evaluate (context, top).getNumberValue ();

      if (seq.type == Value.EMPTY || Double.isNaN (arg2))
        return Value.VAL_EMPTY;
      if (arg2 < 1)
        return seq;

      // the first item is at position 1,
      begin = Math.round (arg2 - 1.0);
      end = -1; // special marker to speed up the evaluation
    }

    Value ret = null;
    while (seq != null)
    {
      if (ret == null && begin == 0)
      {
        ret = seq;
        if (end < 0) // true, if the two parameter version was used
          break;
      }
      else
        begin--;
      end--;
      if (end == 0)
        break;
      seq = seq.next;
    }
    if (ret != null)
    {
      if (end == 0) // reached the end of the requested subsequence
        seq.next = null; // cut the rest
      return ret;
    }
    return Value.VAL_EMPTY;
  }
}
