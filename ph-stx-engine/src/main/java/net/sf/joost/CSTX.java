/*
 * $Id: Constants.java,v 2.4 2004/10/30 15:04:35 obecker Exp $
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is: this file
 *
 * The Initial Developer of the Original Code is Oliver Becker.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */

package net.sf.joost;

/**
 * This interface contains constants shared between different classes.
 *
 * @version $Revision: 2.4 $ $Date: 2004/10/30 15:04:35 $
 * @author Oliver Becker
 */
public final class CSTX
{
  /** The STX namespace */
  public static final String STX_NS = "http://stx.sourceforge.net/2002/ns";

  /** The STX functions namespace */
  public static final String FUNC_NS = "http://stx.sourceforge.net/2003/functions";

  /** The Joost extension namespace */
  public static final String JOOST_EXT_NS = "http://joost.sf.net/extension";

  /*
   * URIs for Identifying Feature Flags and Properties: All XML readers are
   * required to recognize the "http://xml.org/sax/features/namespaces" and the
   * "http://xml.org/sax/features/namespace-prefixes" features (at least to get
   * the feature values, if not set them) and to support a true value for the
   * namespaces property and a false value for the namespace-prefixes property.
   */

  /** URI prefix for SAX features */
  public static String FEATURE_URI_PREFIX = "http://xml.org/sax/features/";

  /** URI for the SAX feature "namespaces" */
  public static String FEAT_NS = FEATURE_URI_PREFIX + "namespaces";

  /** URI for the SAX feature "namespace-prefixes" */
  public static String FEAT_NSPREFIX = FEATURE_URI_PREFIX + "namespace-prefixes";

  /** The default encoding for XML */
  public static String DEFAULT_ENCODING = "UTF-8";

  /**
   * Return value for
   * {@link net.sf.joost.instruction.AbstractInstruction#process}
   */
  public static final short PR_CONTINUE = 0, // continue processing
      PR_CHILDREN = 1, // stx:process-children encountered
      PR_SELF = 2, // stx:process-self encountered
      PR_SIBLINGS = 3, // stx:process-siblings encountered
      PR_ATTRIBUTES = 4, // stx:process-attributes encountered
      PR_BUFFER = 5, // stx:process-buffer
      PR_ERROR = -1; // non-recoverable error

  /** Debugging flag, should be <code>false</code> in release versions */
  public static final boolean DEBUG = false;

  private CSTX ()
  {}
}
