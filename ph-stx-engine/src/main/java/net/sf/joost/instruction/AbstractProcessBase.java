/**
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.1 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is: this file
 *
 *  The Initial Developer of the Original Code is Oliver Becker.
 *
 *  Portions created by Philip Helger
 *  are Copyright (C) 2016-2017 Philip Helger
 *  All Rights Reserved.
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
import net.sf.joost.stx.Value;
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
    private AbstractProcessBase m_aNode;

    public ProcessEnd (final AbstractProcessBase node)
    {
      this.m_aNode = node;
    }

    @Override
    public AbstractNodeBase getNode ()
    {
      return m_aNode;
    }

    @Override
    public short process (final Context ctx)
    {
      ctx.m_aPassedParameters = m_aNode.m_aParamStack.pop ();
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final ProcessEnd theCopy = (ProcessEnd) copy;
      theCopy.m_aNode = (AbstractProcessBase) m_aNode.deepCopy (copies);
    }
  }

  // stack for parameters, used in the subclasses
  private Stack <Hashtable <String, Value>> m_aParamStack = new Stack<> ();

  protected Vector <AbstractInstruction> m_aChildren = new Vector<> ();

  // names of the "group" attribute (if present)
  private String m_sGroupQName, m_sGroupExpName;

  // target group for the next processing
  protected AbstractGroupBase m_aTargetGroup;

  // filter and src values
  private String m_sUseBufQName;
  private String m_sUseBufExpName;
  private AbstractTree m_aFilter;
  private AbstractTree m_aHrefTree;
  private boolean m_bBufScopeDetermined = false;
  private AbstractGroupBase m_aBufGroupScope;

  // Constructor
  public AbstractProcessBase (final String qName,
                              final AbstractNodeBase parent,
                              final ParseContext context,
                              final String groupQName,
                              final String method,
                              final String sSrc) throws SAXParseException
  {
    super (qName, parent, context, true);

    // insert instruction that clears the parameter stack when
    // continuing the processing
    next.next = new ProcessEnd (this);

    this.m_sGroupQName = groupQName;
    if (groupQName != null)
      this.m_sGroupExpName = AbstractFactoryBase.getExpandedName (groupQName, context);

    // Evaluate filter-method and filter-src attributes
    if (method != null)
      m_aFilter = AbstractFactoryBase.parseAVT (method, context);
    String src = sSrc;
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
        m_aHrefTree = AbstractFactoryBase.parseExpr (src.substring (4, src.length () - 1).trim (), context);
      }
      else
        if (src.startsWith ("buffer("))
        {
          m_sUseBufQName = src.substring (7, src.length () - 1).trim ();
          m_sUseBufExpName = "@" + AbstractFactoryBase.getExpandedName (m_sUseBufQName, context);
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
      ancestor = ancestor.m_aParent;
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
                                   ancestor.m_sQName +
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
      throw new SAXParseException ("'" +
                                   m_sQName +
                                   "' must have only stx:with-param children " +
                                   "(encountered text)",
                                   node.m_sPublicID,
                                   node.m_sSystemID,
                                   node.lineNo,
                                   node.colNo);
    }

    if (!(node instanceof WithParamFactory.Instance))
      throw new SAXParseException ("'" +
                                   m_sQName +
                                   "' must have only stx:with-param children " +
                                   "(encountered '" +
                                   node.m_sQName +
                                   "')",
                                   node.m_sPublicID,
                                   node.m_sSystemID,
                                   node.lineNo,
                                   node.colNo);

    m_aChildren.addElement (node);
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
    AbstractNodeBase tmp = m_aParent.m_aParent;
    while (!(tmp instanceof AbstractGroupBase))
      tmp = tmp.m_aParent;
    final AbstractGroupBase parentGroup = (AbstractGroupBase) tmp;

    // Evaluate group attribute
    if (m_sGroupExpName != null)
    {
      m_aTargetGroup = (AbstractGroupBase) parentGroup.m_aNamedGroups.get (m_sGroupExpName);
      if (m_aTargetGroup == null)
        throw new SAXParseException ("Unknown target group '" +
                                     m_sGroupQName +
                                     "' specified for '" +
                                     m_sQName +
                                     "'",
                                     m_sPublicID,
                                     m_sSystemID,
                                     lineNo,
                                     colNo);
    }
    if (m_aTargetGroup == null)
    { // means: still null
      // use current group
      m_aTargetGroup = parentGroup;
    }
    return false; // done
  }

  /**
   * assign target group, save and reset parameters
   */
  @Override
  public short process (final Context context) throws SAXException
  {
    context.targetGroup = m_aTargetGroup;

    m_aParamStack.push (context.m_aPassedParameters);
    context.m_aPassedParameters = new Hashtable<> ();
    return CSTX.PR_CONTINUE;
  }

  /**
   * Returns a handler that performs a transformation according to the specified
   * {@link #m_aFilter} value.
   *
   * @exception SAXException
   *            if this handler couldn't be created
   */
  protected TransformerHandler getProcessHandler (final Context context) throws SAXException
  {
    final String sFilterMethod = m_aFilter.evaluate (context, this).getString ();

    TransformerHandler handler;
    try
    {
      if (m_sUseBufExpName != null)
      {
        if (!m_bBufScopeDetermined)
        {
          m_aBufGroupScope = VariableUtils.findVariableScope (context, m_sUseBufExpName);
          m_bBufScopeDetermined = true;
        }
        handler = context.defaultTransformerHandlerResolver.resolve (sFilterMethod,
                                                                     new BufferReader (context,
                                                                                       m_sUseBufExpName,
                                                                                       m_aBufGroupScope,
                                                                                       m_sPublicID,
                                                                                       m_sSystemID),
                                                                     context.m_aURIResolver,
                                                                     context.m_aErrorHandler.m_aErrorListener,
                                                                     context.m_aPassedParameters);
      }
      else
      {
        final String href = (m_aHrefTree != null) ? m_aHrefTree.evaluate (context, this).getStringValue () : null;
        handler = context.defaultTransformerHandlerResolver.resolve (sFilterMethod,
                                                                     href,
                                                                     m_sSystemID,
                                                                     context.m_aURIResolver,
                                                                     context.m_aErrorHandler.m_aErrorListener,
                                                                     context.m_aPassedParameters);
      }
      if (handler == null)
      {
        context.m_aErrorHandler.fatalError ("Filter '" +
                                            sFilterMethod +
                                            "' not available",
                                            m_sPublicID,
                                            m_sSystemID,
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
      context.m_aErrorHandler.fatalError (e.getMessage (), m_sPublicID, m_sSystemID, lineNo, colNo, e);
      return null;
    }
    catch (final VariableNotFoundException e)
    {
      context.m_aErrorHandler.error ("Can't process an undeclared buffer '" +
                                     m_sUseBufQName +
                                     "'",
                                     m_sPublicID,
                                     m_sSystemID,
                                     lineNo,
                                     colNo);
      // if the error handler returns
      return null;
    }

    final EmitterAdapter adapter = new EmitterAdapter (context.m_aEmitter, this);
    handler.setResult (new SAXResult (adapter));
    return handler;
  }

  @Override
  protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
  {
    super.onDeepCopy (copy, copies);
    final AbstractProcessBase theCopy = (AbstractProcessBase) copy;
    theCopy.m_aParamStack = new Stack<> ();
    if (m_aBufGroupScope != null)
      theCopy.m_aBufGroupScope = (AbstractGroupBase) m_aBufGroupScope.deepCopy (copies);
    if (m_aTargetGroup != null)
      theCopy.m_aTargetGroup = (AbstractGroupBase) m_aTargetGroup.deepCopy (copies);
    theCopy.m_aChildren = new Vector<> (m_aChildren.size ());
    for (final AbstractInstruction aChildI : m_aChildren)
      theCopy.m_aChildren.add (aChildI.deepCopy (copies));
    if (m_aFilter != null)
      theCopy.m_aFilter = m_aFilter.deepCopy (copies);
    if (m_aHrefTree != null)
      theCopy.m_aHrefTree = m_aHrefTree.deepCopy (copies);
  }

  protected boolean hasFilter ()
  {
    return m_aFilter != null;
  }
}
