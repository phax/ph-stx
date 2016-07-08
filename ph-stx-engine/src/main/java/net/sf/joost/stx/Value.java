/*
 * $Id: Value.java,v 1.26 2009/08/21 14:58:42 obecker Exp $
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
 * Contributor(s): Thomas Behrends, Charles Brown.
 */

package net.sf.joost.stx;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.joost.grammar.EvalException;

/**
 * Container class for concrete values (of XPath types)
 * 
 * @version $Revision: 1.26 $ $Date: 2009/08/21 14:58:42 $
 * @author Oliver Becker
 */
public class Value implements Cloneable
{
  // number format for number to string conversion
  private static NumberFormat numberFormat;
  static
  {
    numberFormat = NumberFormat.getInstance (Locale.ENGLISH);
    numberFormat.setGroupingUsed (false);
    numberFormat.setMinimumFractionDigits (0);
    numberFormat.setMaximumFractionDigits (325);
    // The smallest double is 2^-1074, that is 4.9E-324,
    // so 325 digits should be enough
  }

  // value constants
  public final static Value VAL_TRUE = new Value (true);
  public final static Value VAL_FALSE = new Value (false);
  public final static Value VAL_EMPTY = new Value ();
  public final static Value VAL_EMPTY_STRING = new Value ("");
  public final static Value VAL_ZERO = new Value (0);
  public final static Value VAL_NAN = new Value (Double.NaN);

  /** type constant */
  public static final int EMPTY = 0, NODE = 1, BOOLEAN = 2, NUMBER = 3, STRING = 4, OBJECT = 5;

  /** type of this value */
  public int type;

  /** for <code>{@link #type} == {@link #NODE}</code> */
  private SAXEvent event;

  /** for <code>{@link #type} == {@link #BOOLEAN}</code> */
  private boolean bool;

  /** for <code>{@link #type} == {@link #NUMBER}</code> */
  private double number;

  /** for <code>{@link #type} == {@link #STRING}</code> */
  private String string;

  /** for <code>{@link #type} == {@link #OBJECT}</code> */
  private Object object;

  /**
   * The next value of the sequence. A sequence is simply a chained list of
   * Value objects. The empty sequence is represented by a {@link #type} set to
   * {@link #NODE} and {@link #event} set to <code>null</code>
   * (<code>next</code> must be <code>null</code> in this case, too).
   */
  public Value next;

  //
  // Constructors
  //

  /** Constructs an empty sequence */
  private Value ()
  {
    type = EMPTY;
  }

  /** Constructs a <code>Value</code> containing a number */
  public Value (final double d)
  {
    type = NUMBER;
    number = d;
  }

  /** Returns a Value object representing the given boolean value */
  public static Value getBoolean (final boolean b)
  {
    return b ? VAL_TRUE : VAL_FALSE;
  }

  /** Constructs a <code>Value</code> containing a boolean */
  private Value (final boolean b)
  {
    type = BOOLEAN;
    bool = b;
  }

  /** Constructs a <code>Value</code> containing a string */
  public Value (final CharSequence s)
  {
    type = STRING;
    string = s != null ? s.toString () : null;
  }

  /**
   * Constructs a <code>Value</code> containing a node
   * (<code>{@link SAXEvent}</code>).
   * 
   * @param e
   *        the event
   */
  public Value (final SAXEvent e)
  {
    type = NODE;
    event = e;
  }

  /**
   * Constructs a <code>Value</code> containing a custom Java object, possibly
   * converting the object to a known STX type
   */
  public Value (final Object obj)
  {
    if (obj == null)
    {
      type = OBJECT;
      return;
    }
    else
      if (obj instanceof Void)
      {
        type = EMPTY;
        return;
      }
      else
        if (obj instanceof CharSequence || obj instanceof Character)
        {
          type = STRING;
          string = obj.toString ();
          return;
        }
        else
          if (obj instanceof Boolean)
          {
            type = BOOLEAN;
            bool = ((Boolean) obj).booleanValue ();
            return;
          }
          else
            if (obj instanceof Number && obj.getClass ().getPackage ().getName ().equals ("java.lang"))
            {
              // convert only base Number values
              type = NUMBER;
              number = ((Number) obj).doubleValue ();
              return;
            }
            else
            {
              type = OBJECT;
              object = obj;
              return;
            }
  }

  //
  // Methods
  //

  // Getter

  public String getString ()
  {
    return string;
  }

  public SAXEvent getNode ()
  {
    return event;
  }

  public Object getObject ()
  {
    return object;
  }

  // Converter

  /** returns the value of this object converted to a number */
  public double getNumberValue ()
  {
    switch (type)
    {
      case NUMBER:
        return number;
      case EMPTY:
        return Double.NaN;
      case BOOLEAN:
        return (bool ? 1.0 : 0.0);
      case NODE:
      case OBJECT:
        try
        {
          return Double.parseDouble (getStringValue ());
        }
        catch (final NumberFormatException e)
        {
          return Double.NaN;
        }
        // falls through
      case STRING:
        try
        {
          return Double.parseDouble (string);
        }
        catch (final NumberFormatException e)
        {
          return Double.NaN;
        }
      default:
        // Mustn't happen
        throw new RuntimeException ("Don't know how to convert " + type + " to number");
    }
  }

  /** returns the value of this object converted to a string */
  public String getStringValue ()
  {
    switch (type)
    {
      case STRING:
        return string;
      case NODE:
        return event.value;
      case EMPTY:
        return "";
      case BOOLEAN:
        return bool ? "true" : "false";
      case NUMBER:
        if ((number < 1e-3 || number >= 1e7) && !Double.isInfinite (number))
        {
          // This is the range where Double.toString produces the scientific
          // notation - we use NumberFormat to prevent this
          synchronized (numberFormat)
          {
            return numberFormat.format (number);
          }
        }
        else
        {
          String v = Double.toString (number);
          if (v.endsWith (".0"))
            v = v.substring (0, v.length () - 2);
          return v;
        }
      case OBJECT:
        return object != null ? object.toString () : "";
      default:
        // Mustn't happen
        throw new RuntimeException ("Don't know how to convert " + type + " to string");
    }
  }

  /** returns the value of this object converted to a boolean */
  public boolean getBooleanValue ()
  {
    switch (type)
    {
      case BOOLEAN:
        return bool;
      case NODE:
        return true;
      case EMPTY:
        return false;
      case NUMBER:
        return number != 0.0;
      case STRING:
        return !string.equals ("");
      case OBJECT:
        return object == null ? false : !object.toString ().equals ("");
      default:
        // Mustn't happen
        throw new RuntimeException ("Don't know how to convert " + type + " to boolean");
    }
  }

  // Misc

  /**
   * Creates a full copy of the sequence represented by this value.
   */
  public Value copy ()
  {
    final Value ret = new Value ();
    ret.bool = bool;
    ret.event = event;
    ret.number = number;
    ret.object = object;
    ret.string = string;
    ret.type = type;
    if (next != null)
      ret.next = next.copy ();
    return ret;
  }

  /**
   * Returns a single value that is a copy of this value
   */
  public Value singleCopy ()
  {
    switch (type)
    {
      case BOOLEAN:
        return getBoolean (bool);
      case NODE:
        return new Value (event);
      case NUMBER:
        return new Value (number);
      case STRING:
        return new Value (string);
      case OBJECT:
        return new Value (object);
    }
    return VAL_EMPTY;
  }

  /**
   * Creates a sequence by concatenating two values (which are possibly already
   * sequences
   * 
   * @param v1
   *        first value (first part of the resulting sequence)
   * @param v2
   *        second value (second part of the resulting sequence)
   * @return a sequence that consists of v1 and v2
   */
  public static Value concat (final Value v1, final Value v2)
  {
    try
    {
      if (v1.next == null)
      {
        final Value ret = (Value) v1.clone ();
        ret.next = v2;
        return ret;
      }
      else
      {
        Value tmp = v1;
        while (tmp.next.next != null)
          tmp = tmp.next;
        tmp.next = (Value) tmp.next.clone ();
        tmp.next.next = v2;
        return v1;
      }
    }
    catch (final CloneNotSupportedException e)
    {
      // mustn't happen
      return null;
    }
  }

  /**
   * Determines the conversion distance of the contained value to the specified
   * target Java class. Lower results indicate higher preferences.
   * 
   * @param target
   *        the class to which a conversion is desired
   * @return an individual distance value, or {@link Double#POSITIVE_INFINITY}
   *         if a conversion is not possible
   */
  public double getDistanceTo (final Class target)
  {
    if (type == OBJECT)
    {
      if (object == null || target == Object.class)
        return 2;
      if (target == object.getClass ())
        return 0;
      if (target.isAssignableFrom (object.getClass ()))
        return 1;
      if (target == String.class)
        return 100;
    }
    if (target == List.class)
      return 90;
    if (target == Object.class)
      return 100;
    switch (type)
    {
      case EMPTY:
        if (!target.isPrimitive ())
                                   // target is a reference type
                                   return 1;
        break;
      case BOOLEAN:
        if (target == boolean.class)
          return 0;
        if (target == Boolean.class)
          return 1;
        if (target == byte.class)
          return 10;
        if (target == Byte.class)
          return 11;
        if (target == short.class)
          return 12;
        if (target == Short.class)
          return 13;
        if (target == int.class)
          return 14;
        if (target == Integer.class)
          return 15;
        if (target == long.class)
          return 16;
        if (target == Long.class)
          return 17;
        if (target == char.class)
          return 18;
        if (target == Character.class)
          return 19;
        if (target == String.class)
          return 20;
        if (target == float.class)
          return 21;
        if (target == Float.class)
          return 22;
        if (target == double.class)
          return 23;
        if (target == Double.class)
          return 24;
        break;
      case NUMBER:
        if (target == double.class)
          return 0;
        if (target == Double.class)
          return 1;
        if (target == float.class)
          return 2;
        if (target == Float.class)
          return 3;
        if (target == long.class)
          return 4;
        if (target == Long.class)
          return 5;
        if (target == int.class)
          return 6;
        if (target == Integer.class)
          return 7;
        if (target == short.class)
          return 8;
        if (target == Short.class)
          return 9;
        if (target == byte.class)
          return 10;
        if (target == Byte.class)
          return 11;
        if (target == String.class)
          return 20;
        if (target == char.class)
          return 31;
        if (target == Character.class)
          return 32;
        if (target == boolean.class)
          return 33;
        if (target == Boolean.class)
          return 34;
        break;
      case NODE: // treat NODE and STRING equal
      case STRING:
        if (target == String.class)
          return 0;
        if (target == char.class)
          return 1;
        if (target == Character.class)
          return 2;
        if (target == double.class)
          return 10;
        if (target == Double.class)
          return 11;
        if (target == float.class)
          return 12;
        if (target == Float.class)
          return 13;
        if (target == int.class)
          return 14;
        if (target == Integer.class)
          return 15;
        if (target == long.class)
          return 16;
        if (target == Long.class)
          return 17;
        if (target == short.class)
          return 18;
        if (target == Short.class)
          return 19;
        if (target == byte.class)
          return 20;
        if (target == Byte.class)
          return 21;
        if (target == boolean.class)
          return 30;
        if (target == Boolean.class)
          return 31;
        break;
    }
    return Double.POSITIVE_INFINITY;
  }

  /**
   * Converts this value to a Java object.
   * 
   * @return a Java object representing the current value
   * @exception EvalException
   *            if the conversion is not possible
   */
  public Object toJavaObject (final Class target) throws EvalException
  {
    if (target == Object.class)
    {
      switch (type)
      {
        case EMPTY:
          return null;
        case NODE:
          return event.value;
        case BOOLEAN:
          return new Boolean (bool);
        case NUMBER:
          return new Double (number);
        case STRING:
          return string;
        case OBJECT:
          return object;
        default:
          throw new RuntimeException ("Fatal: unexpected type " + type);
      }
    }
    else
      if (type == OBJECT && (object == null || target.isAssignableFrom (object.getClass ())))
      {
        // target is a superclass of object's class (or they are the same)
        return object;
      }
      else
        if (target == List.class)
        {
          if (type == EMPTY)
            return new ArrayList (0);
          final ArrayList list = new ArrayList ();
          for (Value it = this; it != null; it = it.next)
            list.add (it.toJavaObject (Object.class));
          return list;
        }
        else
          if (type == EMPTY && !target.isPrimitive ())
          {
            // target is a reference type
            return null;
          }
          else
            if (target == String.class)
            {
              return getStringValue ();
            }
            else
              if (type == STRING && "".equals (string) && !target.isPrimitive ())
              {
                // convert the "" to null if the target is a non-string
                // reference type
                return null;
              }
              else
                if (target == boolean.class || target == Boolean.class)
                {
                  return new Boolean (getBooleanValue ());
                }
                else
                  if (target == double.class || target == Double.class)
                  {
                    return new Double (getNumberValue ());
                  }
                  else
                    if (target == float.class || target == Float.class)
                    {
                      return new Float (getNumberValue ());
                    }
                    else
                      if (target == int.class || target == Integer.class)
                      {
                        return new Integer ((int) getNumberValue ());
                      }
                      else
                        if (target == long.class || target == Long.class)
                        {
                          return new Long ((long) getNumberValue ());
                        }
                        else
                          if (target == short.class || target == Short.class)
                          {
                            return new Short ((short) getNumberValue ());
                          }
                          else
                            if (target == byte.class || target == Byte.class)
                            {
                              return new Byte ((byte) getNumberValue ());
                            }
                            else
                              if (target == char.class || target == Character.class)
                              {
                                final String s = getStringValue ();
                                if (string.length () == 1)
                                  return new Character (s.charAt (0));
                                else
                                  throw new EvalException ("Cannot convert string '" +
                                                           string +
                                                           "' to character (length is not 1)");
                              }
                              else
                                throw new EvalException ("Conversion to " + target.getName () + " is not supported");
  }

  //
  // for debugging
  //
  @Override
  public String toString ()
  {
    String ret;
    switch (type)
    {
      case EMPTY:
        ret = "()";
        break;
      case NUMBER:
        ret = "number " + number;
        break;
      case BOOLEAN:
        ret = "boolean " + bool;
        break;
      case STRING:
        ret = "string '" + string + "'";
        break;
      case NODE:
        ret = "node " + event;
        break;
      case OBJECT:
        ret = "object " + object;
        break;
      default:
        ret = ("unknown type in Value object");
    }
    if (next != null)
      ret += ", " + next.toString ();
    return ret;
  }
}
