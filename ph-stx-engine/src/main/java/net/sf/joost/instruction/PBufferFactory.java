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

import java.util.HashMap;
import java.util.HashSet;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.stx.BufferReader;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Processor;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.util.VariableNotFoundException;
import net.sf.joost.util.VariableUtils;

/**
 * Factory for <code>process-buffer</code> elements, which are represented by
 * the inner Instance class.
 * 
 * @version $Revision: 2.17 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

public class PBufferFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  //
  // Constructor
  //
  public PBufferFactory ()
  {
    attrNames = new HashSet ();
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
  public NodeBase createNode (final NodeBase parent,
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
  public class Instance extends ProcessBase
  {
    private final String bufName, expName;
    private boolean scopeDetermined = false;
    private GroupBase groupScope = null;

    // Constructor
    public Instance (final String qName,
                     final NodeBase parent,
                     final ParseContext context,
                     final String bufName,
                     final String expName,
                     final String groupQName,
                     final String method,
                     final String src) throws SAXParseException
    {
      super (qName, parent, context, groupQName, method, src);
      this.bufName = bufName;
      this.expName = expName;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      this.localFieldStack.push (context.targetGroup);
      return super.process (context);
    }

    /**
     * Processes a buffer.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.currentInstruction = this;

      if (!scopeDetermined)
      {
        try
        {
          groupScope = VariableUtils.findVariableScope (context, expName);
        }
        catch (final VariableNotFoundException e)
        {
          context.errorHandler.error ("Can't process an undeclared buffer '" +
                                      bufName +
                                      "'",
                                      publicId,
                                      systemId,
                                      lineNo,
                                      colNo);
          // if the error handler returns
          return PR_ERROR;
        }
        scopeDetermined = true;
      }

      final BufferReader br = new BufferReader (context, expName, groupScope, publicId, systemId);

      if (filter != null)
      {
        // use external SAX filter (TransformerHandler)
        final TransformerHandler handler = getProcessHandler (context);
        if (handler == null)
          return PR_ERROR;

        try
        {
          handler.startDocument ();
          br.parse (handler, handler);
          handler.endDocument ();
        }
        catch (final SAXException e)
        {
          // add locator information
          context.errorHandler.fatalError (e.getMessage (), publicId, systemId, lineNo, colNo, e);
          return PR_ERROR;
        }
        // catch any unchecked exception
        catch (final RuntimeException e)
        {
          // wrap exception
          java.io.StringWriter sw = null;
          sw = new java.io.StringWriter ();
          e.printStackTrace (new java.io.PrintWriter (sw));
          context.errorHandler.fatalError ("External processing failed: " + sw, publicId, systemId, lineNo, colNo, e);
          return PR_ERROR;
        }
      }
      else
      {
        // process the events using STX instructions

        // store current group
        final GroupBase prevGroup = context.currentGroup;

        // ensure, that position counters on the top most event are
        // available
        ((SAXEvent) context.ancestorStack.peek ()).enableChildNodes (false);

        final Processor proc = context.currentProcessor;
        proc.startInnerProcessing ();

        // call parse method with the two handler objects directly
        // (no startDocument, endDocument events!)
        br.parse (proc, proc);

        proc.endInnerProcessing ();
        // restore current group
        context.currentGroup = prevGroup;
      }
      context.targetGroup = (GroupBase) localFieldStack.pop ();

      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (groupScope != null)
        theCopy.groupScope = (GroupBase) groupScope.deepCopy (copies);
    }

  }
}
