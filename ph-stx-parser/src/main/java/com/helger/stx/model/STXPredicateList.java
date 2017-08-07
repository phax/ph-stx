/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
package com.helger.stx.model;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.string.ToStringGenerator;

/**
 * A list of {@link STXPredicate} objects used for a step expression. May be
 * empty.
 *
 * @author Philip Helger
 */
public class STXPredicateList implements ISTXObject
{
  private final ICommonsList <STXPredicate> m_aPredicates;

  public STXPredicateList (@Nonnull final Iterable <? extends STXPredicate> aPredicates)
  {
    ValueEnforcer.notNull (aPredicates, "Predicates");
    m_aPredicates = new CommonsArrayList<> (aPredicates);
  }

  public boolean hasAnyPredicate ()
  {
    return m_aPredicates.isNotEmpty ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <STXPredicate> getAllPredicates ()
  {
    return m_aPredicates.getClone ();
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    for (final STXPredicate aPredicate : m_aPredicates)
      aPredicate.writeTo (aWriter);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("predicates", m_aPredicates).getToString ();
  }
}
