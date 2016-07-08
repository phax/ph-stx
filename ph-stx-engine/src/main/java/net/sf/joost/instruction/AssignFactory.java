/*
 * $Id: AssignFactory.java,v 2.12 2008/10/04 17:13:14 obecker Exp $
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
 * Contributor(s): Thomas Behrends.
 */

package net.sf.joost.instruction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.Tree;
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

final public class AssignFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet <String> attrNames;

  // Constructor
  public AssignFactory ()
  {
    attrNames = new HashSet<> ();
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
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    final String nameAtt = getRequiredAttribute (qName, attrs, "name", context);
    final String varName = getExpandedName (nameAtt, context);

    final Tree selectExpr = parseExpr (attrs.getValue ("select"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAtt, varName, selectExpr);
  }

  /** Represents an instance of the <code>assign</code> element. */
  final public class Instance extends NodeBase
  {
    public String varName, expName;
    private Tree select;
    private final String errorMessage;

    private boolean scopeDetermined = false;
    private GroupBase groupScope = null;

    protected Instance (final String qName,
                        final NodeBase parent,
                        final ParseContext context,
                        final String varName,
                        final String expName,
                        final Tree select)
    {
      super (qName,
             parent,
             context,
             // this element must be empty if there is a select attribute
             select == null);
      this.varName = varName;
      this.expName = expName;
      this.select = select;
      this.errorMessage = "('" + qName + "' started in line " + lineNo + ")";
    }

    /**
     * Evaluate the <code>select</code> attribute if present.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      // does this variable have a select attribute?
      if (select != null)
      {
        final Value v = select.evaluate (context, this);
        processVar (v, context);
      }
      else
      {
        // endInstruction present
        super.process (context);
        // create a new StringEmitter for this instance and put it
        // on the emitter stack
        context.pushEmitter (new StringEmitter (new StringBuffer (), errorMessage));
      }
      return PR_CONTINUE;
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
      if (!scopeDetermined)
      {
        try
        {
          groupScope = VariableUtils.findVariableScope (context, expName);
        }
        catch (final VariableNotFoundException e)
        {
          context.errorHandler.error ("Can't assign to undeclared variable '" +
                                      varName +
                                      "'",
                                      publicId,
                                      systemId,
                                      lineNo,
                                      colNo);
          return; // if the errorHandler returns
        }
        scopeDetermined = true;
      }

      final Hashtable <String, Value> vars = (groupScope == null) ? context.localVars
                                                                  : context.groupVars.get (groupScope).peek ();

      // assign new value
      vars.put (expName, v);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (groupScope != null)
        theCopy.groupScope = (GroupBase) groupScope.deepCopy (copies);
      if (select != null)
        theCopy.select = select.deepCopy (copies);
    }

  }
}
