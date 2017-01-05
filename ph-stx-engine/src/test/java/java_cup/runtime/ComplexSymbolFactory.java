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

import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Default Implementation for SymbolFactory, creates plain old Symbols
 *
 * @version last updated 27-03-2006
 * @author Michael Petter
 */

/*
 * ************************************************* class DefaultSymbolFactory
 * interface for creating new symbols
 ***************************************************/
public class ComplexSymbolFactory implements SymbolFactory
{
  public static class Location
  {
    private String m_sUnit = "unknown";
    private int m_nLine, m_nColumn, m_nOffset = -1;

    /**
     * Copy Constructor for other ComplexSymbolFactory based Locations
     *
     * @param other
     */
    public Location (final Location other)
    {
      this (other.m_sUnit, other.m_nLine, other.m_nColumn, other.m_nOffset);
    }

    /**
     * Location Object stores compilation unit, line, column and offset to the
     * file start
     *
     * @param unit
     *        compilation unit, e.g. file name
     * @param line
     *        line number
     * @param column
     *        column number
     * @param offset
     *        offset from file start
     */
    public Location (final String unit, final int line, final int column, final int offset)
    {
      this (unit, line, column);
      this.m_nOffset = offset;
    }

    /**
     * Location Object stores compilation unit, line and column
     *
     * @param unit
     *        compilation unit, e.g. file name
     * @param line
     *        line number
     * @param column
     *        column number
     */
    public Location (final String unit, final int line, final int column)
    {
      this.m_sUnit = unit;
      this.m_nLine = line;
      this.m_nColumn = column;
    }

    /**
     * Location Object stores line, column and offset to the file start
     *
     * @param line
     *        line number
     * @param column
     *        column number
     * @param offset
     *        offset from file start
     */
    public Location (final int line, final int column, final int offset)
    {
      this (line, column);
      this.m_nOffset = offset;
    }

    /**
     * Location Object stores line and column
     *
     * @param line
     *        line number
     * @param column
     *        column number
     */
    public Location (final int line, final int column)
    {
      this.m_nLine = line;
      this.m_nColumn = column;
    }

    /**
     * getColumn
     *
     * @returns column if known, else -1
     */
    public int getColumn ()
    {
      return m_nColumn;
    }

    /**
     * getLine
     *
     * @returns line if known, else -1
     */
    public int getLine ()
    {
      return m_nLine;
    }

    /**
     * move moves this Location by the given differences.
     *
     * @param linediff
     * @param coldiff
     * @param offsetdiff
     */
    public void move (final int linediff, final int coldiff, final int offsetdiff)
    {
      if (this.m_nLine >= 0)
        this.m_nLine += linediff;
      if (this.m_nColumn >= 0)
        this.m_nColumn += coldiff;
      if (this.m_nOffset >= 0)
        this.m_nOffset += offsetdiff;
    }

    /**
     * Cloning factory method
     *
     * @param other
     * @return new cloned Location
     */
    public static Location clone (final Location other)
    {
      return new Location (other);
    }

    /**
     * getUnit
     *
     * @returns compilation unit if known, else 'unknown'
     */
    public String getUnit ()
    {
      return m_sUnit;
    }

    /**
     * getLine
     *
     * @returns line if known, else -1
     */
    @Override
    public String toString ()
    {
      return getUnit () + ":" + getLine () + "/" + getColumn () + "(" + m_nOffset + ")";
    }

    /**
     * Writes the location information directly into an XML document
     *
     * @param writer
     *        the destination XML Document
     * @param orientation
     *        adds details about the orientation of this location as an
     *        attribute; often used with the strings "left" or "right"
     * @throws XMLStreamException
     */
    public void toXML (final XMLStreamWriter writer, final String orientation) throws XMLStreamException
    {
      writer.writeStartElement ("location");
      writer.writeAttribute ("compilationunit", m_sUnit);
      writer.writeAttribute ("orientation", orientation);
      writer.writeAttribute ("linenumber", m_nLine + "");
      writer.writeAttribute ("columnnumber", m_nColumn + "");
      writer.writeAttribute ("offset", m_nOffset + "");
      writer.writeEndElement ();
    }

    /**
     * getOffset
     *
     * @returns offset to start if known, else -1
     */
    public int getOffset ()
    {
      return m_nOffset;
    }
  }

  /**
   * ComplexSymbol with detailed Location Informations and a Name
   */
  public static class ComplexSymbol extends Symbol
  {
    protected String m_sName;
    public Location m_aXLeft, m_aXRight;

    public ComplexSymbol (final String name, final int id)
    {
      super (id);
      this.m_sName = name;
    }

    public ComplexSymbol (final String name, final int id, final Object value)
    {
      super (id, value);
      this.m_sName = name;
    }

    @Override
    public String toString ()
    {
      if (m_aXLeft == null || m_aXRight == null)
        return "Symbol: " + m_sName;
      return "Symbol: " + m_sName + " (" + m_aXLeft + " - " + m_aXRight + ")";
    }

    public String getName ()
    {
      return m_sName;
    }

    public ComplexSymbol (final String name, final int id, final int state)
    {
      super (id, state);
      this.m_sName = name;
    }

    public ComplexSymbol (final String name, final int id, final Symbol left, final Symbol right)
    {
      super (id, left, right);
      this.m_sName = name;
      if (left != null)
        this.m_aXLeft = ((ComplexSymbol) left).m_aXLeft;
      if (right != null)
        this.m_aXRight = ((ComplexSymbol) right).m_aXRight;
    }

    public ComplexSymbol (final String name, final int id, final Location left, final Location right)
    {
      super (id, left.m_nOffset, right.m_nOffset);
      this.m_sName = name;
      this.m_aXLeft = left;
      this.m_aXRight = right;
    }

    public ComplexSymbol (final String name,
                          final int id,
                          @Nonnull final Symbol left,
                          @Nonnull final Symbol right,
                          final Object value)
    {
      super (id, left.m_nLeft, right.m_nRight, value);
      this.m_sName = name;
      this.m_aXLeft = ((ComplexSymbol) left).m_aXLeft;
      this.m_aXRight = ((ComplexSymbol) right).m_aXRight;
    }

    public ComplexSymbol (final String name, final int id, @Nonnull final Symbol left, final Object value)
    {
      super (id, left.m_nRight, left.m_nRight, value);
      this.m_sName = name;
      this.m_aXLeft = ((ComplexSymbol) left).m_aXRight;
      this.m_aXRight = ((ComplexSymbol) left).m_aXRight;
    }

    public ComplexSymbol (final String name,
                          final int id,
                          @Nonnull final Location left,
                          @Nonnull final Location right,
                          final Object value)
    {
      super (id, left.m_nOffset, right.m_nOffset, value);
      this.m_sName = name;
      this.m_aXLeft = left;
      this.m_aXRight = right;
    }

    public Location getLeft ()
    {
      return m_aXLeft;
    }

    public Location getRight ()
    {
      return m_aXRight;
    }
  }

  // Factory methods
  /**
   * newSymbol creates a complex symbol with Location objects for left and right
   * boundaries; this is used for terminals with values!
   */
  public Symbol newSymbol (final String name,
                           final int id,
                           final Location left,
                           final Location right,
                           final Object value)
  {
    return new ComplexSymbol (name, id, left, right, value);
  }

  /**
   * newSymbol creates a complex symbol with Location objects for left and right
   * boundaries; this is used for terminals without values!
   */
  public Symbol newSymbol (final String name, final int id, final Location left, final Location right)
  {
    return new ComplexSymbol (name, id, left, right);
  }

  public Symbol newSymbol (final String name, final int id, final Symbol left, final Object value)
  {
    return new ComplexSymbol (name, id, left, value);
  }

  public Symbol newSymbol (final String name, final int id, final Symbol left, final Symbol right, final Object value)
  {
    return new ComplexSymbol (name, id, left, right, value);
  }

  public Symbol newSymbol (final String name, final int id, final Symbol left, final Symbol right)
  {
    return new ComplexSymbol (name, id, left, right);
  }

  public Symbol newSymbol (final String name, final int id)
  {
    return new ComplexSymbol (name, id);
  }

  public Symbol newSymbol (final String name, final int id, final Object value)
  {
    return new ComplexSymbol (name, id, value);
  }

  public Symbol startSymbol (final String name, final int id, final int state)
  {
    return new ComplexSymbol (name, id, state);
  }
}
