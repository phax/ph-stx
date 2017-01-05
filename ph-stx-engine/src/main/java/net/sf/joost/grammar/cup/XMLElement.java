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
package net.sf.joost.grammar.cup;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.joost.grammar.cup.ComplexSymbolFactory.ComplexSymbol;
import net.sf.joost.grammar.cup.ComplexSymbolFactory.Location;

public abstract class XMLElement
{
  public abstract List <XMLElement> selectById (String s);

  public static void dump (final XMLStreamWriter writer,
                           final XMLElement elem,
                           final String... blacklist) throws XMLStreamException
  {
    dump (null, writer, elem, blacklist);
  }

  public static void dump (final ScannerBuffer buffer,
                           final XMLStreamWriter writer,
                           final XMLElement elem,
                           final String... blacklist) throws XMLStreamException
  {
    writer.writeStartDocument ("utf-8", "1.0");
    writer.writeProcessingInstruction ("xml-stylesheet", "href=\"tree.xsl\" type=\"text/xsl\"");
    writer.writeStartElement ("document");

    if (blacklist.length > 0)
    {
      writer.writeStartElement ("blacklist");
      for (final String s : blacklist)
      {
        writer.writeStartElement ("symbol");
        writer.writeCharacters (s);
        writer.writeEndElement ();
      }
      writer.writeEndElement ();
    }

    writer.writeStartElement ("parsetree");
    elem.dump (writer);
    writer.writeEndElement ();

    if (buffer != null)
    {
      writer.writeStartElement ("tokensequence");
      for (final Symbol s : buffer.getBuffered ())
      {
        if (s instanceof ComplexSymbol)
        {
          final ComplexSymbol cs = (ComplexSymbol) s;
          if (cs.value != null)
          {
            writer.writeStartElement ("token");
            writer.writeAttribute ("name", cs.getName ());
            cs.getLeft ().toXML (writer, "left");
            writer.writeCharacters (cs.value + "");
            cs.getRight ().toXML (writer, "right");
            writer.writeEndElement ();
          }
          else
          {
            writer.writeStartElement ("keyword");
            writer.writeAttribute ("left", cs.getLeft () + "");
            writer.writeAttribute ("right", cs.getRight () + "");
            writer.writeCharacters (cs.getName () + "");
            writer.writeEndElement ();
          }
        }
        else
        {
          writer.writeStartElement ("token");
          writer.writeCharacters (s.toString ());
          writer.writeEndElement ();
        }
      }
      writer.writeEndElement ();
    }
    writer.writeEndElement ();
    writer.writeEndDocument ();
    writer.flush ();
    writer.close ();
  }

  protected String tagname;

  public String getTagname ()
  {
    return tagname;
  }

  public abstract Location right ();

  public abstract Location left ();

  protected abstract void dump (XMLStreamWriter writer) throws XMLStreamException;

  public List <XMLElement> getChildren ()
  {
    return new LinkedList<> ();
  }

  public boolean hasChildren ()
  {
    return false;
  }

  public static class NonTerminal extends XMLElement
  {
    @Override
    public boolean hasChildren ()
    {
      return !list.isEmpty ();
    }

    @Override
    public List <XMLElement> getChildren ()
    {
      return list;
    }

    @Override
    public List <XMLElement> selectById (final String s)
    {
      final LinkedList <XMLElement> response = new LinkedList<> ();
      if (tagname.equals (s))
        response.add (this);
      for (final XMLElement e : list)
      {
        final List <XMLElement> selection = e.selectById (s);
        response.addAll (selection);
      }
      return response;
    }

    private final int variant;

    public int getVariant ()
    {
      return variant;
    }

    LinkedList <XMLElement> list;

    public NonTerminal (final String tagname, final int variant, final XMLElement... l)
    {
      this.tagname = tagname;
      this.variant = variant;
      list = new LinkedList<> (Arrays.asList (l));
    }

    @Override
    public Location left ()
    {
      for (final XMLElement e : list)
      {
        final Location loc = e.left ();
        if (loc != null)
          return loc;
      }
      return null;
    }

    @Override
    public Location right ()
    {
      for (final Iterator <XMLElement> it = list.descendingIterator (); it.hasNext ();)
      {
        final Location loc = it.next ().right ();
        if (loc != null)
          return loc;
      }
      return null;
    }

    @Override
    public String toString ()
    {
      if (list.isEmpty ())
      {
        return "<nonterminal id=\"" + tagname + "\" variant=\"" + variant + "\" />";
      }
      String ret = "<nonterminal id=\"" +
                   tagname +
                   "\" left=\"" +
                   left () +
                   "\" right=\"" +
                   right () +
                   "\" variant=\"" +
                   variant +
                   "\">";
      for (final XMLElement e : list)
        ret += e.toString ();
      return ret + "</nonterminal>";
    }

    @Override
    protected void dump (final XMLStreamWriter writer) throws XMLStreamException
    {
      writer.writeStartElement ("nonterminal");
      writer.writeAttribute ("id", tagname);
      writer.writeAttribute ("variant", variant + "");
      // if (!list.isEmpty()){
      Location loc = left ();
      if (loc != null)
        loc.toXML (writer, "left");
      // }
      for (final XMLElement e : list)
        e.dump (writer);
      loc = right ();
      if (loc != null)
        loc.toXML (writer, "right");
      writer.writeEndElement ();
    }
  }

  public static class Error extends XMLElement
  {
    @Override
    public boolean hasChildren ()
    {
      return false;
    }

    @Override
    public List <XMLElement> selectById (final String s)
    {
      return new LinkedList<> ();
    }

    Location l, r;

    public Error (final Location l, final Location r)
    {
      this.l = l;
      this.r = r;
    }

    @Override
    public Location left ()
    {
      return l;
    }

    @Override
    public Location right ()
    {
      return r;
    }

    @Override
    public String toString ()
    {
      return "<error left=\"" + l + "\" right=\"" + r + "\"/>";
    }

    @Override
    protected void dump (final XMLStreamWriter writer) throws XMLStreamException
    {
      writer.writeStartElement ("error");
      writer.writeAttribute ("left", left () + "");
      writer.writeAttribute ("right", right () + "");
      writer.writeEndElement ();
    }
  }

  public static class Terminal extends XMLElement
  {
    @Override
    public boolean hasChildren ()
    {
      return false;
    }

    @Override
    public List <XMLElement> selectById (final String s)
    {
      final List <XMLElement> ret = new LinkedList<> ();
      if (tagname.equals (s))
      {
        ret.add (this);
      }
      return ret;
    }

    Location l, r;
    Object value;

    public Terminal (final Location l, final String symbolname, final Location r)
    {
      this (l, symbolname, null, r);
    }

    public Terminal (final Location l, final String symbolname, final Object i, final Location r)
    {
      this.l = l;
      this.r = r;
      this.value = i;
      this.tagname = symbolname;
    }

    public Object value ()
    {
      return value;
    }

    @Override
    public Location left ()
    {
      return l;
    }

    @Override
    public Location right ()
    {
      return r;
    }

    @Override
    public String toString ()
    {
      return (value == null) ? "<terminal id=\"" + tagname + "\"/>"
                             : "<terminal id=\"" +
                               tagname +
                               "\" left=\"" +
                               l +
                               "\" right=\"" +
                               r +
                               "\">" +
                               value +
                               "</terminal>";
    }

    @Override
    protected void dump (final XMLStreamWriter writer) throws XMLStreamException
    {
      writer.writeStartElement ("terminal");
      writer.writeAttribute ("id", tagname);
      writer.writeAttribute ("left", left () + "");
      writer.writeAttribute ("right", right () + "");
      if (value != null)
        writer.writeCharacters (value + "");
      writer.writeEndElement ();
    }
  }
}
