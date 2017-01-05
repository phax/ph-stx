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
package net.sf.joost.grammar.cup;

/**
 * Defines the Scanner interface, which CUP uses in the default implementation
 * of <code>lr_parser.scan()</code>. Integration of scanners implementing
 * <code>Scanner</code> is facilitated.
 *
 * @version last updated 23-Jul-1999
 * @author David MacMahon <davidm@smartsc.com>
 */

/*
 * ************************************************* Interface Scanner Declares
 * the next_token() method that should be implemented by scanners. This method
 * is typically called by lr_parser.scan(). End-of-file can be indicated either
 * by returning <code>new Symbol(lr_parser.EOF_sym())</code> or
 * <code>null</code>.
 ***************************************************/
public interface Scanner
{
  /** Return the next token, or <code>null</code> on end-of-file. */
  public Symbol next_token () throws java.lang.Exception;
}
