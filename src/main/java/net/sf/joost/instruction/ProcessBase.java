/*
 * $Id: ProcessBase.java,v 2.20 2009/09/22 21:13:44 obecker Exp $
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

import net.sf.joost.emitter.EmitterAdapter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.BufferReader;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.util.VariableNotFoundException;
import net.sf.joost.util.VariableUtils;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Common base class for all <code>stx:process-<em>xxx</em></code>
 * instructions
 * @version $Revision: 2.20 $ $Date: 2009/09/22 21:13:44 $
 * @author Oliver Becker
 */
public class ProcessBase extends NodeBase
{
   /** Instruction the clears the parameter stack */
   private class ProcessEnd extends AbstractInstruction {
      private ProcessBase node;

      public ProcessEnd(ProcessBase node)
      {
         this.node = node;
      }

      public NodeBase getNode()
      {
         return node;
      }

      public short process(Context ctx)
      {
         ctx.passedParameters = (Hashtable)node.paramStack.pop();
         return PR_CONTINUE;
      }

      protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
      {
         super.onDeepCopy(copy, copies);
         ProcessEnd theCopy = (ProcessEnd) copy;
         theCopy.node = (ProcessBase) node.deepCopy(copies);
      }
   }



   // stack for parameters, used in the subclasses
   private Stack paramStack = new Stack();

   protected Vector children = new Vector();

   // names of the "group" attribute (if present)
   private String groupQName, groupExpName;

   // target group for the next processing
   protected GroupBase targetGroup = null;

   // filter and src values
   protected String useBufQName, useBufExpName;
   protected Tree filter;
   private Tree hrefTree;
   private boolean bufScopeDetermined = false;
   private GroupBase bufGroupScope = null;

   // Constructor
   public ProcessBase(String qName, NodeBase parent,
                      ParseContext context,
                      String groupQName,
                      String method, String src)
      throws SAXParseException
   {
      super(qName, parent, context, true);

      // insert instruction that clears the parameter stack when
      // continuing the processing
      next.next = new ProcessEnd(this);

      this.groupQName = groupQName;
      if (groupQName != null)
         this.groupExpName = FactoryBase.getExpandedName(groupQName, context);

      // Evaluate filter-method and filter-src attributes
      if (method != null)
         filter = FactoryBase.parseAVT(method, context);
      if (src != null) {
         src = src.trim();
         if (!src.endsWith(")"))
            throw new SAXParseException(
               "Invalid filter-src value '" + src +
               "'. Expect url(...) or buffer(...) specification.",
               context.locator);
         if (src.startsWith("url(")) {
            // part between "url(" and ")" will be evaluated as an expression
            hrefTree =
               FactoryBase.parseExpr(src.substring(4, src.length()-1).trim(),
                                     context);
         }
         else if (src.startsWith("buffer(")) {
            useBufQName = src.substring(7, src.length()-1).trim();
            useBufExpName = "@" +
                            FactoryBase.getExpandedName(useBufQName, context);
         }
         else
            throw new SAXParseException(
               "Invalid filter-src value '" + src +
               "'. Expect url(...) or buffer(...) specification.",
               context.locator);
      }

      if (this instanceof PDocumentFactory.Instance ||
          this instanceof PBufferFactory.Instance)
         return;

      // prohibit this instruction inside of group variables
      // and stx:with-param instructions
      NodeBase ancestor = parent;
      while (ancestor != null &&
             !(ancestor instanceof TemplateBase) &&
             !(ancestor instanceof WithParamFactory.Instance))
         ancestor = ancestor.parent;
      if (ancestor == null)
         throw new SAXParseException(
            "'" + qName + "' must be a descendant of stx:template or " +
            "stx:procedure",
            context.locator);
      if (ancestor instanceof WithParamFactory.Instance)
         throw new SAXParseException(
            "'" + qName + "' must not be a descendant of '" +
            ancestor.qName + "'",
            context.locator);
   }


   /**
    * Ensure that only stx:with-param children will be inserted
    */
   public void insert(NodeBase node)
      throws SAXParseException
   {
      if (node instanceof TextNode) {
         if (((TextNode)node).isWhitespaceNode())
            return;
         else
            throw new SAXParseException(
               "'" + qName + "' must have only stx:with-param children " +
               "(encountered text)",
               node.publicId, node.systemId, node.lineNo, node.colNo);
      }

      if (!(node instanceof WithParamFactory.Instance))
         throw new SAXParseException(
            "'" + qName + "' must have only stx:with-param children " +
            "(encountered '" + node.qName + "')",
            node.publicId, node.systemId, node.lineNo, node.colNo);

      children.addElement(node);
      super.insert(node);
   }


   /**
    * Determine target group
    */
   public boolean compile(int pass, ParseContext context)
      throws SAXException
   {
      if (pass == 0)
         return true; // groups not parsed completely yet

      // determine parent group
      // parent is at most a TemplateBase; start with grand-parent
      NodeBase tmp = parent.parent;
      while (!(tmp instanceof GroupBase))
         tmp = tmp.parent;
      GroupBase parentGroup = (GroupBase)tmp;

      // Evaluate group attribute
      if (groupExpName != null) {
         targetGroup = (GroupBase)parentGroup.namedGroups.get(groupExpName);
         if (targetGroup == null)
            throw new SAXParseException(
               "Unknown target group '" + groupQName +
               "' specified for '" + qName + "'",
               publicId, systemId, lineNo, colNo);
      }
      if (targetGroup == null) { // means: still null
         // use current group
         targetGroup = parentGroup;
      }
      return false; // done
   }


   /**
    * assign target group,  save and reset parameters
    */
   public short process(Context context)
      throws SAXException
   {
      context.targetGroup = targetGroup;

      paramStack.push(context.passedParameters);
      context.passedParameters = new Hashtable();
      return PR_CONTINUE;
   }


   /**
    * Returns a handler that performs a transformation according to the
    * specified {@link #filter} value.
    * @exception SAXException if this handler couldn't be created
    */
   protected TransformerHandler getProcessHandler(Context context)
      throws SAXException
   {
      String filterMethod = filter.evaluate(context, this).getString();

      TransformerHandler handler;
      try {
         if (useBufExpName != null) {
            if (!bufScopeDetermined) {
               bufGroupScope =
                  VariableUtils.findVariableScope(context, useBufExpName);
               bufScopeDetermined = true;
            }
            handler =
               context.defaultTransformerHandlerResolver.resolve(
                     filterMethod,
                     new BufferReader(context, useBufExpName, bufGroupScope,
                                      publicId, systemId),
                     context.uriResolver,
                     context.errorHandler.errorListener,
                     context.passedParameters);
         }
         else {
            String href = (hrefTree != null)
               ? hrefTree.evaluate(context, this).getStringValue()
               : null;
            handler =
               context.defaultTransformerHandlerResolver
                      .resolve(filterMethod, href, systemId,
                               context.uriResolver,
                               context.errorHandler.errorListener,
                               context.passedParameters);
         }
         if (handler == null) {
            context.errorHandler.fatalError(
               "Filter '" + filterMethod + "' not available",
               publicId, systemId, lineNo, colNo);
            return null;
         }
      }
      catch (SAXParseException e) {
         // propagate
         throw e;
      }
      catch (SAXException e) {
         // add locator information
         context.errorHandler.fatalError(e.getMessage(),
                                         publicId, systemId, lineNo, colNo,
                                         e);
         return null;
      }
      catch (VariableNotFoundException e) {
         context.errorHandler.error(
               "Can't process an undeclared buffer '" + useBufQName + "'",
               publicId, systemId, lineNo, colNo);
            // if the error handler returns
         return null;
      }

      EmitterAdapter adapter = new EmitterAdapter(context.emitter, this);
      handler.setResult(new SAXResult(adapter));
      return handler;
   }


   protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
   {
      super.onDeepCopy(copy, copies);
      ProcessBase theCopy = (ProcessBase) copy;
      theCopy.paramStack = new Stack();
      if (bufGroupScope != null)
         theCopy.bufGroupScope = (GroupBase) bufGroupScope.deepCopy(copies);
      if (targetGroup != null)
         theCopy.targetGroup = (GroupBase) targetGroup.deepCopy(copies);
      theCopy.children = new Vector();
      for (int i=0; i<children.size(); i++) {
         theCopy.children.add(
               ((AbstractInstruction)children.get(i)).deepCopy(copies));
      }
      if (filter != null)
         theCopy.filter = filter.deepCopy(copies);
      if (hrefTree != null)
         theCopy.hrefTree = hrefTree.deepCopy(copies);
   }


}
