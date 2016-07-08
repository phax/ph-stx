/*
 * $Id: Remove.java,v 1.4 2007/11/25 14:18:00 obecker Exp $
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is: this file
 *
 * The Initial Developer of the Original Code is Oliver Becker.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.stx.function;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.Instance;

/**
 * The <code>remove</code> function.<br>
 * Removes the item in the sequence (first parameter) at the specified position
 * (second parameter).
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-remove"> fn:remove in
 *      "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:00 $
 * @author Oliver Becker
 */
final public class Remove implements Instance
{
  /** @return 2 */
  public int getMinParCount ()
  {
    return 2;
  }

  /** @return 2 */
  public int getMaxParCount ()
  {
    return 2;
  }

  /** @return "remove" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "remove";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final Tree args) throws SAXException, EvalException
  {
    Value seq = args.left.evaluate (context, top);
    final Value arg2 = args.right.evaluate (context, top);

    // make sure that the second parameter is a valid number
    final double dPos = arg2.getNumberValue ();
    if (Double.isNaN (dPos))
      throw new EvalException ("Parameter '" +
                               arg2.getStringValue () +
                               "' is not a valid index for function '" +
                               getName ().substring (FunctionFactory.FNSP.length ()) +
                               "'");
    long position = Math.round (dPos);

    if (seq.type == Value.EMPTY || position < 1)
      return seq;

    Value last = null;
    final Value result = seq;
    while (seq != null && --position != 0)
    {
      last = seq;
      seq = seq.next;
    }

    if (seq == null) // position greater than sequence length
      return result;

    if (last == null)
    { // remove the first item
      if (result.next == null) // the one and only item
        return Value.VAL_EMPTY;
      else
        return result.next;
    }

    last.next = seq.next;
    return result;
  }
}
