/*
 * $Id: EscapeUri.java,v 1.3 2007/05/20 18:00:44 obecker Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.Instance;

import org.xml.sax.SAXException;

/**
 * The <code>escape-uri</code> function.<br>
 * Applies URI escaping rules.
 * 
 * @see <a target="xq1xp2fo"
 *      href="http://www.w3.org/TR/xpath-functions/#func-escape-uri">
 *      fn:escape-uri in "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
final public class EscapeUri implements Instance 
{
   /** @return 2 **/
   public int getMinParCount() { return 2; }

   /** @return 2 **/
   public int getMaxParCount() { return 2; }

   /** @return "escape-uri" */
   public String getName() { return FunctionFactory.FNSP + "escape-uri"; }
   
   /** @return <code>true</code> */
   public boolean isConstant() { return true; }

   public Value evaluate(Context context, int top, Tree args)
      throws SAXException, EvalException
   {
      Value v = args.left.evaluate(context, top);
      String uri = v.getStringValue();
      boolean eReserved = args.right.evaluate(context, top)
                              .getBooleanValue();

      try {
         char[] ch = uri.toCharArray();
         ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
         OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
         StringBuffer sb = new StringBuffer();
         for (int i=0; i<ch.length; i++) {
            // don't escape letters, digits, and marks
            if ((ch[i] >= 'A' && ch[i] <= 'Z') ||
                (ch[i] >= 'a' && ch[i] <= 'z') ||
                (ch[i] >= '0' && ch[i] <= '9') ||
                (ch[i] >= '\'' && ch[i] <= '*') || // ' ( ) *
                "%#-_.!~".indexOf(ch[i]) != -1)
               sb.append(ch[i]);
            // don't escape reserved characters (if requested)
            else if (!eReserved && ";/?:@&=+$,[]".indexOf(ch[i]) != -1)
               sb.append(ch[i]);
            // escape anything else
            else {
               osw.write(ch[i]);
               osw.flush();
               byte ba[] = baos.toByteArray();
               for (int j=0; j<ba.length; j++) {
                  int hex = (ba[j] >>> 4) & 0xF; // first 4 bits
                  sb.append('%')
                    .append(hex < 10 ? (char)('0' + hex) 
                                     : (char)('A' + hex - 10));
                  hex = ba[j] & 0xF; // last 4 bits
                  sb.append(hex < 10 ? (char)('0' + hex) 
                                     : (char)('A' + hex - 10));
               }
               baos.reset();
            }
         }
         return new Value(sb.toString());
      }
      catch (IOException ex) {
         throw new EvalException("Fatal: " + ex.toString());
      }
   }
}