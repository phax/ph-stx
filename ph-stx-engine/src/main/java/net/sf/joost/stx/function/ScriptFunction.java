/*
 * $Id: ScriptFunction.java,v 1.5 2007/05/20 18:00:44 obecker Exp $
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
 * Contributor(s): Nikolay Fiykov.
 */

package net.sf.joost.stx.function;

import java.util.Stack;
import java.util.Vector;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * An instance of this class represents a Javascript extension function defined
 * by the <code>joost:script</code> element.
 *
 * @see net.sf.joost.instruction.ScriptFactory
 * @version $Revision: 1.5 $ $Date: 2007/05/20 18:00:44 $
 * @author Nikolay Fiykov, Oliver Becker
 */
public final class ScriptFunction implements IInstance
{
  /** BSF script engine instance */
  // BSFEngine engine;

  /** the local function name without prefix for this script function */
  String funcName;

  /**
   * the qualified function name including the prefix for this script function
   */
  String qName;

  // public ScriptFunction (final BSFEngine engine, final String funcName, final
  // String qName)
  // {
  // this.engine = engine;
  // this.funcName = funcName;
  // this.qName = qName;
  // }

  /**
   * convert Joost-STXPath arguments Value-tree into an array of simple Objects
   *
   * @param top
   * @param args
   * @return Object[]
   */
  private Object [] convertInputArgs (final Context context, final int top, final AbstractTree args) throws SAXException
  {
    // evaluate current parameters
    final Stack <Value> varr = new Stack<> ();
    if (args != null)
    {
      AbstractTree t = args;
      do
      {
        if (t.m_aRight != null)
          varr.push (t.m_aRight.evaluate (context, top));
        if (t.m_aLeft == null)
          varr.push (t.evaluate (context, top));
      } while ((t = t.m_aLeft) != null);
    }

    // convert values to java objects
    final Vector <Object> ret = new Vector<> ();
    while (!varr.isEmpty ())
    {
      final Value v = varr.pop ();
      try
      {
        ret.add (v.toJavaObject (Object.class));
      }
      catch (final EvalException e)
      {
        // Mustn't happen!
        throw new SAXException (e);
      }
    }

    return ret.toArray ();
  }

  /**
   * evaluate the script function with given input arguments and return the
   * result
   */
  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException,
                                                                                        EvalException
  {
    // convert input params
    @SuppressWarnings ("unused")
    final Object [] scrArgs = convertInputArgs (context, top, args);

    final Object ret = null;
    // execute the script function
    // try
    // {
    // ret = engine.call (null, funcName, scrArgs);
    // }
    // catch (final BSFException e)
    // {
    // throw new EvalException ("Exception while executing " + qName, e);
    // }

    // wrap the result
    return new Value (ret);
  }

  // These functions will never be called.
  // However, they are required by the Instance interface.

  /** Not called */
  public int getMinParCount ()
  {
    return 0;
  }

  /** Not called */
  public int getMaxParCount ()
  {
    return 0;
  }

  /** Not called */
  public String getName ()
  {
    return null;
  }

  /** @return <code>false</code> (we don't know) */
  public boolean isConstant ()
  {
    return false;
  }
}
