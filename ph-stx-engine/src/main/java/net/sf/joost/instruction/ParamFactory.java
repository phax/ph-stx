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
 * Factory for <code>params</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.12 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class ParamFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public ParamFactory ()
  {
    attrNames.add ("name");
    attrNames.add ("required");
    attrNames.add ("select");
  }

  /** @return <code>"param"</code> */
  @Override
  public String getName ()
  {
    return "param";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    if (parent == null || !(parent instanceof AbstractGroupBase || // transform,
                                                                   // group
                            parent instanceof AbstractTemplateBase)) // template,
      // procedure
      throw new SAXParseException ("'" +
                                   qName +
                                   "' must be a top level element " +
                                   "or a child of stx:group, stx:template, or stx:procedure",
                                   context.locator);

    final String nameAtt = getRequiredAttribute (qName, attrs, "name", context);
    final String parName = getExpandedName (nameAtt, context);

    // default is false
    final boolean required = getEnumAttValue ("required", attrs, YESNO_VALUES, context) == YES_VALUE;

    final AbstractTree selectExpr = parseExpr (attrs.getValue ("select"), context);
    if (required && selectExpr != null)
      throw new SAXParseException ("'" +
                                   qName +
                                   "' must not have a 'select' attribute if it " +
                                   "declares the parameter as required",
                                   context.locator);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAtt, parName, selectExpr, required);
  }

  /** Represents an instance of the <code>param</code> element. */
  public static final class Instance extends AbstractVariableBase
  {
    private final String m_sVarName;
    private AbstractTree m_aSelect;
    private final boolean m_bRequired;
    private AbstractInstruction m_aContents;
    private AbstractInstruction m_aSuccessor;
    // private Hashtable globalParams;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final String varName,
                        final String expName,
                        final AbstractTree select,
                        final boolean required)
    {
      super (qName,
             parent,
             context,
             expName,
             false, // keep-value has no meaning here
             // this element may have children if there is no select
             // attribute and the parameter is not required
             select == null && !required);
      this.m_sVarName = varName;
      this.m_aSelect = select;
      this.m_bRequired = required;
    }

    @Override
    public boolean compile (final int pass, final ParseContext context)
    {
      if (pass == 0)
        return true; // nodeEnd not available yet

      m_aContents = next;
      m_aSuccessor = m_aNodeEnd != null ? m_aNodeEnd.next : next;
      return false;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      Value v;
      if (m_aParent instanceof AbstractGroupBase)
      {
        // passed value from the outside
        v = context.globalParameters.get (m_sExpName);
      }
      else
      {
        // passed value from another template via stx:with-param
        v = context.m_aPassedParameters.get (m_sExpName);
      }
      if (v == null)
      {
        // no parameter passed
        if (m_bRequired)
        {
          context.m_aErrorHandler.error ("Missing value for required parameter '" +
                                         m_sVarName +
                                         "'",
                                         m_sPublicID,
                                         m_sSystemID,
                                         lineNo,
                                         colNo);
          return CSTX.PR_CONTINUE; // if the errorHandler returns
        }
        if (m_aSelect != null)
        {
          // select attribute present
          v = m_aSelect.evaluate (context, this);
        }
        else
        {
          // use contents
          next = m_aContents;
          super.process (context);
          context.pushEmitter (new StringEmitter (new StringBuffer (),
                                                  "('" + m_sQName + "' started in line " + lineNo + ")"));
          return CSTX.PR_CONTINUE;
        }
      }
      processParam (v, context);
      if (m_aNodeEnd != null)
      {
        // skip contents, the parameter value is already available
        next = m_aSuccessor;
      }
      return CSTX.PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      processParam (new Value (((StringEmitter) context.popEmitter ()).getBuffer ().toString ()), context);
      return super.processEnd (context);
    }

    /** Declare a parameter */
    public void processParam (final Value v, final Context context) throws SAXException
    {
      // determine scope
      Hashtable <String, Value> varTable;
      if (m_aParent instanceof AbstractGroupBase) // global parameter
        varTable = context.groupVars.get (m_aParent).peek ();
      else
        varTable = context.localVars;

      if (varTable.get (m_sExpName) != null)
      {
        context.m_aErrorHandler.error ("Param '" +
                                       m_sVarName +
                                       "' already declared",
                                       m_sPublicID,
                                       m_sSystemID,
                                       lineNo,
                                       colNo);
        return; // if the errorHandler returns
      }

      varTable.put (m_sExpName, v);

      if (varTable == context.localVars)
        m_aParent.declareVariable (m_sExpName);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aContents != null)
        theCopy.m_aContents = m_aContents.deepCopy (copies);
      if (m_aSuccessor != null)
        theCopy.m_aSuccessor = m_aSuccessor.deepCopy (copies);
      if (m_aSelect != null)
        theCopy.m_aSelect = m_aSelect.deepCopy (copies);
    }

  }
}
