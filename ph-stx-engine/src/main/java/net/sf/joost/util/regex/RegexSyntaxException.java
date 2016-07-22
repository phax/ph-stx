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
/*
 * $Id: RegexSyntaxException.java,v 1.1 2007/06/04 19:57:36 obecker Exp $
 *
 * Copied from Michael Kay's Saxon 8.9
 * Local changes (excluding package declarations and imports) marked as // OB
 */

package net.sf.joost.util.regex;

/**
 * Thrown when an syntactically incorrect regular expression is detected.
 */
public class RegexSyntaxException extends Exception
{
  private final int m_nPosition;

  /**
   * Represents an unknown position within a string containing a regular
   * expression.
   */
  public static final int UNKNOWN_POSITION = -1;

  public RegexSyntaxException (final String detail)
  {
    this (detail, UNKNOWN_POSITION);
  }

  public RegexSyntaxException (final String detail, final int position)
  {
    super (detail);
    this.m_nPosition = position;
  }

  /**
   * Returns the index into the regular expression where the error was detected
   * or <code>UNKNOWN_POSITION</code> if this is unknown.
   *
   * @return the index into the regular expression where the error was detected,
   *         or <code>UNKNOWNN_POSITION</code> if this is unknown
   */
  public int getPosition ()
  {
    return m_nPosition;
  }
}
