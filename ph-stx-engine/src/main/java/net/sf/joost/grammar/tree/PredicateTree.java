/*
 * $Id: PredicateTree.java,v 1.2 2007/05/20 18:00:44 obecker Exp $
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

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;

/**
 * Objects of PredicateTree represent predicate nodes in the syntax tree of a
 * pattern or an STXPath expression.
 *
 * @version $Revision: 1.2 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class PredicateTree extends AbstractTree
{
  /**
   * Constructs a PredicateTree.
   *
   * @param left
   *        the path before the predicate
   * @param right
   *        the contents of the predicate
   */
  public PredicateTree (final AbstractTree left, final AbstractTree right)
  {
    super (PREDICATE, left, right);
  }

  @Override
  public boolean matches (final Context context, final int top, final boolean setPosition) throws SAXException
  {
    // save position in case it mustn't change
    final long pos = context.position;
    boolean retValue = false;
    if (top > 1 &&
        // allow set position for evaluating the predicate
        left.matches (context, top, true))
    {
      final Value v = right.evaluate (context, top);
      if (v.type == Value.NUMBER)
        retValue = (context.position == Math.round (v.getNumberValue ()));
      else
        retValue = v.getBooleanValue ();
    }
    if (!setPosition)
    {
      // restore old position
      context.position = pos;
    }
    return retValue;
  }

  @Override
  public boolean isConstant ()
  {
    return false;
  }
}
