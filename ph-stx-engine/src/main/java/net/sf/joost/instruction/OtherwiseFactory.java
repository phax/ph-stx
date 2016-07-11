/*
 * $Id: OtherwiseFactory.java,v 2.4 2008/01/09 11:16:06 obecker Exp $
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.stx.ParseContext;

/**
 * Factory for <code>otherwise</code> elements, which are represented by the
 * inner Instance class.
 * 
 * @version $Revision: 2.4 $ $Date: 2008/01/09 11:16:06 $
 * @author Oliver Becker
 */
public class OtherwiseFactory extends AbstractFactoryBase
{
  /** @return <code>"otherwise"</code> */
  @Override
  public String getName ()
  {
    return "otherwise";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    if (!(parent instanceof ChooseFactory.Instance))
      throw new SAXParseException ("'" + qName + "' must be child of stx:choose", context.locator);

    checkAttributes (qName, attrs, null, context);
    return new Instance (qName, parent, context);
  }

  /** Represents an instance of the <code>otherwise</code> element. */
  public final class Instance extends AbstractNodeBase
  {
    public Instance (final String qName, final AbstractNodeBase parent, final ParseContext context)
    {
      super (qName, parent, context, true);
    }

    @Override
    public boolean compile (final int pass, final ParseContext context) throws SAXException
    {
      if (pass == 0) // following sibling not available yet
        return true;

      mayDropEnd ();
      return false;
    }

    // no special process and processEnd methods required
  }
}
