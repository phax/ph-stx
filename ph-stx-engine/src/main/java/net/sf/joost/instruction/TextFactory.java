/*
 * $Id: TextFactory.java,v 2.8 2008/10/04 17:13:14 obecker Exp $
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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;

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
  /** allowed attributes for this element */
  private final HashSet <String> attrNames;

  private static final String [] MARKUP_VALUES = { "error", "ignore", "serialize" };

  private static final int NO_MARKUP = 0;
  private static final int IGNORE_MARKUP = 1;
  private static final int SERIALIZE_MARKUP = 2;

  public TextFactory ()
  {
    attrNames = new HashSet<> ();
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
  public class Instance extends AbstractNodeBase
  {
    /** a StreamEmitter or a StringEmitter */
    private IStxEmitter stxEmitter;

    /** the buffer of the StringWriter or the StringEmitter resp. */
    private StringBuffer buffer;

    /** levels of recursive calls */
    private int recursionLevel;

    private final int markup;

    public Instance (final String qName, final AbstractNodeBase parent, final ParseContext context, final int markup)
    {
      super (qName, parent, context, true);
      this.markup = markup;
      init ();
    }

    private void init ()
    {
      if (markup == SERIALIZE_MARKUP)
      {
        // use our StreamEmitter with a StringWriter
        final StringWriter w = new StringWriter ();
        buffer = w.getBuffer ();
        stxEmitter = AbstractStreamEmitter.newXMLEmitter (w);
      }
      else
      {
        // use our StringEmitter
        buffer = new StringBuffer ();
        stxEmitter = new StringEmitter (buffer, markup == NO_MARKUP ? "('" +
                                                                      qName +
                                                                      "' with the 'markup' attribute set to '" +
                                                                      MARKUP_VALUES[NO_MARKUP] +
                                                                      "' started in line " +
                                                                      lineNo +
                                                                      ")"
                                                                    : null);
      }
      recursionLevel = 0;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      if (recursionLevel++ == 0)
      { // outermost invocation
        buffer.setLength (0);
        context.pushEmitter (stxEmitter);
      }
      return CSTX.PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      if (--recursionLevel == 0)
      { // outermost invocation
        context.popEmitter ();
        context.emitter.characters (buffer.toString ().toCharArray (), 0, buffer.length (), this);
      }
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      theCopy.init ();
    }

  }
}
