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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.instruction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Value;
import net.sf.joost.util.VariableNotFoundException;
import net.sf.joost.util.VariableUtils;

/**
 * Factory for <code>assign</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.12 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class AssignFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public AssignFactory ()
  {
    attrNames.add ("name");
    attrNames.add ("select");
  }

  /** @return <code>"assign"</code> */
  @Override
  public String getName ()
  {
    return "assign";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final String nameAtt = getRequiredAttribute (qName, attrs, "name", context);
    final String varName = getExpandedName (nameAtt, context);

    final AbstractTree selectExpr = parseExpr (attrs.getValue ("select"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAtt, varName, selectExpr);
  }

  /** Represents an instance of the <code>assign</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    public String m_sVarName, m_sExpName;
    private AbstractTree m_aSelect;
    private final String m_sErrorMessage;

    private boolean m_bScopeDetermined = false;
    private AbstractGroupBase m_aGroupScope;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final String varName,
                        final String expName,
                        final AbstractTree select)
    {
      super (qName,
             parent,
             context,
             // this element must be empty if there is a select attribute
             select == null);
      this.m_sVarName = varName;
      this.m_sExpName = expName;
      this.m_aSelect = select;
      this.m_sErrorMessage = "('" + qName + "' started in line " + lineNo + ")";
    }

    /**
     * Evaluate the <code>select</code> attribute if present.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      // does this variable have a select attribute?
      if (m_aSelect != null)
      {
        final Value v = m_aSelect.evaluate (context, this);
        processVar (v, context);
      }
      else
      {
        // endInstruction present
        super.process (context);
        // create a new StringEmitter for this instance and put it
        // on the emitter stack
        context.pushEmitter (new StringEmitter (new StringBuffer (), m_sErrorMessage));
      }
      return CSTX.PR_CONTINUE;
    }

    /**
     * Called only if this instruction has no <code>select</code> attribute.
     * Evaluates its contents.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      // use contents
      final Value v = new Value (((StringEmitter) context.popEmitter ()).getBuffer ().toString ());

      processVar (v, context);

      return super.processEnd (context);
    }

    /**
     * Assigns a value to a variable.
     *
     * @param v
     *        the value
     * @param context
     *        the current context
     */
    private void processVar (final Value v, final Context context) throws SAXException
    {
      if (!m_bScopeDetermined)
      {
        try
        {
          m_aGroupScope = VariableUtils.findVariableScope (context, m_sExpName);
        }
        catch (final VariableNotFoundException e)
        {
          context.m_aErrorHandler.error ("Can't assign to undeclared variable '" +
                                         m_sVarName +
                                         "'",
                                         m_sPublicID,
                                         m_sSystemID,
                                         lineNo,
                                         colNo);
          return; // if the errorHandler returns
        }
        m_bScopeDetermined = true;
      }

      final Hashtable <String, Value> vars = (m_aGroupScope == null) ? context.localVars
                                                                     : context.groupVars.get (m_aGroupScope).peek ();

      // assign new value
      vars.put (m_sExpName, v);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aGroupScope != null)
        theCopy.m_aGroupScope = (AbstractGroupBase) m_aGroupScope.deepCopy (copies);
      if (m_aSelect != null)
        theCopy.m_aSelect = m_aSelect.deepCopy (copies);
    }

  }
}
