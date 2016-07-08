/*
 * $Id: Tree.java,v 2.14 2008/10/04 17:13:14 obecker Exp $
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
 * Contributor(s): Thomas Behrends.
 */

package net.sf.joost.grammar;

import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.instruction.NodeBase;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;

import java.util.HashMap;

import org.xml.sax.SAXException;

/**
 * Objects of Tree represent nodes in the syntax tree of a pattern or
 * an STXPath expression.
 * @version $Revision: 2.14 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */
public abstract class Tree implements Cloneable
{
   /** Node type constants for {@link #type} */
   public static final int
      ROOT                = 1,   // root node
      CHILD               = 2,   // child axis "/"
      DESC                = 3,   // descendend axis "//"
      UNION               = 4,   // "|"
      NAME_TEST           = 5,   // an element name (qname)
      WILDCARD            = 6,   // "*"
      URI_WILDCARD        = 7,   // "*:ncname"
      LOCAL_WILDCARD      = 8,   // "prefix:*"
      NODE_TEST           = 9,   // "node()"
      TEXT_TEST           = 10,  // "text()"
      CDATA_TEST          = 100, // "cdata()"
      COMMENT_TEST        = 11,  // "comment()"
      PI_TEST             = 12,  // "pi()", "pi(...)"
      FUNCTION            = 13,  // a function call
      PREDICATE           = 14,  // a predicate "[" ... "]"
      NUMBER              = 15,  // a number
      STRING              = 16,  // a quoted string
      ADD                 = 17,  // "+"
      SUB                 = 18,  // "-"
      MULT                = 19,  // "*"
      DIV                 = 20,  // "div"
      MOD                 = 21,  // "mod"
      AND                 = 22,  // "and"
      OR                  = 23,  // "or"
      EQ                  = 24,  // "="
      NE                  = 25,  // "!="
      LT                  = 26,  // "<"
      LE                  = 27,  // "<="
      GT                  = 28,  // ">"
      GE                  = 29,  // ">="
      ATTR                = 30,  // "@qname"
      ATTR_WILDCARD       = 31,  // "@*"
      ATTR_URI_WILDCARD   = 32,  // "@*:ncname"
      ATTR_LOCAL_WILDCARD = 33,  // "@prefix:*"
      LIST                = 34,  // "," in parameter list
      SEQ                 = 35,  // "," in sequences
      AVT                 = 36,  // "{" ... "}"
      VAR                 = 37,  // "$qname"
      DOT                 = 38,  // "."
      DDOT                = 39,  // ".."
      VALUE               = 40;  // internal: a constructed value leaf


   /** The type of the node in the Tree. */
   public int type;

   /** The left subtree. */
   public Tree left;

   /** The right subtree. */
   public Tree right;

   /** The value of this node as an object. */
   public Object value;

   /** URI if {@link #value} is a qualified name. */
   public String uri;

   /** Local name if {@link #value} is a qualified name. */
   public String lName;


   //
   // Constructors
   //


   /** The most general constructor */
   private Tree(int type, Tree left, Tree right, Object value)
   {
      this.type = type;
      this.left = left;
      this.right = right;
      this.value = value;
      // System.err.println("Tree-Constructor 1: " + this);
   }

   /** Constructs a Tree object as a node with two subtrees. */
   public Tree(int type, Tree left, Tree right)
   {
      this(type, left, right, null);
      // System.err.println("Tree-Constructor 2: " + this);
   }

   /** Constructs a Tree object as a leaf. */
   public Tree(int type, Object value)
   {
      this(type, null, null, value);
      // System.err.println("Tree-Constructor 3: " + this);
   }

   /** Constructs a Tree object as a leaf without a value. */
   public Tree(int type)
   {
      this(type, null, null, null);
   }


   /**
    * Determines if the event stack matches the pattern represented
    * by this Tree object.
    *
    * @param context the Context object
    * @param top the part of the stack to be considered while matching
    *        (the upper most element is at position top-1)
    * @param setPosition <code>true</code> if the context position
    *        ({@link Context#position}) should be set in case the
    *        event stack matches this pattern
    * @return <code>true</code> if the stack matches the pattern represented
    *         by this Tree.
    */
   public boolean matches(Context context, int top, boolean setPosition)
      throws SAXException
   {
      context.errorHandler.fatalError("Fatal: unprocessed type in matching: " +
                                      this,
                                      context.currentInstruction.publicId,
                                      context.currentInstruction.systemId,
                                      context.currentInstruction.lineNo,
                                      context.currentInstruction.colNo);
      return false;
   }


   /**
    * Evaluates the current Tree if it represents an expression.
    * @param context the current Context
    * @param instruction the current instruction, needed for providing
    *        locator information in the event of an error
    * @return a new computed Value object containing the result
    */
   public Value evaluate(Context context, NodeBase instruction)
      throws SAXException
   {
      context.currentInstruction = instruction;
      return evaluate(context, context.ancestorStack.size());
   }


   /**
    * Evaluates the current Tree if it represents an expression.
    * @param context the current Context
    * @param top the part of the stack to be considered for the evaluation
    *            (the upper most element is at position top-1)
    * @return a new computed Value object containing the result
    */
   public Value evaluate(Context context, int top)
      throws SAXException
   {
      context.errorHandler.fatalError("Fatal: unprocessed type in evaluating: " +
                                      this,
                                      context.currentInstruction.publicId,
                                      context.currentInstruction.systemId,
                                      context.currentInstruction.lineNo,
                                      context.currentInstruction.colNo);
      return null;
   }


   /** May be overridden to reconstruct the current tree */
   public Tree reverseAssociativity()
   {
         return this;
   }


   /**
    * Returns the default priority of the STXPath pattern represented by
    * this tree.
    */
   public double getPriority()
   {
      return 0.5;
   }


   /**
    * @return whether the expression represented by this tree is constant
    */
   public boolean isConstant()
   {
      return (right == null || right.isConstant())
                && (left == null || left.isConstant());
   }


   /**
    * Creates a deep copy of this Tree
    * @param copies the map of already copied objects that need to be remembered
    * (mainly of {@link AbstractInstruction})
    * @return the created copy
    */
   public Tree deepCopy(HashMap copies) {
      // no need to keep Tree instances in the copies map because there are
      // no circular references of Trees
      Tree copy;
      try {
         copy = (Tree) clone();
      }
      catch (CloneNotSupportedException e) {
            // mustn't happen since this class implements Cloneable
         throw new RuntimeException(e);
      }
      if (left != null)
         copy.left = left.deepCopy(copies);
      if (right != null)
         copy.right = right.deepCopy(copies);
      // note: cannot clone the value
      return copy;
   }


   // for debugging
   public String toString()
   {
      String ret = "{";
      switch (type) {
      case ROOT:         ret += "ROOT"; break;
      case CHILD:        ret += "CHILD"; break;
      case DESC:         ret += "DESC"; break;
      case NAME_TEST:    ret += "NAME_TEST"; break;
      case TEXT_TEST:    ret += "TEXT_TEST"; break;
      case NODE_TEST:    ret += "NODE_TEST"; break;
      case COMMENT_TEST: ret += "COMMENT_TEST"; break;
      case ATTR:         ret += "ATTR"; break;
      case EQ:           ret += "EQ"; break;
      case STRING:       ret += "STRING"; break;
      case NUMBER:       ret += "NUMBER"; break;
      case WILDCARD:     ret += "*"; break;
      case DDOT:         ret += ".."; break;
      default:           ret += type; break;
      }
      ret += "," + left + "," + right + "," + value;
      if (type == NAME_TEST || type == URI_WILDCARD
                            || type == LOCAL_WILDCARD)
         ret += "(" + uri + "|" + lName + ")";
      return ret + "}";
   }
}
