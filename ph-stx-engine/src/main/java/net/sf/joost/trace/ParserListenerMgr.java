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
package net.sf.joost.trace;

import java.util.ArrayList;
import java.util.List;

import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.stx.IParserListener;
import net.sf.joost.trax.TransformerImpl;

/**
 * This class implements the {@link IParserListener}-Interface for static debug
 * purpose (e.g. validation of breakpoints).
 *
 * @author Zubow
 */
public class ParserListenerMgr implements IParserListener
{

  /** list of all registered {@link IParserListener} */
  private List <IParserListener> parserListeners = null;

  /** default constructor */
  public ParserListenerMgr ()
  {}

  /**
   * Check if parserlisteners are available.
   *
   * @return True if there are registered parserlisteners
   */
  public boolean hasParseListeners ()
  {
    return (parserListeners != null);
  }

  /**
   * Add a parserlistener (debugging and profiling).
   *
   * @param newParserListener
   *        A parserlistener to be added.
   */
  public void addParseListener (final IParserListener newParserListener)
  {
    // set Joost-Transformer in debug-mode
    // todo think about this ???
    TransformerImpl.DEBUG_MODE = true;
    if (parserListeners == null)
    {
      parserListeners = new ArrayList <> ();
    }
    // add new parserlistener
    parserListeners.add (newParserListener);
  }

  /**
   * Remove a parserlistener.
   *
   * @param oldParserListener
   *        A parserlistener to be removed.
   */
  public void removeParseListener (final IParserListener oldParserListener)
  {
    if (parserListeners != null)
    {
      // remove the given parserlistener
      parserListeners.remove (oldParserListener);
    }
  }

  // ----------------------------------------------------------------------
  // Callback methods
  // ----------------------------------------------------------------------

  /** see {@link IParserListener#nodeCreated} */
  public void nodeCreated (final AbstractNodeBase node)
  {
    if (hasParseListeners ())
    {
      for (int i = 0; i < parserListeners.size (); i++)
      {
        final IParserListener pl = parserListeners.get (i);
        pl.nodeCreated (node);
      }
    }
  }

  /** see {@link IParserListener#parseFinished} */
  public void parseFinished ()
  {
    if (hasParseListeners ())
    {
      for (int i = 0; i < parserListeners.size (); i++)
      {
        final IParserListener pl = parserListeners.get (i);
        pl.parseFinished ();
      }
    }
  }
}
