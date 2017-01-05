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
package jflex;

import java.awt.TextArea;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Convenience class for JFlex stdout, redirects output to a TextArea if in GUI
 * mode.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public final class StdOutWriter extends PrintWriter
{

  /** text area to write to if in gui mode, gui mode = (text != null) */
  private TextArea text;

  /**
   * approximation of the current column in the text area for auto wrapping at
   * <code>wrap</code> characters
   **/
  private int col;

  /** auto wrap lines in gui mode at this value */
  private final static int wrap = 78;

  /** A StdOutWriter, attached to System.out, no gui mode */
  public StdOutWriter ()
  {
    super (System.out, true);
  }

  /** A StdOutWrite, attached to the specified output stream, no gui mode */
  public StdOutWriter (final OutputStream out)
  {
    super (out, true);
  }

  /**
   * Set the TextArea to write text to. Will continue to write to System.out if
   * text is <code>null</code>.
   *
   * @param text
   *        the TextArea to write to
   */
  public void setGUIMode (final TextArea text)
  {
    this.text = text;
  }

  /** Write a single character. */
  @Override
  public void write (final int c)
  {
    if (text != null)
    {
      text.append (String.valueOf ((char) c));
      if (++col > wrap)
        println ();
    }
    else
      super.write (c);
  }

  /** Write a portion of an array of characters. */
  @Override
  public void write (final char buf[], final int off, final int len)
  {
    if (text != null)
    {
      text.append (new String (buf, off, len));
      if ((col += len) > wrap)
        println ();
    }
    else
      super.write (buf, off, len);
  }

  /** Write a portion of a string. */
  @Override
  public void write (final String s, final int off, final int len)
  {
    if (text != null)
    {
      text.append (s.substring (off, off + len));
      if ((col += len) > wrap)
        println ();
    }
    else
    {
      super.write (s, off, len);
      flush ();
    }
  }

  /**
   * Begin a new line. Which actual character/s is/are written depends on the
   * runtime platform.
   */
  @Override
  public void println ()
  {
    if (text != null)
    {
      text.append (Out.NL);
      col = 0;
    }
    else
      super.println ();
  }
}
