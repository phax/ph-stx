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
 * This class represents one part (either a symbol or an action) of a
 * production. In this base class it contains only an optional label string that
 * the user can use to refer to the part within actions.
 * <p>
 * This is an abstract class.
 *
 * @see java_cup.production
 * @version last updated: 11/25/95
 * @author Scott Hudson
 */
public abstract class production_part
{

  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Simple constructor. */
  public production_part (final String lab)
  {
    _label = lab;
  }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Optional label for referring to the part within an action (null for no
   * label).
   */
  protected String _label;

  /**
   * Optional label for referring to the part within an action (null for no
   * label).
   */
  public String label ()
  {
    return _label;
  }

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Indicate if this is an action (rather than a symbol). Here in the base
   * class, we don't this know yet, so its an abstract method.
   */
  public abstract boolean is_action ();

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Equality comparison. */
  public boolean equals (final production_part other)
  {
    if (other == null)
      return false;

    /* compare the labels */
    if (label () != null)
      return label ().equals (other.label ());
    else
      return other.label () == null;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Generic equality comparison. */
  @Override
  public boolean equals (final Object other)
  {
    if (!(other instanceof production_part))
      return false;
    else
      return equals ((production_part) other);
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Produce a hash code. */
  @Override
  public int hashCode ()
  {
    return label () == null ? 0 : label ().hashCode ();
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Convert to a string. */
  @Override
  public String toString ()
  {
    if (label () != null)
      return label () + ":";
    else
      return " ";
  }

  /*-----------------------------------------------------------*/

}
