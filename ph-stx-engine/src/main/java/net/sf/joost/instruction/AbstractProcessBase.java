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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.EmitterAdapter;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.BufferReader;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.util.VariableNotFoundException;
import net.sf.joost.util.VariableUtils;

/**
 * Common base class for all <code>stx:process-<em>xxx</em></code> instructions
 *
 * @version $Revision: 2.20 $ $Date: 2009/09/22 21:13:44 $
 * @author Oliver Becker
 */
public abstract class AbstractProcessBase extends AbstractNodeBase
{
  /** Instruction the clears the parameter stack */
  private class ProcessEnd extends AbstractInstruction
  {
    private AbstractProcessBase node;

    public ProcessEnd (final AbstractProcessBase node)
    {
      this.node = node;
    }

    @Override
    public AbstractNodeBase getNode ()
    {
      return node;
    }

    @Override
    public short process (final Context ctx)
    {
      ctx.passedParameters = (Hashtable) node.paramStack.pop ();
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final ProcessEnd theCopy = (ProcessEnd) copy;
      theCopy.node = (AbstractProcessBase) node.deepCopy (copies);
    }
  }

  // stack for parameters, used in the subclasses
  private Stack paramStack = new Stack ();

  protected Vector children = new Vector ();

  // names of the "group" attribute (if present)
  private String groupQName, groupExpName;

  // target group for the next processing
  protected AbstractGroupBase targetGroup = null;

  // filter and src values
  protected String useBufQName, useBufExpName;
  protected AbstractTree filter;
  private AbstractTree hrefTree;
  private boolean bufScopeDetermined = false;
  private AbstractGroupBase bufGroupScope = null;

  // Constructor
  public AbstractProcessBase (final String qName,
                      final AbstractNodeBase parent,
                      final ParseContext context,
                      final String groupQName,
                      final String method,
                      String src) throws SAXParseException
  {
    super (qName, parent, context, true);

    // insert instruction that clears the parameter stack when
    // continuing the processing
    next.next = new ProcessEnd (this);

    this.groupQName = groupQName;
    if (groupQName != null)
      this.groupExpName = AbstractFactoryBase.getExpandedName (groupQName, context);

    // Evaluate filter-method and filter-src attributes
    if (method != null)
      filter = AbstractFactoryBase.parseAVT (method, context);
    if (src != null)
    {
      src = src.trim ();
      if (!src.endsWith (")"))
        throw new SAXParseException ("Invalid filter-src value '" +
                                     src +
                                     "'. Expect url(...) or buffer(...) specification.",
                                     context.locator);
      if (src.startsWith ("url("))
      {
        // part between "url(" and ")" will be evaluated as an expression
        hrefTree = AbstractFactoryBase.parseExpr (src.substring (4, src.length () - 1).trim (), context);
      }
      else
        if (src.startsWith ("buffer("))
        {
          useBufQName = src.substring (7, src.length () - 1).trim ();
          useBufExpName = "@" + AbstractFactoryBase.getExpandedName (useBufQName, context);
        }
        else
          throw new SAXParseException ("Invalid filter-src value '" +
                                       src +
                                       "'. Expect url(...) or buffer(...) specification.",
                                       context.locator);
    }

    if (this instanceof PDocumentFactory.Instance || this instanceof PBufferFactory.Instance)
      return;

    // prohibit this instruction inside of group variables
    // and stx:with-param instructions
    AbstractNodeBase ancestor = parent;
    while (ancestor != null &&
           !(ancestor instanceof AbstractTemplateBase) &&
           !(ancestor instanceof WithParamFactory.Instance))
      ancestor = ancestor.parent;
    if (ancestor == null)
      throw new SAXParseException ("'" +
                                   qName +
                                   "' must be a descendant of stx:template or " +
                                   "stx:procedure",
                                   context.locator);
    if (ancestor instanceof WithParamFactory.Instance)
      throw new SAXParseException ("'" +
                                   qName +
                                   "' must not be a descendant of '" +
                                   ancestor.qName +
                                   "'",
                                   context.locator);
  }

  /**
   * Ensure that only stx:with-param children will be inserted
   */
  @Override
  public void insert (final AbstractNodeBase node) throws SAXParseException
  {
    if (node instanceof TextNode)
    {
      if (((TextNode) node).isWhitespaceNode ())
        return;
      else
        throw new SAXParseException ("'" +
                                     qName +
                                     "' must have only stx:with-param children " +
                                     "(encountered text)",
                                     node.publicId,
                                     node.systemId,
                                     node.lineNo,
                                     node.colNo);
    }

    if (!(node instanceof WithParamFactory.Instance))
      throw new SAXParseException ("'" +
                                   qName +
                                   "' must have only stx:with-param children " +
                                   "(encountered '" +
                                   node.qName +
                                   "')",
                                   node.publicId,
                                   node.systemId,
                                   node.lineNo,
                                   node.colNo);

    children.addElement (node);
    super.insert (node);
  }

  /**
   * Determine target group
   */
  @Override
  public boolean compile (final int pass, final ParseContext context) throws SAXException
  {
    if (pass == 0)
      return true; // groups not parsed completely yet

    // determine parent group
    // parent is at most a TemplateBase; start with grand-parent
    AbstractNodeBase tmp = parent.parent;
    while (!(tmp instanceof AbstractGroupBase))
      tmp = tmp.parent;
    final AbstractGroupBase parentGroup = (AbstractGroupBase) tmp;

    // Evaluate group attribute
    if (groupExpName != null)
    {
      targetGroup = (AbstractGroupBase) parentGroup.namedGroups.get (groupExpName);
      if (targetGroup == null)
        throw new SAXParseException ("Unknown target group '" +
                                     groupQName +
                                     "' specified for '" +
                                     qName +
                                     "'",
                                     publicId,
                                     systemId,
                                     lineNo,
                                     colNo);
    }
    if (targetGroup == null)
    { // means: still null
      // use current group
      targetGroup = parentGroup;
    }
    return false; // done
  }

  /**
   * assign target group, save and reset parameters
   */
  @Override
  public short process (final Context context) throws SAXException
  {
    context.targetGroup = targetGroup;

    paramStack.push (context.passedParameters);
    context.passedParameters = new Hashtable ();
    return CSTX.PR_CONTINUE;
  }

  /**
   * Returns a handler that performs a transformation according to the specified
   * {@link #filter} value.
   *
   * @exception SAXException
   *            if this handler couldn't be created
   */
  protected TransformerHandler getProcessHandler (final Context context) throws SAXException
  {
    final String filterMethod = filter.evaluate (context, this).getString ();

    TransformerHandler handler;
    try
    {
      if (useBufExpName != null)
      {
        if (!bufScopeDetermined)
        {
          bufGroupScope = VariableUtils.findVariableScope (context, useBufExpName);
          bufScopeDetermined = true;
        }
        handler = context.defaultTransformerHandlerResolver.resolve (filterMethod,
                                                                     new BufferReader (context,
                                                                                       useBufExpName,
                                                                                       bufGroupScope,
                                                                                       publicId,
                                                                                       systemId),
                                                                     context.uriResolver,
                                                                     context.errorHandler.errorListener,
                                                                     context.passedParameters);
      }
      else
      {
        final String href = (hrefTree != null) ? hrefTree.evaluate (context, this).getStringValue () : null;
        handler = context.defaultTransformerHandlerResolver.resolve (filterMethod,
                                                                     href,
                                                                     systemId,
                                                                     context.uriResolver,
                                                                     context.errorHandler.errorListener,
                                                                     context.passedParameters);
      }
      if (handler == null)
      {
        context.errorHandler.fatalError ("Filter '" +
                                         filterMethod +
                                         "' not available",
                                         publicId,
                                         systemId,
                                         lineNo,
                                         colNo);
        return null;
      }
    }
    catch (final SAXParseException e)
    {
      // propagate
      throw e;
    }
    catch (final SAXException e)
    {
      // add locator information
      context.errorHandler.fatalError (e.getMessage (), publicId, systemId, lineNo, colNo, e);
      return null;
    }
    catch (final VariableNotFoundException e)
    {
      context.errorHandler.error ("Can't process an undeclared buffer '" +
                                  useBufQName +
                                  "'",
                                  publicId,
                                  systemId,
                                  lineNo,
                                  colNo);
      // if the error handler returns
      return null;
    }

    final EmitterAdapter adapter = new EmitterAdapter (context.emitter, this);
    handler.setResult (new SAXResult (adapter));
    return handler;
  }

  @Override
  protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
  {
    super.onDeepCopy (copy, copies);
    final AbstractProcessBase theCopy = (AbstractProcessBase) copy;
    theCopy.paramStack = new Stack ();
    if (bufGroupScope != null)
      theCopy.bufGroupScope = (AbstractGroupBase) bufGroupScope.deepCopy (copies);
    if (targetGroup != null)
      theCopy.targetGroup = (AbstractGroupBase) targetGroup.deepCopy (copies);
    theCopy.children = new Vector ();
    for (int i = 0; i < children.size (); i++)
    {
      theCopy.children.add (((AbstractInstruction) children.get (i)).deepCopy (copies));
    }
    if (filter != null)
      theCopy.filter = filter.deepCopy (copies);
    if (hrefTree != null)
      theCopy.hrefTree = hrefTree.deepCopy (copies);
  }

}
