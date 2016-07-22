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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.instruction;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
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

public final class ResultBufferFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public ResultBufferFactory ()
  {
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
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
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
  public static final class Instance extends AbstractNodeBase
  {
    private final String m_sBufName, m_sExpName;
    private final boolean m_bClear;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final String bufName,
                        final String expName,
                        final boolean clear)
    {
      super (qName, parent, context, true);
      this.m_sBufName = bufName;
      this.m_sExpName = expName;
      this.m_bClear = clear;
    }

    /**
     * Declares this buffer as the current output buffer in use (for events
     * resulting from a transformation).
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      Object emitter = context.localVars.get (m_sExpName);
      if (emitter == null)
      {
        AbstractGroupBase group = context.currentGroup;
        while (emitter == null && group != null)
        {
          emitter = context.groupVars.get (group).peek ().get (m_sExpName);
          group = group.m_aParentGroup;
        }
      }
      if (emitter == null)
      {
        context.m_aErrorHandler.error ("Can't fill an undeclared buffer '" +
                                       m_sBufName +
                                       "'",
                                       m_sPublicID,
                                       m_sSystemID,
                                       lineNo,
                                       colNo);
        return CSTX.PR_CONTINUE;
      }

      final BufferEmitter buffer = (BufferEmitter) ((Emitter) emitter).m_aContH;
      if (context.m_aEmitter.isEmitterActive (buffer))
      {
        context.m_aErrorHandler.error ("Buffer '" +
                                       m_sBufName +
                                       "' acts already as result buffer",
                                       m_sPublicID,
                                       m_sSystemID,
                                       lineNo,
                                       colNo);
        return CSTX.PR_CONTINUE;
      }

      if (m_bClear)
        buffer.clear ();

      context.pushEmitter ((Emitter) emitter);
      return CSTX.PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      ((BufferEmitter) context.popEmitter ()).filled ();
      return super.processEnd (context);
    }
  }
}
