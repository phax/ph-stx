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

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>has-child-nodes</code> function.<br>
 * Returns true if the context node has children (is not empty)
 *
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class HasChildNodes implements IInstance
{
  /** @return 0 */
  public int getMinParCount ()
  {
    return 0;
  }

  /** @return 0 */
  public int getMaxParCount ()
  {
    return 0;
  }

  /** @return "has-child-nodes" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "has-child-nodes";
  }

  /** @return <code>false</code> */
  public boolean isConstant ()
  {
    return false;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args)
  {
    return Value.getBoolean (context.ancestorStack.size () == 1 || context.ancestorStack.peek ().m_bHasChildNodes);
    // size() == 1 means: the context node is the document node
  }
}
