/*
 * $Id: RegexGroup.java,v 1.5 2008/02/20 10:04:25 obecker Exp $
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

import java.util.Stack;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.Tree;
import net.sf.joost.instruction.AnalyzeTextFactory;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.Instance;

/**
 * The <code>regex-group</code> function.<br>
 * Returns the captured substring that corresponds to a parenthized
 * sub-expression of a regular expression from an <code>stx:match</code>
 * element.
 *
 * @version $Revision: 1.5 $ $Date: 2008/02/20 10:04:25 $
 * @author Oliver Becker
 */
final public class RegexGroup implements Instance
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

  /** @return "regex-group" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "regex-group";
  }

  /** @return <code>false</code> */
  public boolean isConstant ()
  {
    return false;
  }

  public Value evaluate (final Context context, final int top, final Tree args) throws SAXException, EvalException
  {
    final Value v = args.evaluate (context, top);
    final double d = v.getNumberValue ();
    // access a special pseudo variable
    final Stack s = (Stack) context.localVars.get (AnalyzeTextFactory.REGEX_GROUP);
    if (Double.isNaN (d) || d < 0 || s == null || s.size () == 0)
      return Value.VAL_EMPTY_STRING;

    final String [] capSubstr = (String []) s.peek ();
    final int no = Math.round ((float) d);
    if (no >= capSubstr.length)
      return Value.VAL_EMPTY_STRING;

    return capSubstr[no] == null ? Value.VAL_EMPTY_STRING : new Value (capSubstr[no]);
  }
}
