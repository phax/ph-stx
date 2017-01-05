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
 * A specialized version of a production used when we split an existing
 * production in order to remove an embedded action. Here we keep a bit of extra
 * bookkeeping so that we know where we came from.
 * 
 * @version last updated: 11/25/95
 * @author Scott Hudson
 */

public class action_production extends production
{

  /**
   * Constructor.
   * 
   * @param base
   *        the production we are being factored out of.
   * @param lhs_sym
   *        the LHS symbol for this production.
   * @param rhs_parts
   *        array of production parts for the RHS.
   * @param rhs_len
   *        how much of the rhs_parts array is valid.
   * @param action_str
   *        the trailing reduce action for this production.
   * @param indexOfIntermediateResult
   *        the index of the result of the previous intermediate action on the
   *        stack relative to top, -1 if no previous action
   */
  public action_production (final production base,
                            final non_terminal lhs_sym,
                            final production_part rhs_parts[],
                            final int rhs_len,
                            final String action_str,
                            final int indexOfIntermediateResult) throws internal_error
  {
    super (lhs_sym, rhs_parts, rhs_len, action_str);
    _base_production = base;
    this.indexOfIntermediateResult = indexOfIntermediateResult;
  }

  private final int indexOfIntermediateResult;

  /**
   * @return the index of the result of the previous intermediate action on the
   *         stack relative to top, -1 if no previous action
   */
  public int getIndexOfIntermediateResult ()
  {
    return indexOfIntermediateResult;
  }
  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** The production we were taken out of. */
  protected production _base_production;

  /** The production we were taken out of. */
  public production base_production ()
  {
    return _base_production;
  }
}
