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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import net.sf.joost.CSTX;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>element</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.9 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class ElementFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  // Constructor
  public ElementFactory ()
  {
    attrNames.add ("name");
    attrNames.add ("namespace");
  }

  /** @return <code>"element"</code> */
  @Override
  public String getName ()
  {
    return "element";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final AbstractTree nameAVT = parseRequiredAVT (qName, attrs, "name", context);

    final AbstractTree namespaceAVT = parseAVT (attrs.getValue ("namespace"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAVT, namespaceAVT);
  }

  /** Represents an instance of the <code>element</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private AbstractTree m_aName, m_aNamespace;
    private final Hashtable <String, String> nsSet;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final AbstractTree name,
                        final AbstractTree namespace)
    {
      super (qName, parent, context, true);
      this.nsSet = new Hashtable<> (context.nsSet);
      this.m_aName = name;
      this.m_aNamespace = namespace;
    }

    /**
     * Emits an startElement event to the result stream.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      // determine qualified name, local name and namespace uri
      String elName, elUri, elLocal;
      elName = m_aName.evaluate (context, this).getString ();
      final int colon = elName.indexOf (':');
      if (colon != -1)
      { // prefixed name
        final String prefix = elName.substring (0, colon);
        elLocal = elName.substring (colon + 1);
        if (m_aNamespace != null)
        { // namespace attribute present
          elUri = m_aNamespace.evaluate (context, this).getString ();
          if (elUri.equals (""))
          {
            context.m_aErrorHandler.fatalError ("Can't create element '" +
                                                elName +
                                                "' in the null namespace",
                                                m_sPublicID,
                                                m_sSystemID,
                                                lineNo,
                                                colNo);
            return CSTX.PR_CONTINUE; // if the errorHandler returns
          }
        }
        else
        {
          // look into the set of in-scope namespaces
          // (of the transformation sheet)
          elUri = nsSet.get (prefix);
          if (elUri == null)
          {
            context.m_aErrorHandler.fatalError ("Attempt to create element '" +
                                                elName +
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
        elLocal = elName;
        if (m_aNamespace != null) // namespace attribute present
          elUri = m_aNamespace.evaluate (context, this).getString ();
        else
        {
          // no namespace attribute, see above
          elUri = nsSet.get ("");
          if (elUri == null)
            elUri = "";
        }
      }

      context.m_aEmitter.startElement (elUri, elLocal, elName, new AttributesImpl (), null, this);
      m_aLocalFieldStack.push (elUri);
      m_aLocalFieldStack.push (elLocal);
      m_aLocalFieldStack.push (elName);
      return CSTX.PR_CONTINUE;
    }

    /**
     * Emits an endElement event to the result stream.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      final String elName = (String) m_aLocalFieldStack.pop ();
      final String elLocal = (String) m_aLocalFieldStack.pop ();
      final String elUri = (String) m_aLocalFieldStack.pop ();
      context.m_aEmitter.endElement (elUri, elLocal, elName, m_aNodeEnd);
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aName != null)
        theCopy.m_aName = m_aName.deepCopy (copies);
      if (m_aNamespace != null)
        theCopy.m_aNamespace = m_aNamespace.deepCopy (copies);
    }
  }
}
