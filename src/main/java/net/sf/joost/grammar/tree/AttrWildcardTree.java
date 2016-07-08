/*
 * $Id: AttrWildcardTree.java,v 1.3 2007/11/25 14:18:01 obecker Exp $
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

package net.sf.joost.grammar.tree;

import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.stx.Value;

import org.xml.sax.SAXException;

/**
 * Objects of AttrWildcardTree represent "@*" nodes in the syntax tree of a 
 * pattern or an STXPath expression.
 * @version $Revision: 1.3 $ $Date: 2007/11/25 14:18:01 $
 * @author Oliver Becker
 */
final public class AttrWildcardTree extends Tree
{
   /** 
    * Constructs an AttrWildcardTree object.
    */
   public AttrWildcardTree()
   {
      super(ATTR_WILDCARD);
   }

   public boolean matches(Context context, int top, boolean setPosition)
      throws SAXException
   {
      // an attribute requires at least two ancestors
      if (top < 3)
         return false;
      SAXEvent e = (SAXEvent)context.ancestorStack.elementAt(top-1);
      if (e.type != SAXEvent.ATTRIBUTE) 
         return false;
      if (setPosition)
         context.position = 1; // position for attributes is undefined
      return true;
   }

   public Value evaluate(Context context, int top)
      throws SAXException
   {
      // determine effective parent node sequence (-> v1)
      Value v1;
      if (left != null) { // preceding path
         v1 = left.evaluate(context, top);
         if (v1.type == Value.EMPTY)
            return v1;
      }
      else if (top > 0) // use current node
         v1 = new Value((SAXEvent)context.ancestorStack.elementAt(top-1));
      else
         return Value.VAL_EMPTY;

      // iterate through this node sequence
      Value ret = null, last = null; // for constructing the result seq
      do {
         SAXEvent e = v1.getNode();
         if (e == null) {
            context.errorHandler.error("Current item for evaluating '@*" +
                                       "' is not a node (got " + v1 + ")",
                                       context.currentInstruction.publicId,
                                       context.currentInstruction.systemId,
                                       context.currentInstruction.lineNo,
                                       context.currentInstruction.colNo);
            // if the errorHandler decides to continue ...
            return Value.VAL_EMPTY;
         }

         int len = e.attrs.getLength();
         // iterate through attribute list
         for (int i=0; i<len; i++) {
            Value v2 = new Value(SAXEvent.newAttribute(e.attrs, i));
            if (last != null)
               last.next = v2;
            else
               ret = v2;
            last = v2;
         } // for
         v1 = v1.next; // next node
      } while (v1 != null);
      
      if (ret != null)
         return ret;
      else
         return Value.VAL_EMPTY;
   } 

   public double getPriority()
   {
      return -0.5;
   }
   
   public boolean isConstant()
   {
      return false;
   }
}
