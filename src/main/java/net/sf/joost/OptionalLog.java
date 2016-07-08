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

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;

/**
 * Provides a helper class that optionally initializes the Commons Logging
 * facility. If <code>org.apache.commons.logging.LogFactory</code> is present
 * the {@link #getLog(Class)} method returns a normal <code>Log</code> object
 * that must be converted (via a type cast) before using it. Otherwise the
 * method returns <code>null</code>. This approach prevents a
 * <code>NoClassDefFoundError</code> in case logging is not available.
 *
 * @version $Revision: 1.3 $ $Date: 2004/11/06 13:08:51 $
 * @author Oliver Becker
 */
public final class OptionalLog
{
  private static Method getLogMethodClass;
  private static Method getLogMethodString;
  static
  {
    try
    {
      final Class c = Class.forName ("org.apache.commons.logging.LogFactory");
      try
      {
        // look for getLog(Class _class)
        final Class [] declaredParams = { Class.class };
        getLogMethodClass = c.getDeclaredMethod ("getLog", declaredParams);
        // one trial invocation
        final Object [] actualParams = { OptionalLog.class };
        getLogMethodClass.invoke (null, actualParams);
      }
      catch (final Throwable t)
      {
        // Something went wrong, logging is not available
        getLogMethodClass = null;
      }
      try
      {
        // look for getLog(String name)
        final Class [] declaredParams = { String.class };
        getLogMethodString = c.getDeclaredMethod ("getLog", declaredParams);
        // one trial invocation
        final Object [] actualParamsString = { OptionalLog.class.getName () };
        getLogMethodString.invoke (null, actualParamsString);
      }
      catch (final Throwable t)
      {
        // Something went wrong, logging is not available
        getLogMethodString = null;
      }
    }
    catch (final Throwable t)
    {
      // Class not found, logging is not available
      getLogMethodClass = null;
      getLogMethodString = null;
    }
  }

  /**
   * Returns a <code>org.apache.commons.logging.Log</log> object if this
   * class is available, otherwise <code>null</code>
   */
  public static Log getLog (final Class _class)
  {
    if (getLogMethodClass != null)
    {
      final Object [] params = { _class };
      try
      {
        return (Log) getLogMethodClass.invoke (null, params);
      }
      catch (final Throwable t)
      {
        // Shouldn't happen ...
      }
    }
    return null;
  }

  /**
   * Returns a <code>org.apache.commons.logging.Log</log> object if this
   * class is available, otherwise <code>null</code>
   */
  public static Log getLog (final String name)
  {
    if (getLogMethodString != null)
    {
      final Object [] params = { name };
      try
      {
        return (Log) getLogMethodString.invoke (null, params);
      }
      catch (final Throwable t)
      {
        // Shouldn't happen ...
      }
    }
    return null;
  }
}
