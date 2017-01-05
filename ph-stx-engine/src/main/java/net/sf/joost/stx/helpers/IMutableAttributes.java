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
package net.sf.joost.stx.helpers;

import org.xml.sax.Attributes;

/**
 * Defines mutable SAX attributes.
 */
public interface IMutableAttributes extends Attributes
{
  /** Set the value of an attribute at the specified index */
  void setValue (int index, String value);

  /** Add an attribute to the set of attributes */
  void addAttribute (String uri, String lName, String qName, String type, String value);
}
