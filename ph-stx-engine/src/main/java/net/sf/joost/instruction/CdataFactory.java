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

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>cdata</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.5 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class CdataFactory extends AbstractFactoryBase
{
  /** @return <code>"cdata"</code> */
  @Override
  public String getName ()
  {
    return "cdata";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    checkAttributes (qName, attrs, null, context);
    return new Instance (qName, parent, context);
  }

  /** The inner Instance class */
  public static final class Instance extends AbstractNodeBase
  {
    private StringEmitter strEmitter;
    private StringBuffer buffer;

    public Instance (final String qName, final AbstractNodeBase parent, final ParseContext context)
    {
      super (qName, parent, context, true);
      init ();
    }

    private void init ()
    {
      buffer = new StringBuffer ();
      strEmitter = new StringEmitter (buffer, "('" + m_sQName + "' started in line " + lineNo + ")");
    }

    /**
     * Starts a CDATA section.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      if (context.m_aEmitter.isEmitterActive (strEmitter))
      {
        context.m_aErrorHandler.error ("Can't create nested CDATA section here",
                                       m_sPublicID,
                                       m_sSystemID,
                                       lineNo,
                                       colNo);
        return CSTX.PR_CONTINUE; // if the errorHandler returns
      }
      super.process (context);
      buffer.setLength (0);
      context.pushEmitter (strEmitter);
      return CSTX.PR_CONTINUE;
    }

    /**
     * Ends a CDATA section
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.popEmitter ();
      final Emitter emitter = context.m_aEmitter;
      emitter.startCDATA (this);
      emitter.characters (buffer.toString ().toCharArray (), 0, buffer.length (), this);
      emitter.endCDATA ();
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      theCopy.init ();
    }
  }
}
