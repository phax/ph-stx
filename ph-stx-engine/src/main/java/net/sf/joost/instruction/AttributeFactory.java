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
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>attribute</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 2.8 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

final public class AttributeFactory extends FactoryBase
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
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    final Tree selectExpr = parseExpr (attrs.getValue ("select"), context);

    final Tree nameAVT = parseRequiredAVT (qName, attrs, "name", context);

    final Tree namespaceAVT = parseAVT (attrs.getValue ("namespace"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAVT, namespaceAVT, selectExpr);
  }

  /** Represents an instance of the <code>attribute</code> element. */
  final public class Instance extends NodeBase
  {
    private Tree name, namespace, select;
    private final Hashtable <String, String> nsSet;
    private StringEmitter strEmitter;

    protected Instance (final String elementName,
                        final NodeBase parent,
                        final ParseContext context,
                        final Tree name,
                        final Tree namespace,
                        final Tree select)
    {
      super (elementName,
             parent,
             context,
             // this element must be empty if there is a select attribute
             select == null);
      this.nsSet = (Hashtable <String, String>) context.nsSet.clone ();
      this.name = name;
      this.namespace = namespace;
      this.select = select;
      init ();
    }

    private void init ()
    {
      strEmitter = new StringEmitter (new StringBuffer (), "('" + qName + "' started in line " + lineNo + ")");
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
      if (context.emitter.isEmitterActive (strEmitter))
      {
        context.errorHandler.error ("Can't create nested attribute", publicId, systemId, lineNo, colNo);
        return 0; // if the errorHandler returns
      }
      if (select == null)
      {
        // contents and end instruction present
        super.process (context);
        strEmitter.getBuffer ().setLength (0);
        context.pushEmitter (strEmitter);
      }

      String attName, attUri, attLocal;
      // determine attribute name
      attName = name.evaluate (context, this).getString ();
      final int colon = attName.indexOf (':');
      if (colon != -1)
      { // prefixed name
        final String prefix = attName.substring (0, colon);
        attLocal = attName.substring (colon + 1);
        if (namespace != null)
        { // namespace attribute present
          attUri = namespace.evaluate (context, this).getString ();
          if (attUri.equals (""))
          {
            context.errorHandler.error ("Can't put attribute '" +
                                        attName +
                                        "' into the null namespace",
                                        publicId,
                                        systemId,
                                        lineNo,
                                        colNo);
            return CSTX.PR_CONTINUE; // if the errorHandler returns
          }
        }
        else
        { // no namespace attribute
          // look into the set of in-scope namespaces
          // (of the transformation sheet)
          attUri = nsSet.get (prefix);
          if (attUri == null)
          {
            context.errorHandler.error ("Attempt to create attribute '" +
                                        attName +
                                        "' with undeclared prefix '" +
                                        prefix +
                                        "'",
                                        publicId,
                                        systemId,
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
        if (namespace != null)
        { // namespace attribute present
          attUri = namespace.evaluate (context, this).getString ();
          if (!attUri.equals (""))
          {
            context.errorHandler.error ("Can't put attribute '" +
                                        attName +
                                        "' into the non-null namespace '" +
                                        attUri +
                                        "'",
                                        publicId,
                                        systemId,
                                        lineNo,
                                        colNo);
            return CSTX.PR_CONTINUE; // if the errorHandler returns
          }
        }
      }

      if (select != null)
      {
        context.emitter.addAttribute (attUri,
                                      attName,
                                      attLocal,
                                      select.evaluate (context, this).getStringValue (),
                                      this);
      }
      else
      {
        localFieldStack.push (attUri);
        localFieldStack.push (attLocal);
        localFieldStack.push (attName);
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
      final String attName = (String) localFieldStack.pop ();
      final String attLocal = (String) localFieldStack.pop ();
      final String attUri = (String) localFieldStack.pop ();
      context.popEmitter ();
      context.emitter.addAttribute (attUri, attName, attLocal, strEmitter.getBuffer ().toString (), this);
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      theCopy.init ();
      if (name != null)
        theCopy.name = name.deepCopy (copies);
      if (namespace != null)
        theCopy.namespace = namespace.deepCopy (copies);
      if (select != null)
        theCopy.select = select.deepCopy (copies);
    }

  }
}
