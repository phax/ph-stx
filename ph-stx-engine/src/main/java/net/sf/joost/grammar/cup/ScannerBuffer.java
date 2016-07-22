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
package net.sf.joost.grammar.cup;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ScannerBuffer implements Scanner
{
  private final Scanner inner;
  private final List <Symbol> buffer = new LinkedList <> ();

  /**
   * Wraps around a custom scanner and stores all so far produced tokens in a
   * buffer
   * 
   * @param inner
   *        the scanner to buffer
   */
  public ScannerBuffer (final Scanner inner)
  {
    this.inner = inner;
  }

  /**
   * Read-Only access to the buffered Symbols
   * 
   * @return an unmodifiable Version of the buffer
   */
  public List <Symbol> getBuffered ()
  {
    return Collections.unmodifiableList (buffer);
  }

  @Override
  public Symbol next_token () throws Exception
  {
    final Symbol buffered = inner.next_token ();
    buffer.add (buffered);
    return buffered;
  }

}
