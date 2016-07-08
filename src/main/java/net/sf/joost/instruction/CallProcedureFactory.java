/*
 * $Id: CallProcedureFactory.java,v 2.10 2008/10/04 17:13:14 obecker Exp $
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

import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

import java.util.HashMap;
import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Factory for <code>call-procedure</code> elements, which are
 * represented by the inner Instance class.
 * @version $Revision: 2.10 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public class CallProcedureFactory extends FactoryBase
{
   /** allowed attributes for this element */
   private HashSet attrNames;


   //
   // Constructor
   //
   public CallProcedureFactory()
   {
      attrNames = new HashSet();
      attrNames.add("name");
      attrNames.add("group");
   }

   /** @return <code>"call-procedure"</code> */
   public String getName()
   {
      return "call-procedure";
   }

   public NodeBase createNode(NodeBase parent, String qName,
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      String nameAtt = getRequiredAttribute(qName, attrs, "name", context);
      String procName = getExpandedName(nameAtt, context);

      String groupAtt = attrs.getValue("group");

      checkAttributes(qName, attrs, attrNames, context);
      return new Instance(qName, parent, context, nameAtt, procName,
                          groupAtt);
   }


   /** The inner Instance class */
   public class Instance extends ProcessBase
   {
      String procQName, procExpName;
      ProcedureFactory.Instance procedure = null;

      // Constructor
      public Instance(String qName, NodeBase parent, ParseContext context,
                      String procQName, String procExpName,
                      String groupQName)
         throws SAXParseException
      {
         super(qName, parent, context, groupQName, null, null);
         // external filter not possible here (last two params = null)
         this.procQName = procQName;
         this.procExpName = procExpName;
      }


      /**
       * Determine statically the target procedure.
       */
      public boolean compile(int pass, ParseContext context)
         throws SAXException
      {
         if (pass == 0)
            return true; // groups not parsed completely

         // determine procedure object
         // targetGroup stems from compile() in ProcessBase
         super.compile(pass, context);
         procedure = (ProcedureFactory.Instance)
            targetGroup.visibleProcedures.get(procExpName);
         if (procedure == null) {
            // not found, search group procedures
            procedure = (ProcedureFactory.Instance)
               targetGroup.groupProcedures.get(procExpName);
         }
         if (procedure == null) {
            // still not found, search global procedures
            procedure = (ProcedureFactory.Instance)
               targetGroup.globalProcedures.get(procExpName);
         }

         if (procedure == null) {
            throw new SAXParseException(
               "Unknown procedure '" + procQName + "' called with '" +
               qName + "'",
               publicId, systemId, lineNo, colNo);
         }
         lastChild.next = procedure;

         return false; // done
      }


      /**
       * Adjust the return address of the procedure.
       */
      public short process(Context context)
         throws SAXException
      {
         super.process(context);

         localFieldStack.push(procedure.nodeEnd.next);
         procedure.nodeEnd.next = nodeEnd;
         return PR_CONTINUE;
      }

      public short processEnd(Context context)
         throws SAXException
      {
         procedure.nodeEnd.next =
            (AbstractInstruction)localFieldStack.pop();
         return super.processEnd(context);
      }

      protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
      {
         super.onDeepCopy(copy, copies);
         Instance theCopy = (Instance) copy;
         if (procedure != null)
            theCopy.procedure =
               (ProcedureFactory.Instance) procedure.deepCopy(copies);
      }
   }
}
