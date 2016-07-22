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

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>get-in-scope-prefixes</code> function.<br>
 * Returns the names of the in-scope namespaces for this node.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-get-in-scope-prefixes">
 *      fn:get-in-scope-prefixes in "XQuery 1.0 and XPath 2.0 Functions and
 *      Operators"</a>
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:00 $
 * @author Oliver Becker
 */
public final class GetInScopePrefixes implements IInstance
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

  /** @return "get-in-scope-prefixes" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "get-in-scope-prefixes";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException, EvalException
  {
    Value v = args.evaluate (context, top);
    final SAXEvent e = v.getNode ();
    if (e == null)
      throw new EvalException ("The parameter passed to the '" +
                               getName ().substring (FunctionFactory.FNSP.length ()) +
                               "' function must be a node (got " +
                               v +
                               ")");

    if (e.m_aNamespaces == null)
      return Value.VAL_EMPTY;

    Value ret = null, last = null;
    for (final String en : e.m_aNamespaces.values ())
    {
      v = new Value (en);
      if (last != null)
        last.next = v;
      else
        ret = v;
      last = v;
    }
    if (ret != null)
      return ret;
    // shouldn't happen: at least "xml" is always defined
    return Value.VAL_EMPTY;
  }
}
