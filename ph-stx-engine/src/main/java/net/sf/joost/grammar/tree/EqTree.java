/**
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.1 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is: this file
 *
 *  The Initial Developer of the Original Code is Oliver Becker.
 *
 *  Portions created by Philip Helger
 *  are Copyright (C) 2016-2017 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.grammar.tree;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;

/**
 * Objects of EqTree represent comparison nodes (equal, "=") in the syntax tree
 * of a pattern or an STXPath expression.
 *
 * @version $Revision: 1.2 $ $Date: 2005/01/23 19:47:27 $
 * @author Oliver Becker
 */
public final class EqTree extends AbstractTree
{
  public EqTree (final AbstractTree left, final AbstractTree right)
  {
    super (EQ, left, right);
  }

  @Override
  public Value evaluate (final Context context, final int top) throws SAXException
  {
    final Value v1 = m_aLeft.evaluate (context, top);
    final Value v2 = m_aRight.evaluate (context, top);
    if (v1.type == Value.EMPTY || v2.type == Value.EMPTY)
      return Value.VAL_FALSE;

    // sequences: find a pair such that the comparison is true
    for (Value vi = v1; vi != null; vi = vi.next)
    {
      for (Value vj = v2; vj != null; vj = vj.next)
      {
        if (vi.type == Value.BOOLEAN || vj.type == Value.BOOLEAN)
        {
          if (vi.getBooleanValue () == vj.getBooleanValue ())
            return Value.VAL_TRUE;
        }
        else
          if (vi.type == Value.NUMBER || vj.type == Value.NUMBER)
          {
            if (vi.getNumberValue () == vj.getNumberValue ())
              return Value.VAL_TRUE;
          }
          else
          {
            if (vi.getStringValue ().equals (vj.getStringValue ()))
              return Value.VAL_TRUE;
          }
      }
    }
    // none of the item comparisons evaluated to true
    return Value.VAL_FALSE;
  }
}
