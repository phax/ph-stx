/*
 * $Id: AttrLocalWildcardTree.java,v 1.3 2007/11/25 14:18:01 obecker Exp $
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
import net.sf.joost.stx.Value;

/**
 * Objects of AttrLocalWildcardTree represent attribute tests nodes of the form
 * '@ns:*' in the syntax tree of a pattern or an STXPath expression.
 *
 * @version $Revision: 1.3 $ $Date: 2007/11/25 14:18:01 $
 * @author Oliver Becker
 */
public final class AttrLocalWildcardTree extends AbstractTree
{
  // needed only in the error message
  private final String m_sPrefix;

  /**
   * Constructs an AttrLocalWildcardTree object with a given namespace prefix
   *
   * @param prefix
   *        the namespace prefix
   * @param context
   *        the parse context
   */
  public AttrLocalWildcardTree (final String prefix, final ParseContext context) throws SAXParseException
  {
    super (ATTR_LOCAL_WILDCARD);
    this.m_sPrefix = prefix;
    m_sURI = context.nsSet.get (prefix);
    if (m_sURI == null)
      throw new SAXParseException ("Undeclared prefix '" + prefix + "'", context.locator);
  }

  @Override
  public boolean matches (final Context context, final int top, final boolean setPosition) throws SAXException
  {
    // an attribute requires at least two ancestors
    if (top < 3)
      return false;
    final SAXEvent e = context.ancestorStack.elementAt (top - 1);
    if (e.m_nType != SAXEvent.ATTRIBUTE)
      return false;
    if (setPosition)
      context.position = 1; // position for attributes is undefined

    if (m_sURI.equals (e.m_sURI))
      return true;
    return false;
  }

  @Override
  public Value evaluate (final Context context, final int top) throws SAXException
  {
    Value v1;
    // determine effective parent node sequence (-> v1)
    if (m_aLeft != null)
    { // preceding path
      v1 = m_aLeft.evaluate (context, top);
      if (v1.type == Value.EMPTY)
        return v1;
    }
    else
      if (top > 0) // use current node
        v1 = new Value (context.ancestorStack.elementAt (top - 1));
      else
        return Value.VAL_EMPTY;

    // iterate through this node sequence
    Value ret = null, last = null; // for constructing the result seq
    do
    {
      final SAXEvent e = v1.getNode ();
      if (e == null)
      {
        context.m_aErrorHandler.error ("Current item for evaluating '@" +
                                    m_sPrefix +
                                    ":*' is not a node (got " +
                                    v1 +
                                    ")",
                                    context.currentInstruction.m_sPublicID,
                                    context.currentInstruction.m_sSystemID,
                                    context.currentInstruction.lineNo,
                                    context.currentInstruction.colNo);
        // if the errorHandler decides to continue ...
        return Value.VAL_EMPTY;
      }

      final int len = e.m_aAttrs.getLength ();
      // iterate through attribute list
      for (int i = 0; i < len; i++)
      {
        if (m_sURI.equals (e.m_aAttrs.getURI (i)))
        {
          final Value v2 = new Value (SAXEvent.newAttribute (m_sURI,
                                                             e.m_aAttrs.getLocalName (i),
                                                             e.m_aAttrs.getQName (i),
                                                             e.m_aAttrs.getValue (i)));
          if (last != null)
            last.next = v2;
          else
            ret = v2;
          last = v2;
        }
      } // for
      v1 = v1.next; // next node
    } while (v1 != null);

    if (ret != null)
      return ret;
    return Value.VAL_EMPTY;
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
