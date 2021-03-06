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
package net.sf.joost.stx;

import net.sf.joost.instruction.AbstractNodeBase;

/**
 * Callback interface that can be used to receive information about the STX
 * instructions created by a {@link Parser} object.
 * 
 * @version $Revision: 2.3 $ $Date: 2004/01/23 16:16:46 $
 * @author Oliver Becker
 */
public interface IParserListener
{
  /**
   * Send a notification that the parser has created an internal representation
   * of an STX instruction
   * 
   * @param node
   *        the instruction
   */
  public void nodeCreated (AbstractNodeBase node);

  /**
   * Send a notification that the parser has finished its parsing. All nodes
   * reported by {@link #nodeCreated} have been fully initialized.
   */
  public void parseFinished ();
}
