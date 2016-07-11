/*
 * $Id: NoMatchFactory.java,v 1.3 2008/10/04 17:13:14 obecker Exp $
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>no-match</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 1.3 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */
public class NoMatchFactory extends AbstractFactoryBase
{
  /** @return <code>"no-match"</code> */
  @Override
  public String getName ()
  {
    return "no-match";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    if (!(parent instanceof AnalyzeTextFactory.Instance))
      throw new SAXParseException ("'" + qName + "' must be child of stx:analyze-text", context.locator);

    checkAttributes (qName, attrs, null, context);
    return new Instance (qName, parent, context);
  }

  /** Represents an instance of the <code>no-match</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    /** The parent */
    private AnalyzeTextFactory.Instance analyzeText;

    public Instance (final String qName, final AbstractNodeBase parent, final ParseContext context)
    {
      super (qName, parent, context, true);
      analyzeText = (AnalyzeTextFactory.Instance) parent;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      // store value for the regex-group function
      context.localRegExGroup.push (analyzeText.noMatchStr);
      // The next instruction has been set in stx:analyze-text, but
      // this stx:no-match may be interrupted by stx:process-xxx,
      // i.e. we need to store the info of a following stx:match here:
      m_aLocalFieldStack.push (m_aNodeEnd.next);
      m_aLocalFieldStack.push (analyzeText.capSubstr);
      return CSTX.PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.localRegExGroup.pop ();
      // restore the values for the following stx:match
      analyzeText.capSubstr = (String []) m_aLocalFieldStack.pop ();
      m_aNodeEnd.next = (AbstractInstruction) m_aLocalFieldStack.pop ();
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (analyzeText != null)
        theCopy.analyzeText = (AnalyzeTextFactory.Instance) analyzeText.deepCopy (copies);
    }

  }
}
