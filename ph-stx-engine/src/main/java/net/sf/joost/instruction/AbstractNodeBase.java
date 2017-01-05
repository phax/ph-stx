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
package net.sf.joost.instruction;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Abstract base class for all instances of nodes in the STX transformation
 * sheet
 *
 * @version $Revision: 2.15 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */
public abstract class AbstractNodeBase extends AbstractInstruction
{
  //
  // Inner classes
  //

  /**
   * Generic class that represents the end of an element in the STX
   * transformation sheet (the end tag). Its {@link #process} method simply
   * calls {@link #processEnd(Context context)} in the appropriate
   * {@link AbstractNodeBase} object.
   */
  public final class End extends AbstractInstruction
  {
    /**
     * The appropriate start tag.
     */
    private AbstractNodeBase m_aStart;

    private End (final AbstractNodeBase start)
    {
      m_aStart = start;
    }

    /**
     * @return {@link #m_aStart}
     */
    @Override
    public AbstractNodeBase getNode ()
    {
      return m_aStart;
    }

    /**
     * Calls the {@link AbstractNodeBase#processEnd} method in its
     * {@link #m_aStart} object.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      return m_aStart.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final End theCopy = (End) copy;
      if (m_aStart != null)
        theCopy.m_aStart = (AbstractNodeBase) m_aStart.deepCopy (copies);
    }

    // for debugging
    @Override
    public String toString ()
    {
      return "end " + m_aStart;
    }
  }

  // ---------------------------------------------------------------------

  //
  // Member fields
  //

  /** The qualified name of this node */
  public String m_sQName;

  /** The parent of this node */
  public AbstractNodeBase m_aParent;

  /**
   * The reference to the last child, needed for inserting additional nodes
   * while parsing the transformation sheet.
   */
  protected AbstractInstruction m_aLastChild;

  /**
   * The reference to the end instruction. <code>null</code> means: must be an
   * empty element.
   */
  protected AbstractInstruction m_aNodeEnd;

  /** The public identifier of the transformation sheet */
  public String m_sPublicID = "";

  /** The system identifier of the transformation sheet */
  public String m_sSystemID = "";

  /**
   * <code>true</code> if the attribute <code>xml:space</code> on the nearest
   * ancestor element was set to <code>preserve</code>, <code>false</code>
   * otherwise. This field is set in the {@link net.sf.joost.stx.Parser} object.
   */
  public boolean m_bPreserveSpace;

  /**
   * The names of local declared variables of this element, available only if
   * this node has stx:variable children
   */
  protected Vector <String> m_aScopedVariables;

  /** Stack for storing local fields from this or derived classes */
  protected Stack <Object> m_aLocalFieldStack = new Stack<> ();

  // ---------------------------------------------------------------------

  //
  // Constructors
  //

  /*
   * Constructs a node.
   * @param qName the qualified name of this node
   * @param parent the parent of this node
   * @param context the current parse context
   * @param mayHaveChildren <code>true</code> if the node may have children
   */
  protected AbstractNodeBase (final String qName,
                              final AbstractNodeBase parent,
                              final ParseContext context,
                              final boolean mayHaveChildren)
  {
    this.m_sQName = qName;
    this.m_aParent = parent;
    if (context.locator != null)
    {
      m_sPublicID = context.locator.getPublicId ();
      m_sSystemID = context.locator.getSystemId ();
      lineNo = context.locator.getLineNumber ();
      colNo = context.locator.getColumnNumber ();
    }

    if (mayHaveChildren)
    {
      next = m_aNodeEnd = new End (this);
      // indicates that children are allowed
      m_aLastChild = this;
    }
  }

  // ---------------------------------------------------------------------

  //
  // Methods
  //

  @Override
  public final AbstractNodeBase getNode ()
  {
    return this;
  }

  /**
   * Insert a new node as a child of this element
   *
   * @param node
   *        the node to be inserted
   */
  public void insert (final AbstractNodeBase node) throws SAXParseException
  {
    if (m_aLastChild == null)
      throw new SAXParseException ("'" +
                                   m_sQName +
                                   "' must be empty",
                                   node.m_sPublicID,
                                   node.m_sSystemID,
                                   node.lineNo,
                                   node.colNo);

    // append after lastChild
    // first: find end of the subtree represented by node
    AbstractInstruction newLast = node;
    while (newLast.next != null)
      newLast = newLast.next;
    // then: insert the subtree
    newLast.next = m_aLastChild.next;
    m_aLastChild.next = node;
    // adjust lastChild
    m_aLastChild = newLast;

    // create vector for variable names if necessary
    if (node instanceof AbstractVariableBase && m_aScopedVariables == null)
      m_aScopedVariables = new Vector<> ();
  }

  /**
   * Notify this node about its end location (taken from
   * {@link ParseContext#locator} in the <code>context</code> parameter)
   *
   * @param context
   *        the current parse context
   */
  public final void setEndLocation (final ParseContext context)
  {
    if (m_aNodeEnd != null && context.locator != null)
    {
      m_aNodeEnd.lineNo = context.locator.getLineNumber ();
      m_aNodeEnd.colNo = context.locator.getColumnNumber ();
    }
  }

  /**
   * This method may be overwritten to perform compilation tasks (for example
   * optimization) on this node. <code>compile</code> will be called with a
   * parameter <code>0</code> directly after parsing the node, i.e. after
   * parsing all children. The invocation with bigger <code>pass</code>
   * parameters happens not before the whole transformation sheet has been
   * completely parsed.
   *
   * @param pass
   *        the number of invocations already performed on this node
   * @param context
   *        the parse context
   * @return <code>true</code> if another invocation in the next pass is
   *         necessary, <code>false</code> if the compiling is complete. This
   *         instance returns <code>false</code>.
   * @throws SAXException
   *         in case of error
   */
  public boolean compile (final int pass, final ParseContext context) throws SAXException
  {
    return false;
  }

  /**
   * Removes (if possible) the end node ({@link #m_aNodeEnd}) of this
   * instruction from the execution chain. May be invoked from
   * {@link #compile(int, ParseContext)} of concrete instructions only if
   * {@link #processEnd(Context)} hasn't been overridden.
   */
  protected final void mayDropEnd ()
  {
    if (m_aScopedVariables == null)
    {
      m_aLastChild.next = m_aNodeEnd.next;
      if (m_aParent.m_aLastChild == m_aNodeEnd)
        m_aParent.m_aLastChild = m_aLastChild;
      m_aNodeEnd = m_aLastChild;
    }
  }

  /**
   * Store the name of a variable as local for this node.
   *
   * @param name
   *        the variable name
   */
  protected final void declareVariable (final String name)
  {
    m_aScopedVariables.addElement (name);
  }

  /**
   * @return <code>true</code> if {@link #process} can be invoked on this node,
   *         and <code>false</code> otherwise
   */
  public boolean processable ()
  {
    return true;
  }

  /**
   * Save local variables if needed.
   *
   * @return {@link CSTX#PR_CONTINUE}
   * @exception SAXException
   *            if an error occurs (in a derived class)
   */
  @Override
  public short process (final Context context) throws SAXException
  {
    if (m_aScopedVariables != null)
    {
      // store list of local variables (from another instantiation)
      m_aLocalFieldStack.push (m_aScopedVariables.clone ());
      m_aScopedVariables.clear ();
    }
    return CSTX.PR_CONTINUE;
  }

  /**
   * Called when the end tag will be processed. This instance removes local
   * variables declared in this node.
   *
   * @param context
   *        the current context
   * @return {@link CSTX#PR_CONTINUE}
   * @exception SAXException
   *            if an error occurs (in a derived class)
   */
  protected short processEnd (final Context context) throws SAXException
  {
    if (m_aScopedVariables != null)
    {
      // remove all local variables
      final Object [] objs = m_aScopedVariables.toArray ();
      for (final Object obj : objs)
        context.localVars.remove (obj);
      m_aScopedVariables = (Vector <String>) m_aLocalFieldStack.pop ();
    }
    return CSTX.PR_CONTINUE;
  }

  /**
   * Getter for {@link #m_aNodeEnd} used by
   * {@link net.sf.joost.stx.Processor#processEvent}.
   *
   * @return a final ref on <code>AbstractInstruction</code>
   */
  public final AbstractInstruction getNodeEnd ()
  {
    return this.m_aNodeEnd;
  }

  @Override
  protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
  {
    super.onDeepCopy (copy, copies);
    final AbstractNodeBase theCopy = (AbstractNodeBase) copy;
    theCopy.m_aLocalFieldStack = (Stack <Object>) copies.get (m_aLocalFieldStack);
    if (theCopy.m_aLocalFieldStack == null)
    {
      theCopy.m_aLocalFieldStack = new Stack<> ();
      copies.put (m_aLocalFieldStack, theCopy.m_aLocalFieldStack);
    }
    if (m_aLastChild != null)
      theCopy.m_aLastChild = m_aLastChild.deepCopy (copies);
    if (m_aNodeEnd != null)
      theCopy.m_aNodeEnd = m_aNodeEnd.deepCopy (copies);
    if (m_aParent != null)
      theCopy.m_aParent = (AbstractNodeBase) m_aParent.deepCopy (copies);
    if (m_aScopedVariables != null)
      theCopy.m_aScopedVariables = new Vector<> ();
  }

  // for debugging
  @Override
  public String toString ()
  {
    return getClass ().getName () + " " + lineNo;
  }
}
