/**
 * Copyright (C) 2016 Philip Helger (www.helger.com)
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
package com.helger.stx.reader;

import javax.annotation.Nonnull;

import com.helger.stx.STXSourceLocation;
import com.helger.stx.parser.STXNode;

/**
 * Runtime exception that is thrown if interpreting the XPath fails. Only thrown
 * from classes in this package.
 *
 * @author Philip Helger
 */
public class STXHandlingException extends RuntimeException
{
  private final STXNode m_aNode;

  @Nonnull
  private static String _getSourceLocation (@Nonnull final STXNode aNode)
  {
    final StringBuilder aRet = new StringBuilder ();
    final STXSourceLocation aSL = aNode.getSourceLocation ();
    if (aSL != null)
    {
      String sFirstTokenLocation = null;
      if (aSL.hasFirstTokenArea ())
      {
        sFirstTokenLocation = aSL.getFirstTokenLocationAsString ();
        aRet.append (sFirstTokenLocation);
      }
      if (aSL.hasLastTokenArea ())
      {
        final String sLastTokenLocation = aSL.getLastTokenLocationAsString ();
        if (sFirstTokenLocation == null || !sFirstTokenLocation.equals (sLastTokenLocation))
        {
          if (aRet.length () > 0)
            aRet.append ('-');
          aRet.append (sLastTokenLocation);
        }
      }
      if (aRet.length () > 0)
        aRet.append (' ');
    }
    return aRet.toString ();
  }

  public STXHandlingException (@Nonnull final STXNode aNode, @Nonnull final String sMessage)
  {
    super (_getSourceLocation (aNode) + sMessage);
    m_aNode = aNode;
  }

  /**
   * @return The source node where the error occurred. Never <code>null</code>.
   */
  @Nonnull
  public STXNode getNode ()
  {
    return m_aNode;
  }
}
