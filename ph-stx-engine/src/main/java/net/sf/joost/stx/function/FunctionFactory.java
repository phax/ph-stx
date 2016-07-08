/*
 * $Id: FunctionFactory.java,v 1.6 2007/11/25 14:18:00 obecker Exp $
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
 * Contributor(s): Thomas Behrends, Nikolay Fiykov.
 */

package net.sf.joost.stx.function;

import java.util.Hashtable;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFManager;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.Constants;
import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.Tree;
import net.sf.joost.instruction.ScriptFactory;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.stx.Value;

/**
 * Factory for all STXPath function implementations.
 * 
 * @version $Revision: 1.6 $ $Date: 2007/11/25 14:18:00 $
 * @author Oliver Becker, Nikolay Fiykov
 */
final public class FunctionFactory implements Constants
{
  /**
   * Type for all functions
   */
  public static interface Instance
  {
    /** Minimum number of parameters. */
    public int getMinParCount ();

    /** Maximum number of parameters. */
    public int getMaxParCount ();

    /** Expanded name of the function. */
    public String getName ();

    /**
     * @return <code>true</code> if a call to this function with constant
     *         parameters returns always the same result
     */
    public boolean isConstant ();

    /**
     * The evaluation method.
     * 
     * @param context
     *        the Context object
     * @param top
     *        the number of the upper most element on the stack
     * @param args
     *        the current parameters
     * @return a {@link Value} instance containing the result
     * @exception SAXException
     *            if an error occurs while processing
     * @exception EvalException
     *            if an error occurs while processing
     */
    public Value evaluate (Context context, int top, Tree args) throws SAXException, EvalException;
  } // end of Instance

  // namespace to be prepended before function names
  // (function namespace prefix)
  public static final String FNSP = "{" + FUNC_NS + "}";

  // Joost extension namespace prefix
  public static final String JENSP = "{" + JOOST_EXT_NS + "}";

  /** Contains one instance for each function. */
  private static Hashtable functionHash;
  static
  {
    final Instance [] functions = { new StringConv (),
                                    new NumberConv (),
                                    new BooleanConv (),
                                    new Position (),
                                    new HasChildNodes (),
                                    new NodeKind (),
                                    new Name (),
                                    new LocalName (),
                                    new NamespaceURI (),
                                    new GetNamespaceUriForPrefix (),
                                    new GetInScopePrefixes (),
                                    new Not (),
                                    new True (),
                                    new False (),
                                    new Floor (),
                                    new Ceiling (),
                                    new Round (),
                                    new Concat (),
                                    new StringJoin (),
                                    new StringLength (),
                                    new NormalizeSpace (),
                                    new Contains (),
                                    new StartsWith (),
                                    new EndsWith (),
                                    new Substring (),
                                    new SubstringBefore (),
                                    new SubstringAfter (),
                                    new Translate (),
                                    new StringPad (),
                                    new Matches (),
                                    new Replace (),
                                    new Tokenize (),
                                    new EscapeUri (),
                                    new Empty (),
                                    new Exists (),
                                    new ItemAt (),
                                    new IndexOf (),
                                    new Subsequence (),
                                    new InsertBefore (),
                                    new Remove (),
                                    new Count (),
                                    new Sum (),
                                    new Min (),
                                    new Max (),
                                    new Avg (),
                                    new RegexGroup (),
                                    new FilterAvailable (),
                                    new ExtSequence () };
    functionHash = new Hashtable (functions.length);
    for (final Instance function : functions)
      functionHash.put (function.getName (), function);
  }

  /** The parse context for this <code>FunctionFactory</code> instance */
  private final ParseContext pContext;

  /** prefix-uri map of all script declarations */
  private final Hashtable scriptUriMap = new Hashtable ();

  //
  // Constructor
  //

  /**
   * Creates a new <code>FunctionFactory</code> instance with a given parse
   * context
   */
  public FunctionFactory (final ParseContext pContext)
  {
    this.pContext = pContext;
  }

  //
  // Methods
  //

  /**
   * Looks for a function implementation.
   *
   * @param uri
   *        URI of the expanded function name
   * @param lName
   *        local function name
   * @param args
   *        parameters (needed here just for counting)
   * @return the implementation instance for this function
   * @exception SAXParseException
   *            if the function wasn't found or the number of parameters is
   *            wrong
   */
  public Instance getFunction (final String uri,
                               final String lName,
                               final String qName,
                               Tree args) throws SAXParseException
  {
    // execute java methods
    if (uri.startsWith ("java:"))
    {
      if (pContext.allowExternalFunctions)
        return new ExtensionFunction (uri.substring (5), lName, args, pContext.locator);
      else
        throw new SAXParseException ("No permission to call extension function '" + qName + "'", pContext.locator);
    }

    // execute script functions
    if (this.scriptUriMap.containsValue (uri))
      if (pContext.allowExternalFunctions)
      {
        return createScriptFunction (uri, lName, qName);
      }
      else
        throw new SAXParseException ("No permission to call script function '" + qName + "'", pContext.locator);

    final Instance function = (Instance) functionHash.get ("{" + uri + "}" + lName);
    if (function == null)
      throw new SAXParseException ("Unknown function '" + qName + "'", pContext.locator);

    // Count parameters in args
    int argc = 0;
    if (args != null)
    {
      argc = 1;
      while (args.type == Tree.LIST)
      {
        args = args.left;
        argc++;
      }
    }
    if (argc < function.getMinParCount ())
      throw new SAXParseException ("Too few parameters in call of " +
                                   "function '" +
                                   qName +
                                   "' (" +
                                   function.getMinParCount () +
                                   " needed)",
                                   pContext.locator);
    if (argc > function.getMaxParCount ())
      throw new SAXParseException ("Too many parameters in call of " +
                                   "function '" +
                                   qName +
                                   "' (" +
                                   function.getMaxParCount () +
                                   " allowed)",
                                   pContext.locator);
    return function;
  }

  /**
   * @return a value for an optional function argument. Either the argument was
   *         present, or the current item will be used.
   * @exception SAXException
   *            from evaluating <code>args</code>
   */
  static Value getOptionalValue (final Context context, final int top, final Tree args) throws SAXException
  {
    if (args != null) // argument present
      return args.evaluate (context, top);
    else
      if (top > 0) // use current node
        return new Value ((SAXEvent) context.ancestorStack.elementAt (top - 1));
      else // no event available (e.g. init of global variables)
        return Value.VAL_EMPTY;
  }

  // ************************************************************************

  //
  // Accessing script functions via BSF
  //

  /** BSF Manager instance, singleton */
  private BSFManager bsfManager;

  /** uri-BSFEngine map of all script declarations */
  private final Hashtable uriEngineMap = new Hashtable ();

  /**
   * @return BSF manager, creates one if neccessary
   */
  private BSFManager getBSFManager ()
  {
    if (bsfManager == null)
      bsfManager = new BSFManager ();
    return bsfManager;
  }

  /**
   * @param prefix
   *        a namespace prefix
   * @return <code>true</code> if this prefix was used for a script element
   */
  public boolean isScriptPrefix (final String prefix)
  {
    return this.scriptUriMap.get (prefix) != null;
  }

  /**
   * Called from {@link ScriptFactory.Instance} to create a new script part.
   * 
   * @param scriptElement
   *        the <code>joost:script</code> instance
   * @param scriptCode
   *        the script code
   * @throws SAXException
   */
  public void addScript (final ScriptFactory.Instance scriptElement, final String scriptCode) throws SAXException
  {
    final String nsPrefix = scriptElement.getPrefix ();
    final String nsUri = scriptElement.getUri ();
    this.scriptUriMap.put (nsPrefix, nsUri);

    // set scripting engine
    BSFEngine engine = null;
    try
    {
      engine = getBSFManager ().loadScriptingEngine (scriptElement.getLang ());
      this.uriEngineMap.put (nsUri, engine);
    }
    catch (final Exception e)
    {
      throw new SAXParseException ("Exception while creating scripting " +
                                   "engine for prefix �" +
                                   nsPrefix +
                                   "' and language '" +
                                   scriptElement.getLang () +
                                   "'",
                                   scriptElement.publicId,
                                   scriptElement.systemId,
                                   scriptElement.lineNo,
                                   scriptElement.colNo,
                                   e);
    }
    // execute stx-global script code
    try
    {
      engine.exec ("JoostScript", -1, -1, scriptCode);
    }
    catch (final Exception e)
    {
      throw new SAXParseException ("Exception while executing the script " +
                                   "for prefix '" +
                                   nsPrefix +
                                   "'",
                                   scriptElement.publicId,
                                   scriptElement.systemId,
                                   scriptElement.lineNo,
                                   scriptElement.colNo,
                                   e);
    }
  }

  private ScriptFunction createScriptFunction (final String uri, final String lName, final String qName)
  {
    return new ScriptFunction (((BSFEngine) this.uriEngineMap.get (uri)), lName, qName);
  }
}
