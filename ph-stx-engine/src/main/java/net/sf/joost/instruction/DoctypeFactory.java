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
 * Factory for <code>doctype</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 1.4 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public class DoctypeFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames = new HashSet<> ();

  public DoctypeFactory ()
  {
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
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final AbstractTree nameAVT = parseRequiredAVT (qName, attrs, "name", context);

    final AbstractTree publicAVT = parseAVT (attrs.getValue ("public-id"), context);
    final AbstractTree systemAVT = parseAVT (attrs.getValue ("system-id"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAVT, publicAVT, systemAVT);
  }

  /** Represents an instance of the <code>doctype</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private AbstractTree m_aNameAVT, m_aPublicAVT, m_aSystemAVT;

    public Instance (final String qName,
                     final AbstractNodeBase parent,
                     final ParseContext context,
                     final AbstractTree nameAVT,
                     final AbstractTree publicAVT,
                     final AbstractTree systemAVT)
    {
      super (qName,
             parent,
             context,
             // current restriction: this element must be empty
             false);
      this.m_aNameAVT = nameAVT;
      this.m_aPublicAVT = publicAVT;
      this.m_aSystemAVT = systemAVT;
    }

    /**
     * Create a document type definition.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      context.m_aEmitter.createDTD (this,
                                    m_aNameAVT.evaluate (context, this).getStringValue (),
                                    m_aPublicAVT != null ? m_aPublicAVT.evaluate (context, this).getStringValue ()
                                                         : null,
                                    m_aSystemAVT != null ? m_aSystemAVT.evaluate (context, this).getStringValue ()
                                                         : null);
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aNameAVT != null)
        theCopy.m_aNameAVT = m_aNameAVT.deepCopy (copies);
      if (m_aPublicAVT != null)
        theCopy.m_aPublicAVT = m_aPublicAVT.deepCopy (copies);
      if (m_aSystemAVT != null)
        theCopy.m_aSystemAVT = m_aSystemAVT.deepCopy (copies);
    }
  }
}
