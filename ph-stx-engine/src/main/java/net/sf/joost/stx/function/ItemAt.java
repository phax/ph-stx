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
 * The <code>item-at</code> function.<br>
 * Returns the item in the sequence (first parameter) at the specified position
 * (second parameter).
 *
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:00 $
 * @author Oliver Becker
 */
public final class ItemAt implements IInstance
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

  /** @return "item-at" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "item-at";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException,
                                                                                        EvalException
  {
    Value seq = args.m_aLeft.evaluate (context, top);
    final double dpos = args.m_aRight.evaluate (context, top).getNumberValue ();

    if (seq.type == Value.EMPTY || Double.isNaN (dpos))
      return Value.VAL_EMPTY;

    long position = Math.round (dpos);
    while (seq != null && --position != 0)
      seq = seq.next;

    if (seq == null)
      throw new EvalException ("Position " +
                               dpos +
                               " out of bounds in call to function '" +
                               getName ().substring (FunctionFactory.FNSP.length ()) +
                               "'");
    return seq.singleCopy ();
  }
}
