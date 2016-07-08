/*
 * $Id: NumberConv.java,v 1.3 2007/05/20 18:00:44 obecker Exp $
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

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.Instance;

import org.xml.sax.SAXException;

/**
 * The <code>number</code> function.<br>
 * Returns its argument converted to a number.
 * 
 * @see <a target="xq1xp2fo"
 *      href="http://www.w3.org/TR/xpath-functions/#func-number"> fn:number in
 *      "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
final public class NumberConv implements Instance
{
   /** @return 0 */
   public int getMinParCount() { return 0; }

   /** @return 1 */
   public int getMaxParCount() { return 1; }

   /** @return "number" */
   public String getName() { return FunctionFactory.FNSP + "number"; }

   /** @return <code>false</code> */
   public boolean isConstant() { return false; }

   public Value evaluate(Context context, int top, Tree args)
      throws SAXException, EvalException
   {
      return 
         new Value(FunctionFactory.getOptionalValue(context, top, args).getNumberValue());
   }
}