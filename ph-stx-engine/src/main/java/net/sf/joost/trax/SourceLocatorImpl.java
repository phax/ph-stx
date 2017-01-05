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
package net.sf.joost.trax;

import javax.xml.transform.SourceLocator;

//jaxp
import org.xml.sax.Locator;

/**
 * Implementation of the {@link javax.xml.transform.SourceLocator}
 * 
 * @version $Revision: 1.5 $ $Date: 2005/01/23 19:47:30 $
 * @author Anatolij Zubow
 */
public class SourceLocatorImpl implements SourceLocator
{

  /**
   * Unique public key
   */
  private String m_sPublicID;

  /**
   * Unique system key
   */
  private String m_sSystemID;

  /**
   * Indicates the line number in the document
   */
  private int m_nLineNo = -1;

  /**
   * Indicates the column number in the document
   */
  private int m_nColumnNo = -1;

  /**
   * Constructor
   * 
   * @param locator
   *        {@link org.xml.sax.Locator}
   */
  public SourceLocatorImpl (final Locator locator)
  {

    if (locator != null)
    {
      this.m_sPublicID = locator.getPublicId ();
      this.m_sSystemID = locator.getSystemId ();
      this.m_nLineNo = locator.getLineNumber ();
      this.m_nColumnNo = locator.getColumnNumber ();
    }
  }

  /**
   * Constructor
   * 
   * @param publicId
   *        Unique public key
   * @param systemId
   *        Unique system key for path resolution
   * @param lineNo
   *        Line number
   * @param colNo
   *        Column number
   */
  public SourceLocatorImpl (final String publicId, final String systemId, final int lineNo, final int colNo)
  {

    this.m_sPublicID = publicId;
    this.m_sSystemID = systemId;
    this.m_nLineNo = lineNo;
    this.m_nColumnNo = colNo;
  }

  /**
   * Getting the attribute {@link #m_sPublicID}
   * 
   * @return A string containing the public identifier, or null if none is
   *         available
   */
  public String getPublicId ()
  {
    return this.m_sPublicID;
  }

  /**
   * Getting the attribute {@link #m_sSystemID}
   * 
   * @return A string containing the system identifier, or null if none is
   *         available
   */
  public String getSystemId ()
  {
    return this.m_sSystemID;
  }

  /**
   * Getting the attribute {@link #m_nLineNo}
   * 
   * @return The line number, or -1 if none is available
   */
  public int getLineNumber ()
  {
    return this.m_nLineNo;
  }

  /**
   * Getting the attribute {@link #m_nColumnNo}
   * 
   * @return The column number, or -1 if none is available
   */
  public int getColumnNumber ()
  {
    return this.m_nColumnNo;
  }
}
