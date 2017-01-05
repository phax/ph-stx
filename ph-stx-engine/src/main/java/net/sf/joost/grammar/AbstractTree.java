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
package net.sf.joost.grammar;

import java.util.HashMap;

import org.xml.sax.SAXException;

import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;

/**
 * Objects of Tree represent nodes in the syntax tree of a pattern or an STXPath
 * expression.
 *
 * @version $Revision: 2.14 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */
public abstract class AbstractTree implements Cloneable
{
  /** Node type constants for {@link #m_nType} */
  public static final int ROOT = 1; // root node
  public static final int CHILD = 2; // child axis "/"
  public static final int DESC = 3; // descendend axis "//"
  public static final int UNION = 4; // "|"
  public static final int NAME_TEST = 5; // an element name (qname)
  public static final int WILDCARD = 6; // "*"
  public static final int URI_WILDCARD = 7; // "*:ncname"
  public static final int LOCAL_WILDCARD = 8; // "prefix:*"
  public static final int NODE_TEST = 9; // "node()"
  public static final int TEXT_TEST = 10; // "text()"
  public static final int CDATA_TEST = 100; // "cdata()"
  public static final int COMMENT_TEST = 11; // "comment()"
  public static final int PI_TEST = 12; // "pi()"; "pi(...)"
  public static final int FUNCTION = 13; // a function call
  public static final int PREDICATE = 14; // a predicate "[" ... "]"
  public static final int NUMBER = 15; // a number
  public static final int STRING = 16; // a quoted string
  public static final int ADD = 17; // "+"
  public static final int SUB = 18; // "-"
  public static final int MULT = 19; // "*"
  public static final int DIV = 20; // "div"
  public static final int MOD = 21; // "mod"
  public static final int AND = 22; // "and"
  public static final int OR = 23; // "or"
  public static final int EQ = 24; // "="
  public static final int NE = 25; // "!="
  public static final int LT = 26; // "<"
  public static final int LE = 27; // "<="
  public static final int GT = 28; // ">"
  public static final int GE = 29; // ">="
  public static final int ATTR = 30; // "@qname"
  public static final int ATTR_WILDCARD = 31; // "@*"
  public static final int ATTR_URI_WILDCARD = 32; // "@*:ncname"
  public static final int ATTR_LOCAL_WILDCARD = 33; // "@prefix:*"
  public static final int LIST = 34; // ";" in parameter list
  public static final int SEQ = 35; // ";" in sequences
  public static final int AVT = 36; // "{" ... "}"
  public static final int VAR = 37; // "$qname"
  public static final int DOT = 38; // "."
  public static final int DDOT = 39; // ".."
  public static final int VALUE = 40; // internal: a constructed value leaf

  /** The type of the node in the Tree. */
  private final int m_nType;

  /** The left subtree. */
  public AbstractTree m_aLeft;

  /** The right subtree. */
  public AbstractTree m_aRight;

  /** The value of this node as an object. */
  public Object m_aValue;

  /** URI if {@link #m_aValue} is a qualified name. */
  public String m_sURI;

  /** Local name if {@link #m_aValue} is a qualified name. */
  public String m_sLocalName;

  //
  // Constructors
  //

  /** The most general constructor */
  private AbstractTree (final int type, final AbstractTree left, final AbstractTree right, final Object value)
  {
    m_nType = type;
    m_aLeft = left;
    m_aRight = right;
    m_aValue = value;
  }

  /** Constructs a Tree object as a node with two subtrees. */
  public AbstractTree (final int type, final AbstractTree left, final AbstractTree right)
  {
    this (type, left, right, null);
  }

  /** Constructs a Tree object as a leaf. */
  public AbstractTree (final int type, final Object value)
  {
    this (type, null, null, value);
  }

  /** Constructs a Tree object as a leaf without a value. */
  public AbstractTree (final int type)
  {
    this (type, null, null, null);
  }

  public int getType ()
  {
    return m_nType;
  }

  /**
   * Determines if the event stack matches the pattern represented by this Tree
   * object.
   *
   * @param context
   *        the Context object
   * @param top
   *        the part of the stack to be considered while matching (the upper
   *        most element is at position top-1)
   * @param setPosition
   *        <code>true</code> if the context position ({@link Context#position})
   *        should be set in case the event stack matches this pattern
   * @return <code>true</code> if the stack matches the pattern represented by
   *         this Tree.
   */
  public boolean matches (final Context context, final int top, final boolean setPosition) throws SAXException
  {
    context.m_aErrorHandler.fatalError ("Fatal: unprocessed type in matching: " +
                                        this,
                                        context.currentInstruction.m_sPublicID,
                                        context.currentInstruction.m_sSystemID,
                                        context.currentInstruction.lineNo,
                                        context.currentInstruction.colNo);
    return false;
  }

  /**
   * Evaluates the current Tree if it represents an expression.
   *
   * @param context
   *        the current Context
   * @param instruction
   *        the current instruction, needed for providing locator information in
   *        the event of an error
   * @return a new computed Value object containing the result
   */
  public Value evaluate (final Context context, final AbstractNodeBase instruction) throws SAXException
  {
    context.currentInstruction = instruction;
    return evaluate (context, context.ancestorStack.size ());
  }

  /**
   * Evaluates the current Tree if it represents an expression.
   *
   * @param context
   *        the current Context
   * @param top
   *        the part of the stack to be considered for the evaluation (the upper
   *        most element is at position top-1)
   * @return a new computed Value object containing the result
   */
  public Value evaluate (final Context context, final int top) throws SAXException
  {
    context.m_aErrorHandler.fatalError ("Fatal: unprocessed type in evaluating: " +
                                        this,
                                        context.currentInstruction.m_sPublicID,
                                        context.currentInstruction.m_sSystemID,
                                        context.currentInstruction.lineNo,
                                        context.currentInstruction.colNo);
    return null;
  }

  /** May be overridden to reconstruct the current tree */
  public AbstractTree reverseAssociativity ()
  {
    return this;
  }

  /**
   * Returns the default priority of the STXPath pattern represented by this
   * tree.
   */
  public double getPriority ()
  {
    return 0.5;
  }

  /**
   * @return whether the expression represented by this tree is constant
   */
  public boolean isConstant ()
  {
    return (m_aRight == null || m_aRight.isConstant ()) && (m_aLeft == null || m_aLeft.isConstant ());
  }

  /**
   * Creates a deep copy of this Tree
   *
   * @param copies
   *        the map of already copied objects that need to be remembered (mainly
   *        of {@link AbstractInstruction})
   * @return the created copy
   */
  public AbstractTree deepCopy (final HashMap <Object, Object> copies)
  {
    // no need to keep Tree instances in the copies map because there are
    // no circular references of Trees
    AbstractTree copy;
    try
    {
      copy = (AbstractTree) clone ();
    }
    catch (final CloneNotSupportedException ex)
    {
      throw new RuntimeException ("this is not cloneable", ex);
    }

    if (m_aLeft != null)
      copy.m_aLeft = m_aLeft.deepCopy (copies);
    if (m_aRight != null)
      copy.m_aRight = m_aRight.deepCopy (copies);
    // note: cannot clone the value
    return copy;
  }

  // for debugging
  @Override
  public String toString ()
  {
    String ret = "{";
    switch (m_nType)
    {
      case ROOT:
        ret += "ROOT";
        break;
      case CHILD:
        ret += "CHILD";
        break;
      case DESC:
        ret += "DESC";
        break;
      case NAME_TEST:
        ret += "NAME_TEST";
        break;
      case TEXT_TEST:
        ret += "TEXT_TEST";
        break;
      case NODE_TEST:
        ret += "NODE_TEST";
        break;
      case COMMENT_TEST:
        ret += "COMMENT_TEST";
        break;
      case ATTR:
        ret += "ATTR";
        break;
      case EQ:
        ret += "EQ";
        break;
      case STRING:
        ret += "STRING";
        break;
      case NUMBER:
        ret += "NUMBER";
        break;
      case WILDCARD:
        ret += "*";
        break;
      case DDOT:
        ret += "..";
        break;
      default:
        ret += m_nType;
        break;
    }
    ret += "," + m_aLeft + "," + m_aRight + "," + m_aValue;
    if (m_nType == NAME_TEST || m_nType == URI_WILDCARD || m_nType == LOCAL_WILDCARD)
      ret += "(" + m_sURI + "|" + m_sLocalName + ")";
    return ret + "}";
  }
}
