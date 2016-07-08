/*
 * $Id: PIFactory.java,v 2.9 2008/10/04 17:13:14 obecker Exp $
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

import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>processing-instruction</code> elements, which are
 * represented by the inner Instance class.
 * 
 * @version $Revision: 2.9 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

final public class PIFactory extends FactoryBase
{
  /** allowed attributes for this element */
  private final HashSet attrNames;

  // Constructor
  public PIFactory ()
  {
    attrNames = new HashSet ();
    attrNames.add ("name");
    attrNames.add ("select");
  }

  /* @return <code>"processing-instruction"</code> */
  @Override
  public String getName ()
  {
    return "processing-instruction";
  }

  @Override
  public NodeBase createNode (final NodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    final Tree nameAVT = parseRequiredAVT (qName, attrs, "name", context);

    final Tree selectExpr = parseExpr (attrs.getValue ("select"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, nameAVT, selectExpr);
  }

  /**
   * Represents an instance of the <code>processing-instruction</code> element.
   */
  final public class Instance extends NodeBase
  {
    private Tree name, select;
    private StringEmitter strEmitter;
    private StringBuffer buffer;
    private String piName;

    protected Instance (final String qName,
                        final NodeBase parent,
                        final ParseContext context,
                        final Tree name,
                        final Tree select)
    {
      super (qName,
             parent,
             context,
             // this element must be empty if there is a select attribute
             select == null);
      this.name = name;
      this.select = select;
      init ();
    }

    private void init ()
    {
      buffer = new StringBuffer ();
      strEmitter = new StringEmitter (buffer, "('" + qName + "' started in line " + lineNo + ")");
    }

    /**
     * Activate a StringEmitter for collecting the data of the new PI
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      piName = name.evaluate (context, this).getString ();
      // TO DO: is this piName valid?

      if (select == null)
      {
        super.process (context);
        // check for nesting of this stx:processing-instruction
        if (context.emitter.isEmitterActive (strEmitter))
        {
          context.errorHandler.error ("Can't create nested processing instruction here",
                                      publicId,
                                      systemId,
                                      lineNo,
                                      colNo);
          return PR_CONTINUE; // if the errorHandler returns
        }
        buffer.setLength (0);
        context.pushEmitter (strEmitter);
      }
      else
      {
        String pi = select.evaluate (context, this).getStringValue ();
        int index = pi.lastIndexOf ("?>");
        if (index != -1)
        {
          final StringBuffer piBuf = new StringBuffer (pi);
          do
            piBuf.insert (index + 1, ' ');
          while ((index = pi.lastIndexOf ("?>", --index)) != -1);
          pi = piBuf.toString ();
        }
        context.emitter.processingInstruction (piName, pi, this);
      }
      return PR_CONTINUE;
    }

    /**
     * Emits a processing-instruction to the result stream
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.popEmitter ();
      int index = buffer.length ();
      if (index != 0)
      {
        // are there any "?>" in the pi data?
        final String str = buffer.toString ();
        while ((index = str.lastIndexOf ("?>", --index)) != -1)
          buffer.insert (index + 1, ' ');
      }
      context.emitter.processingInstruction (piName, buffer.toString (), this);
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
      if (select != null)
        theCopy.select = select.deepCopy (copies);
    }

  }
}
