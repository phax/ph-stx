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
package java_cup;

/** Exception subclass for reporting internal errors in JavaCup. */
public class internal_error extends Exception
{
  /** Constructor with a message */
  public internal_error (final String msg)
  {
    super (msg);
  }

  /**
   * Method called to do a forced error exit on an internal error for cases when
   * we can't actually throw the exception.
   */
  public void crash ()
  {
    ErrorManager.getManager ().emit_fatal ("JavaCUP Internal Error Detected: " + getMessage ());
    printStackTrace ();
    System.exit (-1);
  }
}
