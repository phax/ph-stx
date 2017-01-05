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

import net.sf.joost.stx.ParseContext;

/**
 * Common base class for variables, parameters, and buffers.
 *
 * @version $Revision: 2.2 $ $Date: 2003/06/03 14:30:27 $
 * @author Oliver Becker
 */
public abstract class AbstractVariableBase extends AbstractNodeBase
{
  protected final String m_sExpName;
  protected final boolean m_bKeepValue;

  public AbstractVariableBase (final String qName,
                               final AbstractNodeBase parent,
                               final ParseContext context,
                               final String expName,
                               final boolean keepValue,
                               final boolean mayHaveChildren)
  {
    super (qName, parent, context, mayHaveChildren);
    m_sExpName = expName;
    m_bKeepValue = keepValue;
  }
}
