/*
 * Copyright (c) 2001-2004 Ant-Contrib project.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.joost.plugins.attributes;

import java.util.Hashtable;

/**
 * created on Mar 9, 2005
 * 
 * @author fiykov
 * @version $Revision: 1.1 $
 * @since
 */
public class StringAttribute extends Attribute
{

  public StringAttribute (final String name, final String [] validValues, final String defVal, final Hashtable col)
  {
    super (name, validValues, defVal, col);
  }

  public StringAttribute (final String name, final String defVal, final Hashtable col)
  {
    this (name, new String [] {}, defVal, col);
  }

  @Override
  public Object newValue (final String value)
  {
    return value;
  }

}
