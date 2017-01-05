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
