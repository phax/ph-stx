/*
 * $Id: ProcedureFactory.java,v 2.9 2007/12/19 10:39:37 obecker Exp $
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

import java.util.HashSet;
import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>procedure</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 2.9 $ $Date: 2007/12/19 10:39:37 $
 * @author Oliver Becker
 */

public final class ProcedureFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element. */
  private final HashSet attrNames;

  // Constructor
  public ProcedureFactory ()
  {
    attrNames = new HashSet ();
    attrNames.add ("name");
    attrNames.add ("visibility");
    attrNames.add ("public");
    attrNames.add ("new-scope");
  }

  /** @return <code>"procedure"</code> */
  @Override
  public String getName ()
  {
    return "procedure";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    if (parent == null || !(parent instanceof AbstractGroupBase))
      throw new SAXParseException ("'" +
                                   qName +
                                   "' must be a top level " +
                                   "element or a child of stx:group",
                                   context.locator);

    final String nameAtt = getRequiredAttribute (qName, attrs, "name", context);
    final String expName = getExpandedName (nameAtt, context);

    int visibility = getEnumAttValue ("visibility", attrs, AbstractTemplateBase.VISIBILITY_VALUES, context);
    if (visibility == -1)
      visibility = AbstractTemplateBase.LOCAL_VISIBLE; // default value

    final int publicAttVal = getEnumAttValue ("public", attrs, YESNO_VALUES, context);
    // default value depends on the parent:
    // "yes" (true) for top-level procedures,
    // "no" (false) for others
    final boolean isPublic = parent instanceof TransformFactory.Instance ? (publicAttVal != NO_VALUE) // default
                                                                                                      // is
                                                                                                      // true
                                                                         : (publicAttVal == YES_VALUE); // default
                                                                                                        // is
                                                                                                        // false

    // default is "no" (false)
    final boolean newScope = getEnumAttValue ("new-scope", attrs, YESNO_VALUES, context) == YES_VALUE;

    checkAttributes (qName, attrs, attrNames, context);

    return new Instance (qName, parent, context, nameAtt, expName, visibility, isPublic, newScope);
  }

  // -----------------------------------------------------------------------

  /** The inner Instance class */
  public final class Instance extends AbstractTemplateBase
  {
    /** The expanded name of this procedure */
    protected String expName;

    /** The qualified name of this procedure */
    protected String procName;

    // Constructor
    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final String procName,
                        final String expName,
                        final int visibility,
                        final boolean isPublic,
                        final boolean newScope)
    {
      super (qName, parent, context, visibility, isPublic, newScope);
      this.expName = expName;
      this.procName = procName;
    }

    /*
     * Saving and restoring the current group is necessary if this procedure was
     * entered as a public procedure from a parent group (otherwise a following
     * process-xxx instruction would use the wrong group).
     */

    @Override
    public short process (final Context context) throws SAXException
    {
      localFieldStack.push (context.currentGroup);
      // save and reset local variables
      localFieldStack.push (context.localVars.clone ());
      context.localVars.clear ();
      return super.process (context);
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      super.processEnd (context);
      // restore local variables
      context.localVars = (Hashtable) localFieldStack.pop ();
      context.currentGroup = (AbstractGroupBase) localFieldStack.pop ();
      return CSTX.PR_CONTINUE;
    }

    // for debugging
    @Override
    public String toString ()
    {
      return "procedure:" + procName + "(" + lineNo + ")";
    }
  }
}
