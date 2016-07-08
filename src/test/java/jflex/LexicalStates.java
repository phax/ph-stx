/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * JFlex 1.6.1                                                             *
 * Copyright (C) 1998-2015  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package jflex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple symbol table, mapping lexical state names to integers.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class LexicalStates
{

  /** maps state name to state number */
  Map <String, Integer> states;

  /** codes of inclusive states (subset of states) */
  List <Integer> inclusive;

  /** number of declared states */
  int numStates;

  /**
   * constructs a new lexical state symbol table
   */
  public LexicalStates ()
  {
    states = new LinkedHashMap<> ();
    inclusive = new ArrayList<> ();
  }

  /**
   * insert a new state declaration
   */
  public void insert (final String name, final boolean is_inclusive)
  {
    if (states.containsKey (name))
      return;

    final Integer code = numStates++;
    states.put (name, code);

    if (is_inclusive)
      inclusive.add (code);
  }

  /**
   * returns the number (code) of a declared state, <code>null</code> if no such
   * state has been declared.
   */
  public Integer getNumber (final String name)
  {
    return states.get (name);
  }

  /**
   * returns the number of declared states
   */
  public int number ()
  {
    return numStates;
  }

  /**
   * returns the names of all states
   */
  public Set <String> names ()
  {
    return states.keySet ();
  }

  /**
   * returns the code of all inclusive states
   */
  public List <Integer> getInclusiveStates ()
  {
    return inclusive;
  }
}
