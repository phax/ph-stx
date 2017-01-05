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
 * Objects of AvtTree represent attribute value trees.
 *
 * @version $Revision: 1.1 $ $Date: 2004/09/29 05:59:51 $
 * @author Oliver Becker
 */
public final class AvtTree extends AbstractTree
{
  public AvtTree (final AbstractTree left, final AbstractTree right)
  {
    super (AVT, left, right);
  }

  @Override
  public Value evaluate (final Context context, final int top) throws SAXException
  {
    final Value v1 = m_aRight.evaluate (context, top);
    if (m_aLeft != null)
    {
      final Value v2 = m_aLeft.evaluate (context, top);
      return new Value (v2.getStringValue () + v1.getStringValue ());
    }
    return new Value (v1.getStringValue ());
  }
}
