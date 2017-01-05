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
import java.util.Vector;

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
 * Factory for <code>with-param</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 2.8 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class WithParamFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public WithParamFactory ()
  {
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
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    if (parent == null || !(parent instanceof AbstractProcessBase))
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
    final Vector <AbstractInstruction> siblings = ((AbstractProcessBase) parent).m_aChildren;
    if (siblings != null)
      for (int i = 0; i < siblings.size (); i++)
        if (((Instance) siblings.elementAt (i)).m_sExpName.equals (expName))
          throw new SAXParseException ("Parameter '" +
                                       nameAtt +
                                       "' already passed in line " +
                                       ((AbstractNodeBase) siblings.elementAt (i)).lineNo,
                                       context.locator);

    final AbstractTree selectExpr = parseExpr (attrs.getValue ("select"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, expName, selectExpr);
  }

  /** Represents an instance of the <code>with-param</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private final String m_sExpName;
    private AbstractTree m_aSelect;
    private final String m_sErrorMessage;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final String expName,
                        final AbstractTree select)
    {
      super (qName,
             parent,
             context,
             // this element may have children if there is no select attr
             select == null);
      this.m_sExpName = expName;
      this.m_aSelect = select;
      this.m_sErrorMessage = "('" + qName + "' started in line " + lineNo + ")";
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      if (m_aSelect == null)
      {
        super.process (context);
        // create a new StringEmitter for this instance and put it
        // on the emitter stack
        context.pushEmitter (new StringEmitter (new StringBuffer (), m_sErrorMessage));
      }
      else
        context.m_aPassedParameters.put (m_sExpName, m_aSelect.evaluate (context, this));

      return CSTX.PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.m_aPassedParameters.put (m_sExpName,
                                       new Value (((StringEmitter) context.popEmitter ()).getBuffer ().toString ()));

      return super.processEnd (context);
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
