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
 * This class represents a reduce action within the parse table. The action
 * simply stores the production that it reduces with and responds to queries
 * about its type.
 *
 * @version last updated: 11/25/95
 * @author Scott Hudson
 */
public class reduce_action extends parse_action
{

  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Simple constructor.
   * 
   * @param prod
   *        the production this action reduces with.
   */
  public reduce_action (final production prod) throws internal_error
  {
    /* sanity check */
    if (prod == null)
      throw new internal_error ("Attempt to create a reduce_action with a null production");

    _reduce_with = prod;
  }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/

  /** The production we reduce with. */
  protected production _reduce_with;

  /** The production we reduce with. */
  public production reduce_with ()
  {
    return _reduce_with;
  }

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Quick access to type of action. */
  @Override
  public int kind ()
  {
    return REDUCE;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Equality test. */
  public boolean equals (final reduce_action other)
  {
    return other != null && other.reduce_with () == reduce_with ();
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Generic equality test. */
  @Override
  public boolean equals (final Object other)
  {
    if (other instanceof reduce_action)
      return equals ((reduce_action) other);
    else
      return false;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Compute a hash code. */
  @Override
  public int hashCode ()
  {
    /* use the hash code of the production we are reducing with */
    return reduce_with ().hashCode ();
  }

  /** Convert to string. */
  @Override
  public String toString ()
  {
    return "REDUCE(with prod " + reduce_with ().index () + ")";
  }

  /*-----------------------------------------------------------*/

}
