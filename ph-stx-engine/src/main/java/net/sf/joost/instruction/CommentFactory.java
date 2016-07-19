/*
 * $Id: CommentFactory.java,v 2.7 2008/10/04 17:13:14 obecker Exp $
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
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>comment</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.7 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public class CommentFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  public CommentFactory ()
  {
    attrNames.add ("select");
  }

  /** @return <code>"comment"</code> */
  @Override
  public String getName ()
  {
    return "comment";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final AbstractTree selectExpr = parseExpr (attrs.getValue ("select"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, selectExpr);
  }

  /** Represents an instance of the <code>comment</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private AbstractTree m_aSelect;
    private StringEmitter m_aStrEmitter;
    private StringBuffer m_aBuffer;

    public Instance (final String qName,
                     final AbstractNodeBase parent,
                     final ParseContext context,
                     final AbstractTree select)
    {
      super (qName,
             parent,
             context,
             // this element must be empty if there is a select attribute
             select == null);
      this.m_aSelect = select;
      init ();
    }

    private void init ()
    {
      m_aBuffer = new StringBuffer ();
      m_aStrEmitter = new StringEmitter (m_aBuffer, "('" + m_sQName + "' started in line " + lineNo + ")");
    }

    /**
     * Activate a StringEmitter for collecting the contents of this instruction.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      if (m_aSelect == null)
      {
        // we have contents to be processed
        super.process (context);
        // check for nesting of this stx:comment instructions
        if (context.m_aEmitter.isEmitterActive (m_aStrEmitter))
        {
          context.m_aErrorHandler.error ("Can't create nested comment here", m_sPublicID, m_sSystemID, lineNo, colNo);
          return CSTX.PR_CONTINUE; // if the errorHandler returns
        }
        m_aBuffer.setLength (0);
        context.pushEmitter (m_aStrEmitter);
      }
      else
      {
        final String comment = m_aSelect.evaluate (context, this).getStringValue ();
        // Most comments won't have dashes inside, so it's reasonable
        // to skip the StringBuffer creation in these cases
        if (comment.indexOf ('-') != -1)
        {
          // have a closer look at the dashes
          emitComment (new StringBuffer (comment), context);
        }
        else
        {
          // produce the comment immediately
          context.m_aEmitter.comment (comment.toCharArray (), 0, comment.length (), this);
        }
      }

      return CSTX.PR_CONTINUE;
    }

    /**
     * Emit a comment to the result stream from the contents of the
     * StringEmitter.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.popEmitter ();

      emitComment (m_aBuffer, context);

      // It would be sensible to clear the buffer here,
      // but setLength(0) doesn't really free any memory ...
      // So it's more logical to "clear" the buffer at the beginning
      // of the processing.

      return super.processEnd (context);
    }

    /**
     * Check the new comment for contained dashes and send it to the emitter.
     *
     * @param comment
     *        the contents of the new comment
     * @param context
     *        the context
     */
    private void emitComment (final StringBuffer comment, final Context context) throws SAXException
    {
      int index = comment.length ();
      if (index != 0)
      {
        // does the new comment start with '-'?
        if (comment.charAt (0) == '-')
          comment.insert (0, ' ');

        // are there any "--" in the inner of the new comment?
        // // this compiles only in JDK1.4 or above
        // int index;
        // while ((index = buffer.indexOf("--")) != -1)
        // buffer.insert(index+1, ' ');
        // 1.0 solution:
        final String str = comment.toString ();
        while ((index = str.lastIndexOf ("--", --index)) != -1)
          comment.insert (index + 1, ' ');

        // does the new comment end with '-'?
        if (comment.charAt (comment.length () - 1) == '-')
          comment.append (' ');
      }

      context.m_aEmitter.comment (comment.toString ().toCharArray (), 0, comment.length (), this);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      theCopy.init ();
      if (m_aSelect != null)
        theCopy.m_aSelect = m_aSelect.deepCopy (copies);
    }

  }
}
