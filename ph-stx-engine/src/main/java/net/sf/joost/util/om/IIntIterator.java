/*
 * $Id: IntIterator.java,v 1.1 2007/06/04 19:57:35 obecker Exp $
 *
 * Copied from Michael Kay's Saxon 8.9
 * Local changes (excluding package declarations and imports) marked as // OB
 */

package net.sf.joost.util.om;

/**
 * An iterator over a sequence of unboxed int values
 */
public interface IIntIterator
{
  /**
   * Test whether there are any more integers in the sequence
   */
  boolean hasNext ();

  /**
   * Return the next integer in the sequence. The result is undefined unless
   * hasNext() has been called and has returned true.
   */
  int next ();
}

//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//