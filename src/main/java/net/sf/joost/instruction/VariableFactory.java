/*
 * $Id: VariableFactory.java,v 2.8 2008/10/04 17:13:14 obecker Exp $
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

package net.sf.joost.instruction;

import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Factory for <code>variable</code> elements, which are represented by
 * the inner Instance class.
 * @version $Revision: 2.8 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

final public class VariableFactory extends FactoryBase
{
   /** allowed attributes for this element */
   private HashSet attrNames;

   // Constructor
   public VariableFactory()
   {
      attrNames = new HashSet();
      attrNames.add("name");
      attrNames.add("select");
      attrNames.add("keep-value");
   }

   /** @return <code>"variable"</code> */
   public String getName()
   {
      return "variable";
   }

   public NodeBase createNode(NodeBase parent, String qName,
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      String nameAtt = getRequiredAttribute(qName, attrs, "name", context);
      String varName = getExpandedName(nameAtt, context);

      Tree selectExpr = parseExpr(attrs.getValue("select"), context);

      int keepValueIndex = getEnumAttValue("keep-value", attrs, YESNO_VALUES,
                                           context);
      if (keepValueIndex != -1 && !(parent instanceof GroupBase))
         throw new SAXParseException(
            "Attribute 'keep-value' is not allowed for local variables",
            context.locator);

      // default is "no" (false)
      boolean keepValue = (keepValueIndex == YES_VALUE);

      checkAttributes(qName, attrs, attrNames, context);
      return new Instance(qName, context, nameAtt, varName, selectExpr,
                          keepValue, parent);
   }


   /** Represents an instance of the <code>variable</code> element. */
   public class Instance extends VariableBase
   {
      private String varName;
      private Tree select;
      private String errorMessage;
      private final boolean isGroupVar;


      protected Instance(String qName, ParseContext context, String varName,
                         String expName, Tree select, boolean keepValue,
                         NodeBase parent)
      {
         super(qName, parent, context, expName, keepValue,
               // this element must be empty if there is a select attribute
               select == null);
         this.varName = varName;
         this.select = select;
         this.keepValue = keepValue;
         this.errorMessage =
            "('" + qName + "' started in line " + lineNo + ")";
         this.isGroupVar = parent instanceof GroupBase;
      }


      public short process(Context context)
         throws SAXException
      {
         // does this variable have a select attribute?
         if (select != null) {
            // select attribute present
            Value v = select.evaluate(context, this);
            processVar(v, context);
         }
         else {
            // endInstruction present
            super.process(context);
            // create a new StringEmitter for this instance and put it
            // on the emitter stack
            context.pushEmitter(
               new StringEmitter(new StringBuffer(), errorMessage));
         }
         return PR_CONTINUE;
      }


      public short processEnd(Context context)
         throws SAXException
      {
         Value v = new Value(((StringEmitter)context.popEmitter())
                                                    .getBuffer().toString());

         processVar(v, context);

         return super.processEnd(context);
      }


      /** Declares a variable */
      private void processVar(Value v, Context context)
         throws SAXException
      {
         // determine scope
         Hashtable varTable;
         if (isGroupVar)
            varTable = (Hashtable)((Stack)context.groupVars.get(parent))
                                          .peek();
         else {
            varTable = context.localVars;
            parent.declareVariable(expName);
         }

         if (varTable.get(expName) != null) {
            context.errorHandler.error(
               "Variable '" + varName + "' already declared",
               publicId, systemId, lineNo, colNo);
            return; // if the errorHandler returns
         }
         varTable.put(expName, v);
      }


      protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
      {
         super.onDeepCopy(copy, copies);
         Instance theCopy = (Instance) copy;
         if (select != null)
            theCopy.select = select.deepCopy(copies);
      }
   }
}
