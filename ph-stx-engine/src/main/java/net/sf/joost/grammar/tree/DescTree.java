/*
 * $Id: DescTree.java,v 1.2 2007/05/20 18:00:44 obecker Exp $
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
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;

/**
 * Objects of DescTree represent a descendant step "//" in the syntax tree of a
 * pattern or an STXPath expression.
 *
 * @version $Revision: 1.2 $ $Date: 2007/05/20 18:00:44 $
 * @author Oliver Becker
 */
public final class DescTree extends AbstractReversableTree
{
  public DescTree (final AbstractTree left, final AbstractTree right)
  {
    super (DESC, left, right);
  }

  @Override
  public boolean matches (final Context context, int top, final boolean setPosition) throws SAXException
  {
    // need at least 3 nodes (document, node1, node2), because
    // DESC may appear only between two nodes but not at the root
    if (top < 3)
      return false;
    if (right.matches (context, top, setPosition))
    {
      // look for a matching sub path on the left
      while (top > 1)
      {
        if (left.matches (context, top - 1, false))
          return true;
        top--;
      }
    }
    return false;
  }

  @Override
  public Value evaluate (final Context context, int top) throws SAXException
  {
    Value ret = null, last = null; // for constructing the result seq
    while (top < context.ancestorStack.size ())
    {
      final Value v1 = right.evaluate (context, top++);
      if (v1.type == Value.NODE)
      {
        if (ret != null)
        {
          // skip duplicates
          Value vi = v1;
          while (vi != null)
          {
            Value vj;
            for (vj = ret; vj != null; vj = vj.next)
              if (vi.getNode () == vj.getNode ())
                break;
            if (vj == null)
            { // vi not found in ret
              last.next = vi;
              last = vi;
            }
            vi = vi.next;
            last.next = null; // because last=vi above
          }
        }
        else
        {
          ret = v1;
          for (last = v1; last.next != null; last = last.next)
          {}
        }
      }
    }
    if (ret != null)
      return ret;
    // empty sequence
    return Value.VAL_EMPTY;
  }

  @Override
  public boolean isConstant ()
  {
    return false;
  }
}
