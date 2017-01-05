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
package net.sf.joost.stx;

import java.util.Hashtable;
import java.util.Stack;

import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Locator;

import net.sf.joost.IOutputURIResolver;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.instruction.AbstractGroupBase;
import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.instruction.PSiblingsFactory;

/**
 * Instances of this class provide context information while processing an input
 * document.
 *
 * @version $Revision: 2.21 $ $Date: 2008/12/07 19:10:40 $
 * @author Oliver Becker
 */
public final class Context implements Cloneable
{
  /** The locator object for the input stream */
  public Locator locator;

  /** The emitter object for the transformation */
  public Emitter m_aEmitter;

  /** The current ancestor stack */
  public Stack <SAXEvent> ancestorStack = new Stack<> ();

  /** The position of the current node. */
  public long position;

  /** The currently processed statement in the transformation sheet */
  public AbstractNodeBase currentInstruction;

  /** The group, the current template is a child of */
  public AbstractGroupBase currentGroup;

  /** The Processor object (needed by <code>stx:process-buffer</code>) */
  public Processor currentProcessor;

  /**
   * The target group, set by <code>stx:process-<em>xxx</em></code> instructions
   */
  public AbstractGroupBase targetGroup;

  /** Encountered <code>stx:process-siblings</code> instruction */
  public PSiblingsFactory.Instance psiblings;

  /**
   * Hashtable for Stacks of group variables (key=group instance, value=Stack of
   * Hashtables).
   */
  public Hashtable <AbstractGroupBase, Stack <Hashtable <String, Value>>> groupVars = new Hashtable<> ();

  /** Local defined variables of a template. */
  public Hashtable <String, Value> localVars = new Hashtable<> ();
  public Stack <String []> localRegExGroup;

  /** External parameters passed to the transformation */
  public Hashtable <String, Value> globalParameters = new Hashtable<> ();

  /** Parameters passed to the next template */
  public Hashtable <String, Value> m_aPassedParameters = new Hashtable<> ();

  /** An ErrorHandler for reporting errors and warnings */
  public ErrorHandlerImpl m_aErrorHandler = new ErrorHandlerImpl ();

  /** The default TransformerHandlerResolver */
  public TransformerHandlerResolverImpl defaultTransformerHandlerResolver = new TransformerHandlerResolverImpl ();

  /**
   * The target handler, set by <code>stx:process-<em>xxx</em></code>
   * instructions
   */
  public TransformerHandler targetHandler;

  /** The URIResolver for <code>stx:process-document</code> */
  public URIResolver m_aURIResolver;

  /** The OutputURIResolver for <code>stx:result-document</code> */
  public IOutputURIResolver outputUriResolver;

  /**
   * The message emitter for <code>stx:message</code>, either explicitely set by
   * {@link Processor#setMessageEmitter} or automatically created in the first
   * {@link net.sf.joost.instruction.MessageFactory.Instance#process}
   * invocation.
   */
  public IStxEmitter messageEmitter;

  /** Instantiate a new emitter object for a new result event stream */
  public void pushEmitter (final IStxEmitter stxEmitter)
  {
    m_aEmitter = m_aEmitter.pushEmitter (stxEmitter);
  }

  /** re-use a previous emitter for the event stream */
  public void pushEmitter (final Emitter anEmitter)
  {
    anEmitter.m_aPrev = m_aEmitter;
    m_aEmitter = anEmitter;
  }

  /** Restore previous emitter after finishing a result event stream */
  public IStxEmitter popEmitter ()
  {
    final IStxEmitter stxEmitter = (IStxEmitter) m_aEmitter.m_aContH;
    m_aEmitter = m_aEmitter.m_aPrev;
    return stxEmitter;
  }
}
