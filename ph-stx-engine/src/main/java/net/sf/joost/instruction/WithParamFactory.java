/*
 * $Id: WithParamFactory.java,v 2.8 2008/10/04 17:13:14 obecker Exp $
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
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Value;

/**
 * Factory for <code>with-param</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 2.8 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

final public class WithParamFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet <String> attrNames;

  // Constructor
  public WithParamFactory ()
  {
    attrNames = new HashSet <> ();
    attrNames.add ("name");
    attrNames.add ("select");
  }

  /** @return <code>"with-param"</code> */
  @Override
  public String getName ()
  {
    return "with-param";
  }

  @Override
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    if (parent == null || !(parent instanceof ProcessBase))
    {
      throw new SAXParseException ("'" +
                                   qName +
                                   "' must be used only as a child of " +
                                   "stx:call-procedure or an stx:process-... instruction",
                                   context.locator);
    }

    final String nameAtt = getRequiredAttribute (qName, attrs, "name", context);
    final String expName = getExpandedName (nameAtt, context);

    // Check for uniqueness
    final Vector siblings = ((ProcessBase) parent).children;
    if (siblings != null)
      for (int i = 0; i < siblings.size (); i++)
        if (((Instance) siblings.elementAt (i)).expName.equals (expName))
          throw new SAXParseException ("Parameter '" +
                                       nameAtt +
                                       "' already passed in line " +
                                       ((NodeBase) siblings.elementAt (i)).lineNo,
                                       context.locator);

    final Tree selectExpr = parseExpr (attrs.getValue ("select"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, expName, selectExpr);
  }

  /** Represents an instance of the <code>with-param</code> element. */
  public class Instance extends NodeBase
  {
    private final String expName;
    private Tree select;
    private final String errorMessage;

    protected Instance (final String qName,
                        final NodeBase parent,
                        final ParseContext context,
                        final String expName,
                        final Tree select)
    {
      super (qName,
             parent,
             context,
             // this element may have children if there is no select attr
             select == null);
      this.expName = expName;
      this.select = select;
      this.errorMessage = "('" + qName + "' started in line " + lineNo + ")";
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      if (select == null)
      {
        super.process (context);
        // create a new StringEmitter for this instance and put it
        // on the emitter stack
        context.pushEmitter (new StringEmitter (new StringBuffer (), errorMessage));
      }
      else
        context.passedParameters.put (expName, select.evaluate (context, this));

      return PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.passedParameters.put (expName,
                                    new Value (((StringEmitter) context.popEmitter ()).getBuffer ().toString ()));

      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (select != null)
        theCopy.select = select.deepCopy (copies);
    }

  }
}
