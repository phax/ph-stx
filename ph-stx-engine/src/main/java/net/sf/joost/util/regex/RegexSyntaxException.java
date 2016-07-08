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
  private final int position;

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
    this.position = position;
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
    return position;
  }
}
