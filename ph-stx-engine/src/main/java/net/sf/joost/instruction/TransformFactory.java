/*
 * $Id: TransformFactory.java,v 2.17 2007/12/19 10:39:37 obecker Exp $
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
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Processor;

/**
 * Factory for <code>transform</code> elements, which are represented by the
 * inner Instance class
 *
 * @version $Revision: 2.17 $ $Date: 2007/12/19 10:39:37 $
 * @author Oliver Becker
 */

public class TransformFactory extends AbstractFactoryBase
{
  /** allowed values for the <code>pass-through</code> attribute */
  private static final String [] PASS_THROUGH_VALUES = { "none", "text", "all" };

  private static final String EXCLUDE_RESULT_PREFIXES = "exclude-result-prefixes";

  /** allowed attributes for this element. */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public TransformFactory ()
  {
    attrNames.add ("version");
    attrNames.add ("output-encoding");
    attrNames.add ("output-method");
    attrNames.add ("stxpath-default-namespace");
    attrNames.add ("pass-through");
    attrNames.add ("recognize-cdata");
    attrNames.add ("strip-space");
    attrNames.add (EXCLUDE_RESULT_PREFIXES);
  }

  /** @return <code>"transform"</code> */
  @Override
  public String getName ()
  {
    return "transform";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    if (parent != null && parent.m_sSystemID.equals (context.locator.getSystemId ()))
      throw new SAXParseException ("'" + qName + "' is allowed only as root element", context.locator);
    // parent.systemId != locator.systemId means: it is included

    final String version = getRequiredAttribute (qName, attrs, "version", context);
    if (!version.equals ("1.0"))
      throw new SAXParseException ("Unknown STX version '" +
                                   version +
                                   "'. The only supported version is 1.0.",
                                   context.locator);

    final String encodingAtt = attrs.getValue ("output-encoding");

    String methodAtt = attrs.getValue ("output-method");
    if (methodAtt != null)
    {
      if (methodAtt.indexOf (':') != -1)
        methodAtt = getExpandedName (methodAtt, context);
      else
        if (!methodAtt.equals ("text") && !methodAtt.equals ("xml"))
          throw new SAXParseException ("Value of attribute 'output-method' must be 'xml', 'text', " +
                                       "or a qualified name. Found '" +
                                       methodAtt +
                                       "'",
                                       context.locator);
    }

    final String defStxpNsAtt = attrs.getValue ("stxpath-default-namespace");

    // default is "none"
    byte passThrough = 0;
    switch (getEnumAttValue ("pass-through", attrs, PASS_THROUGH_VALUES, context))
    {
      case -1:
      case 0:
        passThrough = Processor.PASS_THROUGH_NONE;
        break;
      case 1:
        passThrough = Processor.PASS_THROUGH_TEXT;
        break;
      case 2:
        passThrough = Processor.PASS_THROUGH_ALL;
        break;
      default:
        // mustn't happen
        throw new SAXParseException ("Unexpected return value from getEnumAttValue", context.locator);
    }

    // default is "no" (false)
    final boolean stripSpace = getEnumAttValue ("strip-space", attrs, YESNO_VALUES, context) == YES_VALUE;

    // default is "yes" (true)
    final boolean recognizeCdata = getEnumAttValue ("recognize-cdata", attrs, YESNO_VALUES, context) != NO_VALUE;

    final String excludedPrefixes = attrs.getValue (EXCLUDE_RESULT_PREFIXES);
    final Set <String> excludedNamespaces = new HashSet<> ();
    excludedNamespaces.add (CSTX.STX_NS);
    if (excludedPrefixes != null)
    {
      int tokenNo = 0;
      final StringTokenizer tokenizer = new StringTokenizer (excludedPrefixes);
      while (tokenizer.hasMoreTokens ())
      {
        tokenNo++;
        String prefix = tokenizer.nextToken ();
        if ("#all".equals (prefix))
        {
          if (tokenNo != 1 || tokenizer.hasMoreTokens ())
            throw new SAXParseException ("The value '#all' must be used standalone in the '" +
                                         EXCLUDE_RESULT_PREFIXES +
                                         "' attribute",
                                         context.locator);
          excludedNamespaces.addAll (context.nsSet.values ());
          break; // while
        }
        if ("#default".equals (prefix))
          prefix = "";
        final String ns = context.nsSet.get (prefix);
        if (ns != null)
          excludedNamespaces.add (ns);
        else
          if (prefix == "") // #default
            throw new SAXParseException ("No default namespace declared to be excluded by " +
                                         "using the value '#default' in the '" +
                                         EXCLUDE_RESULT_PREFIXES +
                                         "' attribute",
                                         context.locator);
          else
            throw new SAXParseException ("No namespace declared for prefix '" +
                                         prefix +
                                         "' in the '" +
                                         EXCLUDE_RESULT_PREFIXES +
                                         "' attribute",
                                         context.locator);

      }
    }

    checkAttributes (qName, attrs, attrNames, context);

    return new Instance (parent,
                         qName,
                         context,
                         encodingAtt,
                         methodAtt,
                         defStxpNsAtt,
                         passThrough,
                         stripSpace,
                         recognizeCdata,
                         excludedNamespaces);
  }

  /* --------------------------------------------------------------------- */

  /** Represents an instance of the <code>transform</code> element. */
  public static final class Instance extends AbstractGroupBase
  {
    /** mapping table for <code>stx:namespace-alias</code> instructions */
    public Hashtable <String, String> m_aNamespaceAliases;

    // stx:transform attributes (options)
    public String m_sOutputEncoding;
    public String m_sOutputMethod;
    public String m_sStxpathDefaultNamespace;
    public Set <String> m_aExcludedNamespaces;

    // used to transfer the list of compilable nodes from an included
    // STX sheet to the calling Parser object
    public List <AbstractNodeBase> m_aCompilableNodes;

    // Constructor
    public Instance (final AbstractNodeBase aParent,
                     final String qName,
                     final ParseContext context,
                     final String outputEncoding,
                     final String outputMethod,
                     final String stxpathDefaultNamespace,
                     final byte passThrough,
                     final boolean stripSpace,
                     final boolean recognizeCdata,
                     final Set <String> excludedNamespaces)
    {
      super (qName, aParent, context, passThrough, stripSpace, recognizeCdata);
      if (aParent == null)
      {
        m_aNamedGroups = new Hashtable<> (); // shared with all sub-groups
        m_aGlobalProcedures = new Hashtable<> (); // also shared
        m_aNamespaceAliases = new Hashtable<> (); // also shared
      }
      else
      {
        // use global parameters of the including STX sheet
        // (have to do the following lookup, because
        // context.transformNode is still null
        // -> should be improved/fixed)
        AbstractNodeBase parent = aParent;
        while (!(parent instanceof TransformFactory.Instance))
          parent = parent.m_aParent;
        m_aNamespaceAliases = ((TransformFactory.Instance) parent).m_aNamespaceAliases;
      }

      this.m_sOutputEncoding = (outputEncoding != null) ? outputEncoding : CSTX.DEFAULT_ENCODING; // in
      // Constants

      this.m_sOutputMethod = (outputMethod != null) ? outputMethod : "xml";

      this.m_sStxpathDefaultNamespace = (stxpathDefaultNamespace != null) ? stxpathDefaultNamespace : "";

      this.m_aExcludedNamespaces = excludedNamespaces;
    }

    /** @return all top level elements of the transformation sheet */
    public Vector <AbstractNodeBase> getChildren ()
    {
      return m_aChildren;
    }

    @Override
    public void insert (final AbstractNodeBase node) throws SAXParseException
    {
      if (m_aCompilableNodes != null)
        // will only happen after this transform
        // element was inserted by
        // an stx:include instruction
        throw new SAXParseException ("'" +
                                     m_sQName +
                                     "' must be empty",
                                     node.m_sPublicID,
                                     node.m_sSystemID,
                                     node.lineNo,
                                     node.colNo);

      if (node instanceof AbstractTemplateBase || // template, procedure
          node instanceof AbstractGroupBase || // group, transform (= include)
          node instanceof AbstractVariableBase) // param, variable, buffer
        super.insert (node);
      else
        if (node instanceof NSAliasFactory.Instance || node instanceof ScriptFactory.Instance)
        {
          // nothing to do in this case
        }
        else
          throw new SAXParseException ("'" +
                                       node.m_sQName +
                                       "' not allowed as top level element",
                                       node.m_sPublicID,
                                       node.m_sSystemID,
                                       node.lineNo,
                                       node.colNo);
    }
  }
}
