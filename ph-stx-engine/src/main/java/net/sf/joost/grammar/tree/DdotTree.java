/*
 * $Id: DdotTree.java,v 1.2 2007/05/20 18:00:44 obecker Exp $
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

import net.sf.joost.grammar.AbstractReversableTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;

/**
 * Objects of DdotTree represent a ".." step in the syntax tree of a pattern or
 * an STXPath expression.
 *
 * @version $Revision: 1.2 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class DdotTree extends AbstractReversableTree
{
  public DdotTree ()
  {
    super (DDOT);
  }

  @Override
  public Value evaluate (final Context context, final int top) throws SAXException
  {
    if (top > 1)
    {
      if (m_aRight != null)
      {
        // path continues, evaluate recursively with top-1
        return m_aRight.evaluate (context, top - 1);
      }
      // return the node at position top-1
      return new Value (context.ancestorStack.elementAt (top - 2));
    }
    // path selects nothing
    return Value.VAL_EMPTY;
  }

  @Override
  public boolean isConstant ()
  {
    return false;
  }
}
