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
 * This class represents a part of a production which contains an action. These
 * are eventually eliminated from productions and converted to trailing actions
 * by factoring out with a production that derives the empty string (and ends
 * with this action).
 *
 * @see java_cup.production
 * @version last update: 11/25/95
 * @author Scott Hudson
 */

public class action_part extends production_part
{

  /*-----------------------------------------------------------*/
  /*--- Constructors ------------------------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Simple constructor.
   * 
   * @param code_str
   *        string containing the actual user code.
   */
  public action_part (final String code_str)
  {
    super (/* never have a label on code */null);
    _code_string = code_str;
  }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/

  /** String containing code for the action in question. */
  protected String _code_string;

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** String containing code for the action in question. */
  public String code_string ()
  {
    return _code_string;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Set the code string. */
  public void set_code_string (final String new_str)
  {
    _code_string = new_str;
  }

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Override to report this object as an action. */
  @Override
  public boolean is_action ()
  {
    return true;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Equality comparison for properly typed object. */
  public boolean equals (final action_part other)
  {
    /* compare the strings */
    return other != null && super.equals (other) && other.code_string ().equals (code_string ());
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Generic equality comparison. */
  @Override
  public boolean equals (final Object other)
  {
    if (!(other instanceof action_part))
      return false;
    else
      return equals ((action_part) other);
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Produce a hash code. */
  @Override
  public int hashCode ()
  {
    return super.hashCode () ^ (code_string () == null ? 0 : code_string ().hashCode ());
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Convert to a string. */
  @Override
  public String toString ()
  {
    return super.toString () + "{" + code_string () + "}";
  }

  /*-----------------------------------------------------------*/
}
