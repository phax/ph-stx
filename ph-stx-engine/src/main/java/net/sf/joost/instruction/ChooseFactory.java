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
package net.sf.joost.instruction;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>choose</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.7 $ $Date: 2008/05/17 17:01:03 $
 * @author Oliver Becker
 */

public final class ChooseFactory extends AbstractFactoryBase
{
  /** @return <code>"choose"</code> */
  @Override
  public String getName ()
  {
    return "choose";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    checkAttributes (qName, attrs, null, context);
    return new Instance (qName, parent, context);
  }

  /** Represents an instance of the <code>choose</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private boolean otherwisePresent;

    protected Instance (final String qName, final AbstractNodeBase parent, final ParseContext context)
    {
      super (qName, parent, context, true);
      otherwisePresent = false;
    }

    /**
     * Ensures that only <code>stx:when</code> and <code>stx:otherwise</code>
     * children will be inserted.
     */
    @Override
    public void insert (final AbstractNodeBase node) throws SAXParseException
    {
      if (node instanceof TextNode)
      {
        if (((TextNode) node).isWhitespaceNode ())
          return;
        throw new SAXParseException ("'" +
                                     m_sQName +
                                     "' may only contain stx:when and stx:otherwise children " +
                                     "(encountered text)",
                                     node.m_sPublicID,
                                     node.m_sSystemID,
                                     node.lineNo,
                                     node.colNo);
      }

      if (!(node instanceof WhenFactory.Instance || node instanceof OtherwiseFactory.Instance))
        throw new SAXParseException ("'" +
                                     m_sQName +
                                     "' may only contain stx:when and stx:otherwise children " +
                                     "(encountered '" +
                                     node.m_sQName +
                                     "')",
                                     node.m_sPublicID,
                                     node.m_sSystemID,
                                     node.lineNo,
                                     node.colNo);

      if (otherwisePresent)
        throw new SAXParseException ("'" +
                                     m_sQName +
                                     "' must not have more children after stx:otherwise",
                                     node.m_sPublicID,
                                     node.m_sSystemID,
                                     node.lineNo,
                                     node.colNo);

      if (node instanceof OtherwiseFactory.Instance)
      {
        if (m_aLastChild == this)
        {
          throw new SAXParseException ("'" +
                                       m_sQName +
                                       "' must have at least one stx:when child " +
                                       "before stx:otherwise",
                                       node.m_sPublicID,
                                       node.m_sSystemID,
                                       node.lineNo,
                                       node.colNo);
        }
        otherwisePresent = true;
      }

      super.insert (node);
    }

    /**
     * Check if there is at least one child.
     */
    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXParseException
    {
      if (m_aLastChild == this)
        throw new SAXParseException ("'" +
                                     m_sQName +
                                     "' must have at least one stx:when child",
                                     m_sPublicID,
                                     m_sSystemID,
                                     lineNo,
                                     colNo);

      mayDropEnd ();
      return false;
    }

    // No specific process and processEnd methods necessary.
    // The magic of stx:choose is completely in WhenFactory.Instance.compile
  }
}
