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
package net.sf.joost.grammar.cup;

import java.util.HashMap;
import java.util.List;

public class SyntaxTreeDFS
{
  public static interface ElementHandler
  {
    public void handle (XMLElement parent, List <XMLElement> children);
  }

  public static abstract class AbstractVisitor implements Visitor
  {
    private final HashMap <String, ElementHandler> preMap = new HashMap<> ();
    private final HashMap <String, ElementHandler> postMap = new HashMap<> ();

    public abstract void defaultPre (XMLElement element, List <XMLElement> children);

    public abstract void defaultPost (XMLElement element, List <XMLElement> children);

    @Override
    public void preVisit (final XMLElement element)
    {
      final ElementHandler handler = preMap.get (element.tagname);
      if (handler == null)
      {
        defaultPre (element, element.getChildren ());
      }
      else
        handler.handle (element, element.getChildren ());
    }

    @Override
    public void postVisit (final XMLElement element)
    {
      final ElementHandler handler = postMap.get (element.tagname);
      if (handler == null)
      {
        defaultPost (element, element.getChildren ());
      }
      else
        handler.handle (element, element.getChildren ());
    }

    public void registerPreVisit (final String s, final ElementHandler h)
    {
      preMap.put (s, h);
    }

    public void registerPostVisit (final String s, final ElementHandler h)
    {
      postMap.put (s, h);
    }
  }

  public static interface Visitor
  {
    public void preVisit (XMLElement element);

    public void postVisit (XMLElement element);
  }

  public static void dfs (final XMLElement element, final Visitor visitor)
  {
    visitor.preVisit (element);
    for (final XMLElement el : element.getChildren ())
    {
      dfs (el, visitor);
    }
    visitor.postVisit (element);
  }
}
