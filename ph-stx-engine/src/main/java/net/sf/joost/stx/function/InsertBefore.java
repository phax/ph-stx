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
package net.sf.joost.stx.function;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>insert-before</code> function.<br>
 * Inserts an item or sequence of items into a specified position of a sequence.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-insert-before">
 *      fn:insert-before in
 *      "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:00 $
 * @author Oliver Becker
 */
public final class InsertBefore implements IInstance
{
  /** @return 3 */
  public int getMinParCount ()
  {
    return 3;
  }

  /** @return 3 */
  public int getMaxParCount ()
  {
    return 3;
  }

  /** @return "insert-before" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "insert-before";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException, EvalException
  {
    Value target = args.m_aLeft.m_aLeft.evaluate (context, top);
    final Value arg2 = args.m_aLeft.m_aRight.evaluate (context, top);
    Value inserts = args.m_aRight.evaluate (context, top);

    // make sure that the second parameter is a valid number
    final double dPos = arg2.getNumberValue ();
    if (Double.isNaN (dPos))
      throw new EvalException ("Parameter '" +
                               arg2.getStringValue () +
                               "' is not a valid index for function '" +
                               getName ().substring (FunctionFactory.FNSP.length ()) +
                               "'");
    long position = Math.round (dPos);

    if (inserts.type == Value.EMPTY)
      return target;
    if (target.type == Value.EMPTY)
      return inserts;

    Value result;
    if (position <= 1)
                      // insert before the first item of target
                      result = inserts;
    else
    {
      result = target;
      // determine position
      while (target.next != null && --position > 1)
        target = target.next;
      final Value rest = target.next;
      target.next = inserts;
      target = rest;
    }
    // append rest of target
    while (inserts.next != null)
      inserts = inserts.next;
    inserts.next = target;

    return result;
  }
}
