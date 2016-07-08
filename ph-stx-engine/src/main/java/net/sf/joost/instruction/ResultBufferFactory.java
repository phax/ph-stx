/*
 * $Id: ResultBufferFactory.java,v 2.5 2007/12/19 10:39:37 obecker Exp $
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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.emitter.BufferEmitter;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>result-buffer</code> elements, which are represented by the
 * inner Instance class.
 * 
 * @version $Revision: 2.5 $ $Date: 2007/12/19 10:39:37 $
 * @author Oliver Becker
 */

final public class ResultBufferFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  // Constructor
  public ResultBufferFactory ()
  {
    attrNames = new HashSet ();
    attrNames.add ("name");
    attrNames.add ("clear");
  }

  /** @return <code>"result-buffer"</code> */
  @Override
  public String getName ()
  {
    return "result-buffer";
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

    // default is "no" (false)
    final boolean clear = getEnumAttValue ("clear", attrs, YESNO_VALUES, context) == YES_VALUE;

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAtt, bufName, clear);
  }

  /** Represents an instance of the <code>result-buffer</code> element. */
  final public class Instance extends NodeBase
  {
    private final String bufName, expName;
    private final boolean clear;

    protected Instance (final String qName,
                        final NodeBase parent,
                        final ParseContext context,
                        final String bufName,
                        final String expName,
                        final boolean clear)
    {
      super (qName, parent, context, true);
      this.bufName = bufName;
      this.expName = expName;
      this.clear = clear;
    }

    /**
     * Declares this buffer as the current output buffer in use (for events
     * resulting from a transformation).
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      Object emitter = context.localVars.get (expName);
      if (emitter == null)
      {
        GroupBase group = context.currentGroup;
        while (emitter == null && group != null)
        {
          emitter = ((Hashtable) ((Stack) context.groupVars.get (group)).peek ()).get (expName);
          group = group.parentGroup;
        }
      }
      if (emitter == null)
      {
        context.errorHandler.error ("Can't fill an undeclared buffer '" +
                                    bufName +
                                    "'",
                                    publicId,
                                    systemId,
                                    lineNo,
                                    colNo);
        return PR_CONTINUE;
      }

      final BufferEmitter buffer = (BufferEmitter) ((Emitter) emitter).contH;
      if (context.emitter.isEmitterActive (buffer))
      {
        context.errorHandler.error ("Buffer '" +
                                    bufName +
                                    "' acts already as result buffer",
                                    publicId,
                                    systemId,
                                    lineNo,
                                    colNo);
        return PR_CONTINUE;
      }

      if (clear)
        buffer.clear ();

      context.pushEmitter ((Emitter) emitter);
      return PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      ((BufferEmitter) context.popEmitter ()).filled ();
      return super.processEnd (context);
    }
  }
}
