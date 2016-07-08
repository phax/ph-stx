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
package com.helger.stx.model;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

/**
 * Base interface for all objects that are part of the XPath 2 data model-
 *
 * @author Philip Helger
 */
public interface ISTXObject
{
  /**
   * Write the respective XPath expression to the passed Writer.
   *
   * @param aWriter
   *        The writer to write to. Never <code>null</code>.
   * @throws IOException
   *         In case writing fails.
   */
  void writeTo (@Nonnull Writer aWriter) throws IOException;
}
