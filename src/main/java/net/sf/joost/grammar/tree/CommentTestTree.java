/*
 * $Id: CommentTestTree.java,v 1.2 2007/05/20 18:00:43 obecker Exp $
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

import org.xml.sax.SAXException;

/**
 * Objects of CommentTestTree represent comment test nodes ("comment()") in 
 * the syntax tree of a pattern or an STXPath expression.
 * @version $Revision: 1.2 $ $Date: 2007/05/20 18:00:43 $
 * @author Oliver Becker
 */
final public class CommentTestTree extends Tree
{
   public CommentTestTree()
   {
      super(COMMENT_TEST);
   }
   
   public boolean matches(Context context, int top, boolean setPosition)
      throws SAXException
   {
      if (top < 2)
         return false;
      if (((SAXEvent)context.ancestorStack.elementAt(top-1)).type == 
                     SAXEvent.COMMENT) {
         if (setPosition)
            context.position =
               ((SAXEvent)context.ancestorStack.elementAt(top-2))
                                 .getPositionOfComment();
         return true;
      }
      return false;
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
