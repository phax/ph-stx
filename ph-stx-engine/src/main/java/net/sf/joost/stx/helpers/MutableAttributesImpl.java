/*
 * $Id: MutableAttributesImpl.java,v 1.2 2004/10/06 07:15:08 obecker Exp $
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
 * The Initial Developer of the Original Code is Thomas Behrends.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): Oliver Becker.
 */
package net.sf.joost.stx.helpers;

import org.xml.sax.Attributes;

/**
 * Mutable attributes implementation.
 */
public final class MutableAttributesImpl implements MutableAttributes
{
  private int _length;
  private int _max;
  private String [] _uris;
  private String [] _lNames;
  private String [] _qNames;
  private String [] _values;
  private String [] _types;

  public MutableAttributesImpl (final Attributes attributes)
  {
    this (attributes, attributes.getLength ());
  }

  public MutableAttributesImpl (final Attributes attributes, final int length)
  {
    _length = length;
    _max = _length + 2; // _max is initially at least 2
    _uris = new String [_max];
    _lNames = new String [_max];
    _qNames = new String [_max];
    _values = new String [_max];
    _types = new String [_max];

    for (int n = _length; n-- > 0;)
    {
      _uris[n] = attributes.getURI (n);
      _lNames[n] = attributes.getLocalName (n);
      _qNames[n] = attributes.getQName (n);
      _values[n] = attributes.getValue (n);
      _types[n] = attributes.getType (n);
    }
  }

  public int getIndex (final String uri, final String localName)
  {
    for (int n = _length; n-- > 0;)
    {
      if (localName.equals (_lNames[n]) && uri.equals (_uris[n]))
        return n;
    }
    return -1;
  }

  public int getLength ()
  {
    return _length;
  }

  public String getLocalName (final int index)
  {
    return _lNames[index];
  }

  public String getQName (final int index)
  {
    return _qNames[index];
  }

  public String getType (final int index)
  {
    return _types[index];
  }

  public String getType (final String qName)
  {
    for (int n = _length; n-- > 0;)
    {
      if (qName.equals (_qNames[n]))
        return _types[n];
    }
    return null;
  }

  public String getType (final String uri, final String localName)
  {
    for (int n = _length; n-- > 0;)
    {
      if (localName.equals (_lNames[n]) && _uris[n].equals (uri))
        return _types[n];
    }
    return null;
  }

  public String getURI (final int index)
  {
    return _uris[index];
  }

  public String getValue (final int index)
  {
    return _values[index];
  }

  public String getValue (final String qName)
  {
    for (int n = _length; n-- > 0;)
    {
      if (qName.equals (_qNames[n]))
        return _values[n];
    }
    return null;
  }

  public String getValue (final String uri, final String localName)
  {
    for (int n = _length; n-- > 0;)
    {
      if (_lNames[n].equals (localName) && _uris[n].equals (uri))
        return _values[n];
    }
    return null;
  }

  public int getIndex (final String qName)
  {
    for (int n = _length; n-- > 0;)
    {
      if (qName.equals (_qNames[n]))
        return n;
    }
    return -1;
  }

  public void setValue (final int index, final String value)
  {
    _values[index] = value;
  }

  public void addAttribute (final String uri,
                            final String lName,
                            final String qName,
                            final String type,
                            final String value)
  {
    if (_length == _max)
    {
      _max <<= 1; // * 2

      final String [] uris = new String [_max + 1];
      final String [] lNames = new String [_max + 1];
      final String [] qNames = new String [_max + 1];
      final String [] values = new String [_max + 1];
      final String [] types = new String [_max + 1];

      System.arraycopy (_uris, 0, uris, 0, _length);
      System.arraycopy (_lNames, 0, lNames, 0, _length);
      System.arraycopy (_qNames, 0, qNames, 0, _length);
      System.arraycopy (_values, 0, values, 0, _length);
      System.arraycopy (_types, 0, types, 0, _length);

      _uris = uris;
      _lNames = lNames;
      _qNames = qNames;
      _values = values;
      _types = types;
    }

    _uris[_length] = uri;
    _lNames[_length] = lName;
    _qNames[_length] = qName;
    _values[_length] = value;
    _types[_length] = type;

    _length++;
  }
}
