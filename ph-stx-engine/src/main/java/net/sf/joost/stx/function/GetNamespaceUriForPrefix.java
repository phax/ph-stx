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
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>get-namespace-uri-for-prefix</code> function.<br>
 * Returns the names of the in-scope namespaces for this node.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-get-namespace-uri-for-prefix">
 *      fn:get-namespace-uri-for-prefix in "XQuery 1.0 and XPath 2.0 Functions
 *      and Operators"</a>
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:00 $
 * @author Oliver Becker
 */
public final class GetNamespaceUriForPrefix implements IInstance
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

  /** @return "get-namespace-uri-for-prefix" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "get-namespace-uri-for-prefix";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException,
                                                                                        EvalException
  {
    final String prefix = args.m_aLeft.evaluate (context, top).getStringValue ();

    final Value v = args.m_aRight.evaluate (context, top);
    final SAXEvent e = v.getNode ();
    if (e == null)
      throw new EvalException ("The second parameter passed to the '" +
                               getName ().substring (FunctionFactory.FNSP.length ()) +
                               "' function must be a node (got " +
                               v +
                               ")");

    if (e.m_aNamespaces == null)
      return Value.VAL_EMPTY;

    final String uri = e.m_aNamespaces.get (prefix);
    if (uri == null)
      return Value.VAL_EMPTY;
    return new Value (uri);
  }
}
