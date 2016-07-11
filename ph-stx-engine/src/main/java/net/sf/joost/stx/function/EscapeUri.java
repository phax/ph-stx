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

import org.xml.sax.SAXException;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>escape-uri</code> function.<br>
 * Applies URI escaping rules.
 *
 * @see <a target="xq1xp2fo" href=
 *      "http://www.w3.org/TR/xpath-functions/#func-escape-uri"> fn:escape-uri
 *      in "XQuery 1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.3 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class EscapeUri implements IInstance
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

  /** @return "escape-uri" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "escape-uri";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException, EvalException
  {
    final Value v = args.m_aLeft.evaluate (context, top);
    final String uri = v.getStringValue ();
    final boolean eReserved = args.m_aRight.evaluate (context, top).getBooleanValue ();

    try
    {
      final char [] ch = uri.toCharArray ();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream (4);
      final OutputStreamWriter osw = new OutputStreamWriter (baos, "UTF-8");
      final StringBuffer sb = new StringBuffer ();
      for (final char element : ch)
      {
        // don't escape letters, digits, and marks
        if ((element >= 'A' && element <= 'Z') ||
            (element >= 'a' && element <= 'z') ||
            (element >= '0' && element <= '9') ||
            (element >= '\'' && element <= '*') || // ' ( ) *
            "%#-_.!~".indexOf (element) != -1)
          sb.append (element);
        // don't escape reserved characters (if requested)
        else
          if (!eReserved && ";/?:@&=+$,[]".indexOf (element) != -1)
            sb.append (element);
          // escape anything else
          else
          {
            osw.write (element);
            osw.flush ();
            final byte ba[] = baos.toByteArray ();
            for (final byte element2 : ba)
            {
              int hex = (element2 >>> 4) & 0xF; // first 4 bits
              sb.append ('%').append (hex < 10 ? (char) ('0' + hex) : (char) ('A' + hex - 10));
              hex = element2 & 0xF; // last 4 bits
              sb.append (hex < 10 ? (char) ('0' + hex) : (char) ('A' + hex - 10));
            }
            baos.reset ();
          }
      }
      return new Value (sb.toString ());
    }
    catch (final IOException ex)
    {
      throw new EvalException ("Fatal: " + ex.toString ());
    }
  }
}
