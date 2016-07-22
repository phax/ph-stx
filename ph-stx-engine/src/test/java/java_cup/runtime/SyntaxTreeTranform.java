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
package java_cup.runtime;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class SyntaxTreeTranform
{

  private static class ChainRemover extends SyntaxTreeDFS.AbstractVisitor
  {
    public XMLElement root ()
    {
      return stack.pop ();
    }

    private final Stack <XMLElement> stack = new Stack <> ();

    @Override
    public void defaultPost (final XMLElement arg0, final List <XMLElement> arg1)
    {
      int n = arg1.size ();
      if (n > 1)
      {
        final LinkedList <XMLElement> elems = new LinkedList<> ();
        while (n-- > 0)
          elems.addFirst (stack.pop ());
        final XMLElement ne = new XMLElement.NonTerminal (arg0.getTagname (), 0, elems.toArray (new XMLElement [0]));
        stack.push (ne);
        return;
      }
      // if (n==1){}
      if (n == 0)
        stack.push (arg0);
    }

    @Override
    public void defaultPre (final XMLElement arg0, final List <XMLElement> arg1)
    {}

  }

  public static XMLElement removeUnaryChains (final XMLElement elem)
  {
    final ChainRemover cr = new ChainRemover ();
    SyntaxTreeDFS.dfs (elem, cr);
    return cr.root ();
  }

}
