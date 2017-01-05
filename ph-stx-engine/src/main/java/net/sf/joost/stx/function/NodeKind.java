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
package net.sf.joost.stx.function;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.IInstance;

/**
 * The <code>node-kind</code> function.<br>
 * Returns a string representing the node type of its argument
 *
 * @version $Revision: 1.4 $ $Date: 2007/11/25 14:18:00 $
 * @author Oliver Becker
 */
public final class NodeKind implements IInstance
{
  /** @return 1 */
  public int getMinParCount ()
  {
    return 1;
  }

  /** @return 1 */
  public int getMaxParCount ()
  {
    return 1;
  }

  /** @return "node-kind" */
  public String getName ()
  {
    return FunctionFactory.FNSP + "node-kind";
  }

  /** @return <code>true</code> */
  public boolean isConstant ()
  {
    return true;
  }

  public Value evaluate (final Context context, final int top, final AbstractTree args) throws SAXException, EvalException
  {
    final Value v = args.evaluate (context, top);
    if (v.type == Value.EMPTY)
      return v;

    final SAXEvent event = v.getNode ();
    if (event == null)
      throw new EvalException ("The parameter passed to the '" +
                               getName ().substring (FunctionFactory.FNSP.length ()) +
                               "' function must be a node (got " +
                               v +
                               ")");

    switch (event.m_nType)
    {
      case SAXEvent.ROOT:
        return new Value ("document");
      case SAXEvent.ELEMENT:
        return new Value ("element");
      case SAXEvent.ATTRIBUTE:
        return new Value ("attribute");
      case SAXEvent.TEXT:
        return new Value ("text");
      case SAXEvent.CDATA:
        return new Value ("cdata");
      case SAXEvent.PI:
        return new Value ("processing-instruction");
      case SAXEvent.COMMENT:
        return new Value ("comment");
    }
    throw new SAXException ("unexpected node type: " + event);
  }
}
