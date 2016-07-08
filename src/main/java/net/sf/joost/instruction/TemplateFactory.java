/*
 * $Id: TemplateFactory.java,v 2.11 2008/10/04 17:13:14 obecker Exp $
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

import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

import java.util.HashMap;
import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Factory for <code>template</code> elements, which are represented by
 * the inner Instance class.
 * @version $Revision: 2.11 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class TemplateFactory extends FactoryBase
{
   /** allowed attributes for this element. */
   private HashSet attrNames;


   // Constructor
   public TemplateFactory()
   {
      attrNames = new HashSet();
      attrNames.add("match");
      attrNames.add("priority");
      attrNames.add("visibility");
      attrNames.add("public");
      attrNames.add("new-scope");
   }

   /** @return <code>"template"</code> */
   public String getName()
   {
      return "template";
   }

   public NodeBase createNode(NodeBase parent, String qName,
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      if (parent == null || !(parent instanceof GroupBase))
         throw new SAXParseException("'" + qName + "' must be a top level " +
                                     "element or a child of stx:group",
                                     context.locator);

      Tree matchPattern = parseRequiredPattern(qName, attrs, "match", context);

      String priorityAtt = attrs.getValue("priority");
      double priority;
      if (priorityAtt != null) {
         try {
            priority = Double.parseDouble(priorityAtt);
         }
         catch (NumberFormatException ex) {
            throw new SAXParseException("The priority value '" +
                                        priorityAtt + "' is not a number",
                                        context.locator);
         }
      }
      else {
         priority = matchPattern.getPriority();
      }

      int visibility = getEnumAttValue("visibility", attrs,
                                       TemplateBase.VISIBILITY_VALUES,
                                       context);
      if (visibility == -1)
         visibility =  TemplateBase.LOCAL_VISIBLE; // default value

      int publicAttVal =
         getEnumAttValue("public", attrs, YESNO_VALUES, context);
      // default value depends on the parent:
      // "yes" (true) for top-level templates,
      // "no" (false) for others
      boolean isPublic = parent instanceof TransformFactory.Instance
         ? (publicAttVal != NO_VALUE)   // default is true
         : (publicAttVal == YES_VALUE); // default is false

      // default is "no" (false)
      boolean newScope =
         getEnumAttValue("new-scope", attrs, YESNO_VALUES, context)
         == YES_VALUE;

      checkAttributes(qName, attrs, attrNames, context);

      return new Instance(qName, parent, context,
                          matchPattern, priority, visibility, isPublic,
                          newScope);
   }


   // -----------------------------------------------------------------------


   /** The inner Instance class */
   public final class Instance extends TemplateBase implements Comparable
   {
      /** The match pattern */
      private Tree match;

      /** The priority of this template */
      private double priority;


      //
      // Constructor
      //
      protected Instance(String qName, NodeBase parent, ParseContext context,
                         Tree match, double priority, int visibility,
                         boolean isPublic, boolean newScope)
      {
         super(qName, parent, context, visibility, isPublic, newScope);
         this.match = match;
         this.priority = priority;
      }


      /**
       * @param context the Context object
       * @param setPosition <code>true</code> if the context position
       *        ({@link Context#position}) should be set in case the
       *        event stack matches the pattern in {@link #match}.
       * @return true if the current event stack matches the pattern of
       *         this template
       * @exception SAXParseException if an error occured while evaluating
       * the match expression
       */
      public boolean matches(Context context, boolean setPosition)
         throws SAXException
      {
         context.currentInstruction = this;
         context.currentGroup = parentGroup;
         return match.matches(context, context.ancestorStack.size(),
                              setPosition);
      }


      /**
       * Splits a match pattern that is a union into several template
       * instances. The match pattern of the object itself loses one
       * union.
       * @return a template Instance object without a union in its
       *         match pattern or <code>null</code>
       */
      public Instance split()
         throws SAXException
      {
         if (match.type != Tree.UNION)
            return null;

         Instance copy = null;
         try {
            copy = (Instance)clone();
         }
         catch (CloneNotSupportedException e) {
            throw new SAXException("Can't split " + this, e);
         }
         copy.match = match.right; // non-union
         if (Double.isNaN(copy.priority)) // no priority specified
            copy.priority = copy.match.getPriority();
         match = match.left;       // may contain another union
         if (Double.isNaN(priority)) // no priority specified
            priority = match.getPriority();
         return copy;
      }


      /**
       * @return the priority of this template
       */
      public double getPriority()
      {
         return priority;
      }

      /**
       * @return the match pattern
       */
      public Tree getMatchPattern()
      {
         return match;
      }

      /**
       * Compares two templates according to their inverse priorities.
       * This results in a descending natural order with
       * java.util.Arrays.sort()
       */
      public int compareTo(Object o)
      {
         double p = ((Instance)o).priority;
         return (p < priority) ? -1 : ((p > priority) ? 1 : 0);
      }


      protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
      {
         super.onDeepCopy(copy, copies);
         Instance theCopy = (Instance) copy;
         if (match != null)
            theCopy.match = match.deepCopy(copies);
      }



      // for debugging
      public String toString()
      {
         return "template:" + lineNo + " " + match + " " + priority;
      }
   }
}
