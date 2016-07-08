/*
 * $Id: Translate.java,v 1.3 2007/05/20 18:00:44 obecker Exp $
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
 * The <code>translate</code> function.<br>
 * Replaces in the first parameter all characters given in the second parameter
 * by their counterparts in the third parameter and returns the result.
 * 
 * @see <a target="xq1xp2fo"
 *      href="http://www.w3.org/TR/xpath-functions/#func-translate">
 *      fn:translate in "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
final public class Translate implements Instance 
{
   /** @return 3 **/
   public int getMinParCount() { return 3; }

   /** @return 3 **/
   public int getMaxParCount() { return 3; }

   /** @return "translate" */
   public String getName() { return FunctionFactory.FNSP + "translate"; }
   
   /** @return <code>true</code> */
   public boolean isConstant() { return true; }

   public Value evaluate(Context context, int top, Tree args)
      throws SAXException, EvalException
   {
      String s1 = args.left.left.evaluate(context, top).getStringValue();
      String s2 = args.left.right.evaluate(context, top).getStringValue();
      String s3 = args.right.evaluate(context, top).getStringValue();
      StringBuffer result = new StringBuffer();
      int s1len = s1.length();
      int s3len = s3.length();
      for (int i=0; i<s1len; i++) {
         char c = s1.charAt(i);
         int index = s2.indexOf(c);
         if (index < s3len)
            result.append(index < 0 ? c : s3.charAt(index));
      }
      return new Value(result.toString());
   }
}