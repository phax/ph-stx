/*
 * $Id: StxEmitterBase.java,v 1.1 2005/03/13 17:12:49 obecker Exp $
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
package net.sf.joost.emitter;

/**
 * Abstract base StxEmitter implementation class that provides
 * {@link #setSystemId(String)} and {@link #getSystemId()}.
 *
 * @version $Revision: 1.1 $ $Date: 2005/03/13 17:12:49 $
 * @author Oliver Becker
 */
public abstract class StxEmitterBase implements StxEmitter
{
  /** The system identifier of this STX emitter (optional) */
  private String systemId;

  /*
   * (non-Javadoc)
   * @see net.sf.joost.emitter.StxEmitter#setSystemId(java.lang.String)
   */
  public void setSystemId (final String systemId)
  {
    this.systemId = systemId;
  }

  /*
   * (non-Javadoc)
   * @see net.sf.joost.emitter.StxEmitter#getSystemId()
   */
  public String getSystemId ()
  {
    return systemId;
  }
}
