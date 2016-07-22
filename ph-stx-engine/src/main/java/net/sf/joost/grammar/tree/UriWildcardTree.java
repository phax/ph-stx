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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.grammar.tree;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.SAXEvent;

/**
 * Objects of UriWildcardTree represent element name test "*:lname" nodes in the
 * syntax tree of a pattern or an STXPath expression.
 *
 * @version $Revision: 1.2 $ $Date: 2007/05/20 18:00:43 $
 * @author Oliver Becker
 */
public final class UriWildcardTree extends AbstractTree
{
  /**
   * Constructs a UriWildcardTree object with a given local name.
   *
   * @param lName
   *        the local name in the name test
   * @param context
   *        the parse context
   */
  public UriWildcardTree (final String lName, final ParseContext context)
  {
    super (URI_WILDCARD);
    this.m_sLocalName = lName;
  }

  @Override
  public boolean matches (final Context context, final int top, final boolean setPosition) throws SAXException
  {
    if (top < 2)
      return false;

    final SAXEvent e = context.ancestorStack.elementAt (top - 1);
    if (e.m_nType != SAXEvent.ELEMENT || !m_sLocalName.equals (e.m_sLocalName))
      return false;

    if (setPosition)
      context.position = context.ancestorStack.elementAt (top - 2).getPositionOf ("*", m_sLocalName);

    return true;
  }

  @Override
  public double getPriority ()
  {
    return -0.25;
  }

  @Override
  public boolean isConstant ()
  {
    return false;
  }
}
