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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.grammar.Tree;
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

public class PSiblingsFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  // Constructor
  public PSiblingsFactory ()
  {
    attrNames = new HashSet ();
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
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    final String groupAtt = attrs.getValue ("group");

    final Tree whilePattern = parsePattern (attrs.getValue ("while"), context);

    final Tree untilPattern = parsePattern (attrs.getValue ("until"), context);

    checkAttributes (qName, attrs, attrNames, context);

    return new Instance (qName, parent, context, groupAtt, whilePattern, untilPattern);
  }

  /** The inner Instance class */
  public class Instance extends ProcessBase
  {
    private Tree whilePattern, untilPattern;
    private GroupBase parentGroup;

    public Instance (final String qName,
                     NodeBase parent,
                     final ParseContext context,
                     final String groupQName,
                     final Tree whilePattern,
                     final Tree untilPattern) throws SAXParseException
    {
      super (qName, parent, context, groupQName, null, null);
      this.whilePattern = whilePattern;
      this.untilPattern = untilPattern;

      // determine parent group (needed for matches())
      do // parent itself is not a group
        parent = parent.parent;
      while (!(parent instanceof GroupBase));
      parentGroup = (GroupBase) parent;
    }

    /**
     * @return {@link #PR_SELF} if the context node can have siblings
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      // no need to call super.processEnd(), there are no local
      // variable declarations
      final SAXEvent event = (SAXEvent) context.ancestorStack.peek ();
      if (event.type == SAXEvent.ATTRIBUTE || event.type == SAXEvent.ROOT)
      {
        // These nodes don't have siblings, keep processing.
        return PR_CONTINUE;
      }
      else
      {
        // store this instruction (the Processor object will store it)
        context.psiblings = this;
        return PR_SIBLINGS;
      }
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
      return (whilePattern == null || whilePattern.matches (context, context.ancestorStack.size (), false)) &&
             (untilPattern == null || !untilPattern.matches (context, context.ancestorStack.size (), false));
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (parentGroup != null)
        theCopy.parentGroup = (GroupBase) parentGroup.deepCopy (copies);
      if (untilPattern != null)
        theCopy.untilPattern = untilPattern.deepCopy (copies);
      if (whilePattern != null)
        theCopy.whilePattern = whilePattern.deepCopy (copies);
    }

  }
}
