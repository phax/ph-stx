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
package net.sf.joost.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a default ErrorListener for the {@link TransformerImpl
 * TransformerImpl}.
 */
public class TransformationErrListener implements ErrorListener
{

  // Define a static logger variable so that it references the
  // Logger instance named "TransformationErrListener".
  private static Logger log = LoggerFactory.getLogger (TransformationErrListener.class);

  private ErrorListener m_aUserErrorListener;

  /**
   * Default constructor.
   */
  public TransformationErrListener ()
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
  public void warning (final TransformerException tE) throws TransformerException
  {
    if (m_aUserErrorListener != null)
    {
      try
      {
        m_aUserErrorListener.warning (tE);
      }
      catch (final TransformerException e2)
      {
        log.warn ("Exception", e2);
        throw e2;
      }
    }
    else
    {
      log.warn ("Exception", tE);
    }
  }

  /**
   * Receive notification of a recoverable error. Details
   * {@link ErrorListener#error}
   */
  public void error (final TransformerException tE) throws TransformerException
  {
    if (m_aUserErrorListener != null)
    {
      try
      {
        m_aUserErrorListener.error (tE);
      }
      catch (final TransformerException e2)
      {
        log.error ("Exception", e2);
        throw e2;
      }
    }
    else
    {
      log.error ("Exception", tE);
      // no user defined errorlistener, so throw this exception
      throw tE;
    }
  }

  /**
   * Receive notification of a non-recoverable error. Details
   * {@link ErrorListener#fatalError}
   */
  public void fatalError (final TransformerException tE) throws TransformerException
  {
    if (m_aUserErrorListener != null)
    {
      try
      {
        m_aUserErrorListener.fatalError (tE);
      }
      catch (final TransformerException e2)
      {
        log.error ("Exception", e2);
        throw e2;
      }
    }
    else
    {
      log.error ("Exception", tE);
      // no user defined errorlistener, so throw this exception
      throw tE;
    }
  }
}
