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
package net.sf.joost.grammar;

/**
 * A ReversableTree object can be reconstructed by reversing its associativity.
 *
 * @version $Revision: 2.1 $ $Date: 2004/09/29 05:59:50 $
 * @author Oliver Becker
 */
public abstract class AbstractReversableTree extends AbstractTree
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
    if (m_aLeft != null)
    {
      newRoot = m_aLeft.reverseAssociativity ();
      m_aLeft.m_aRight = this;
    }
    else
      newRoot = this;
    m_aLeft = m_aRight;
    m_aRight = null;
    return newRoot;
  }
}
