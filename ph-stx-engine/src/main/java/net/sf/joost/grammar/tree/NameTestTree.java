/*
 * $Id: NameTestTree.java,v 1.3 2007/11/25 14:18:01 obecker Exp $
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
 * Objects of NameTestTree represent element name test nodes in the syntax tree
 * of a pattern or an STXPath expression.
 *
 * @version $Revision: 1.3 $ $Date: 2007/11/25 14:18:01 $
 * @author Oliver Becker
 */
public final class NameTestTree extends AbstractTree
{
  public NameTestTree (final String value, final ParseContext context) throws SAXParseException
  {
    super (NAME_TEST, value);

    // value contains the qualified name
    final int colon = value.indexOf (":");
    if (colon != -1)
    {
      m_sURI = context.nsSet.get (value.substring (0, colon));
      if (m_sURI == null)
      {
        throw new SAXParseException ("Undeclared prefix '" + value.substring (0, colon) + "'", context.locator);
      }
      m_sLocalName = value.substring (colon + 1);
    }
    else
    {
      // no qualified name: uri depends on the value of
      // <stx:transform stxpath-default-namespace="..." />
      m_sURI = context.transformNode.m_sStxpathDefaultNamespace;
      m_sLocalName = value;
    }
  }

  @Override
  public boolean matches (final Context context, final int top, final boolean setPosition) throws SAXException
  {
    if (top < 2)
      return false;

    final SAXEvent e = context.ancestorStack.elementAt (top - 1);
    if (e.m_nType != SAXEvent.ELEMENT || !(m_sURI.equals (e.m_sURI) && m_sLocalName.equals (e.m_sLocalName)))
      return false;

    if (setPosition)
      context.position = context.ancestorStack.elementAt (top - 2).getPositionOf (m_sURI, m_sLocalName);

    return true;
  }

  @Override
  public double getPriority ()
  {
    return 0;
  }

  @Override
  public boolean isConstant ()
  {
    return false;
  }
}
