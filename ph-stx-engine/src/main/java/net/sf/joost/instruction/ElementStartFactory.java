/*
 * $Id: ElementStartFactory.java,v 2.8 2008/10/04 17:13:14 obecker Exp $
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>start-element</code> elements, which are represented by the
 * inner Instance class.
 * 
 * @version $Revision: 2.8 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

final public class ElementStartFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  // Constructor
  public ElementStartFactory ()
  {
    attrNames = new HashSet ();
    attrNames.add ("name");
    attrNames.add ("namespace");
  }

  /* @return <code>"start-element"</code> */
  @Override
  public String getName ()
  {
    return "start-element";
  }

  @Override
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    final Tree nameAVT = parseRequiredAVT (qName, attrs, "name", context);

    final Tree namespaceAVT = parseAVT (attrs.getValue ("namespace"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAVT, namespaceAVT);
  }

  /** Represents an instance of the <code>start-element</code> element. */
  final public class Instance extends NodeBase
  {
    private Tree name, namespace;
    private final Hashtable nsSet;

    protected Instance (final String qName,
                        final NodeBase parent,
                        final ParseContext context,
                        final Tree name,
                        final Tree namespace)
    {
      super (qName, parent, context, false);
      this.nsSet = (Hashtable) context.nsSet.clone ();
      this.name = name;
      this.namespace = namespace;
    }

    /**
     * Emits a startElement event to the emitter.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      String elName, elUri, elLocal;
      elName = name.evaluate (context, this).getString ();
      final int colon = elName.indexOf (':');
      if (colon != -1)
      { // prefixed name
        final String prefix = elName.substring (0, colon);
        elLocal = elName.substring (colon + 1);
        if (namespace != null)
        { // namespace attribute present
          elUri = namespace.evaluate (context, this).getString ();
          if (elUri.equals (""))
          {
            context.errorHandler.fatalError ("Can't create element '" +
                                             elName +
                                             "' in the null namespace",
                                             publicId,
                                             systemId,
                                             lineNo,
                                             colNo);
            return PR_CONTINUE; // if the errorHandler returns
          }
        }
        else
        {
          // look into the set of in-scope namespaces
          // (of the transformation sheet)
          elUri = (String) nsSet.get (prefix);
          if (elUri == null)
          {
            context.errorHandler.fatalError ("Attempt to create element '" +
                                             elName +
                                             "' with undeclared prefix '" +
                                             prefix +
                                             "'",
                                             publicId,
                                             systemId,
                                             lineNo,
                                             colNo);
            return PR_CONTINUE; // if the errorHandler returns
          }
        }
      }
      else
      { // unprefixed name
        elLocal = elName;
        if (namespace != null) // namespace attribute present
          elUri = namespace.evaluate (context, this).getString ();
        else
        {
          // no namespace attribute, see above
          elUri = (String) nsSet.get ("");
          if (elUri == null)
            elUri = "";
        }
      }

      context.emitter.startElement (elUri, elLocal, elName, new AttributesImpl (), null, this);

      return PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (name != null)
        theCopy.name = name.deepCopy (copies);
      if (namespace != null)
        theCopy.namespace = namespace.deepCopy (copies);
    }
  }
}
