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
package java_cup.runtime;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SyntaxTreeXPath
{
  public static List <XMLElement> query (String query, final XMLElement element)
  {
    if (query.startsWith ("/"))
      query = query.substring (1);
    return query0 (new LinkedList <> (Arrays.asList (query.split ("/"))), 0, element, 0);
  }

  private static List <XMLElement> query0 (final List <String> q,
                                           final int idx,
                                           final XMLElement element,
                                           final int seq)
  {

    if (q.get (idx).isEmpty ())
    { // match deeper descendant q[1]
      return matchDeeperDescendant (q, idx + 1, element, seq);
    }
    final List <XMLElement> l = new LinkedList <> ();

    if (!match (q.get (idx), element, seq))
      return new LinkedList ();
    if (q.size () - 1 == idx)
      return singleton (element);
    final List <XMLElement> children = element.getChildren ();
    for (int i = 0; i < children.size (); i++)
    {
      final XMLElement child = children.get (i);
      l.addAll (query0 (q, idx + 1, child, i));
    }
    return l;
  }

  private static List <XMLElement> matchDeeperDescendant (final List <String> query,
                                                          final int idx,
                                                          final XMLElement element,
                                                          final int seq)
  {
    if (query.size () <= idx)
      return singleton (element);
    final boolean matches = match (query.get (idx), element, seq);
    final List <XMLElement> l = new LinkedList <> ();
    final List <XMLElement> children = element.getChildren ();
    if (matches)
      return query0 (query, idx, element, seq);
    for (int i = 0; i < children.size (); i++)
    {
      final XMLElement child = children.get (i);
      l.addAll (matchDeeperDescendant (query, idx, child, i));
    }
    return l;
  }

  private static boolean match (final String m, final XMLElement elem, final int seq)
  {
    // System.out.println("Matching "+elem.tagname+" with "+m);
    boolean result = true;
    final String [] name = m.split ("\\[");
    final String [] tag = name[0].split ("\\*");
    if (tag[0].isEmpty ())
    { // start is wildcard
      if (tag.length > 2)
        result &= elem.tagname.contains (tag[1]);
      else
        if (tag.length == 2)
          result &= elem.tagname.endsWith (tag[1]);
        else
          result &= false;
    }
    else
    { // match with start
      if (tag.length == 2)
        result &= elem.tagname.startsWith (tag[1]);
      else
        result = elem.tagname.equals (tag[0]);
    }
    for (int i = 1; i < name.length; i++)
    {
      String predicate = name[i];
      if (!predicate.endsWith ("]"))
        return false;
      predicate = predicate.substring (0, predicate.length () - 1);

      if (predicate.startsWith ("@"))
      {
        if (predicate.substring (1).startsWith ("variant"))
          if ((elem instanceof XMLElement.NonTerminal) &&
              Integer.parseInt (predicate.substring (9)) == ((XMLElement.NonTerminal) elem).getVariant ())
            result &= true;
          else
            return false;
        else
          return false;
      }
      else
        if (predicate.matches ("\\d+"))
        {
          result &= Integer.parseInt (predicate) == seq;
        }
        else
          return false; // submatch
    }
    return result;
  }

  private static List <XMLElement> singleton (final XMLElement elem)
  {
    final LinkedList <XMLElement> l = new LinkedList <> ();
    l.add (elem);
    return l;
  }
}
