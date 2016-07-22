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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.AbstractStreamEmitter;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>text</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.8 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public class TextFactory extends AbstractFactoryBase
{
  private static final String [] MARKUP_VALUES = { "error", "ignore", "serialize" };

  private static final int NO_MARKUP = 0;
  private static final int IGNORE_MARKUP = 1;
  private static final int SERIALIZE_MARKUP = 2;

  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  public TextFactory ()
  {
    attrNames.add ("markup");
  }

  /** @return <code>"text"</code> */
  @Override
  public String getName ()
  {
    return "text";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    int markup = getEnumAttValue ("markup", attrs, MARKUP_VALUES, context);
    if (markup == -1)
      markup = NO_MARKUP; // default value

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, markup);
  }

  /** The inner Instance class */
  public static final class Instance extends AbstractNodeBase
  {
    /** a StreamEmitter or a StringEmitter */
    private IStxEmitter m_aSTXxEmitter;

    /** the buffer of the StringWriter or the StringEmitter resp. */
    private StringBuffer m_aBuffer;

    /** levels of recursive calls */
    private int m_nRecursionLevel;

    private final int m_nMarkup;

    public Instance (final String qName, final AbstractNodeBase parent, final ParseContext context, final int markup)
    {
      super (qName, parent, context, true);
      this.m_nMarkup = markup;
      init ();
    }

    private void init ()
    {
      if (m_nMarkup == SERIALIZE_MARKUP)
      {
        // use our StreamEmitter with a StringWriter
        final StringWriter w = new StringWriter ();
        m_aBuffer = w.getBuffer ();
        m_aSTXxEmitter = AbstractStreamEmitter.newXMLEmitter (w);
      }
      else
      {
        // use our StringEmitter
        m_aBuffer = new StringBuffer ();
        m_aSTXxEmitter = new StringEmitter (m_aBuffer, m_nMarkup == NO_MARKUP ? "('" +
                                                                                m_sQName +
                                                                                "' with the 'markup' attribute set to '" +
                                                                                MARKUP_VALUES[NO_MARKUP] +
                                                                                "' started in line " +
                                                                                lineNo +
                                                                                ")"
                                                                              : null);
      }
      m_nRecursionLevel = 0;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      if (m_nRecursionLevel++ == 0)
      { // outermost invocation
        m_aBuffer.setLength (0);
        context.pushEmitter (m_aSTXxEmitter);
      }
      return CSTX.PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      if (--m_nRecursionLevel == 0)
      { // outermost invocation
        context.popEmitter ();
        context.m_aEmitter.characters (m_aBuffer.toString ().toCharArray (), 0, m_aBuffer.length (), this);
      }
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
