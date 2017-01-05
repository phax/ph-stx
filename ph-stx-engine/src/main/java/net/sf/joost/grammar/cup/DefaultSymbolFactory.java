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
package net.sf.joost.grammar.cup;

/**
 * Default Implementation for SymbolFactory, creates plain old Symbols
 *
 * @version last updated 27-03-2006
 * @author Michael Petter
 */

/*
 * ************************************************* class DefaultSymbolFactory
 * interface for creating new symbols
 ***************************************************/
public class DefaultSymbolFactory implements SymbolFactory
{
  // Factory methods
  /**
   * DefaultSymbolFactory for CUP. Users are strongly encoraged to use
   * ComplexSymbolFactory instead, since it offers more detailed information
   * about Symbols in source code. Yet since migrating has always been a
   * critical process, You have the chance of still using the oldstyle Symbols.
   *
   * @deprecated as of CUP v11a replaced by the new
   *             java_cup.runtime.ComplexSymbolFactory
   */
  // @deprecated
  @Deprecated
  public DefaultSymbolFactory ()
  {}

  public Symbol newSymbol (final String name, final int id, final Symbol left, final Symbol right, final Object value)
  {
    return new Symbol (id, left, right, value);
  }

  public Symbol newSymbol (final String name, final int id, final Symbol left, final Object value)
  {
    return new Symbol (id, left, value);
  }

  public Symbol newSymbol (final String name, final int id, final Symbol left, final Symbol right)
  {
    return new Symbol (id, left, right);
  }

  public Symbol newSymbol (final String name, final int id, final int left, final int right, final Object value)
  {
    return new Symbol (id, left, right, value);
  }

  public Symbol newSymbol (final String name, final int id, final int left, final int right)
  {
    return new Symbol (id, left, right);
  }

  public Symbol startSymbol (final String name, final int id, final int state)
  {
    return new Symbol (id, state);
  }

  public Symbol newSymbol (final String name, final int id)
  {
    return new Symbol (id);
  }

  public Symbol newSymbol (final String name, final int id, final Object value)
  {
    return new Symbol (id, value);
  }
}
