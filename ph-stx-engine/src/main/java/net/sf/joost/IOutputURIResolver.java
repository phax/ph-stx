/*
 * $Id: OutputURIResolver.java,v 1.1 2007/07/15 15:20:42 obecker Exp $
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
 * The Initial Developer of the Original Code is Michael H. Kay.
 *
 * Portions created by Oliver Becker
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */
package net.sf.joost;

import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

/**
 * This interface defines an OutputURIResolver. This is a counterpart to the
 * JAXP URIResolver, but is used to map the URI of a secondary result document
 * to a Result object which acts as the destination for the new document.
 *
 * @author Michael H. Kay
 * @author Oliver Becker
 */

public interface IOutputURIResolver
{
  /**
   * Resolve an output URI.
   *
   * @param href
   *        The relative URI of the output document. This corresponds to the
   *        <code>href</code> attribute of the <code>stx:result-document</code>
   *        instruction.
   * @param base
   *        The base URI that should be used. This is the base URI of the
   *        element that contained the href attribute. It may be null if no
   *        systemID was supplied for the stylesheet.
   * @param outputProperties
   *        The output properties that are in scope for the output document.
   *        These are the properties of the main transformation plus the values
   *        of the optional attributes <code>output-encoding</code> and
   *        <code>output-method</code>.
   * @param append
   *        If set to <code>true</code> then the result should be appended to a
   *        possibly already existing document.
   * @return a Result object representing the destination for the XML document.
   *         The method can also return null, in which case the standard output
   *         URI resolver will be used to create a Result object.
   */

  Result resolve (String href, String base, Properties outputProperties, boolean append) throws TransformerException;

  /**
   * Signal completion of the result document. This method is called by the
   * system when the result document has been successfully written. It allows
   * the resolver to perform tidy-up actions such as closing output streams, or
   * firing off processes that take this result tree as input. Note that the
   * OutputURIResolver is stateless, so the the original Result object is
   * supplied to identify the document that has been completed.
   *
   * @param result
   *        The result object returned by the previous call of resolve()
   */
  void close (Result result) throws TransformerException;
}
