/*
 * $Id: EvalException.java,v 1.2 2006/03/17 19:54:31 obecker Exp $
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

package net.sf.joost.grammar;

/**
 * Signals an error while evaluating an expression.
 * 
 * @version $Revision: 1.2 $ $Date: 2006/03/17 19:54:31 $
 * @author Oliver Becker
 */

public class EvalException extends Exception
{
  public EvalException (final String msg)
  {
    super (msg);
  }

  public EvalException (final String msg, final Exception cause)
  {
    super (msg, cause);
  }

  public EvalException (final Exception cause)
  {
    super (cause);
  }

  /**
   * @see java.lang.Throwable#getMessage()
   */
  @Override
  public String getMessage ()
  {
    final String message = super.getMessage ();
    final Throwable cause = getCause ();
    if (message == null && cause != null)
      return cause.getMessage ();
    else
      return message;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString ()
  {
    if (getCause () != null)
      return getCause ().toString ();
    return super.toString ();
  }
}
