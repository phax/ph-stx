/*
 * $Id: UnionTree.java,v 1.1 2004/09/29 05:59:51 obecker Exp $
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

/**
 * Objects of UnionTree represent union nodes ("|") in the syntax tree of a
 * pattern or an STXPath expression.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/09/29 05:59:51 $
 * @author Oliver Becker
 */
public final class UnionTree extends AbstractTree
{
  public UnionTree (final AbstractTree left, final AbstractTree right)
  {
    super (UNION, left, right);
  }

  @Override
  public boolean matches (final Context context, final int top, final boolean setPosition) throws SAXException
  {
    // Note: templates with a pattern containing a UNION will be split.
    // This branch should be encountered only for patterns at other
    // places (for example in <stx:copy attributes="pattern" /> or
    // <stx:process-siblings while="pattern" />
    if (m_aLeft.matches (context, top, false))
      return true;
    return m_aRight.matches (context, top, false);
  }

  @Override
  public double getPriority ()
  {
    return Double.NaN;
  }
}
