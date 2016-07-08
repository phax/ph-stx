/*
 * $Id: SubstringBefore.java,v 1.3 2007/05/20 18:00:44 obecker Exp $
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
 * The <code>substring-before</code> function.<br>
 * Returns the substring from the first parameter that occurs before the second
 * parameter.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-substring-before">
 *      fn:substring-before in "XQuery 1.0 and XPath 2.0 Functions and
 *      Operators"</a>
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
final public class SubstringBefore implements Instance
{
  /** @return 2 **/
  public int getMinParCount ()
  {
    return 2;
  }

  /** @return 2 **/
  public int getMaxParCount ()
  {
    return 2;
  }

  /** @return "substring-before" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "substring-before";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final Tree args) throws SAXException, EvalException
  {
    final String s1 = args.left.evaluate (context, top).getStringValue ();
    final String s2 = args.right.evaluate (context, top).getStringValue ();
    final int index = s1.indexOf (s2);
    if (index != -1)
      return new Value (s1.substring (0, index));
    else
      return Value.VAL_EMPTY_STRING;
  }
}
