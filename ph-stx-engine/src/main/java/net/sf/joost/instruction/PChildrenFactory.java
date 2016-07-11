/*
 * $Id: PChildrenFactory.java,v 2.4 2007/11/25 14:18:01 obecker Exp $
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
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.instruction;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.SAXEvent;

/**
 * Factory for <code>process-children</code> elements, which are represented by
 * the inner Instance class.
 *
 * @version $Revision: 2.4 $ $Date: 2007/11/25 14:18:01 $
 * @author Oliver Becker
 */

public class PChildrenFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames;

  // Constructor
  public PChildrenFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("group");
    attrNames.add ("filter-method");
    attrNames.add ("filter-src");
  }

  /** @return <code>"process-children"</code> */
  @Override
  public String getName ()
  {
    return "process-children";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final String groupAtt = attrs.getValue ("group");

    final String filterMethodAtt = attrs.getValue ("filter-method");

    if (groupAtt != null && filterMethodAtt != null)
      throw new SAXParseException ("It's not allowed to use both 'group' and 'filter-method' attributes",
                                   context.locator);

    final String filterSrcAtt = attrs.getValue ("filter-src");

    if (filterSrcAtt != null && filterMethodAtt == null)
      throw new SAXParseException ("Missing 'filter-method' attribute in '" +
                                   qName +
                                   "' ('filter-src' is present)",
                                   context.locator);

    checkAttributes (qName, attrs, attrNames, context);

    return new Instance (qName, parent, context, groupAtt, filterMethodAtt, filterSrcAtt);
  }

  /** The inner Instance class */
  public static final class Instance extends AbstractProcessBase
  {
    // Constructor
    public Instance (final String qName,
                     final AbstractNodeBase parent,
                     final ParseContext context,
                     final String groupQName,
                     final String method,
                     final String src) throws SAXParseException
    {
      super (qName, parent, context, groupQName, method, src);
    }

    /**
     * @return {@link #PR_CHILDREN} if the context node is an element or the
     *         root
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      // no need to call super.processEnd(), there are no local
      // variable declarations
      final SAXEvent event = context.ancestorStack.peek ();
      if (event.m_nType == SAXEvent.ELEMENT || event.m_nType == SAXEvent.ROOT)
      {
        if (hasFilter ())
        {
          // use external SAX filter (TransformerHandler)
          context.targetHandler = getProcessHandler (context);
          if (context.targetHandler == null)
            return CSTX.PR_ERROR;
        }
        return CSTX.PR_CHILDREN;
      }
      return CSTX.PR_CONTINUE;
    }
  }
}
