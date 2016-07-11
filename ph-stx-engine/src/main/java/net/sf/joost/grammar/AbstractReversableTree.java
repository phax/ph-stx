/*
 * $Id: ReversableTree.java,v 2.1 2004/09/29 05:59:50 obecker Exp $
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is: this file
 *
 * The Initial Developer of the Original Code is Oliver Becker.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.grammar;

/**
 * A ReversableTree object can be reconstructed by reversing its associativity.
 * 
 * @version $Revision: 2.1 $ $Date: 2004/09/29 05:59:50 $
 * @author Oliver Becker
 */
public abstract  class AbstractReversableTree extends AbstractTree
{
  public AbstractReversableTree (final int type)
  {
    super (type);
  }

  public AbstractReversableTree (final int type, final AbstractTree left, final AbstractTree right)
  {
    super (type, left, right);
  }

  /**
   * Transforms a location path by reversing the associativity of the path
   * operators <code>/</code> and <code>//</code>
   * 
   * @return the new root
   */
  @Override
  public AbstractTree reverseAssociativity ()
  {
    AbstractTree newRoot;
    if (left != null)
    {
      newRoot = left.reverseAssociativity ();
      left.right = this;
    }
    else
      newRoot = this;
    left = right;
    right = null;
    return newRoot;
  }
}
