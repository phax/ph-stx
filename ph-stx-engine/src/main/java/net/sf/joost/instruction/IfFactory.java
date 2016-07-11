/*
 * $Id: IfFactory.java,v 2.10 2008/10/04 17:13:14 obecker Exp $
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

/**
 * Factory for <code>if</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.10 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class IfFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames;

  // Constructor
  public IfFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("test");
  }

  /** @return <code>"if"</code> */
  @Override
  public String getName ()
  {
    return "if";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final AbstractTree testExpr = parseRequiredExpr (qName, attrs, "test", context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, testExpr);
  }

  /** Represents an instance of the <code>if</code> element. */
  public final class Instance extends AbstractNodeBase
  {
    /** the parsed <code>select</code> expression */
    private AbstractTree test;

    /** next instruction if the test evaluates to true */
    private AbstractInstruction trueNext;

    /** next instruction if the test evaluates to false */
    private AbstractInstruction falseNext;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final AbstractTree test)
    {
      super (qName, parent, context, true);
      this.test = test;
    }

    /**
     * Assign {@link #trueNext} and {@link #falseNext}
     */
    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXException
    {
      if (pass == 0) // nodeEnd.next not available yet
        return true;

      // adjust true and false branches
      trueNext = next;
      falseNext = m_aNodeEnd.next;
      if (falseNext instanceof ElseFactory.Instance)
        m_aNodeEnd.next = ((ElseFactory.Instance) falseNext).m_aNodeEnd.next;

      return false; // done
    }

    /**
     * Evaluates the expression given in the test attribute and change the value
     * of the <code>next</code> instruction.
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
      {
        // skip if instruction
        next = falseNext;
      }
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
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
      return "stx:if test='" + test + "'";
    }
  }
}
