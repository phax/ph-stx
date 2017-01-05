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

/**
 * This class serves as the base class for entries in a parse action table. Full
 * entries will either be SHIFT(state_num), REDUCE(production), NONASSOC, or
 * ERROR. Objects of this base class will default to ERROR, while the other
 * three types will be represented by subclasses.
 *
 * @see java_cup.reduce_action
 * @see java_cup.shift_action
 * @version last updated: 7/2/96
 * @author Frank Flannery
 */

public class parse_action
{

  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Simple constructor. */
  public parse_action ()
  {
    /* nothing to do in the base class */
  }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Static (Class) Variables ------------------*/
  /*-----------------------------------------------------------*/

  /** Constant for action type -- error action. */
  public static final int ERROR = 0;

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Constant for action type -- shift action. */
  public static final int SHIFT = 1;

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Constants for action type -- reduce action. */
  public static final int REDUCE = 2;

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Constants for action type -- reduce action. */
  public static final int NONASSOC = 3;

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Quick access to the type -- base class defaults to error. */
  public int kind ()
  {
    return ERROR;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Equality test. */
  public boolean equals (final parse_action other)
  {
    /* we match all error actions */
    return other != null && other.kind () == ERROR;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Generic equality test. */
  @Override
  public boolean equals (final Object other)
  {
    if (other instanceof parse_action)
      return equals ((parse_action) other);
    else
      return false;
  }
  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Compute a hash code. */
  @Override
  public int hashCode ()
  {
    /* all objects of this class hash together */
    return 0xCafe123;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Convert to string. */
  @Override
  public String toString ()
  {
    return "ERROR";
  }

  /*-----------------------------------------------------------*/
}
