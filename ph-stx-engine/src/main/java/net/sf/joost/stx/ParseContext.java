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
package net.sf.joost.stx;

import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.URIResolver;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;

import net.sf.joost.instruction.TransformFactory;
import net.sf.joost.stx.function.FunctionFactory;

/**
 * Instances of this class provide context information while parsing an STX
 * document.
 *
 * @version $Revision: 2.11 $ $Date: 2006/03/21 19:25:08 $
 * @author Oliver Becker
 */
public final class ParseContext
{
  /** The locator object for the input stream */
  public Locator locator;

  /** The set of namespaces currently in scope */
  public Map <String, String> nsSet;

  /** The error handler for the parser */
  private ErrorHandler errorHandler;

  /** The URI resolver for <code>stx:include</code> instructions */
  public URIResolver uriResolver;

  /** An optional ParserListener for <code>stx:include</code> instructions */
  public IParserListener parserListener;

  /** The root element of the transform sheet */
  public TransformFactory.Instance transformNode;

  /** Are calls on Java extension functions allowed? */
  public boolean allowExternalFunctions = true;

  /**
   * The function table for maintaining function definitions, especially of the
   * script functions
   */
  private FunctionFactory functionFactory;

  //
  // Constructors
  //

  /** Default constructor */
  public ParseContext ()
  {}

  /** Copy constructor */
  public ParseContext (final ParseContext pContext)
  {
    errorHandler = pContext.errorHandler;
    uriResolver = pContext.uriResolver;
    parserListener = pContext.parserListener;
    allowExternalFunctions = pContext.allowExternalFunctions;
  }

  //
  // Methods
  //

  /** Returns (and constructs if necessary) an error handler */
  public ErrorHandler getErrorHandler ()
  {
    if (errorHandler == null)
      errorHandler = new ErrorHandlerImpl (null, true);
    return errorHandler;
  }

  /** Sets an error listener that will be used to construct an error handler */
  public void setErrorListener (final ErrorListener errorListener)
  {
    errorHandler = new ErrorHandlerImpl (errorListener, true);
  }

  public FunctionFactory getFunctionFactory ()
  {
    if (functionFactory == null)
      functionFactory = new FunctionFactory (this);
    return functionFactory;
  }
}
