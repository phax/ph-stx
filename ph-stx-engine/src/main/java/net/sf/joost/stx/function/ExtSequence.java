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

import java.util.List;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>sequence</code> extension function.<br>
 * Converts a Java array or a {@link List} object to a sequence. Any other value
 * will be returned unchanged.
 *
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class ExtSequence implements IInstance
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

  /** @return "sequence" */
  public String getName ()
  {
    return FunctionFactory.JENSP + "sequence";
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
    // in case there's no object
    if (v.type != Value.OBJECT)
      return v;

    Object [] objs = null;
    final Object vo = v.getObject ();
    if (vo instanceof Object [])
      objs = (Object []) vo;
    else
      if (vo instanceof List)
        objs = ((List <?>) vo).toArray ();

    if (objs != null)
    {
      // an empty array
      if (objs.length == 0)
        return Value.VAL_EMPTY;

      // ok, there's at least one element
      v = new Value (objs[0]);
      // create the rest of the sequence
      Value last = v;
      for (int i = 1; i < objs.length; i++)
      {
        last.next = new Value (objs[i]);
        last = last.next;
      }
    }

    return v;
  }
}
