/*
 * $Id: ForEachFactory.java,v 2.11 2008/10/04 17:13:14 obecker Exp $
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
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Value;

/**
 * Factory for <code>for-each-item</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 2.11 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class ForEachFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames;

  // Constructor
  public ForEachFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("name");
    attrNames.add ("select");
  }

  /** @return <code>"for-each-item"</code> */
  @Override
  public String getName ()
  {
    return "for-each-item";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final String nameAtt = getRequiredAttribute (qName, attrs, "name", context);
    final String expName = getExpandedName (nameAtt, context);

    final AbstractTree selectExpr = parseRequiredExpr (qName, attrs, "select", context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAtt, expName, selectExpr);
  }

  /** Represents an instance of the <code>for-each-item</code> element. */
  public final class Instance extends AbstractNodeBase
  {
    private final String m_sVarName, m_sExpName;
    private AbstractTree m_aSelect;

    /**
     * Stack that stores the remaining sequence of the select attribute in case
     * this for-each-item was interrupted via
     * <code>stx:process-<em>xxx</em></code>
     */
    private Stack <Value> m_aResultStack = new Stack<> ();

    private AbstractInstruction contents, successor;

    /**
     * Determines whether this instruction is encountered the first time
     * (<code>false</code>; i.e. the <code>select</code> attribute needs to be
     * evaluated) or during the processing (<code>true</code>; i.e. this is part
     * of the loop)
     */
    private boolean continued = false;

    // Constructor
    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final String varName,
                        final String expName,
                        final AbstractTree select)
    {
      super (qName, parent, context, true);
      this.m_sVarName = varName;
      this.m_sExpName = expName;
      this.m_aSelect = select;

      // this instruction declares a local variable
      m_aScopedVariables = new Vector<> ();
    }

    /**
     * Create the loop by connecting the end with the start
     */
    @Override
    public boolean compile (final int pass, final ParseContext context)
    {
      if (pass == 0) // successor not available yet
        return true;

      contents = next;
      successor = m_aNodeEnd.next;
      m_aNodeEnd.next = this; // loop
      return false;
    }

    /**
     * If {@link #continued} is <code>true</code> then take the next item from a
     * previously computed sequence, otherwise evaluate the <code>select</code>
     * attribute and take the first item.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      Value selectResult;
      if (continued)
      {
        selectResult = m_aResultStack.pop ();
        continued = false;
      }
      else
      {
        // perform this check only once per for-each-item
        if (context.localVars.get (m_sExpName) != null)
        {
          context.m_aErrorHandler.fatalError ("Variable '" +
                                           m_sVarName +
                                           "' already declared",
                                           m_sPublicID,
                                           m_sSystemID,
                                           lineNo,
                                           colNo);
          return CSTX.PR_ERROR;// if the errorHandler returns
        }

        selectResult = m_aSelect.evaluate (context, this);
      }

      if (selectResult == null || selectResult.type == Value.EMPTY)
      {
        // for-each-item finished (empty sequence left)
        next = successor;
        return CSTX.PR_CONTINUE;
      }

      super.process (context); // enter new scope for local variables
      m_aResultStack.push (selectResult.next);
      selectResult.next = null;

      context.localVars.put (m_sExpName, selectResult);
      declareVariable (m_sExpName);

      next = contents;
      return CSTX.PR_CONTINUE;
    }

    /**
     * Sets {@link #continued} to <code>true</code> to signal the loop.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      continued = true;
      return super.processEnd (context);
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
      if (m_aSelect != null)
        theCopy.m_aSelect = m_aSelect.deepCopy (copies);
      theCopy.continued = false;
      theCopy.m_aResultStack = new Stack<> ();
    }
  }
}
