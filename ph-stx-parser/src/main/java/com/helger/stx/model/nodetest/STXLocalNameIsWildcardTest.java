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
package com.helger.stx.model.nodetest;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;

/**
 * Wildcard test where the local name can be anything but the namespace URL is
 * fixed.
 *
 * @author Philip Helger
 */
public class STXLocalNameIsWildcardTest extends AbstractSTXNameTest
{
  private final String m_sNamespaceURL;

  public STXLocalNameIsWildcardTest (@Nonnull @Nonempty final String sNamespaceURL)
  {
    m_sNamespaceURL = ValueEnforcer.notEmpty (sNamespaceURL, "NamespaceURL");
  }

  @Nonnull
  @Nonempty
  public String getNamespaceURL ()
  {
    return m_sNamespaceURL;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    aWriter.write (m_sNamespaceURL);
    aWriter.write ('*');
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).appendIfNotNull ("namespaceURL", m_sNamespaceURL).toString ();
  }
}
