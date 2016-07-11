/*
 * $Id: AttributeFactory.java,v 2.8 2008/10/04 17:13:14 obecker Exp $
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
import java.util.Hashtable;
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
 * Factory for <code>attribute</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 2.8 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class AttributeFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames;

  // Constructor
  public AttributeFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("name");
    attrNames.add ("select");
    attrNames.add ("namespace");
  }

  /** @return <code>"attribute"</code> */
  @Override
  public String getName ()
  {
    return "attribute";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final AbstractTree selectExpr = parseExpr (attrs.getValue ("select"), context);

    final AbstractTree nameAVT = parseRequiredAVT (qName, attrs, "name", context);

    final AbstractTree namespaceAVT = parseAVT (attrs.getValue ("namespace"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAVT, namespaceAVT, selectExpr);
  }

  /** Represents an instance of the <code>attribute</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private AbstractTree m_aName, m_aNamespace, m_aSelect;
    private final Hashtable <String, String> m_aNSSet;
    private StringEmitter m_aStrEmitter;

    protected Instance (final String elementName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final AbstractTree name,
                        final AbstractTree namespace,
                        final AbstractTree select)
    {
      super (elementName,
             parent,
             context,
             // this element must be empty if there is a select attribute
             select == null);
      this.m_aNSSet = new Hashtable<> (context.nsSet);
      this.m_aName = name;
      this.m_aNamespace = namespace;
      this.m_aSelect = select;
      _init ();
    }

    private void _init ()
    {
      m_aStrEmitter = new StringEmitter (new StringBuffer (), "('" + m_sQName + "' started in line " + lineNo + ")");
    }

    /**
     * Evaluate the <code>name</code> attribute; if the <code>select</code>
     * attribute is present, evaluate this attribute too and create an result
     * attribute.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      // check for nesting of this stx:attribute
      if (context.m_aEmitter.isEmitterActive (m_aStrEmitter))
      {
        context.m_aErrorHandler.error ("Can't create nested attribute", m_sPublicID, m_sSystemID, lineNo, colNo);
        return 0; // if the errorHandler returns
      }
      if (m_aSelect == null)
      {
        // contents and end instruction present
        super.process (context);
        m_aStrEmitter.getBuffer ().setLength (0);
        context.pushEmitter (m_aStrEmitter);
      }

      String attName, attUri, attLocal;
      // determine attribute name
      attName = m_aName.evaluate (context, this).getString ();
      final int colon = attName.indexOf (':');
      if (colon != -1)
      { // prefixed name
        final String prefix = attName.substring (0, colon);
        attLocal = attName.substring (colon + 1);
        if (m_aNamespace != null)
        { // namespace attribute present
          attUri = m_aNamespace.evaluate (context, this).getString ();
          if (attUri.equals (""))
          {
            context.m_aErrorHandler.error ("Can't put attribute '" +
                                           attName +
                                           "' into the null namespace",
                                           m_sPublicID,
                                           m_sSystemID,
                                           lineNo,
                                           colNo);
            return CSTX.PR_CONTINUE; // if the errorHandler returns
          }
        }
        else
        { // no namespace attribute
          // look into the set of in-scope namespaces
          // (of the transformation sheet)
          attUri = m_aNSSet.get (prefix);
          if (attUri == null)
          {
            context.m_aErrorHandler.error ("Attempt to create attribute '" +
                                           attName +
                                           "' with undeclared prefix '" +
                                           prefix +
                                           "'",
                                           m_sPublicID,
                                           m_sSystemID,
                                           lineNo,
                                           colNo);
            return CSTX.PR_CONTINUE; // if the errorHandler returns
          }
        }
      }
      else
      { // unprefixed name
        attLocal = attName;
        attUri = "";
        if (m_aNamespace != null)
        { // namespace attribute present
          attUri = m_aNamespace.evaluate (context, this).getString ();
          if (!attUri.equals (""))
          {
            context.m_aErrorHandler.error ("Can't put attribute '" +
                                           attName +
                                           "' into the non-null namespace '" +
                                           attUri +
                                           "'",
                                           m_sPublicID,
                                           m_sSystemID,
                                           lineNo,
                                           colNo);
            return CSTX.PR_CONTINUE; // if the errorHandler returns
          }
        }
      }

      if (m_aSelect != null)
      {
        context.m_aEmitter.addAttribute (attUri,
                                      attName,
                                      attLocal,
                                      m_aSelect.evaluate (context, this).getStringValue (),
                                      this);
      }
      else
      {
        m_aLocalFieldStack.push (attUri);
        m_aLocalFieldStack.push (attLocal);
        m_aLocalFieldStack.push (attName);
      }

      return CSTX.PR_CONTINUE;
    }

    /**
     * Called only if there's no <code>select</code> attribute; create a result
     * attribute from the contents of the element.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      final String attName = (String) m_aLocalFieldStack.pop ();
      final String attLocal = (String) m_aLocalFieldStack.pop ();
      final String attUri = (String) m_aLocalFieldStack.pop ();
      context.popEmitter ();
      context.m_aEmitter.addAttribute (attUri, attName, attLocal, m_aStrEmitter.getBuffer ().toString (), this);
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      theCopy._init ();
      if (m_aName != null)
        theCopy.m_aName = m_aName.deepCopy (copies);
      if (m_aNamespace != null)
        theCopy.m_aNamespace = m_aNamespace.deepCopy (copies);
      if (m_aSelect != null)
        theCopy.m_aSelect = m_aSelect.deepCopy (copies);
    }
  }
}
