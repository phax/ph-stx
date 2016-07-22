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
package net.sf.joost.stx;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.trax.SourceLocatorImpl;

/**
 * Class for receiving notifications of warnings and errors and for passing them
 * to a registered ErrorListener object.
 *
 * @version $Revision: 1.4 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */
public final class ErrorHandlerImpl implements ErrorHandler
{
  /** Optional <code>ErrorListener</code> object */
  public ErrorListener m_aErrorListener;

  /**
   * if set to <code>true</code> this object creates
   * TransformerConfigurationExceptions
   */
  private boolean m_bConfigurationFlag = false;

  //
  // Constructors
  //

  /** Default constructor, no ErrorListener registered */
  public ErrorHandlerImpl ()
  {}

  /**
   * Constructs an ErrorHandlerImpl and registers an ErrorListener.
   *
   * @param el
   *        the ErrorLister for this object
   */
  public ErrorHandlerImpl (final ErrorListener el)
  {
    m_aErrorListener = el;
  }

  /**
   * Constructs an ErrorHandlerImpl, no ErrorListener registered
   *
   * @param configurationFlag
   *        if set to <code>true</code> then this handler constructs
   *        {@link TransformerConfigurationException}s rather than
   *        {@link TransformerException}s
   */
  public ErrorHandlerImpl (final boolean configurationFlag)
  {
    this.m_bConfigurationFlag = configurationFlag;
  }

  /**
   * Constructs an ErrorHandlerImpl and registers an ErrorListener.
   *
   * @param el
   *        the ErrorLister for this object
   * @param configurationFlag
   *        if set to <code>true</code> then this handler constructs
   *        {@link TransformerConfigurationException}s rather than
   *        {@link TransformerException}s
   */
  public ErrorHandlerImpl (final ErrorListener el, final boolean configurationFlag)
  {
    m_aErrorListener = el;
    this.m_bConfigurationFlag = configurationFlag;
  }

  /**
   * Creates an Exception dependent from the value of
   * {@link #m_bConfigurationFlag}
   */
  private TransformerException newException (final String msg, final SourceLocator sl, final Throwable cause)
  {
    if (m_bConfigurationFlag)
      return new TransformerConfigurationException (msg, sl, cause);
    return new TransformerException (msg, sl, cause);
  }

  /**
   * Reports a warning to a registered {@link #m_aErrorListener}. Does nothing
   * if there's no such listener object.
   *
   * @param msg
   *        the message of this warning
   * @param loc
   *        a SAX <code>Locator</code> object
   * @param cause
   *        an optional {@link Throwable} that caused this warning
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void warning (final String msg, final Locator loc, final Throwable cause) throws SAXException
  {
    warning (newException (msg, new SourceLocatorImpl (loc), cause));
  }

  /**
   * Reports a warning to a registered {@link #m_aErrorListener}. Does nothing
   * if there's no such listener object.
   *
   * @param msg
   *        the message of this warning
   * @param pubId
   *        the public identifier of the source
   * @param sysId
   *        the system identifier of the source
   * @param lineNo
   *        the line number in the source which causes the warning
   * @param colNo
   *        the column number in the source which causes the warning
   * @param cause
   *        an optional {@link Throwable} that caused this warning
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void warning (final String msg,
                       final String pubId,
                       final String sysId,
                       final int lineNo,
                       final int colNo,
                       final Throwable cause) throws SAXException
  {
    warning (newException (msg, new SourceLocatorImpl (pubId, sysId, lineNo, colNo), cause));
  }

  /**
   * Calls {@link #warning(String, String, String, int, int, Throwable)} with
   * the <code>cause</code> parameter set to <code>null</code>.
   */
  public void warning (final String msg,
                       final String pubId,
                       final String sysId,
                       final int lineNo,
                       final int colNo) throws SAXException
  {
    warning (msg, pubId, sysId, lineNo, colNo, null);
  }

  /**
   * Reports a warning to a registered {@link #m_aErrorListener}. Does nothing
   * if there's no such listener object.
   *
   * @param te
   *        the warning encapsulated in a <code>TransformerException</code>
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void warning (final TransformerException te) throws SAXException
  {
    try
    {
      if (m_aErrorListener == null)
        return; // default: do nothing
      m_aErrorListener.warning (te);
    }
    catch (final TransformerException ex)
    {
      throw new SAXException (ex);
    }
  }

  /**
   * Receive a notification of a warning from the parser. If an
   * {@link #m_aErrorListener} was registered, the provided parameter
   * <code>SAXParseException</code> will be passed to this object wrapped in a
   * {@link TransformerException}
   *
   * @throws SAXException
   *         wrapping {@link TransformerException}
   */
  public void warning (final SAXParseException pe) throws SAXException
  {
    if (m_aErrorListener == null)
      return;
    warning (pe.getMessage (), pe.getPublicId (), pe.getSystemId (), pe.getLineNumber (), pe.getColumnNumber (), pe);
  }

  /**
   * Reports a recoverable error to a registered {@link #m_aErrorListener}.
   *
   * @param msg
   *        the message of this error
   * @param loc
   *        a SAX <code>Locator</code> object
   * @param cause
   *        an optional {@link Throwable} that caused this error
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void error (final String msg, final Locator loc, final Throwable cause) throws SAXException
  {
    error (newException (msg, new SourceLocatorImpl (loc), cause));
  }

  /**
   * Reports a recoverable error to a registered {@link #m_aErrorListener}.
   *
   * @param msg
   *        the message of this error
   * @param pubId
   *        the public identifier of the source
   * @param sysId
   *        the system identifier of the source
   * @param lineNo
   *        the line number in the source which causes the error
   * @param colNo
   *        the column number in the source which causes the error
   * @param cause
   *        an optional {@link Throwable} that caused this error
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void error (final String msg,
                     final String pubId,
                     final String sysId,
                     final int lineNo,
                     final int colNo,
                     final Throwable cause) throws SAXException
  {
    error (newException (msg, new SourceLocatorImpl (pubId, sysId, lineNo, colNo), cause));
  }

  /**
   * Calls {@link #error(String, String, String, int, int, Throwable)} with the
   * <code>cause</code> parameter set to <code>null</code>.
   */
  public void error (final String msg,
                     final String pubId,
                     final String sysId,
                     final int lineNo,
                     final int colNo) throws SAXException
  {
    error (msg, pubId, sysId, lineNo, colNo, null);
  }

  /**
   * Reports a recoverable error to a registered {@link #m_aErrorListener}.
   *
   * @param te
   *        the error encapsulated in a <code>TransformerException</code>
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void error (final TransformerException te) throws SAXException
  {
    try
    {
      if (m_aErrorListener == null)
        throw te;
      m_aErrorListener.error (te);
    }
    catch (final TransformerException ex)
    {
      throw new SAXException (ex);
    }
  }

  /**
   * Receive a notification of a recoverable error from the parser. If an
   * {@link #m_aErrorListener} was registered, the provided parameter
   * <code>SAXParseException</code> will be passed to this object wrapped in a
   * {@link TransformerException}
   *
   * @throws SAXException
   *         wrapping a {@link TransformerException}
   */
  public void error (final SAXParseException pe) throws SAXException
  {
    final Exception em = pe.getException ();
    if (em instanceof RuntimeException)
      throw (RuntimeException) em;
    error (pe.getMessage (), pe.getPublicId (), pe.getSystemId (), pe.getLineNumber (), pe.getColumnNumber (), pe);
  }

  /**
   * Reports a non-recoverable error to a registered {@link #m_aErrorListener}.
   *
   * @param msg
   *        the message of this error
   * @param loc
   *        a SAX <code>Locator</code> object
   * @param cause
   *        an optional {@link Throwable} that caused this error
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void fatalError (final String msg, final Locator loc, final Throwable cause) throws SAXException
  {
    fatalError (newException (msg, new SourceLocatorImpl (loc), cause));
  }

  /**
   * Reports a non-recoverable error to a registered {@link #m_aErrorListener}
   *
   * @param msg
   *        the message of this error
   * @param pubId
   *        the public identifier of the source
   * @param sysId
   *        the system identifier of the source
   * @param lineNo
   *        the line number in the source which causes the error
   * @param colNo
   *        the column number in the source which causes the error
   * @param cause
   *        an optional {@link Throwable} that caused this error
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void fatalError (final String msg,
                          final String pubId,
                          final String sysId,
                          final int lineNo,
                          final int colNo,
                          final Throwable cause) throws SAXException
  {
    fatalError (newException (msg, new SourceLocatorImpl (pubId, sysId, lineNo, colNo), cause));
  }

  /**
   * Calls {@link #fatalError(String, String, String, int, int, Throwable)} with
   * the <code>cause</code> parameter set to <code>null</code>.
   */
  public void fatalError (final String msg,
                          final String pubId,
                          final String sysId,
                          final int lineNo,
                          final int colNo) throws SAXException
  {
    fatalError (msg, pubId, sysId, lineNo, colNo, null);
  }

  /**
   * Reports a non-recoverable error to a registered {@link #m_aErrorListener}
   *
   * @param te
   *        the error encapsulated in a <code>TransformerException</code>
   * @throws SAXException
   *         wrapping a <code>TransformerException</code>
   */
  public void fatalError (final TransformerException te) throws SAXException
  {
    try
    {
      if (m_aErrorListener == null)
        throw te;
      m_aErrorListener.fatalError (te);
    }
    catch (final TransformerException ex)
    {
      throw new SAXException (ex);
    }
  }

  /**
   * Receive a notification of a non-recoverable error from the parser. If an
   * {@link #m_aErrorListener} was registered, the provided parameter
   * <code>SAXParseException</code> will be passed to this object wrapped in a
   * {@link TransformerException}
   *
   * @throws SAXException
   *         wrapping a {@link TransformerException}
   */
  public void fatalError (final SAXParseException pe) throws SAXException
  {
    final Exception em = pe.getException ();
    if (em instanceof RuntimeException)
      throw (RuntimeException) em;
    fatalError (pe.getMessage (), pe.getPublicId (), pe.getSystemId (), pe.getLineNumber (), pe.getColumnNumber (), pe);
  }
}
