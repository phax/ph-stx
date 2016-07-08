/*
 * $Id: SAXEvent.java,v 1.20 2007/11/25 14:18:01 obecker Exp $
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
 * Contributor(s): Thomas Behrends.
 */

package net.sf.joost.stx;

import java.util.HashMap;
import java.util.Hashtable;

import net.sf.joost.stx.helpers.MutableAttributes;
import net.sf.joost.stx.helpers.MutableAttributesImpl;

import org.xml.sax.Attributes;


/** 
 * SAXEvent stores all information attached to an incoming SAX event,
 * it is the representation of a node in STX.
 * @version $Revision: 1.20 $ $Date: 2007/11/25 14:18:01 $
 * @author Oliver Becker
 */
final public class SAXEvent
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

   public int type;
   public String uri;
   public String lName;
   public String qName; // PI->target, MAPPING->prefix
   public MutableAttributes attrs;
   public Hashtable namespaces;
   public String value = ""; 
      // PI->data, MAPPING->uri, TEXT, ATTRIBUTES as usual
      // ELEMENT->text look-ahead
   public boolean hasChildNodes = false;

   /** contains the position counters */
   private HashMap posHash;



   //
   // private constructor
   //

   private SAXEvent()
   { }



   //
   // Factory methods
   //

   /** Create a new element node */
   public static SAXEvent newElement(String uri, String lName, String qName,
                                     Attributes attrs, boolean mutable,
                                     Hashtable inScopeNamespaces)
   {
      SAXEvent event = new SAXEvent();
      event.type = attrs != null ? ELEMENT : ELEMENT_END;
      event.uri = uri;
      event.lName = lName;
      event.qName = qName;
      
      if (attrs != null)
         event.attrs = new MutableAttributesImpl(attrs);

      event.namespaces = inScopeNamespaces;
      event.hasChildNodes = false;
      event.value = "";
      return event;
   }

   /** Create a new text node */
   public static SAXEvent newText(String value)
   {
      SAXEvent event = new SAXEvent();
      event.type = TEXT;
      event.value = value;
      return event;
   }


   /** Create a new CDATA node */
   public static SAXEvent newCDATA(String value)
   {
      SAXEvent event = new SAXEvent();
      event.type = CDATA;
      event.value = value;
      return event;
   }


   /** Create a root node */
   public static SAXEvent newRoot()
   {
      SAXEvent event = new SAXEvent();
      event.type = ROOT;
      event.enableChildNodes(true);
      return event;
   }


   /** Create a new comment node */
   public static SAXEvent newComment(String value)
   {
      SAXEvent event = new SAXEvent();
      event.type = COMMENT;
      event.value = value;
      return event;
   }


   /** Create a new processing instruction node */
   public static SAXEvent newPI(String target, String data)
   {
      SAXEvent event = new SAXEvent();
      event.type = PI;
      event.qName = target;
      event.value = data;
      return event;
   }

   /** Create a new attribute node */
   public static SAXEvent newAttribute(String uri, String lname, String qName, 
                                       String value)
   {
      SAXEvent event = new SAXEvent();
      event.type = ATTRIBUTE;
      event.uri = uri;
      event.lName = lname;
      event.qName = qName;
      event.value = value;
      return event;
   }

   /** Create a new attribute node */
   public static SAXEvent newAttribute(Attributes attrs, int index)
   {
      SAXEvent event = new SAXEvent();
      event.type = ATTRIBUTE;
      event.uri = attrs.getURI(index);
      event.lName = attrs.getLocalName(index);
      event.qName = attrs.getQName(index);
      event.value = attrs.getValue(index);
      return event;
   }

   /** Create a new representation for a namespace mapping */
   public static SAXEvent newMapping(String prefix, String uri)
   {
      SAXEvent event = new SAXEvent();
      event.type = uri != null ? MAPPING : MAPPING_END;
      event.qName = prefix;
      event.value = uri;
      return event;
   }



   /**
    * Enables the counting of child nodes.
    * @param hasChildNodes <code>true</code>, if there are really child nodes;
    *                      <code>false</code>, if only the counting has to be
    *                      supported (e.g. in <code>stx:process-buffer</code>)
    */
   public void enableChildNodes(boolean hasChildNodes)
   {
      if (hasChildNodes) {
         posHash = new HashMap();
         this.hasChildNodes = true;
      }
      else
         if (posHash == null)
            posHash = new HashMap();
   }



   // *******************************************************************

   /** 
    * This class replaces java.lang.Long for counting because I need to 
    * change the wrapped value and want to avoid the creation of a new
    * object in each increment. Is this really better (faster)?
    */
   private final class Counter
   {
      public long value;
      public Counter()
      {
         value = 1;
      }
   }

   // *******************************************************************

   /**
    * This class acts as a wrapper for a pair of {@link String} objects.
    * Such a pair is needed as a key for a hashtable.
    */
   private static final class DoubleString
   {
      private String s1, s2;
      private int hashValue;
      public DoubleString(String s1, String s2)
      {
         this.s1 = s1;
         this.s2 = s2;
         hashValue = (s1.hashCode() << 1) ^ s2.hashCode();
      }

      public int hashCode()
      {
         return hashValue;
      }

      public boolean equals(Object o)
      {
         if (!(o instanceof DoubleString))
            return false;
         DoubleString ds = (DoubleString)o;
         return s1.equals(ds.s1) && s2.equals(ds.s2);
      }
   }

   // *******************************************************************


   private static final DoubleString GENERIC_ELEMENT = 
      new DoubleString("*", "*");
   /**
    * Increments the associated counters for an element.
    */
   public void countElement(String uri, String lName)
   {
      Object[] keys = { "node()", GENERIC_ELEMENT,  
                        new DoubleString(uri, lName),
                        new DoubleString("*", lName),
                        new DoubleString(uri, "*") };
      _countPosition(keys);
   }

   /**
    * Increments the associated counters for a text node.
    */
   public void countText()
   {
      String[] keys = { "node()", "text()" };
      _countPosition(keys);
   }

   /**
    * Increments the associated counters for a text CDATA node.
    */
   public void countCDATA()
   {
      String[] keys = { "node()", "text()", "cdata()" };
      _countPosition(keys);
   }

   /**
    * Increments the associated counters for a comment node.
    */
   public void countComment()
   {
      String[] keys = { "node()", "comment()" };
      _countPosition(keys);
   }

   private static final DoubleString GENERIC_PI = 
      new DoubleString("pi()", "");
   /**
    * Increment the associated counters for a processing instruction node.
    */
   public void countPI(String target)
   {
      Object[] keys = { "node()", GENERIC_PI, 
                        new DoubleString("pi()", target) };
      _countPosition(keys);
   }

   /**
    * Performs the real counting. Will be used by the count* functions.
    */
   private void _countPosition(Object[] keys)
   {
      Counter c;
      for (int i=0; i<keys.length; i++) {
         c = (Counter)posHash.get(keys[i]);
         if (c == null)
            posHash.put(keys[i], new Counter());
         else
            c.value++;
         // posHash.put(keys[i], new Long(l.longValue()+1));
      }
   }


   public long getPositionOf(String uri, String lName)
   {
      Counter c = (Counter)posHash.get(new DoubleString(uri, lName));
      if (c == null) {
         // Shouldn't happen
         throw new NullPointerException();
      }
      return c.value;
   }

   public long getPositionOfNode()
   {
      Counter c = (Counter)posHash.get("node()");
      if (c == null) {
         // Shouldn't happen
         throw new NullPointerException();
      }
      return c.value;
   }

   public long getPositionOfText()
   {
      Counter c = (Counter)posHash.get("text()");
      if (c == null) {
         // Shouldn't happen
         throw new NullPointerException();
      }
      return c.value;
   }

   public long getPositionOfCDATA()
   {
      Counter c = (Counter)posHash.get("cdata()");
      if (c == null) {
         // Shouldn't happen
         throw new NullPointerException();
      }
      return c.value;
   }

   public long getPositionOfComment()
   {
      Counter c = (Counter)posHash.get("comment()");
      if (c == null) {
         // Shouldn't happen
         throw new NullPointerException();
      }
      return c.value;
   }

   public long getPositionOfPI(String target)
   {
      Counter c = (Counter)posHash.get(new DoubleString("pi()", target));
      if (c == null) {
         // Shouldn't happen
         throw new NullPointerException();
      }
      return c.value;
   }

   public Object clone() {
       SAXEvent event = new SAXEvent();
       event.type = type;
       event.qName = qName;
       return event;
   }

   //
   // for debugging
   //
   public String toString()
   {
      String ret = "SAXEvent ";
      switch (type) {
      case ROOT:
         return ret + "/";
      case ELEMENT:
         return ret + "<" + qName + ">";
      case ELEMENT_END:
         return ret + "</" + qName + ">";
      case TEXT:
         return ret + "'" + value + "'";
      case CDATA:
         return ret + "<![CDATA[" + value + "]]>";
      case COMMENT:
         return ret + "<!--" + value + "-->";
      case PI:
         return ret + "<?" + qName + " " + value + "?>";
      case ATTRIBUTE:
         return ret + qName + "='" + value + "'";
      case MAPPING:
         return "xmlns:" + qName + "=" + value;
      default:
         return "SAXEvent ???";
      }
   }
}
