/*
 * $Id: TraceListener.java,v 1.5 2004/09/19 13:36:42 obecker Exp $
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
 * The Initial Developer of the Original Code is Anatolij Zubow.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): Oliver Becker.
 */

package net.sf.joost.trace;

import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.stx.SAXEvent;

/**
 * The STX processor calls this interface when it matches a source node, selects
 * a set of source nodes, or generates a result node. To react on trace events
 * you have to register your own object on the
 * {@link net.sf.joost.trax.TransformerImpl} with the help of the
 * {@link net.sf.joost.trace.TraceManager}
 * {@link net.sf.joost.trax.TransformerImpl#getTraceManager()}.
 * 
 * @version $Revision: 1.5 $ $Date: 2004/09/19 13:36:42 $
 * @author Zubow
 */
public interface ITraceListener
{

  // ----------------------------------------------
  // Source events
  // ----------------------------------------------

  /**
   * Called at the start of processing.
   */
  void startSourceDocument ();

  /**
   * Called at end of processing.
   */
  void endSourceDocument ();

  /**
   * Called when a start element event of the source was received.
   */
  void startSourceElement (SAXEvent saxevent);

  /**
   * Called when a end element event of the source was received.
   */
  void endSourceElement (SAXEvent saxevent);

  /**
   * Called when a text event of the source was received.
   */
  void sourceText (SAXEvent saxevent);

  /**
   * Called when a PI event of the source was received.
   */
  void sourcePI (SAXEvent saxevent);

  /**
   * Called when a ns mapping of the source was received.
   */
  void sourceMapping (SAXEvent saxevent);

  /**
   * Called when a comment of the source was received.
   */
  void sourceComment (SAXEvent saxevent);

  // ----------------------------------------------
  // Transformation sheet events
  // ----------------------------------------------

  /**
   * Called when an element of the stylesheet gets processed.
   */
  void enterInstructionNode (AbstractInstruction inst, SAXEvent event);

  /**
   * Called after an element of the stylesheet got processed.
   */
  void leaveInstructionNode (AbstractInstruction inst, SAXEvent event);

  // ----------------------------------------------
  // Emitter events
  // ----------------------------------------------

  /**
   * Called for emitter start document event.
   */
  void startResultDocument ();

  /**
   * Called for emitter end document event.
   */
  void endResultDocument ();

  /**
   * Called for emitter start element event.
   */
  void startResultElement (SAXEvent saxevent);

  /**
   * Called for emitter end element event.
   */
  void endResultElement (SAXEvent saxevent);

  /**
   * Called for emitter text event.
   */
  void resultText (SAXEvent saxevent);

  /**
   * Called for emitter PI event.
   */
  void resultPI (SAXEvent saxevent);

  /**
   * Called for emitter comment event.
   */
  void resultComment (SAXEvent saxevent);

  /**
   * Called for emitter start CDATA event.
   */
  void startResultCDATA ();

  /**
   * Called for emitter end CDATA event.
   */
  void endResultCDATA ();
}
