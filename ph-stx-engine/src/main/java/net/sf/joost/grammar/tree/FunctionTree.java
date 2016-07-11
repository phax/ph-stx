/*
 * $Id: FunctionTree.java,v 1.8 2009/08/21 12:46:18 obecker Exp $
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
 * Contributor(s): Thomas Behrends.
 */

package net.sf.joost.grammar.tree;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory;

/**
 * Objects of FunctionTree represent function calls in the syntax tree of a
 * pattern or an STXPath expression.
 * 
 * @version $Revision: 1.8 $ $Date: 2009/08/21 12:46:18 $
 * @author Oliver Becker
 */
public final class FunctionTree extends AbstractTree
{
  /*** the function instance */
  private final FunctionFactory.Instance func;

  /**
   * Constructs a FunctionTree object.
   * 
   * @param qName
   *        the qualified function name
   * @param left
   *        the parameters
   * @param context
   *        the parse context
   */
  public FunctionTree (final String qName, final AbstractTree left, final ParseContext context) throws SAXParseException
  {
    super (FUNCTION, left, null);

    final int colon = qName.indexOf (":");
    if (colon != -1)
    {
      uri = (String) context.nsSet.get (qName.substring (0, colon));
      if (uri == null)
      {
        throw new SAXParseException ("Undeclared prefix '" + qName.substring (0, colon) + "'", context.locator);
      }
      lName = qName.substring (colon + 1);
    }
    else
    {
      uri = CSTX.FUNC_NS;
      lName = qName;
    }

    func = context.getFunctionFactory ().getFunction (uri, lName, qName, left);
  }

  @Override
  public Value evaluate (final Context context, final int top) throws SAXException
  {
    try
    {
      return func.evaluate (context, top, left);
    }
    catch (final EvalException e)
    {
      context.errorHandler.error (e.getMessage (),
                                  context.currentInstruction.publicId,
                                  context.currentInstruction.systemId,
                                  context.currentInstruction.lineNo,
                                  context.currentInstruction.colNo,
                                  e);
      // if the errorHandler decides to continue ...
      return Value.VAL_EMPTY;
    }
  }

  @Override
  public boolean isConstant ()
  {
    return func.isConstant () && (left == null || left.isConstant ());
  }
}
