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
 * Stores a regular expression from the rules section of a JFlex specification.
 * This class provides storage for one Object of content. It is used for all
 * regular expressions that are constructed from one object. For instance: a* is
 * new RegExp1(sym.STAR, 'a');
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class RegExp1 extends RegExp
{

  /**
   * The child of this expression node in the syntax tree of a regular
   * expression.
   */
  Object content;

  /** true if this regexp was created from a dot/point (.) metachar */
  boolean isPoint;

  /**
   * Constructs a new regular expression with one child object.
   *
   * @param type
   *        a value from the cup generated class sym, defining the kind of this
   *        regular expression
   * @param content
   *        the child of this expression
   */
  public RegExp1 (final int type, final Object content)
  {
    super (type);
    this.content = content;
  }

  /**
   * Returns a String-representation of this regular expression with the
   * specified indentation.
   *
   * @param tab
   *        a String that should contain only space characters and that is
   *        inserted in front of standard String-representation pf this object.
   */
  @Override
  public String print (final String tab)
  {
    if (content instanceof RegExp)
    {
      return tab + "type = " + type + Out.NL + tab + "content :" + Out.NL + ((RegExp) content).print (tab + "  ");
    }
    else
      return tab + "type = " + type + Out.NL + tab + "content :" + Out.NL + tab + "  " + content;
  }

  /**
   * Returns a String-representation of this regular expression
   */
  @Override
  public String toString ()
  {
    return print ("");
  }
}
