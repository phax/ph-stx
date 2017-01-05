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
package jflex;

import java.io.File;

/**
 * This Exception could be thrown while scanning the specification (e.g.
 * unmatched input)
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class ScannerException extends RuntimeException
{

  /**
   * 
   */
  private static final long serialVersionUID = -6119623765759220207L;
  public int line;
  public int column;
  public ErrorMessages message;
  public File file;

  private ScannerException (final File file,
                            final String text,
                            final ErrorMessages message,
                            final int line,
                            final int column)
  {
    super (text);
    this.file = file;
    this.message = message;
    this.line = line;
    this.column = column;
  }

  /**
   * Creates a new ScannerException with a message only.
   *
   * @param message
   *        the code for the error description presented to the user.
   */
  public ScannerException (final ErrorMessages message)
  {
    this (null, ErrorMessages.get (message), message, -1, -1);
  }

  /**
   * Creates a new ScannerException for a file with a message only.
   *
   * @param file
   *        the file in which the error occured
   * @param message
   *        the code for the error description presented to the user.
   */
  public ScannerException (final File file, final ErrorMessages message)
  {
    this (file, ErrorMessages.get (message), message, -1, -1);
  }

  /**
   * Creates a new ScannerException with a message and line number.
   *
   * @param message
   *        the code for the error description presented to the user.
   * @param line
   *        the number of the line in the specification that contains the error
   */
  public ScannerException (final ErrorMessages message, final int line)
  {
    this (null, ErrorMessages.get (message), message, line, -1);
  }

  /**
   * Creates a new ScannerException for a file with a message and line number.
   *
   * @param message
   *        the code for the error description presented to the user.
   * @param line
   *        the number of the line in the specification that contains the error
   */
  public ScannerException (final File file, final ErrorMessages message, final int line)
  {
    this (file, ErrorMessages.get (message), message, line, -1);
  }

  /**
   * Creates a new ScannerException with a message, line number and column.
   *
   * @param message
   *        the code for the error description presented to the user.
   * @param line
   *        the number of the line in the specification that contains the error
   * @param column
   *        the column where the error starts
   */
  public ScannerException (final File file, final ErrorMessages message, final int line, final int column)
  {
    this (file, ErrorMessages.get (message), message, line, column);
  }

}
