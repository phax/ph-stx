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
package net.sf.joost.stx;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.xml.sax.Attributes;

import net.sf.joost.stx.helpers.IMutableAttributes;
import net.sf.joost.stx.helpers.MutableAttributesImpl;

/**
 * SAXEvent stores all information attached to an incoming SAX event, it is the
 * representation of a node in STX.
 *
 * @version $Revision: 1.20 $ $Date: 2007/11/25 14:18:01 $
 * @author Oliver Becker
 */
public final class SAXEvent
{
  public static final int ROOT = 0;
  public static final int ELEMENT = 1;
  public static final int TEXT = 2;
  public static final int CDATA = 3;
  public static final int PI = 4;
  public static final int COMMENT = 5;
  public static final int ATTRIBUTE = 6;
  // needed in buffers:
  public static final int ELEMENT_END = 7;
  public static final int MAPPING = 8;
  public static final int MAPPING_END = 9;

  public int m_nType;
  public String m_sURI;
  public String m_sLocalName;
  public String m_sQName; // PI->target, MAPPING->prefix
  public IMutableAttributes m_aAttrs;
  public Map <String, String> m_aNamespaces;
  public String m_sValue = "";
  // PI->data, MAPPING->uri, TEXT, ATTRIBUTES as usual
  // ELEMENT->text look-ahead
  public boolean m_bHasChildNodes = false;

  /** contains the position counters */
  private Map <Object, Counter> m_aPosHash;

  private SAXEvent ()
  {}

  //
  // Factory methods
  //

  /**
   * Create a new element node
   *
   * @param uri
   * @param lName
   * @param qName
   * @param attrs
   * @param mutable
   * @param inScopeNamespaces
   * @return new {@link SAXEvent}
   */
  @Nonnull
  public static SAXEvent newElement (final String uri,
                                     final String lName,
                                     final String qName,
                                     @Nullable final Attributes attrs,
                                     final boolean mutable,
                                     final Map <String, String> inScopeNamespaces)
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = attrs != null ? ELEMENT : ELEMENT_END;
    event.m_sURI = uri;
    event.m_sLocalName = lName;
    event.m_sQName = qName;

    if (attrs != null)
      event.m_aAttrs = new MutableAttributesImpl (attrs);

    event.m_aNamespaces = inScopeNamespaces;
    event.m_bHasChildNodes = false;
    event.m_sValue = "";
    return event;
  }

  /** Create a new text node */
  public static SAXEvent newText (final String value)
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = TEXT;
    event.m_sValue = value;
    return event;
  }

  /** Create a new CDATA node */
  public static SAXEvent newCDATA (final String value)
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = CDATA;
    event.m_sValue = value;
    return event;
  }

  /** Create a root node */
  public static SAXEvent newRoot ()
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = ROOT;
    event.enableChildNodes (true);
    return event;
  }

  /** Create a new comment node */
  public static SAXEvent newComment (final String value)
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = COMMENT;
    event.m_sValue = value;
    return event;
  }

  /** Create a new processing instruction node */
  public static SAXEvent newPI (final String target, final String data)
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = PI;
    event.m_sQName = target;
    event.m_sValue = data;
    return event;
  }

  /** Create a new attribute node */
  public static SAXEvent newAttribute (final String uri, final String lname, final String qName, final String value)
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = ATTRIBUTE;
    event.m_sURI = uri;
    event.m_sLocalName = lname;
    event.m_sQName = qName;
    event.m_sValue = value;
    return event;
  }

  /** Create a new attribute node */
  public static SAXEvent newAttribute (final Attributes attrs, final int index)
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = ATTRIBUTE;
    event.m_sURI = attrs.getURI (index);
    event.m_sLocalName = attrs.getLocalName (index);
    event.m_sQName = attrs.getQName (index);
    event.m_sValue = attrs.getValue (index);
    return event;
  }

  /** Create a new representation for a namespace mapping */
  public static SAXEvent newMapping (final String prefix, final String uri)
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = uri != null ? MAPPING : MAPPING_END;
    event.m_sQName = prefix;
    event.m_sValue = uri;
    return event;
  }

  /**
   * Enables the counting of child nodes.
   *
   * @param bHasChildNodes
   *        <code>true</code>, if there are really child nodes;
   *        <code>false</code>, if only the counting has to be supported (e.g.
   *        in <code>stx:process-buffer</code>)
   */
  public void enableChildNodes (final boolean bHasChildNodes)
  {
    if (bHasChildNodes)
    {
      m_aPosHash = new HashMap<> ();
      this.m_bHasChildNodes = true;
    }
    else
      if (m_aPosHash == null)
        m_aPosHash = new HashMap<> ();
  }

  // *******************************************************************

  /**
   * This class replaces java.lang.Long for counting because I need to change
   * the wrapped value and want to avoid the creation of a new object in each
   * increment. Is this really better (faster)?
   */
  private final class Counter
  {
    public long m_nValue;

    public Counter ()
    {
      m_nValue = 1;
    }
  }

  // *******************************************************************

  /**
   * This class acts as a wrapper for a pair of {@link String} objects. Such a
   * pair is needed as a key for a hashtable.
   */
  private static final class DoubleString
  {
    private final String m_s1, m_s2;
    private final int m_nHashValue;

    public DoubleString (final String s1, final String s2)
    {
      this.m_s1 = s1;
      this.m_s2 = s2;
      m_nHashValue = (s1.hashCode () << 1) ^ s2.hashCode ();
    }

    @Override
    public int hashCode ()
    {
      return m_nHashValue;
    }

    @Override
    public boolean equals (final Object o)
    {
      if (o == this)
        return true;
      if (o == null || !getClass ().equals (o.getClass ()))
        return false;
      final DoubleString ds = (DoubleString) o;
      return m_s1.equals (ds.m_s1) && m_s2.equals (ds.m_s2);
    }
  }

  // *******************************************************************

  private static final DoubleString GENERIC_ELEMENT = new DoubleString ("*", "*");

  /**
   * Increments the associated counters for an element.
   */
  public void countElement (final String uri, final String lName)
  {
    final Object [] keys = { "node()",
                             GENERIC_ELEMENT,
                             new DoubleString (uri, lName),
                             new DoubleString ("*", lName),
                             new DoubleString (uri, "*") };
    _countPosition (keys);
  }

  /**
   * Increments the associated counters for a text node.
   */
  public void countText ()
  {
    final String [] keys = { "node()", "text()" };
    _countPosition (keys);
  }

  /**
   * Increments the associated counters for a text CDATA node.
   */
  public void countCDATA ()
  {
    final String [] keys = { "node()", "text()", "cdata()" };
    _countPosition (keys);
  }

  /**
   * Increments the associated counters for a comment node.
   */
  public void countComment ()
  {
    final String [] keys = { "node()", "comment()" };
    _countPosition (keys);
  }

  private static final DoubleString GENERIC_PI = new DoubleString ("pi()", "");

  /**
   * Increment the associated counters for a processing instruction node.
   */
  public void countPI (final String target)
  {
    final Object [] keys = { "node()", GENERIC_PI, new DoubleString ("pi()", target) };
    _countPosition (keys);
  }

  /**
   * Performs the real counting. Will be used by the count* functions.
   */
  private void _countPosition (final Object [] keys)
  {
    Counter c;
    for (final Object key : keys)
    {
      c = m_aPosHash.get (key);
      if (c == null)
        m_aPosHash.put (key, new Counter ());
      else
        c.m_nValue++;
      // posHash.put(keys[i], new Long(l.longValue()+1));
    }
  }

  public long getPositionOf (final String uri, final String lName)
  {
    final Counter c = m_aPosHash.get (new DoubleString (uri, lName));
    if (c == null)
    {
      // Shouldn't happen
      throw new NullPointerException ();
    }
    return c.m_nValue;
  }

  public long getPositionOfNode ()
  {
    final Counter c = m_aPosHash.get ("node()");
    if (c == null)
    {
      // Shouldn't happen
      throw new NullPointerException ();
    }
    return c.m_nValue;
  }

  public long getPositionOfText ()
  {
    final Counter c = m_aPosHash.get ("text()");
    if (c == null)
    {
      // Shouldn't happen
      throw new NullPointerException ();
    }
    return c.m_nValue;
  }

  public long getPositionOfCDATA ()
  {
    final Counter c = m_aPosHash.get ("cdata()");
    if (c == null)
    {
      // Shouldn't happen
      throw new NullPointerException ();
    }
    return c.m_nValue;
  }

  public long getPositionOfComment ()
  {
    final Counter c = m_aPosHash.get ("comment()");
    if (c == null)
    {
      // Shouldn't happen
      throw new NullPointerException ();
    }
    return c.m_nValue;
  }

  public long getPositionOfPI (final String target)
  {
    final Counter c = m_aPosHash.get (new DoubleString ("pi()", target));
    if (c == null)
    {
      // Shouldn't happen
      throw new NullPointerException ();
    }
    return c.m_nValue;
  }

  @Override
  public Object clone ()
  {
    final SAXEvent event = new SAXEvent ();
    event.m_nType = m_nType;
    event.m_sQName = m_sQName;
    return event;
  }

  //
  // for debugging
  //
  @Override
  public String toString ()
  {
    final String ret = "SAXEvent ";
    switch (m_nType)
    {
      case ROOT:
        return ret + "/";
      case ELEMENT:
        return ret + "<" + m_sQName + ">";
      case ELEMENT_END:
        return ret + "</" + m_sQName + ">";
      case TEXT:
        return ret + "'" + m_sValue + "'";
      case CDATA:
        return ret + "<![CDATA[" + m_sValue + "]]>";
      case COMMENT:
        return ret + "<!--" + m_sValue + "-->";
      case PI:
        return ret + "<?" + m_sQName + " " + m_sValue + "?>";
      case ATTRIBUTE:
        return ret + m_sQName + "='" + m_sValue + "'";
      case MAPPING:
        return "xmlns:" + m_sQName + "=" + m_sValue;
      default:
        return "SAXEvent ???";
    }
  }
}
