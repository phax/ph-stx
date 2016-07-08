/*
 * $Id: CopyFactory.java,v 2.13 2008/10/04 17:13:14 obecker Exp $
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

import net.sf.joost.OptionalLog;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.SAXEvent;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Factory for <code>copy</code> elements, which are represented by
 * the inner Instance class.
 * @version $Revision: 2.13 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

final public class CopyFactory extends FactoryBase
{
   /** allowed attributes for this element. */
   private HashSet attrNames;

   /** empty attribute list (needed as parameter for startElement) */
   private static Attributes emptyAttList = new AttributesImpl();

   // Log initialization
   private static Log log = OptionalLog.getLog(CopyFactory.class);


   // Constructor
   public CopyFactory()
   {
      attrNames = new HashSet();
      attrNames.add("attributes");
   }


   /** @return <code>"copy"</code> */
   public String getName()
   {
      return "copy";
   }

   public NodeBase createNode(NodeBase parent, String qName,
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      Tree attributesPattern = parsePattern(attrs.getValue("attributes"),
                                            context);

      checkAttributes(qName, attrs, attrNames, context);
      return new Instance(qName, parent, context, attributesPattern);
   }


   /** Represents an instance of the <code>copy</code> element. */
   final public class Instance extends NodeBase
   {
      /** the pattern in the <code>attributes</code> attribute,
          <code>null</code> if this attribute is missing */
      private Tree attPattern;

      /** <code>true</code> if {@link #attPattern} is a wildcard
          (<code>@*</code>) */
      private boolean attrWildcard = false;

      /** instruction pointers */
      private AbstractInstruction contents, successor;

      //
      // Constructor
      //

      public Instance(String qName, NodeBase parent, ParseContext context,
                      Tree attPattern)
      {
         super(qName, parent, context, true);
         this.attPattern = attPattern;
         if (attPattern != null && attPattern.type == Tree.ATTR_WILDCARD)
            attrWildcard = true;
      }


      /** Store pointers to the contents and the successor */
      public boolean compile(int pass, ParseContext context)
      {
         if (pass == 0)
            return true; // successor not available yet

         contents = next;
         successor = nodeEnd.next;
         return false;
      }


      /**
       * Copy the begin of the current node to the result stream.
       */
      public short process(Context context)
         throws SAXException
      {
         SAXEvent event = (SAXEvent)context.ancestorStack.peek();
         switch(event.type) {
         case SAXEvent.ROOT:
            super.process(context);
            next = contents;
            break;
         case SAXEvent.ELEMENT: {
            super.process(context);
            Attributes attList = attrWildcard ? event.attrs : emptyAttList;
            context.emitter.startElement(event.uri, event.lName, event.qName,
                                         attList, event.namespaces, this);
            if (attPattern != null && !attrWildcard) {
               // attribute pattern present, but no wildcard (@*)
               int attrNum = event.attrs.getLength();
               for (int i=0; i<attrNum; i++) {
                  // put attributes on the event stack for matching
                  context.ancestorStack.push(
                     SAXEvent.newAttribute(event.attrs, i));
                  if (attPattern.matches(context,
                                         context.ancestorStack.size(),
                                         false)) {
                     SAXEvent attrEvent =
                        (SAXEvent)context.ancestorStack.peek();
                     context.emitter.addAttribute(
                        attrEvent.uri, attrEvent.qName, attrEvent.lName,
                        attrEvent.value, this);
                  }
                  // remove attribute
                  context.ancestorStack.pop();
               }
            }
            next = contents;
            break;
         }
         case SAXEvent.TEXT:
            context.emitter.characters(event.value.toCharArray(),
                                       0, event.value.length(), this);
            next = successor;
            break;
         case SAXEvent.CDATA:
            context.emitter.startCDATA(this);
            context.emitter.characters(event.value.toCharArray(),
                                       0, event.value.length(), this);
            context.emitter.endCDATA();
            next = successor;
            break;
         case SAXEvent.PI:
            context.emitter.processingInstruction(event.qName, event.value,
                                                  this);
            next = successor;
            break;
         case SAXEvent.COMMENT:
            context.emitter.comment(event.value.toCharArray(),
                                    0, event.value.length(), this);
            next = successor;
            break;
         case SAXEvent.ATTRIBUTE:
            context.emitter.addAttribute(event.uri, event.qName, event.lName,
                                         event.value, this);
            next = successor;
            break;
         default:
            if (log != null)
               log.error("Unknown SAXEvent type " + event.type);
            throw new SAXParseException("Unknown SAXEvent type",
                                        publicId, systemId, lineNo, colNo);
         }
         return PR_CONTINUE;
      }


      /**
       * Copy the end, if the current node is an element.
       */
      public short processEnd(Context context)
         throws SAXException
      {
         SAXEvent event = (SAXEvent)context.ancestorStack.peek();
         if (event.type == SAXEvent.ELEMENT)
            context.emitter.endElement(event.uri, event.lName, event.qName,
                                       this);
         return super.processEnd(context);
      }


      protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
      {
         super.onDeepCopy(copy, copies);
         Instance theCopy = (Instance) copy;
         if (contents != null)
            theCopy.contents = contents.deepCopy(copies);
         if (successor != null)
            theCopy.successor = successor.deepCopy(copies);
         if (attPattern != null)
            theCopy.attPattern = attPattern.deepCopy(copies);
      }
   }
}
