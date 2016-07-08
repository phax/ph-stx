/*
 * $Id: NoMatchFactory.java,v 1.3 2008/10/04 17:13:14 obecker Exp $
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
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Factory for <code>no-match</code> elements, which are represented by
 * the inner Instance class.
 * @version $Revision: 1.3 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */
public class NoMatchFactory extends FactoryBase
{
   /** @return <code>"no-match"</code> */
   public String getName()
   {
      return "no-match";
   }

   public NodeBase createNode(NodeBase parent, String qName,
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      if (!(parent instanceof AnalyzeTextFactory.Instance))
         throw new SAXParseException(
            "'" + qName + "' must be child of stx:analyze-text",
            context.locator);

      checkAttributes(qName, attrs, null, context);
      return new Instance(qName, parent, context);
   }



   /** Represents an instance of the <code>no-match</code> element. */
   final public class Instance extends NodeBase
   {
      /** The parent */
      AnalyzeTextFactory.Instance analyzeText;

      public Instance(String qName, NodeBase parent, ParseContext context)
      {
         super(qName, parent, context, true);
         analyzeText = (AnalyzeTextFactory.Instance)parent;
      }


      public short process(Context context)
         throws SAXException
      {
         super.process(context);
         // store value for the regex-group function
         ((Stack)context.localVars.get(AnalyzeTextFactory.REGEX_GROUP))
                                  .push(analyzeText.noMatchStr);
         // The next instruction has been set in stx:analyze-text, but
         // this stx:no-match may be interrupted by stx:process-xxx,
         // i.e. we need to store the info of a following stx:match here:
         localFieldStack.push(nodeEnd.next);
         localFieldStack.push(analyzeText.capSubstr);
         return PR_CONTINUE;
      }


      public short processEnd(Context context)
         throws SAXException
      {
         ((Stack)context.localVars.get(AnalyzeTextFactory.REGEX_GROUP)).pop();
         // restore the values for the following stx:match
         analyzeText.capSubstr = (String[])localFieldStack.pop();
         nodeEnd.next = (AbstractInstruction)localFieldStack.pop();
         return super.processEnd(context);
      }


      protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
      {
         super.onDeepCopy(copy, copies);
         Instance theCopy = (Instance) copy;
         if (analyzeText != null)
            theCopy.analyzeText =
               (AnalyzeTextFactory.Instance) analyzeText.deepCopy(copies);
      }

   }
}
