/*
 * $Id: ConfigurationErrListener.java,v 1.3 2004/10/25 20:39:34 obecker Exp $
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
 * The Initial Developer of the Original Code is Anatolij Zubow.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): Oliver Becker.
 */

package net.sf.joost.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a default ErrorListener for the
 * {@link TransformerFactoryImpl TransformerFactory}.
 */
public class ConfigurationErrListener implements ErrorListener
{
  // Define a static logger variable so that it references the
  // Logger instance named "ConfigurationErrListener".
  private static Logger log = LoggerFactory.getLogger (ConfigurationErrListener.class);

  private ErrorListener m_aUserErrorListener;

  /**
   * Default constructor.
   */
  public ConfigurationErrListener ()
  {}

  public ErrorListener getUserErrorListener ()
  {
    return m_aUserErrorListener;
  }

  public void setUserErrorListener (final ErrorListener userErrorListener)
  {
    this.m_aUserErrorListener = userErrorListener;
  }

  /**
   * Receive notification of a warning. Details {@link ErrorListener#warning}
   */
  public void warning (final TransformerException tE) throws TransformerConfigurationException
  {
    if (m_aUserErrorListener != null)
    {
      try
      {
        m_aUserErrorListener.warning (tE);
      }
      catch (final TransformerException e2)
      {
        log.warn ("warning", e2);
        if (e2 instanceof TransformerConfigurationException)
          throw (TransformerConfigurationException) tE;
        throw new TransformerConfigurationException (tE.getMessage (), tE);
      }
    }
    else
    {
      log.warn ("warning", tE);
      // no user defined errorlistener, so throw this exception
      if (tE instanceof TransformerConfigurationException)
        throw (TransformerConfigurationException) tE;
      throw new TransformerConfigurationException (tE.getMessage (), tE);
    }
  }

  /**
   * Receive notification of a recoverable error. Details
   * {@link ErrorListener#error}
   */
  public void error (final TransformerException tE) throws TransformerConfigurationException
  {
    if (m_aUserErrorListener != null)
    {
      try
      {
        m_aUserErrorListener.error (tE);
      }
      catch (final TransformerException e2)
      {
        log.error ("error", e2);
        if (e2 instanceof TransformerConfigurationException)
          throw (TransformerConfigurationException) tE;
        throw new TransformerConfigurationException (tE.getMessage (), tE);
      }
    }
    else
    {
      log.error ("error", tE);
      // no user defined errorlistener, so throw this exception
      if (tE instanceof TransformerConfigurationException)
        throw (TransformerConfigurationException) tE;
      throw new TransformerConfigurationException (tE.getMessage (), tE);
    }
  }

  /**
   * Receive notification of a non-recoverable error. Details
   * {@link ErrorListener#fatalError}
   */
  public void fatalError (final TransformerException tE) throws TransformerConfigurationException
  {
    if (m_aUserErrorListener != null)
    {
      try
      {
        m_aUserErrorListener.fatalError (tE);
      }
      catch (final TransformerException e2)
      {
        log.error ("fatal", e2);
        if (e2 instanceof TransformerConfigurationException)
          throw (TransformerConfigurationException) tE;
        throw new TransformerConfigurationException (tE.getMessage (), tE);
      }
    }
    else
    {
      log.error ("fatal", tE);
      // no user defined errorlistener, so throw this exception
      if (tE instanceof TransformerConfigurationException)
        throw (TransformerConfigurationException) tE;
      throw new TransformerConfigurationException (tE.getMessage (), tE);
    }
  }
}
