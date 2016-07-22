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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Symbol table and expander for macros. Maps macros to their (expanded)
 * definitions, detects cycles and unused macros.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class Macros
{

  /** Maps names of macros to their definition */
  private final Map <String, RegExp> macros;

  /** Maps names of macros to their "used" flag */
  private final Map <String, Boolean> used;

  /**
   * Creates a new macro expander.
   */
  public Macros ()
  {
    macros = new HashMap<> ();
    used = new HashMap<> ();
  }

  /**
   * Stores a new macro and its definition.
   *
   * @param name
   *        the name of the new macro
   * @param definition
   *        the definition of the new macro
   * @return <code>true</code>, iff the macro name has not been stored before.
   */
  public boolean insert (final String name, final RegExp definition)
  {

    if (Options.DEBUG)
      Out.debug ("inserting macro " + name + " with definition :" + Out.NL + definition); //$NON-NLS-1$ //$NON-NLS-2$

    used.put (name, Boolean.FALSE);
    return macros.put (name, definition) == null;
  }

  /**
   * Marks a makro as used.
   *
   * @return <code>true</code>, iff the macro name has been stored before.
   */
  public boolean markUsed (final String name)
  {
    return used.put (name, Boolean.TRUE) != null;
  }

  /**
   * Tests if a macro has been used.
   *
   * @return <code>true</code>, iff the macro has been used in a regular
   *         expression.
   */
  public boolean isUsed (final String name)
  {
    return used.get (name);
  }

  /**
   * Returns all unused macros.
   *
   * @return the macro names that have not been used.
   */
  public List <String> unused ()
  {

    final List <String> unUsed = new ArrayList<> ();

    for (final String name : used.keySet ())
    {
      final Boolean isUsed = used.get (name);
      if (!isUsed)
        unUsed.add (name);
    }

    return unUsed;
  }

  /**
   * Fetches the definition of the macro with the specified name,
   * <p>
   * The definition will either be the same as stored (expand() not called), or
   * an equivalent one, that doesn't contain any macro usages (expand() called
   * before).
   *
   * @param name
   *        the name of the macro
   * @return the definition of the macro, <code>null</code> if no macro with the
   *         specified name has been stored.
   * @see jflex.Macros#expand
   */
  public RegExp getDefinition (final String name)
  {
    return macros.get (name);
  }

  /**
   * Expands all stored macros, so that getDefinition always returns a defintion
   * that doesn't contain any macro usages.
   *
   * @throws MacroException
   *         if there is a cycle in the macro usage graph.
   */
  public void expand () throws MacroException
  {
    for (final String name : macros.keySet ())
    {
      if (isUsed (name))
        macros.put (name, expandMacro (name, getDefinition (name)));
      // this put doesn't get a new key, so only a new value
      // is set for the key "name"
    }
  }

  /**
   * Expands the specified macro by replacing each macro usage with the stored
   * definition.
   * 
   * @param name
   *        the name of the macro to expand (for detecting cycles)
   * @param definition
   *        the definition of the macro to expand
   * @return the expanded definition of the macro.
   * @throws MacroException
   *         when an error (such as a cyclic definition) occurs during expansion
   */
  private RegExp expandMacro (final String name, final RegExp definition) throws MacroException
  {

    // Out.print("checking macro "+name);
    // Out.print("definition is "+definition);

    switch (definition.type)
    {
      case sym.BAR:
      case sym.CONCAT:
        final RegExp2 binary = (RegExp2) definition;
        binary.r1 = expandMacro (name, binary.r1);
        binary.r2 = expandMacro (name, binary.r2);
        return definition;

      case sym.STAR:
      case sym.PLUS:
      case sym.QUESTION:
      case sym.BANG:
      case sym.TILDE:
        final RegExp1 unary = (RegExp1) definition;
        unary.content = expandMacro (name, (RegExp) unary.content);
        return definition;

      case sym.MACROUSE:
        final String usename = (String) ((RegExp1) definition).content;

        if (name.equals (usename))
          throw new MacroException (ErrorMessages.get (ErrorMessages.MACRO_CYCLE, name));

        final RegExp usedef = getDefinition (usename);

        if (usedef == null)
          throw new MacroException (ErrorMessages.get (ErrorMessages.MACRO_DEF_MISSING, usename, name));

        markUsed (usename);

        return expandMacro (name, usedef);

      case sym.STRING:
      case sym.STRING_I:
      case sym.CHAR:
      case sym.CHAR_I:
      case sym.CCLASS:
      case sym.CCLASSNOT:
        return definition;

      default:
        throw new MacroException ("unknown expression type " + definition.type + " in macro expansion"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
}
