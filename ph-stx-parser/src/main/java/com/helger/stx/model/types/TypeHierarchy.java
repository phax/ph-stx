/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.stx.model.types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.ICommonsMap;

public class TypeHierarchy
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (TypeHierarchy.class);

  private final ICommonsMap <QName, XPathType> m_aMap = new CommonsHashMap<> ();

  public TypeHierarchy ()
  {}

  @Nonnull
  public <T extends XPathType> T registerType (@Nonnull final T aType)
  {
    ValueEnforcer.notNull (aType, "Type");
    final QName aName = aType.getName ();
    if (m_aMap.containsKey (aName))
      throw new IllegalArgumentException ("A type with name " + aName.toString () + " is already registered!");
    if (aType.hasParentType () && !m_aMap.containsKey (aType.getParentType ().getName ()))
      throw new IllegalArgumentException ("The parent type " +
                                          aType.getParentType ().getName ().toString () +
                                          " is unknown!");
    m_aMap.put (aName, aType);

    if (s_aLogger.isDebugEnabled ())
      s_aLogger.debug ("Registered type " + aName.toString ());
    return aType;
  }

  public boolean containsType (@Nullable final QName aTypeName)
  {
    return aTypeName != null && m_aMap.containsKey (aTypeName);
  }

  @Nullable
  public XPathType getType (@Nullable final QName aTypeName)
  {
    return aTypeName == null ? null : m_aMap.get (aTypeName);
  }

  public boolean isSameOrSubTypeOf (@Nullable final QName aSearchTypeName, @Nullable final QName aParentTypeName)
  {
    final XPathType aSearchType = getType (aSearchTypeName);
    if (aSearchType != null)
    {
      final XPathType aParentType = getType (aParentTypeName);
      if (aParentType != null)
      {
        // Same type
        if (aSearchType.equals (aParentType))
          return true;

        XPathType aCurType = aSearchType;
        while (!aCurType.isRootType ())
        {
          final XPathType aCurParentType = aCurType.getParentType ();
          if (aCurParentType.equals (aParentType))
            return true;
          aCurType = aCurParentType;
        }
      }
    }
    return false;
  }

  /**
   * Source: http://www.w3.org/TR/xquery-operators/#datatypes
   *
   * @return The built-in XML Schema type hierarchy.
   */
  @Nonnull
  public static TypeHierarchy createBuiltIntTypeHierarchy ()
  {
    final TypeHierarchy ret = new TypeHierarchy ();
    final XSType aAnyType = ret.registerType (new XSType ("anyType", null));
    final XSType aAnySimpleType = ret.registerType (new XSType ("anySimpleType", aAnyType));
    ret.registerType (new XSType ("IDREFS", aAnySimpleType));
    ret.registerType (new XSType ("NMTOKENS", aAnySimpleType));
    ret.registerType (new XSType ("ENTITIES", aAnySimpleType));
    final XSType aAnyAtomicType = ret.registerType (new XSType ("anyAtomicType", aAnySimpleType));
    ret.registerType (new XSType ("untypedAtomic", aAnyAtomicType));
    ret.registerType (new XSType ("dateTime", aAnyAtomicType));
    ret.registerType (new XSType ("date", aAnyAtomicType));
    ret.registerType (new XSType ("time", aAnyAtomicType));
    {
      final XSType aDuration = ret.registerType (new XSType ("duration", aAnyAtomicType));
      ret.registerType (new XSType ("yearMonthDuration", aDuration));
      ret.registerType (new XSType ("dayTimeDuration", aDuration));
    }
    ret.registerType (new XSType ("float", aAnyAtomicType));
    ret.registerType (new XSType ("double", aAnyAtomicType));
    {
      final XSType aDecimal = ret.registerType (new XSType ("decimal", aAnyAtomicType));
      final XSType aInteger = ret.registerType (new XSType ("integer", aDecimal));
      final XSType aNonPositiveInteger = ret.registerType (new XSType ("nonPositiveInteger", aInteger));
      ret.registerType (new XSType ("negativeInteger", aNonPositiveInteger));
      final XSType aLong = ret.registerType (new XSType ("long", aInteger));
      final XSType aInt = ret.registerType (new XSType ("int", aLong));
      final XSType aShort = ret.registerType (new XSType ("short", aInt));
      ret.registerType (new XSType ("byte", aShort));
      final XSType aNonNegativeInteger = ret.registerType (new XSType ("nonNegativeInteger", aInteger));
      final XSType aUnsignedLong = ret.registerType (new XSType ("unsignedLong", aNonNegativeInteger));
      final XSType aUnsignedInt = ret.registerType (new XSType ("unsignedInt", aUnsignedLong));
      final XSType aUnsignedShort = ret.registerType (new XSType ("unsignedShort", aUnsignedInt));
      ret.registerType (new XSType ("unsignedByte", aUnsignedShort));
      ret.registerType (new XSType ("positiveInteger", aNonNegativeInteger));
    }
    ret.registerType (new XSType ("gYearMonth", aAnyAtomicType));
    ret.registerType (new XSType ("gYear", aAnyAtomicType));
    ret.registerType (new XSType ("gMonthDay", aAnyAtomicType));
    ret.registerType (new XSType ("gDay", aAnyAtomicType));
    ret.registerType (new XSType ("gMonth", aAnyAtomicType));
    ret.registerType (new XSType ("boolean", aAnyAtomicType));
    ret.registerType (new XSType ("base64Binary", aAnyAtomicType));
    ret.registerType (new XSType ("hexBinary", aAnyAtomicType));
    ret.registerType (new XSType ("anyURI", aAnyAtomicType));
    ret.registerType (new XSType ("QName", aAnyAtomicType));
    ret.registerType (new XSType ("NOTATION", aAnyAtomicType));
    {
      final XSType aString = ret.registerType (new XSType ("string", aAnyAtomicType));
      final XSType aNormalizedString = ret.registerType (new XSType ("normalizedString", aString));
      final XSType aToken = ret.registerType (new XSType ("token", aNormalizedString));
      ret.registerType (new XSType ("language", aToken));
      ret.registerType (new XSType ("NMTOKEN", aToken));
      final XSType aName = ret.registerType (new XSType ("Name", aToken));
      final XSType aNCName = ret.registerType (new XSType ("NCName", aName));
      ret.registerType (new XSType ("ID", aNCName));
      ret.registerType (new XSType ("IDREF", aNCName));
      ret.registerType (new XSType ("ENTITY", aNCName));
    }
    ret.registerType (new XSType ("untyped", aAnyType));
    return ret;
  }
}
