/*
 * $Id: MatchFactory.java,v 1.7 2008/10/04 17:13:14 obecker Exp $
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>match</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 1.7 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

final public class MatchFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  // Constructor
  public MatchFactory ()
  {
    attrNames = new HashSet ();
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
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    if (!(parent instanceof AnalyzeTextFactory.Instance))
      throw new SAXParseException ("'" + qName + "' must be child of stx:analyze-text", context.locator);

    final Tree regexAVT = parseRequiredAVT (qName, attrs, "regex", context);

    final Tree flagsAVT = parseAVT (attrs.getValue ("flags"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, regexAVT, flagsAVT);
  }

  /** Represents an instance of the <code>match</code> element. */
  final public class Instance extends NodeBase
  {
    /**
     * The AVT in the <code>regex</code> attribute; it will be evaluated in the
     * <code>stx:analyze-text</code> parent
     */
    protected Tree regex;

    /**
     * The AVT in the <code>flags</code> attribute; it will be evaluated in the
     * <code>stx:analyze-text</code> parent
     */
    protected Tree flags;

    /** The parent */
    private AnalyzeTextFactory.Instance analyzeText;

    protected Instance (final String qName,
                        final NodeBase parent,
                        final ParseContext context,
                        final Tree regex,
                        final Tree flags)
    {
      super (qName, parent, context, true);
      this.regex = regex;
      this.flags = flags;
      analyzeText = (AnalyzeTextFactory.Instance) parent;
    }

    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXException
    {
      nodeEnd.next = analyzeText.nodeEnd; // back to stx:analyze-text
      return false;
    }

    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      // store value for the regex-group function
      context.localRegExGroup.push (analyzeText.capSubstr);
      return PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.localRegExGroup.pop ();
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (analyzeText != null)
        theCopy.analyzeText = (AnalyzeTextFactory.Instance) analyzeText.deepCopy (copies);
      if (flags != null)
        theCopy.flags = flags.deepCopy (copies);
      if (regex != null)
        theCopy.regex = regex.deepCopy (copies);
    }

    //
    // for debugging
    //
    @Override
    public String toString ()
    {
      return "stx:match regex='" + regex + "'";
    }
  }
}
