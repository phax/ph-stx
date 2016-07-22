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
 * This class represents one row (corresponding to one machine state) of the
 * reduce-goto parse table.
 */
public class parse_reduce_row
{
  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Simple constructor. Note: this should not be used until the number of
   * terminals in the grammar has been established.
   */
  public parse_reduce_row ()
  {
    /* make sure the size is set */
    if (_size <= 0)
      _size = non_terminal.number ();

    /* allocate the array */
    under_non_term = new lalr_state [size ()];
  }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Static (Class) Variables ------------------*/
  /*-----------------------------------------------------------*/

  /** Number of columns (non terminals) in every row. */
  protected static int _size = 0;

  /** Number of columns (non terminals) in every row. */
  public static int size ()
  {
    return _size;
  }

  // Hm Added clear to clear all static fields
  public static void clear ()
  {
    _size = 0;
  }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/

  /** Actual entries for the row. */
  public lalr_state under_non_term[];
}
