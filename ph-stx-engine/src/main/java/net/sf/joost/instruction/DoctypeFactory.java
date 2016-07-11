/*
 * $Id: DoctypeFactory.java,v 1.4 2008/10/04 17:13:14 obecker Exp $
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

import net.sf.joost.CSTX;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>doctype</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 1.4 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public class DoctypeFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  public DoctypeFactory ()
  {
    attrNames = new HashSet ();
    attrNames.add ("name");
    attrNames.add ("public-id");
    attrNames.add ("system-id");
  }

  /** @return <code>"doctype"</code> */
  @Override
  public String getName ()
  {
    return "doctype";
  }

  @Override
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    final Tree nameAVT = parseRequiredAVT (qName, attrs, "name", context);

    final Tree publicAVT = parseAVT (attrs.getValue ("public-id"), context);
    final Tree systemAVT = parseAVT (attrs.getValue ("system-id"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAVT, publicAVT, systemAVT);
  }

  /** Represents an instance of the <code>doctype</code> element. */
  public class Instance extends NodeBase
  {
    private Tree nameAVT, publicAVT, systemAVT;

    public Instance (final String qName,
                     final NodeBase parent,
                     final ParseContext context,
                     final Tree nameAVT,
                     final Tree publicAVT,
                     final Tree systemAVT)
    {
      super (qName,
             parent,
             context,
             // current restriction: this element must be empty
             false);
      this.nameAVT = nameAVT;
      this.publicAVT = publicAVT;
      this.systemAVT = systemAVT;
    }

    /**
     * Create a document type definition.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      context.emitter.createDTD (this,
                                 nameAVT.evaluate (context, this).getStringValue (),
                                 publicAVT != null ? publicAVT.evaluate (context, this).getStringValue () : null,
                                 systemAVT != null ? systemAVT.evaluate (context, this).getStringValue () : null);
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (nameAVT != null)
        theCopy.nameAVT = nameAVT.deepCopy (copies);
      if (publicAVT != null)
        theCopy.publicAVT = publicAVT.deepCopy (copies);
      if (systemAVT != null)
        theCopy.systemAVT = systemAVT.deepCopy (copies);
    }

  }
}
