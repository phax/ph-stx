/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * JFlex 1.6.1                                                             *
 * Copyright (C) 1998-2015  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package jflex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple table to store EOF actions for each lexical state.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class EOFActions
{

  /** maps lexical states to actions */
  private final Map <Integer, Action> actions = new HashMap<> ();
  private Action defaultAction;
  private int numLexStates;

  public void setNumLexStates (final int num)
  {
    numLexStates = num;
  }

  public void add (final List <Integer> stateList, final Action action)
  {

    if (stateList != null && stateList.size () > 0)
    {
      for (final Integer state : stateList)
        add (state, action);
    }
    else
    {
      defaultAction = action.getHigherPriority (defaultAction);

      for (int state = 0; state < numLexStates; state++)
      {
        if (actions.get (state) != null)
        {
          final Action oldAction = actions.get (state);
          actions.put (state, oldAction.getHigherPriority (action));
        }
      }
    }
  }

  public void add (final Integer state, final Action action)
  {
    if (actions.get (state) == null)
      actions.put (state, action);
    else
    {
      final Action oldAction = actions.get (state);
      actions.put (state, oldAction.getHigherPriority (action));
    }
  }

  public boolean isEOFAction (final Object a)
  {
    if (a == defaultAction)
      return true;

    for (final Action action : actions.values ())
      if (a == action)
        return true;

    return false;
  }

  public Action getAction (final int state)
  {
    return actions.get (state);
  }

  public Action getDefault ()
  {
    return defaultAction;
  }

  public int numActions ()
  {
    return actions.size ();
  }
}
