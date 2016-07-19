/*
 * $Id: WhileFactory.java,v 2.10 2008/10/04 17:13:14 obecker Exp $
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
 * Factory for <code>while</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.10 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class WhileFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public WhileFactory ()
  {
    attrNames.add ("test");
  }

  /** @return <code>"while"</code> */
  @Override
  public String getName ()
  {
    return "while";
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

  /** Represents an instance of the <code>while</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private AbstractTree m_aTest;
    private AbstractInstruction contents, successor;

    // Constructor
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
      if (pass == 0) // successor not available yet
        return true;

      mayDropEnd ();
      contents = next;
      successor = m_aNodeEnd.next;
      m_aNodeEnd.next = this; // loop
      return false; // done
    }

    /**
     * Evaluate the expression given in the test attribute and adjust the next
     * instruction depending on the result.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      if (m_aTest.evaluate (context, this).getBooleanValue ())
      {
        super.process (context);
        next = contents;
      }
      else
        next = successor;
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (contents != null)
        theCopy.contents = contents.deepCopy (copies);
      if (successor != null)
        theCopy.successor = successor.deepCopy (copies);
      if (m_aTest != null)
        theCopy.m_aTest = m_aTest.deepCopy (copies);
    }
  }
}
