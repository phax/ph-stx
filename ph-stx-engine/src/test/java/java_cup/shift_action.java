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
 * This class represents a shift action within the parse table. The action
 * simply stores the state that it shifts to and responds to queries about its
 * type.
 *
 * @version last updated: 11/25/95
 * @author Scott Hudson
 */
public class shift_action extends parse_action
{

  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Simple constructor.
   * 
   * @param shft_to
   *        the state that this action shifts to.
   */
  public shift_action (final lalr_state shft_to) throws internal_error
  {
    /* sanity check */
    if (shft_to == null)
      throw new internal_error ("Attempt to create a shift_action to a null state");

    _shift_to = shft_to;
  }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/

  /** The state we shift to. */
  protected lalr_state _shift_to;

  /** The state we shift to. */
  public lalr_state shift_to ()
  {
    return _shift_to;
  }

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Quick access to type of action. */
  @Override
  public int kind ()
  {
    return SHIFT;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Equality test. */
  public boolean equals (final shift_action other)
  {
    return other != null && other.shift_to () == shift_to ();
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Generic equality test. */
  @Override
  public boolean equals (final Object other)
  {
    if (other instanceof shift_action)
      return equals ((shift_action) other);
    else
      return false;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Compute a hash code. */
  @Override
  public int hashCode ()
  {
    /* use the hash code of the state we are shifting to */
    return shift_to ().hashCode ();
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Convert to a string. */
  @Override
  public String toString ()
  {
    return "SHIFT(to state " + shift_to ().index () + ")";
  }

  /*-----------------------------------------------------------*/

}
