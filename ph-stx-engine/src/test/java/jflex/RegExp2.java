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
package jflex;

/**
 * Regular expression with two children (e.g. a | b)
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class RegExp2 extends RegExp
{

  RegExp r1, r2;

  public RegExp2 (final int type, final RegExp r1, final RegExp r2)
  {
    super (type);
    this.r1 = r1;
    this.r2 = r2;
  }

  @Override
  public String print (final String tab)
  {
    return tab +
           "type = " + //$NON-NLS-1$
           type +
           Out.NL +
           tab +
           "child 1 :" + //$NON-NLS-1$
           Out.NL +
           r1.print (tab + "  ") + //$NON-NLS-1$
           Out.NL +
           tab +
           "child 2 :" + //$NON-NLS-1$
           Out.NL +
           r2.print (tab + "  "); //$NON-NLS-1$
  }

  @Override
  public String toString ()
  {
    return print (""); //$NON-NLS-1$
  }
}
