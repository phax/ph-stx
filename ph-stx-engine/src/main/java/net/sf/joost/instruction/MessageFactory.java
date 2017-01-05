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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.AbstractStreamEmitter;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.trax.SourceLocatorImpl;

/**
 * Factory for <code>message</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.11 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

public final class MessageFactory extends AbstractFactoryBase
{
  /** enumerated values for the level attribute */
  private static final String [] LEVEL_VALUES = { "trace", "debug", "info", "warn", "error", "fatal" };

  /** index in {@link #LEVEL_VALUES} */
  private static final int TRACE_LEVEL = 0;
  private static final int DEBUG_LEVEL = 1;
  private static final int INFO_LEVEL = 2;
  private static final int WARN_LEVEL = 3;
  private static final int ERROR_LEVEL = 4;
  private static final int FATAL_LEVEL = 5;

  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  public MessageFactory ()
  {
    attrNames.add ("select");
    attrNames.add ("terminate");
    attrNames.add ("level");
    attrNames.add ("logger");
  }

  /** @return <code>"message"</code> */
  @Override
  public String getName ()
  {
    return "message";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final AbstractTree selectExpr = parseExpr (attrs.getValue ("select"), context);
    final AbstractTree terminateAVT = parseAVT (attrs.getValue ("terminate"), context);
    final int level = getEnumAttValue ("level", attrs, LEVEL_VALUES, context);

    final String loggerAtt = attrs.getValue ("logger");

    // if one of 'level' or 'logger' is present, we need both
    if ((level != -1) ^ (loggerAtt != null))
      throw new SAXParseException (level != -1 ? "Missing 'logger' attribute when 'level' is present"
                                               : "Missing 'level' attribute when 'logger' is present",
                                   context.locator);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, selectExpr, terminateAVT, level, loggerAtt);
  }

  /** Represents an instance of the <code>message</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private AbstractTree m_aSelect;
    private AbstractTree m_aTerminate;
    private Logger log;
    private final int m_nLevel;

    // used only when log != null
    private StringBuffer m_aBuffer;

    // initialized on first processing
    private IStxEmitter m_aEmitter;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final AbstractTree select,
                        final AbstractTree terminate,
                        final int level,
                        final String logger)
    {
      super (qName,
             parent,
             context,
             // this element must be empty if there is a select attribute
             select == null);

      this.m_aSelect = select;
      this.m_aTerminate = terminate;
      this.m_nLevel = level;
      if (logger != null)
        log = LoggerFactory.getLogger (logger);
    }

    /**
     * Activate the object {@link Context#messageEmitter} for the contents of
     * this element. If this object is <code>null</code> this method first
     * creates a {@link AbstractStreamEmitter} object that writes to stderr and
     * saves it in {@link Context#messageEmitter} for other
     * <code>stx:message</code> instructions.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      if (m_aEmitter == null)
      {
        // create proper StreamEmitter only once
        try
        {
          if (log != null)
          {
            // Create emitter with a StringWriter
            final StringWriter writer = new StringWriter ();
            m_aBuffer = writer.getBuffer ();
            // Note: encoding parameter is irrelevant here
            final AbstractStreamEmitter se = AbstractStreamEmitter.newEmitter (writer,
                                                                               CSTX.DEFAULT_ENCODING,
                                                                               context.currentProcessor.m_aOutputProperties);
            se.setOmitXmlDeclaration (true);
            m_aEmitter = se;
          }
          else
            if (context.messageEmitter == null)
            {
              // create global message emitter using stderr
              final AbstractStreamEmitter se = AbstractStreamEmitter.newEmitter (System.err,
                                                                                 context.currentProcessor.m_aOutputProperties);
              se.setOmitXmlDeclaration (true);
              context.messageEmitter = m_aEmitter = se;
            }
            else
              // use global message emitter
              m_aEmitter = context.messageEmitter;
        }
        catch (final java.io.IOException ex)
        {
          context.m_aErrorHandler.fatalError (ex.toString (), m_sPublicID, m_sSystemID, lineNo, colNo, ex);
          return CSTX.PR_CONTINUE; // if the errorHandler returns
        }
      }

      if (m_aSelect == null)
      {
        super.process (context);
        m_aEmitter.startDocument ();
        context.pushEmitter (m_aEmitter);
      }
      else
      {
        m_aEmitter.startDocument ();
        final String msg = m_aSelect.evaluate (context, this).getStringValue ();
        m_aEmitter.characters (msg.toCharArray (), 0, msg.length ());
        m_aEmitter.endDocument ();
        processMessage (context);
      }

      return CSTX.PR_CONTINUE;
    }

    /**
     * Deactivate the message emitter. Called only when there's no
     * <code>select</code> attribute.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.popEmitter ().endDocument (); // flushes stderr
      processMessage (context);
      return super.processEnd (context);
    }

    /**
     * Process the message: use the logger if it is available and evaluate the
     * optional 'terminate' attribute
     *
     * @throws SAXException
     *         when the transformation shall terminate
     */
    private void processMessage (final Context context) throws SAXException
    {
      if (log != null)
      {
        // include locator info for logging
        final StringBuffer sb = new StringBuffer (m_sSystemID).append (':')
                                                              .append (lineNo)
                                                              .append (':')
                                                              .append (colNo)
                                                              .append (": ")
                                                              .append (m_aBuffer);
        switch (m_nLevel)
        {
          case TRACE_LEVEL:
            log.trace (sb.toString ());
            break;
          case DEBUG_LEVEL:
            log.debug (sb.toString ());
            break;
          case INFO_LEVEL:
            log.info (sb.toString ());
            break;
          case WARN_LEVEL:
            log.warn (sb.toString ());
            break;
          case ERROR_LEVEL:
            log.error (sb.toString ());
            break;
          case FATAL_LEVEL:
            log.error (sb.toString ());
            break;
        }
        m_aBuffer.setLength (0);
      }

      if (m_aTerminate == null)
        return;

      final String terminateValue = m_aTerminate.evaluate (context, this).getString ();
      if (terminateValue.equals ("yes"))
        throw new SAXException (new TransformerException ("Transformation terminated",
                                                          new SourceLocatorImpl (m_sPublicID,
                                                                                 m_sSystemID,
                                                                                 lineNo,
                                                                                 colNo)));

      if (!terminateValue.equals ("no"))
        context.m_aErrorHandler.fatalError ("Attribute 'terminate' of '" +
                                            m_sQName +
                                            "' must be 'yes' or 'no', found '" +
                                            terminateValue +
                                            "'",
                                            m_sPublicID,
                                            m_sSystemID,
                                            lineNo,
                                            colNo);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      theCopy.m_aBuffer = null;
      theCopy.m_aEmitter = null;
      if (m_aSelect != null)
        theCopy.m_aSelect = m_aSelect.deepCopy (copies);
      if (m_aTerminate != null)
        theCopy.m_aTerminate = m_aTerminate.deepCopy (copies);
    }

  }
}
