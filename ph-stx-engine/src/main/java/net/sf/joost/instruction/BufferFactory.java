/*
 * $Id: BufferFactory.java,v 2.6 2007/12/19 10:39:37 obecker Exp $
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
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.BufferEmitter;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>buffer</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.6 $ $Date: 2007/12/19 10:39:37 $
 * @author Oliver Becker
 */

public final class BufferFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final Set <String> attrNames;

  // Constructor
  public BufferFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("name");
  }

  @Override
  public String getName ()
  {
    return "buffer";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final String nameAtt = getRequiredAttribute (qName, attrs, "name", context);

    // Buffers will be treated as special variables -- the same scoping
    // rules apply. To avoid name conflicts with variables the expanded
    // name of a buffer carries a "@" prefix
    final String bufName = "@" + getExpandedName (nameAtt, context);

    checkAttributes (qName, attrs, attrNames, context);

    return new Instance (qName, parent, context, nameAtt, bufName);
  }

  /** Represents an instance of the <code>buffer</code> element. */
  public final class Instance extends AbstractVariableBase
  {
    private final String varName;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final String varName,
                        final String expName)
    {
      super (qName, parent, context, expName, false, true);
      this.varName = varName;
    }

    /**
     * Declares a buffer
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      Hashtable varTable;
      if (parent instanceof AbstractGroupBase) // group scope
        varTable = context.groupVars.get (parent).peek ();
      else
        varTable = context.localVars;

      if (varTable.get (expName) != null)
      {
        context.errorHandler.error ("Buffer '" + varName + "' already declared", publicId, systemId, lineNo, colNo);
        return CSTX.PR_CONTINUE; // if the errorHandler returns
      }

      final BufferEmitter buffer = new BufferEmitter ();
      context.pushEmitter (buffer);
      varTable.put (expName, context.emitter);

      if (varTable == context.localVars)
        parent.declareVariable (expName);

      return CSTX.PR_CONTINUE;
    }

    @Override
    public short processEnd (final Context context) throws SAXException
    {
      ((BufferEmitter) context.popEmitter ()).filled ();
      return super.processEnd (context);
    }
  }
}
