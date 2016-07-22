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
package net.sf.joost.plugins.attributes;

import java.util.Hashtable;
import java.util.Locale;

/**
 * created on Mar 9, 2005
 *
 * @author fiykov
 * @version $Revision: 1.1 $
 * @since
 */
public class BooleanAttribute extends AbstractAttribute
{
  public BooleanAttribute (final String name, final String defVal, final Hashtable <String, AbstractAttribute> col)
  {
    super (name, new String [] { "true", "false" }, defVal, col);
  }

  @Override
  public Object newValue (final String value)
  {
    return Boolean.valueOf (Boolean.parseBoolean (value.toLowerCase (Locale.US)));
  }

  public boolean booleanValue ()
  {
    return ((Boolean) m_aValue).booleanValue ();
  }
}
