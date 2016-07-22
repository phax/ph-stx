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
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>match</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 1.7 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class MatchFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public MatchFactory ()
  {
    attrNames.add ("regex");
    attrNames.add ("flags");
  }

  /** @return <code>"match"</code> */
  @Override
  public String getName ()
  {
    return "match";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    if (!(parent instanceof AnalyzeTextFactory.Instance))
      throw new SAXParseException ("'" + qName + "' must be child of stx:analyze-text", context.locator);

    final AbstractTree regexAVT = parseRequiredAVT (qName, attrs, "regex", context);

    final AbstractTree flagsAVT = parseAVT (attrs.getValue ("flags"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, regexAVT, flagsAVT);
  }

  /** Represents an instance of the <code>match</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    /**
     * The AVT in the <code>regex</code> attribute; it will be evaluated in the
     * <code>stx:analyze-text</code> parent
     */
    protected AbstractTree m_aRegex;

    /**
     * The AVT in the <code>flags</code> attribute; it will be evaluated in the
     * <code>stx:analyze-text</code> parent
     */
    protected AbstractTree m_aFlags;

    /** The parent */
    private AnalyzeTextFactory.Instance m_aAnalyzeText;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final AbstractTree regex,
                        final AbstractTree flags)
    {
      super (qName, parent, context, true);
      this.m_aRegex = regex;
      this.m_aFlags = flags;
      m_aAnalyzeText = (AnalyzeTextFactory.Instance) parent;
    }

    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXException
    {
      m_aNodeEnd.next = m_aAnalyzeText.m_aNodeEnd; // back to stx:analyze-text
      return false;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      // store value for the regex-group function
      context.localRegExGroup.push (m_aAnalyzeText.capSubstr);
      return CSTX.PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.localRegExGroup.pop ();
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aAnalyzeText != null)
        theCopy.m_aAnalyzeText = (AnalyzeTextFactory.Instance) m_aAnalyzeText.deepCopy (copies);
      if (m_aFlags != null)
        theCopy.m_aFlags = m_aFlags.deepCopy (copies);
      if (m_aRegex != null)
        theCopy.m_aRegex = m_aRegex.deepCopy (copies);
    }

    //
    // for debugging
    //
    @Override
    public String toString ()
    {
      return "stx:match regex='" + m_aRegex + "'";
    }
  }
}
