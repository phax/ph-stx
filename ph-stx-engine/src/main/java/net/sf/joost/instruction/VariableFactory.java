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

/**
 * Factory for <code>variable</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 2.8 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class VariableFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public VariableFactory ()
  {
    attrNames.add ("name");
    attrNames.add ("select");
    attrNames.add ("keep-value");
  }

  /** @return <code>"variable"</code> */
  @Override
  public String getName ()
  {
    return "variable";
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

    final int keepValueIndex = getEnumAttValue ("keep-value", attrs, YESNO_VALUES, context);
    if (keepValueIndex != -1 && !(parent instanceof AbstractGroupBase))
      throw new SAXParseException ("Attribute 'keep-value' is not allowed for local variables", context.locator);

    // default is "no" (false)
    final boolean keepValue = (keepValueIndex == YES_VALUE);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, context, nameAtt, varName, selectExpr, keepValue, parent);
  }

  /** Represents an instance of the <code>variable</code> element. */
  public static final class Instance extends AbstractVariableBase
  {
    private final String m_sVarName;
    private AbstractTree m_aSelect;
    private final String m_sErrorMessage;
    private final boolean m_bIsGroupVar;

    protected Instance (final String qName,
                        final ParseContext context,
                        final String varName,
                        final String expName,
                        final AbstractTree select,
                        final boolean keepValue,
                        final AbstractNodeBase parent)
    {
      super (qName,
             parent,
             context,
             expName,
             keepValue,
             // this element must be empty if there is a select attribute
             select == null);
      this.m_sVarName = varName;
      this.m_aSelect = select;
      this.m_sErrorMessage = "('" + qName + "' started in line " + lineNo + ")";
      this.m_bIsGroupVar = parent instanceof AbstractGroupBase;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      // does this variable have a select attribute?
      if (m_aSelect != null)
      {
        // select attribute present
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

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      final Value v = new Value (((StringEmitter) context.popEmitter ()).getBuffer ().toString ());

      processVar (v, context);

      return super.processEnd (context);
    }

    /** Declares a variable */
    private void processVar (final Value v, final Context context) throws SAXException
    {
      // determine scope
      Hashtable <String, Value> varTable;
      if (m_bIsGroupVar)
        varTable = context.groupVars.get (m_aParent).peek ();
      else
      {
        varTable = context.localVars;
        m_aParent.declareVariable (m_sExpName);
      }

      if (varTable.get (m_sExpName) != null)
      {
        context.m_aErrorHandler.error ("Variable '" +
                                       m_sVarName +
                                       "' already declared",
                                       m_sPublicID,
                                       m_sSystemID,
                                       lineNo,
                                       colNo);
        return; // if the errorHandler returns
      }
      varTable.put (m_sExpName, v);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aSelect != null)
        theCopy.m_aSelect = m_aSelect.deepCopy (copies);
    }
  }
}
