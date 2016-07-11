/*
 * $Id: TemplateBase.java,v 2.9 2008/10/04 17:13:14 obecker Exp $
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

import org.xml.sax.SAXException;

import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Common base class for {@link TemplateFactory.Instance} and
 * {@link ProcedureFactory.Instance}.
 *
 * @version $Revision: 2.9 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public abstract class AbstractTemplateBase extends AbstractNodeBase
{
  /** Visibility values */
  public static final int LOCAL_VISIBLE = 0, GROUP_VISIBLE = 1, GLOBAL_VISIBLE = 2;

  /** Attribute value strings for the above visibility values */
  // note: same order required!
  protected static final String [] VISIBILITY_VALUES = { "local", "group", "global" };

  /** The visibility of this template */
  public int visibility;

  /** Whether this template is public */
  public boolean isPublic;

  /** Does this template establish a new scope for group variables? */
  private final boolean newScope;

  /** The parent of this template */
  public AbstractGroupBase parentGroup;

  //
  // Constructor
  //

  protected AbstractTemplateBase (final String qName,
                                  final AbstractNodeBase parent,
                                  final ParseContext context,
                                  final int visibility,
                                  final boolean isPublic,
                                  final boolean newScope)
  {
    super (qName, parent, context, true);
    parentGroup = (AbstractGroupBase) parent;
    this.visibility = visibility;
    this.isPublic = isPublic;
    this.newScope = newScope;
  }

  @Override
  public short process (final Context context) throws SAXException
  {
    context.currentGroup = parentGroup;
    if (newScope)
    {
      // initialize group variables
      parentGroup.enterRecursionLevel (context);
    }
    return super.process (context);
  }

  @Override
  public short processEnd (final Context context) throws SAXException
  {
    if (newScope)
      parentGroup.exitRecursionLevel (context);
    return super.processEnd (context);
  }

  @Override
  protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
  {
    super.onDeepCopy (copy, copies);
    final AbstractTemplateBase theCopy = (AbstractTemplateBase) copy;
    if (parentGroup != null)
      theCopy.parentGroup = (AbstractGroupBase) parentGroup.deepCopy (copies);
  }

}
