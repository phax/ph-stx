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

/**
 * Factory for <code>when</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.13 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class WhenFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  public WhenFactory ()
  {
    attrNames.add ("test");
  }

  /** @return <code>"when"</code> */
  @Override
  public String getName ()
  {
    return "when";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    if (!(parent instanceof ChooseFactory.Instance))
      throw new SAXParseException ("'" + qName + "' must be child of stx:choose", context.locator);

    final AbstractTree testExpr = parseRequiredExpr (qName, attrs, "test", context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, testExpr);
  }

  /** Represents an instance of the <code>when</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private AbstractTree m_aTest;
    private AbstractInstruction m_aTrueNext, m_aFalseNext;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final AbstractTree test)
    {
      super (qName, parent, context, true);
      this.m_aTest = test;
    }

    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXException
    {
      if (pass == 0) // nodeEnd.next not available yet
        return true;

      final AbstractInstruction siblingOfChoose = m_aParent.m_aNodeEnd.next;
      if (next == m_aNodeEnd)
        next = siblingOfChoose;
      m_aTrueNext = next;
      m_aFalseNext = m_aNodeEnd.next; // the sibling
      m_aNodeEnd.next = siblingOfChoose;
      return false;
    }

    /**
     * Evaluate the <code>test</code> attribute and adjust the next instruction
     * depending on the result
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      if (m_aTest.evaluate (context, this).getBooleanValue ())
      {
        super.process (context);
        next = m_aTrueNext;
      }
      else
        next = m_aFalseNext;
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aTest != null)
        theCopy.m_aTest = m_aTest.deepCopy (copies);
      if (m_aTrueNext != null)
        theCopy.m_aTrueNext = m_aTrueNext.deepCopy (copies);
      if (m_aFalseNext != null)
        theCopy.m_aFalseNext = m_aFalseNext.deepCopy (copies);
    }

    //
    // for debugging
    //
    @Override
    public String toString ()
    {
      return "stx:when test='" + m_aTest + "'";
    }
  }
}
