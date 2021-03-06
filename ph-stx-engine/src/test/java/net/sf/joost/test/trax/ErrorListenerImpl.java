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
package net.sf.joost.test.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * ErrorListener implementation
 *
 * @author Zubow
 */
public class ErrorListenerImpl implements ErrorListener
{

  private final String m_sName;

  /**
   * Constructor
   */
  public ErrorListenerImpl (final String name)
  {
    this.m_sName = name;
  }

  /**
   * Receive notification of a warning.
   * {@link javax.xml.transform.ErrorListener#warning(TransformerException)}
   * <p>
   * {@link javax.xml.transform.Transformer} can use this method to report
   * conditions that are not errors or fatal errors. The default behaviour is to
   * take no action.
   * </p>
   * <p>
   * After invoking this method, the Transformer must continue with the
   * transformation. It should still be possible for the application to process
   * the document through to the end.
   * </p>
   *
   * @param exception
   *        The warning information encapsulated in a transformer exception.
   * @throws javax.xml.transform.TransformerException
   *         if the application chooses to discontinue the transformation.
   * @see javax.xml.transform.TransformerException
   */
  public void warning (final TransformerException exception) throws TransformerException
  {

    System.err.println ("WARNING occured - ErrorListenerImpl " + m_sName);
  }

  /**
   * Receive notification of a recoverable error.
   * {@link javax.xml.transform.ErrorListener#error(TransformerException)}
   * <p>
   * The transformer must continue to try and provide normal transformation
   * after invoking this method. It should still be possible for the application
   * to process the document through to the end if no other errors are
   * encountered.
   * </p>
   *
   * @param exception
   *        The error information encapsulated in a transformer exception.
   * @throws javax.xml.transform.TransformerException
   *         if the application chooses to discontinue the transformation.
   * @see javax.xml.transform.TransformerException
   */
  public void error (final TransformerException exception) throws TransformerException
  {

    System.err.println ("ERROR occured - ErrorListenerImpl " + m_sName);
    System.err.println (exception.getMessageAndLocation ());
    throw exception;
  }

  /**
   * Receive notification of a non-recoverable error.
   * {@link javax.xml.transform.ErrorListener#fatalError(TransformerException)}
   * <p>
   * The transformer must continue to try and provide normal transformation
   * after invoking this method. It should still be possible for the application
   * to process the document through to the end if no other errors are
   * encountered, but there is no guarantee that the output will be useable.
   * </p>
   *
   * @param exception
   *        The error information encapsulated in a transformer exception.
   * @throws javax.xml.transform.TransformerException
   *         if the application chooses to discontinue the transformation.
   * @see javax.xml.transform.TransformerException
   */
  public void fatalError (final TransformerException exception) throws TransformerException
  {

    System.err.println ("FATALERROR occured - ErrorListenerImpl " + m_sName);
    System.err.println (exception.getMessage ());
    // cancel transformation
    throw exception;
  }
}
