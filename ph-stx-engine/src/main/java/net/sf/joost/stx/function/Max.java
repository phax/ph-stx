/*
 * $Id: Max.java,v 1.3 2007/05/20 18:00:44 obecker Exp $
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

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>max</code> function.<br>
 * Returns the greatest value in the sequence.
 *
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class Max implements IInstance
{
  /** @return 1 */
  public int getMinParCount ()
  {
    return 1;
  }

  /** @return 1 */
  public int getMaxParCount ()
  {
    return 1;
  }

  /** @return "max" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "max";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException,
                                                                                        EvalException
  {
    Value v = args.evaluate (context, top);
    if (v.type == Value.EMPTY) // empty sequence
      return v;
    double max = Double.NEGATIVE_INFINITY;
    while (v != null)
    {
      final Value next = v.next;
      final double n = v.getNumberValue ();
      if (Double.isNaN (n))
        return Value.VAL_NAN;
      max = n > max ? n : max;
      v = next;
    }
    return new Value (max);
  }
}
