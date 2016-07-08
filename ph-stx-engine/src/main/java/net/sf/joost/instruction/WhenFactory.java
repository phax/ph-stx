/*
 * $Id: WhenFactory.java,v 2.13 2008/10/04 17:13:14 obecker Exp $
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

/**
 * Factory for <code>when</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.13 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

final public class WhenFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet <String> attrNames;

  //
  // Constructor
  //
  public WhenFactory ()
  {
    attrNames = new HashSet <> ();
    attrNames.add ("test");
  }

  /** @return <code>"when"</code> */
  @Override
  public String getName ()
  {
    return "when";
  }

  @Override
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    if (!(parent instanceof ChooseFactory.Instance))
      throw new SAXParseException ("'" + qName + "' must be child of stx:choose", context.locator);

    final Tree testExpr = parseRequiredExpr (qName, attrs, "test", context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, testExpr);
  }

  /** Represents an instance of the <code>when</code> element. */
  final public class Instance extends NodeBase
  {
    private Tree test;

    private AbstractInstruction trueNext, falseNext;

    protected Instance (final String qName, final NodeBase parent, final ParseContext context, final Tree test)
    {
      super (qName, parent, context, true);
      this.test = test;
    }

    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXException
    {
      if (pass == 0) // nodeEnd.next not available yet
        return true;

      final AbstractInstruction siblingOfChoose = parent.nodeEnd.next;
      if (next == nodeEnd)
        next = siblingOfChoose;
      trueNext = next;
      falseNext = nodeEnd.next; // the sibling
      nodeEnd.next = siblingOfChoose;
      return false;
    }

    /**
     * Evaluate the <code>test</code> attribute and adjust the next instruction
     * depending on the result
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      if (test.evaluate (context, this).getBooleanValue ())
      {
        super.process (context);
        next = trueNext;
      }
      else
        next = falseNext;
      return PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (test != null)
        theCopy.test = test.deepCopy (copies);
      if (trueNext != null)
        theCopy.trueNext = trueNext.deepCopy (copies);
      if (falseNext != null)
        theCopy.falseNext = falseNext.deepCopy (copies);
    }

    //
    // for debugging
    //
    @Override
    public String toString ()
    {
      return "stx:when test='" + test + "'";
    }
  }
}
