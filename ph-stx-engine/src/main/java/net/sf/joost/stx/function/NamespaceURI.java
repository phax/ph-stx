/*
 * $Id: NamespaceURI.java,v 1.4 2007/11/25 14:18:00 obecker Exp $
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
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>namespace-uri</code> function.<br>
 * Returns the namespace URI of this node.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-namespace-uri">
 *      fn:namespace-uri in
 *      "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:00 $
 * @author Oliver Becker
 */
public final class NamespaceURI implements IInstance
{
  /** @return 0 */
  public int getMinParCount ()
  {
    return 0;
  }

  /** @return 1 */
  public int getMaxParCount ()
  {
    return 1;
  }

  /** @return "namespace-uri" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "namespace-uri";
  }

  /** @return <code>false</code> */
  public boolean isConstant ()
  {
    return false;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException, EvalException
  {
    final Value v = FunctionFactory.getOptionalValue (context, top, args);
    if (v.type == Value.EMPTY)
      return Value.VAL_EMPTY_STRING;

    final SAXEvent event = v.getNode ();
    if (event == null)
      throw new EvalException ("The parameter passed to the '" +
                               getName ().substring (FunctionFactory.FNSP.length ()) +
                               "' function must be a node (got " +
                               v +
                               ")");

    if (event.type == SAXEvent.ELEMENT || event.type == SAXEvent.ATTRIBUTE)
      return new Value (event.uri);
    else
      return Value.VAL_EMPTY_STRING;
  }
}
