/*
 * $Id: LocalWildcardTree.java,v 1.3 2007/11/25 14:18:01 obecker Exp $
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

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.SAXEvent;

/**
 * Objects of LocalWildcardTree represent element name test "prefix:*" nodes in
 * the syntax tree of a pattern or an STXPath expression.
 * 
 * @version $Revision: 1.3 $ $Date: 2007/11/25 14:18:01 $
 * @author Oliver Becker
 */
public final class LocalWildcardTree extends AbstractTree
{
  /**
   * Constructs a LocalWildcardTree object with a given namespace prefix.
   * 
   * @param prefix
   *        the namespace prefix of the name test
   * @param context
   *        the parse context
   */
  public LocalWildcardTree (final String prefix, final ParseContext context) throws SAXParseException
  {
    super (LOCAL_WILDCARD);

    uri = (String) context.nsSet.get (prefix);
    if (uri == null)
      throw new SAXParseException ("Undeclared prefix '" + prefix + "'", context.locator);
  }

  @Override
  public boolean matches (final Context context, final int top, final boolean setPosition) throws SAXException
  {
    if (top < 2)
      return false;

    final SAXEvent e = (SAXEvent) context.ancestorStack.elementAt (top - 1);
    if (e.type != SAXEvent.ELEMENT || !uri.equals (e.uri))
      return false;

    if (setPosition)
      context.position = ((SAXEvent) context.ancestorStack.elementAt (top - 2)).getPositionOf (uri, "*");

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
