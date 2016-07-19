/*
 * $Id: PSiblingsFactory.java,v 2.5 2008/10/04 17:13:14 obecker Exp $
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.SAXEvent;

/**
 * Factory for <code>process-siblings</code> elements, which are represented by
 * the inner Instance class.
 *
 * @version $Revision: 2.5 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public class PSiblingsFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  public PSiblingsFactory ()
  {
    attrNames.add ("group");
    attrNames.add ("while");
    attrNames.add ("until");
  }

  /** @return <code>"process-siblings"</code> */
  @Override
  public String getName ()
  {
    return "process-siblings";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final String groupAtt = attrs.getValue ("group");

    final AbstractTree whilePattern = parsePattern (attrs.getValue ("while"), context);

    final AbstractTree untilPattern = parsePattern (attrs.getValue ("until"), context);

    checkAttributes (qName, attrs, attrNames, context);

    return new Instance (qName, parent, context, groupAtt, whilePattern, untilPattern);
  }

  /** The inner Instance class */
  public static final class Instance extends AbstractProcessBase
  {
    private AbstractTree m_aWhilePattern, m_aUntilPattern;
    private AbstractGroupBase parentGroup;

    public Instance (final String qName,
                     final AbstractNodeBase aParent,
                     final ParseContext context,
                     final String groupQName,
                     final AbstractTree whilePattern,
                     final AbstractTree untilPattern) throws SAXParseException
    {
      super (qName, aParent, context, groupQName, null, null);
      this.m_aWhilePattern = whilePattern;
      this.m_aUntilPattern = untilPattern;

      // determine parent group (needed for matches())
      AbstractNodeBase parent = aParent;
      do
      {
        // parent itself is not a group
        parent = parent.m_aParent;
      } while (!(parent instanceof AbstractGroupBase));
      parentGroup = (AbstractGroupBase) parent;
    }

    /**
     * @return {@link #PR_SELF} if the context node can have siblings
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      // no need to call super.processEnd(), there are no local
      // variable declarations
      final SAXEvent event = context.ancestorStack.peek ();
      if (event.m_nType == SAXEvent.ATTRIBUTE || event.m_nType == SAXEvent.ROOT)
      {
        // These nodes don't have siblings, keep processing.
        return CSTX.PR_CONTINUE;
      }

      // store this instruction (the Processor object will store it)
      context.psiblings = this;
      return CSTX.PR_SIBLINGS;
    }

    /**
     * Tests if the current node matches the <code>while</code> and
     * <code>until</code> conditions of this <code>stx:process-siblings</code>
     * instruction.
     *
     * @return <code>true</code> if the current node matches the pattern in the
     *         <code>while</code> attribute and doesn't match the pattern in the
     *         <code>until</code> attribute; and <code>false</code> otherwise
     */
    public boolean matches (final Context context) throws SAXException
    {
      context.currentInstruction = this;
      context.currentGroup = parentGroup;
      return (m_aWhilePattern == null || m_aWhilePattern.matches (context, context.ancestorStack.size (), false)) &&
             (m_aUntilPattern == null || !m_aUntilPattern.matches (context, context.ancestorStack.size (), false));
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (parentGroup != null)
        theCopy.parentGroup = (AbstractGroupBase) parentGroup.deepCopy (copies);
      if (m_aUntilPattern != null)
        theCopy.m_aUntilPattern = m_aUntilPattern.deepCopy (copies);
      if (m_aWhilePattern != null)
        theCopy.m_aWhilePattern = m_aWhilePattern.deepCopy (copies);
    }
  }
}
