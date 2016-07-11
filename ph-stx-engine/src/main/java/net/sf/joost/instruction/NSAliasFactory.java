/*
 * $Id: NSAliasFactory.java,v 2.6 2007/12/19 10:39:37 obecker Exp $
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>namespace-alias</code> elements
 * 
 * @version $Revision: 2.6 $ $Date: 2007/12/19 10:39:37 $
 * @author Oliver Becker
 */

public final class NSAliasFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  // Constructor
  public NSAliasFactory ()
  {
    attrNames = new HashSet ();
    attrNames.add ("sheet-prefix");
    attrNames.add ("result-prefix");
  }

  /** @return <code>"namespace-alias"</code> */
  @Override
  public String getName ()
  {
    return "namespace-alias";
  }

  /** Returns an instance of {@link Instance} */
  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXException
  {
    // check parent
    if (parent != null && !(parent instanceof TransformFactory.Instance))
      throw new SAXParseException ("'" + qName + "' not allowed as child of '" + parent.qName + "'", context.locator);

    String fromPrefix = getRequiredAttribute (qName, attrs, "sheet-prefix", context);
    // check value syntax
    if ((fromPrefix.indexOf ('#') != -1 && !fromPrefix.equals ("#default")) || fromPrefix.indexOf (':') != -1)
      throw new SAXParseException ("The value of 'sheet-prefix' must be either a NCName or " +
                                   "the string '#default'. Found '" +
                                   fromPrefix +
                                   "'",
                                   context.locator);
    // "#default" used?
    if (fromPrefix.equals ("#default"))
      fromPrefix = "";
    // declared namespace?
    Object fromURI = context.nsSet.get (fromPrefix);
    if (fromURI == null)
    {
      if (fromPrefix != "")
        throw new SAXParseException ("Undeclared namespace prefix '" +
                                     fromPrefix +
                                     "' found in the 'sheet-prefix' attribute",
                                     context.locator);
      else
        fromURI = "";
    }

    // dito for result-prefix:
    String toPrefix = getRequiredAttribute (qName, attrs, "result-prefix", context);
    if ((toPrefix.indexOf ('#') != -1 && !toPrefix.equals ("#default")) || toPrefix.indexOf (':') != -1)
      throw new SAXParseException ("The value of 'result-prefix' must be either a NCName or " +
                                   "the string '#default', found '" +
                                   toPrefix +
                                   "'",
                                   context.locator);
    if (toPrefix.equals ("#default"))
      toPrefix = "";
    Object toURI = context.nsSet.get (toPrefix);
    if (toURI == null)
    {
      if (toPrefix != "")
        throw new SAXParseException ("Undeclared namespace prefix '" +
                                     toPrefix +
                                     "' found in the 'result-prefix' attribute",
                                     context.locator);
      else
        toURI = "";
    }

    // alias already defined?
    final Hashtable namespaceAliases = ((TransformFactory.Instance) parent).namespaceAliases;
    final Object alias = namespaceAliases.get (fromURI);
    if (alias != null && !alias.equals (toURI))
    {
      throw new SAXParseException ("Namespace alias for prefix '" +
                                   (fromPrefix == "" ? "#default" : fromPrefix) +
                                   "' already declared as '" +
                                   (alias == "" ? "#default" : alias) +
                                   "'",
                                   context.locator);
    }
    // establish new alias mapping
    namespaceAliases.put (fromURI, toURI);

    checkAttributes (qName, attrs, attrNames, context);

    return new Instance (qName, parent, context);
  }

  /**
   * Represents an instance of the <code>namespace-alias</code> element.
   * <p>
   * It has no real functionality of its own; it is only needed to simplify the
   * parsing process of the transformation sheet.
   */
  public final class Instance extends AbstractNodeBase
  {
    protected Instance (final String qName, final AbstractNodeBase parent, final ParseContext context)
    {
      super (qName, parent, context, false);
    }

    @Override
    public boolean processable ()
    {
      return false;
    }

    // Shouldn't be called
    @Override
    public short process (final Context c) throws SAXException
    {
      throw new SAXParseException ("process called for " + qName, publicId, systemId, lineNo, colNo);
    }
  }
}
