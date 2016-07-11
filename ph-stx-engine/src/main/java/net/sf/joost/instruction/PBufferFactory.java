/*
 * $Id: PBufferFactory.java,v 2.17 2009/08/21 12:46:17 obecker Exp $
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.stx.BufferReader;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Processor;
import net.sf.joost.util.VariableNotFoundException;
import net.sf.joost.util.VariableUtils;

/**
 * Factory for <code>process-buffer</code> elements, which are represented by
 * the inner Instance class.
 *
 * @version $Revision: 2.17 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

public class PBufferFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames;

  //
  // Constructor
  //
  public PBufferFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("name");
    attrNames.add ("group");
    attrNames.add ("filter-method");
    attrNames.add ("filter-src");
  }

  /** @return <code>"process-buffer"</code> */
  @Override
  public String getName ()
  {
    return "process-buffer";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final String nameAtt = getRequiredAttribute (qName, attrs, "name", context);
    // buffers are special variables with an "@" prefix
    final String bufName = "@" + getExpandedName (nameAtt, context);

    final String groupAtt = attrs.getValue ("group");

    final String filterMethodAtt = attrs.getValue ("filter-method");

    if (groupAtt != null && filterMethodAtt != null)
      throw new SAXParseException ("It's not allowed to use both 'group' and 'filter-method' attributes",
                                   context.locator);

    final String filterSrcAtt = attrs.getValue ("filter-src");

    if (filterSrcAtt != null && filterMethodAtt == null)
      throw new SAXParseException ("Missing 'filter-method' attribute in '" +
                                   qName +
                                   "' ('filter-src' is present)",
                                   context.locator);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAtt, bufName, groupAtt, filterMethodAtt, filterSrcAtt);
  }

  /** The inner Instance class */
  public static final class Instance extends AbstractProcessBase
  {
    private final String m_sBufName;
    private final String m_sExpName;
    private boolean m_bScopeDetermined = false;
    private AbstractGroupBase m_aGroupScope;

    // Constructor
    public Instance (final String qName,
                     final AbstractNodeBase parent,
                     final ParseContext context,
                     final String bufName,
                     final String expName,
                     final String groupQName,
                     final String method,
                     final String src) throws SAXParseException
    {
      super (qName, parent, context, groupQName, method, src);
      this.m_sBufName = bufName;
      this.m_sExpName = expName;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      this.m_aLocalFieldStack.push (context.targetGroup);
      return super.process (context);
    }

    /**
     * Processes a buffer.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.currentInstruction = this;

      if (!m_bScopeDetermined)
      {
        try
        {
          m_aGroupScope = VariableUtils.findVariableScope (context, m_sExpName);
        }
        catch (final VariableNotFoundException e)
        {
          context.m_aErrorHandler.error ("Can't process an undeclared buffer '" +
                                         m_sBufName +
                                         "'",
                                         m_sPublicID,
                                         m_sSystemID,
                                         lineNo,
                                         colNo);
          // if the error handler returns
          return CSTX.PR_ERROR;
        }
        m_bScopeDetermined = true;
      }

      final BufferReader br = new BufferReader (context, m_sExpName, m_aGroupScope, m_sPublicID, m_sSystemID);

      if (hasFilter ())
      {
        // use external SAX filter (TransformerHandler)
        final TransformerHandler handler = getProcessHandler (context);
        if (handler == null)
          return CSTX.PR_ERROR;

        try
        {
          handler.startDocument ();
          br.parse (handler, handler);
          handler.endDocument ();
        }
        catch (final SAXException e)
        {
          // add locator information
          context.m_aErrorHandler.fatalError (e.getMessage (), m_sPublicID, m_sSystemID, lineNo, colNo, e);
          return CSTX.PR_ERROR;
        }
        // catch any unchecked exception
        catch (final RuntimeException e)
        {
          // wrap exception
          final StringWriter sw = new StringWriter ();
          e.printStackTrace (new PrintWriter (sw));
          context.m_aErrorHandler.fatalError ("External processing failed: " +
                                              sw,
                                              m_sPublicID,
                                              m_sSystemID,
                                              lineNo,
                                              colNo,
                                              e);
          return CSTX.PR_ERROR;
        }
      }
      else
      {
        // process the events using STX instructions

        // store current group
        final AbstractGroupBase prevGroup = context.currentGroup;

        // ensure, that position counters on the top most event are
        // available
        context.ancestorStack.peek ().enableChildNodes (false);

        final Processor proc = context.currentProcessor;
        proc.startInnerProcessing ();

        // call parse method with the two handler objects directly
        // (no startDocument, endDocument events!)
        br.parse (proc, proc);

        proc.endInnerProcessing ();
        // restore current group
        context.currentGroup = prevGroup;
      }
      context.targetGroup = (AbstractGroupBase) m_aLocalFieldStack.pop ();

      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aGroupScope != null)
        theCopy.m_aGroupScope = (AbstractGroupBase) m_aGroupScope.deepCopy (copies);
    }

  }
}
