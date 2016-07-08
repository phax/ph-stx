/*
 * $Id: AnalyzeTextFactory.java,v 1.13 2009/08/21 12:46:17 obecker Exp $
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

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.util.regex.JRegularExpression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Factory for <code>analyze-text</code> elements, which are represented by
 * the inner Instance class.
 * @version $Revision: 1.13 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

final public class AnalyzeTextFactory extends FactoryBase
{
   /**
    * Name of a special pseudo-variable that contains the values for
    * the regex-group function. (Note: "normal" variables start always
    * with a namespace in curly braces like '<code>{uri}name</code>')
    * @see net.sf.joost.stx.function.RegexGroup
    */
   public static String REGEX_GROUP = "%REGEX-GROUP";


   /** allowed attributes for this element */
   private HashSet attrNames;

   //
   // Constructor
   //
   public AnalyzeTextFactory()
   {
      attrNames = new HashSet();
      attrNames.add("select");
   }

   /** @return <code>"analyze-text"</code> */
   public String getName()
   {
      return "analyze-text";
   }

   public NodeBase createNode(NodeBase parent, String qName,
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      Tree selectExpr = parseRequiredExpr(qName, attrs, "select", context);

      checkAttributes(qName, attrs, attrNames, context);
      return new Instance(qName, parent, context, selectExpr);
   }



   /** Represents an instance of the <code>analyze-text</code> element. */
   final public class Instance extends NodeBase
   {
      private Tree select;

      private AbstractInstruction successor;

      // this instruction manages its children itself
      private Vector mVector = new Vector();
      private MatchFactory.Instance[] matchChildren;
      private NodeBase noMatchChild;

      // Constructor
      protected Instance(String qName, NodeBase parent, ParseContext context,
                         Tree select)
      {
         super(qName, parent, context, true);
         this.select = select;
      }


      /**
       * Ensures that only <code>stx:match</code> or <code>stx:no-match</code>
       * children will be inserted.
       */
      public void insert(NodeBase node)
         throws SAXParseException
      {
         if (node instanceof MatchFactory.Instance) {
            if (noMatchChild != null) {
               // this test is not really necessary for the implementation,
               // however, it is required by the specification
               throw new SAXParseException(
                  "'" + qName +
                  "' must not have more children after stx:no-match",
                  node.publicId, node.systemId, node.lineNo, node.colNo);
            }
            mVector.add(node);
         }
         else if (node instanceof NoMatchFactory.Instance) {
            if (noMatchChild != null)
               throw new SAXParseException(
                  "'" + qName + "' must have at most one '"+ node.qName +
                  "' child",
                  node.publicId, node.systemId, node.lineNo, node.colNo);
            noMatchChild = node;
         }
         else if (node instanceof TextNode) {
            if (((TextNode)node).isWhitespaceNode())
               return; // ignore white space nodes (from xml:space="preserve")
            else
               throw new SAXParseException(
                  "'" + qName +
                  "' may only contain stx:match and stx:no-match children " +
                  "(encountered text)",
                  node.publicId, node.systemId, node.lineNo, node.colNo);
         }
         else
            throw new SAXParseException(
               "'" + qName +
               "' may only contain stx:match and stx:no-match children " +
               "(encountered '" + node.qName + "')",
               node.publicId, node.systemId, node.lineNo, node.colNo);

         // no invocation of super.insert(node) necessary
         // the children have been stored in #mVector and #noMatchChild
      }


      /**
       * Check if there is at least one <code>stx:match</code> child
       * and establish a loop
       */
      public boolean compile(int pass, ParseContext context)
         throws SAXParseException
      {
         if (pass == 0)
            return true;

         if (mVector.size() == 0)
            throw new SAXParseException(
               "'" + qName + "' must have at least one stx:match child",
               publicId, systemId, lineNo, colNo);

         // transform the Vector into an array
         matchChildren = new MatchFactory.Instance[mVector.size()];
         mVector.toArray(matchChildren);
         mVector = null; // for garbage collection

         successor = nodeEnd.next;
         nodeEnd.next = this; // loop

         return false;
      }


      // needed to detect recursive invocations
      private boolean continued = false;

      /**
       * For the regex-group function (accessed from the stx:match and
       * stx:no-match children, so they cannot be private)
       * @see net.sf.joost.stx.function.RegexGroup
       */
      protected String[] capSubstr, noMatchStr;


      /**
       * Evaluate the expression given in the <code>select</code> attribute;
       * find and process the child with the matching regular expression
       */
      public short process(Context context)
         throws SAXException
      {
         String text;
         int lastIndex;
         Matcher[] matchers;

         if (continued) {
            // restore previous values
            text = (String)localFieldStack.pop();
            lastIndex = ((Integer)localFieldStack.pop()).intValue();
            matchers = (Matcher[])localFieldStack.pop();
            continued = false; // in case there will be an stx:process-xxx
         }
         else { // this is a new invocation
            text = select.evaluate(context, this).getStringValue();
            lastIndex = 0;
            // create a pseudo variable for regex-group()
            if (context.localVars.get(REGEX_GROUP) == null)
               context.localVars.put(REGEX_GROUP, new Stack());
            matchers = new Matcher[matchChildren.length];
            for (int i=0; i<matchChildren.length; i++) {
               String re =
                  matchChildren[i].regex.evaluate(context,
                                                  matchChildren[i]).getString();

               String flags = matchChildren[i].flags != null
                  ? matchChildren[i].flags.evaluate(context,
                                                    matchChildren[i]).getString()
                  : "";
               try {
                  matchers[i] =
                     new JRegularExpression(re, true, flags).matcher(text);
               }
               catch (EvalException e) {
                  context.errorHandler.fatalError(e.getMessage(),
                                                  publicId, systemId,
                                                  lineNo, colNo, e);
                  return PR_ERROR;
               }
            }
         }

         if (text.length() != lastIndex) {
            int newIndex = text.length();
            int maxSubstringLength = 0;
            int matchIndex = -1;

            for (int i=0; i<matchers.length; i++) {
               int start = -1;
               if (matchers[i].find(lastIndex)) {
                  if (matchers[i].start() == matchers[i].end()) {
                     while (matchers[i].find()) {
                        if (matchers[i].start() != matchers[i].end()) {
                           start = matchers[i].start();
                           break;
                        }
                     }
                  }
                  else
                     start = matchers[i].start();
               }
               if (start > -1 && start <= newIndex) {
                  int length = matchers[i].end() - start;
                  if (start < newIndex || length > maxSubstringLength) {
                     newIndex = matchers[i].start();
                     maxSubstringLength = length;
                     matchIndex = i;
                  }
               }
            }

            noMatchStr = new String[1];
            if (matchIndex != -1) { // found an stx:match
               capSubstr = new String[matchers[matchIndex].groupCount() + 1];
               for (int i=0; i<capSubstr.length; i++)
                  capSubstr[i] = matchers[matchIndex].group(i);
               noMatchStr[0] = text.substring(lastIndex, newIndex);
               localFieldStack.push(matchers);
               localFieldStack.push(new Integer(newIndex + maxSubstringLength));
               localFieldStack.push(text);
               if (noMatchChild != null && newIndex != lastIndex) {
                  // invoke stx:no-match before stx:match
                  next = noMatchChild;
                  noMatchChild.nodeEnd.next = matchChildren[matchIndex];
               }
               else
                  next = matchChildren[matchIndex];
            }
            else { // no matching regex found
               if (noMatchChild != null) {
                  noMatchStr[0] = text.substring(lastIndex);
                  next = noMatchChild;
                  // leave stx:analyze-text after stx:no-match
                  noMatchChild.nodeEnd.next = successor;
               }
               else
                  next = successor; // leave stx:analyze-text instantly
            }
         }
         else // text.length() == lastIndex, we're done
            next = successor;

         return PR_CONTINUE;
      }


      public short processEnd(Context context)
         throws SAXException
      {
         continued = true;
         return PR_CONTINUE;
      }


      protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
      {
         super.onDeepCopy(copy, copies);
         Instance theCopy = (Instance) copy;
         theCopy.continued = false;
         theCopy.capSubstr = theCopy.noMatchStr = null;
         if (matchChildren != null) {
            theCopy.matchChildren =
               new MatchFactory.Instance[matchChildren.length];
            for (int i=0; i<matchChildren.length; i++) {
               theCopy.matchChildren[i] =
                  (MatchFactory.Instance) matchChildren[i].deepCopy(copies);
            }
         }
         if (noMatchChild != null)
            theCopy.noMatchChild = (NodeBase) noMatchChild.deepCopy(copies);
         if (successor != null)
            theCopy.successor = successor.deepCopy(copies);
         if (select != null)
            theCopy.select = select.deepCopy(copies);
      }

   }
}
