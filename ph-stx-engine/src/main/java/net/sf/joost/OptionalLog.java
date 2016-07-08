/*
 * $Id: OptionalLog.java,v 1.3 2004/11/06 13:08:51 obecker Exp $
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
package net.sf.joost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a helper class that optionally initializes the Commons Logging
 * facility. If <code>org.apache.commons.logging.LogFactory</code> is present
 * the {@link #getLog(Class)} method returns a normal <code>Logger</code> object
 * that must be converted (via a type cast) before using it. Otherwise the
 * method returns <code>null</code>. This approach prevents a
 * <code>NoClassDefFoundError</code> in case logging is not available.
 *
 * @version $Revision: 1.3 $ $Date: 2004/11/06 13:08:51 $
 * @author Oliver Becker
 */
public final class OptionalLog
{
  /**
   * Returns a <code>org.slf4j.Logger</log> object if this
   * class is available, otherwise <code>null</code>
   */
  public static Logger getLog (final Class <?> _class)
  {
    return LoggerFactory.getLogger (_class);
  }

  /**
   * Returns a <code>org.slf4j.Logger</log> object if this
   * class is available, otherwise <code>null</code>
   */
  public static Logger getLog (final String name)
  {
    return LoggerFactory.getLogger (name);
  }
}
