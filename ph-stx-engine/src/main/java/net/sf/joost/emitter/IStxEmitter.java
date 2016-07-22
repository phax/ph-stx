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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.emitter;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * Common interface - All emitter implementations have to implement this
 * interface.
 *
 * @version $Revision: 1.5 $ $Date: 2005/03/13 17:12:49 $
 * @author Zubow
 */
public interface IStxEmitter extends ContentHandler, LexicalHandler
{
  /**
   * Set the system identifier for this emitter. This is optional - the system
   * identifier may be used to resolve relative output identifiers.
   *
   * @param systemId
   *        the system identifier as a URI string
   */
  void setSystemId (String systemId);

  /**
   * Get the system identifier that was set with {@link #setSystemId(String)}.
   *
   * @return the system identifier or <code>null</code> if
   *         {@link #setSystemId(String)} was not called.
   */
  String getSystemId ();
}
