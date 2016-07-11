/*
 * $Id: AbstractInstruction.java,v 2.3 2008/10/04 17:13:14 obecker Exp $
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
 * The Initial Developer of the Original Code is Oliver Becker.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.instruction;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.xml.sax.SAXException;

import net.sf.joost.stx.Context;

/**
 * Abstract base class for all nodes in an STX transformation sheet. Actually
 * nodes will be represented similar to tags. For an element from the
 * transformation sheet two objects (derived from
 * <code>AbstractInstruction</code>) will be created: the first to be processed
 * at the beginning of the element, the second to be processed at the end (see
 * {@link AbstractNodeBase}).
 *
 * @version $Revision: 2.3 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */
public abstract class AbstractInstruction implements Cloneable
{
  /**
   * The next instruction in the chain. The subtree of nodes in a template or
   * procedure will be represented as a linked list.
   */
  public AbstractInstruction next;

  /**
   * The line number of this instruction in the transformation sheet. Normally
   * this corresponds to the position of a start tag or an end tag resp.
   */
  public int lineNo = -1;

  /**
   * The column number of this instruction in the transformation sheet. Normally
   * this corresponds to the position of a start tag or an end tag resp.
   */
  public int colNo = -1;

  /**
   * @return the node this instruction belongs to
   */
  public abstract AbstractNodeBase getNode ();

  /**
   * The method that does the actual processing. This method will be called
   * while traversing the list of nodes.
   *
   * @param context
   *        the current context
   * @return {@link net.sf.joost.CSTX#PR_CONTINUE}, when the processing should
   *         continue with the next node; otherwise when the processing should
   *         be suspended due to an <code>stx:process-<em>xxx</em></code>
   *         instruction. This in turn means that only the implementations for
   *         these <code>stx:process-<em>xxx</em></code> instructions must
   *         return a value other than <code>PR_CONTINUE</code>. (Exception from
   *         the rule: non-recoverable errors)
   */
  public abstract short process (Context context) throws SAXException;

  /**
   * Callback that will be called when a clone of this instance has been
   * created. To be overridden in subclasses.
   *
   * @param copy
   *        the created clones
   * @param aCopies
   *        the map of already copied objects
   */
  protected void onDeepCopy (final AbstractInstruction copy,
                             final HashMap <AbstractInstruction, AbstractInstruction> aCopies)
  {
    if (next != null)
      copy.next = next.deepCopy (aCopies);
  }

  /**
   * Creates a deep copy of this instruction
   *
   * @param aCopies
   *        the map of already copied objects
   * @return the copy of this instruction
   */
  public final AbstractInstruction deepCopy (final HashMap <AbstractInstruction, AbstractInstruction> aCopies)
  {
    AbstractInstruction copy = aCopies.get (this);
    if (copy == null)
    {
      try
      {
        copy = (AbstractInstruction) this.clone ();
      }
      catch (final CloneNotSupportedException e)
      {
        // mustn't happen since this class implements Cloneable
        throw new RuntimeException (e);
      }
      aCopies.put (this, copy);
      onDeepCopy (copy, aCopies);
    }
    return copy;
  }

  /**
   * Create a deep copy of a {@link Hashtable} that contains
   * {@link AbstractInstruction} instances as values
   *
   * @param hashtable
   *        the Hashtable to be copied
   * @param copies
   *        the map of already copied objects
   * @return the copy
   */
  public static final Hashtable deepHashtableCopy (final Hashtable hashtable, final HashMap copies)
  {
    Hashtable copy = (Hashtable) copies.get (hashtable);
    if (copy == null)
    {
      copy = new Hashtable (hashtable.size ());
      for (final Enumeration e = hashtable.keys (); e.hasMoreElements ();)
      {
        final Object key = e.nextElement ();
        copy.put (key, ((AbstractInstruction) hashtable.get (key)).deepCopy (copies));
      }
      copies.put (hashtable, copy);
    }
    return copy;
  }

  /**
   * Create a deep copy of an array of STX template instances.
   *
   * @param templates
   *        the array to be copied
   * @param copies
   *        the map of already copied objects
   * @return the copy
   */
  public static final TemplateFactory.Instance [] deepTemplateArrayCopy (final TemplateFactory.Instance [] templates,
                                                                         final HashMap copies)
  {
    TemplateFactory.Instance [] copy = (TemplateFactory.Instance []) copies.get (templates);
    if (copy == null)
    {
      copy = new TemplateFactory.Instance [templates.length];
      for (int i = 0; i < templates.length; i++)
      {
        copy[i] = (TemplateFactory.Instance) templates[i].deepCopy (copies);
      }
    }
    return copy;
  }
}
