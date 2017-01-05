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

/**
 * This Exception is used in class CharClasses.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class CharClassException extends RuntimeException
{

  /**
   * 
   */
  private static final long serialVersionUID = 7199804506062103569L;

  /**
   * Creates a new CharClassException without message
   */
  public CharClassException ()
  {}

  /**
   * Creates a new CharClassException with the specified message
   *
   * @param message
   *        the error description presented to the user.
   */
  public CharClassException (final String message)
  {
    super (message);
  }

}
