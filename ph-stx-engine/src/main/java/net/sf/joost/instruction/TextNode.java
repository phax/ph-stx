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

import org.xml.sax.SAXException;

import net.sf.joost.CSTX;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

/**
 * Instances created by this factory represent text nodes in the transformation
 * sheet
 *
 * @version $Revision: 2.3 $ $Date: 2006/02/27 19:47:18 $
 * @author Oliver Becker
 */

public class TextNode extends AbstractNodeBase
{
  private final String string;

  public TextNode (final String s, final AbstractNodeBase parent, final ParseContext context)
  {
    super ("", parent, context, false);
    string = s;
  }

  /**
   * Emit the text of this node to the result stream
   */
  @Override
  public short process (final Context context) throws SAXException
  {
    context.m_aEmitter.characters (string.toCharArray (), 0, string.length (), this);
    return CSTX.PR_CONTINUE;
  }

  /**
   * @return <code>true</code> if this text node contains only white space
   *         characters, otherwise <code>false</code>
   */
  public boolean isWhitespaceNode ()
  {
    return string.trim ().length () == 0;
  }

  /**
   * @return the text content of this node.
   */
  public String getContents ()
  {
    return string;
  }
}
