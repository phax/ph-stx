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

package java_cup;

/**
 * This class represents a shift/reduce nonassociative error within the parse
 * table. If action_table element is assign to type nonassoc_action, it cannot
 * be changed, and signifies that there is a conflict between shifting and
 * reducing a production and a terminal that shouldn't be next to each other.
 *
 * @version last updated: 7/2/96
 * @author Frank Flannery
 */
public class nonassoc_action extends parse_action
{

  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Simple constructor.
   */
  public nonassoc_action () throws internal_error
  {
    /* don't need to set anything, since it signifies error */
  }

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Quick access to type of action. */
  @Override
  public int kind ()
  {
    return NONASSOC;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Equality test. */
  @Override
  public boolean equals (final parse_action other)
  {
    return other != null && other.kind () == NONASSOC;
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
    return 0xCafe321;
  }

  /** Convert to string. */
  @Override
  public String toString ()
  {
    return "NONASSOC";
  }

  /*-----------------------------------------------------------*/

}
