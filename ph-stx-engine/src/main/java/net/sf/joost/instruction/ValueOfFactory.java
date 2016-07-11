/*
 * $Id: ValueOfFactory.java,v 2.10 2008/10/04 17:13:14 obecker Exp $
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
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Value;

/**
 * Factory for <code>value-of</code> elements, which are represented by the
 * inner Instance class.
 *
 * @version $Revision: 2.10 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class ValueOfFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final HashSet <String> attrNames;

  // Constructor
  public ValueOfFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("select");
    attrNames.add ("separator");
  }

  /** @return <code>"value-of"</code> */
  @Override
  public String getName ()
  {
    return "value-of";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    final AbstractTree selectExpr = parseRequiredExpr (qName, attrs, "select", context);

    final AbstractTree separatorAVT = parseAVT (attrs.getValue ("separator"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, selectExpr, separatorAVT);
  }

  /** Represents an instance of the <code>value-of</code> element. */
  public final class Instance extends AbstractNodeBase
  {
    private AbstractTree select, separator;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final AbstractTree select,
                        final AbstractTree separator)
    {
      super (qName, parent, context, false);
      this.select = select;
      this.separator = separator;
    }

    /**
     * Evaluates the expression given in the select attribute and outputs its
     * value to emitter.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      Value v = select.evaluate (context, this);
      String s;
      if (v.next == null)
        s = v.getStringValue ();
      else
      {
        // create a string from a sequence
        // evaluate separator
        final String sep = (separator != null) ? separator.evaluate (context, this).getString () : " "; // default
                                                                                                        // value
        // use a string buffer for creating the result
        final StringBuffer sb = new StringBuffer ();
        Value nextVal = v.next;
        v.next = null;
        sb.append (v.getStringValue ());
        while (nextVal != null)
        {
          sb.append (sep);
          v = nextVal;
          nextVal = v.next;
          v.next = null;
          sb.append (v.getStringValue ());
        }
        s = sb.toString ();
      }
      context.emitter.characters (s.toCharArray (), 0, s.length (), this);
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (select != null)
        theCopy.select = select.deepCopy (copies);
      if (separator != null)
        theCopy.separator = separator.deepCopy (copies);
    }
  }
}
