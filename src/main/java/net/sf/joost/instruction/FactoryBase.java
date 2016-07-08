/*
 * $Id: FactoryBase.java,v 2.11 2007/12/19 10:39:37 obecker Exp $
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

package net.sf.joost.instruction;

import java.io.StringReader;
import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.Constants;
import net.sf.joost.grammar.ExprParser;
import net.sf.joost.grammar.PatternParser;
import net.sf.joost.grammar.Sym;
import net.sf.joost.grammar.Tree;
import net.sf.joost.grammar.Yylex;
import net.sf.joost.grammar.tree.AvtTree;
import net.sf.joost.grammar.tree.StringTree;
import net.sf.joost.stx.ParseContext;

/**
 * Abstract base class for all factory classes which produce nodes
 * ({@link NodeBase}) for the tree representation of an STX transformation
 * sheet.
 * 
 * @version $Revision: 2.11 $ $Date: 2007/12/19 10:39:37 $
 * @author Oliver Becker
 */

public abstract class FactoryBase implements Constants
{
  /** @return the local name of this STX element */
  public abstract String getName ();

  /**
   * The factory method.
   * 
   * @param parent
   *        the parent Node
   * @param qName
   *        the full name of this node
   * @param attrs
   *        the attribute set of this node
   * @param context
   *        the parse context
   * @return an Instance of the appropriate Node
   * @exception SAXParseException
   *            for missing or wrong attributes, etc.
   */
  public abstract NodeBase createNode (NodeBase parent,
                                       String qName,
                                       Attributes attrs,
                                       ParseContext context) throws SAXException;

  /**
   * Looks for the required attribute <code>name</code> in <code>attrs</code>.
   * 
   * @param elementName
   *        the name of the parent element
   * @param attrs
   *        the attribute set
   * @param name
   *        the name of the attribute to look for
   * @param context
   *        the parse context
   * @return the attribute value as a String
   * @exception SAXParseException
   *            if this attribute is not present
   */
  protected static String getRequiredAttribute (final String elementName,
                                                final Attributes attrs,
                                                final String name,
                                                final ParseContext context) throws SAXParseException
  {
    final String att = attrs.getValue (name);
    if (att == null)
      throw new SAXParseException ("'" + elementName + "' must have a '" + name + "' attribute", context.locator);

    return att;
  }

  /**
   * Attribute values "yes" and "no"
   */
  static protected final String [] YESNO_VALUES = { "yes", "no" };

  /**
   * Index in {@link #YESNO_VALUES}
   */
  static protected final int YES_VALUE = 0, NO_VALUE = 1;

  /**
   * Looks for the attribute <code>name</code> in <code>attrs</code> and checks
   * if the value is among the values of <code>enumValues</code>.
   * 
   * @param name
   *        the name of the attribute to look for
   * @param attrs
   *        the attribute set
   * @param enumValues
   *        allowed attribute values
   * @param context
   *        the parse context
   * @return the index of the attribute value in <code>enumValues</code>, -1 if
   *         the attribute isn't present in <code>attrs</code>
   * @exception SAXParseException
   *            if the attribute value isn't in <code>enumValues</code>
   */
  protected static int getEnumAttValue (final String name,
                                        final Attributes attrs,
                                        final String [] enumValues,
                                        final ParseContext context) throws SAXParseException
  {
    String value = attrs.getValue (name);
    if (value == null)
      return -1; // attribute not present

    value = value.trim ();
    for (int i = 0; i < enumValues.length; i++)
      if (enumValues[i].equals (value))
        return i;

    // wrong attribute value
    if (enumValues.length == 2)
      throw new SAXParseException ("Value of attribute '" +
                                   name +
                                   "' must be either '" +
                                   enumValues[0] +
                                   "' or '" +
                                   enumValues[1] +
                                   "' (found '" +
                                   value +
                                   "')",
                                   context.locator);
    else
    {
      String msg = "Value of attribute '" + name + "' must be one of ";
      for (int i = 0; i < enumValues.length - 1; i++)
        msg += "'" + enumValues[i] + "', ";
      msg += "or '" + enumValues[enumValues.length - 1] + "' (found '" + value + "')";
      throw new SAXParseException (msg, context.locator);
    }
  }

  /**
   * Looks for extra attributes and throws an exception if present
   * 
   * @param elementName
   *        the name of the parent element
   * @param attrs
   *        the attribute set
   * @param attNames
   *        a set of allowed attribute names
   * @param context
   *        the parse context
   * @exception SAXParseException
   *            if an attribute was found that is not in <code>attNames</code>
   */
  protected static void checkAttributes (final String elementName,
                                         final Attributes attrs,
                                         final HashSet attNames,
                                         final ParseContext context) throws SAXParseException
  {
    final int len = attrs.getLength ();
    for (int i = 0; i < len; i++)
      if ("".equals (attrs.getURI (i)) && (attNames == null || !attNames.contains (attrs.getQName (i))))
        throw new SAXParseException ("'" +
                                     elementName +
                                     "' must not have a '" +
                                     attrs.getQName (i) +
                                     "' attribute",
                                     context.locator);
  }

  /**
   * Parses a qualified name by extracting local name and namespace URI. The
   * result string has the form "{namespace-uri}local-name".
   * 
   * @param qName
   *        string representing the qualified name
   * @param context
   *        the parse context
   */
  protected static String getExpandedName (String qName, final ParseContext context) throws SAXParseException
  {
    final StringBuffer result = new StringBuffer ("{");

    qName = qName.trim ();
    final int colon = qName.indexOf (':');
    if (colon != -1)
    { // prefixed name
      final String prefix = qName.substring (0, colon);
      final String uri = (String) context.nsSet.get (prefix);
      if (uri == null)
        throw new SAXParseException ("Undeclared prefix '" + prefix + "'", context.locator);
      result.append (uri);
      qName = qName.substring (colon + 1); // the local part
    }
    // else: nothing to do for the namespace-uri, because
    // the default namespace is not used

    return result.append ('}').append (qName).toString ();
  }

  /**
   * Parses the string given in <code>string</code> as a pattern.
   * 
   * @param string
   *        the string to be parsed
   * @param context
   *        the parse context
   * @return a {@link Tree} representation of the pattern
   * @exception SAXParseException
   *            if a parse error occured
   */
  protected static Tree parsePattern (final String string, final ParseContext context) throws SAXParseException
  {
    if (string == null)
      return null;

    final StringReader sr = new StringReader (string);
    final Yylex lexer = new Yylex (sr);
    final PatternParser parser = new PatternParser (lexer, context);

    Tree pattern;
    try
    {
      pattern = (Tree) parser.parse ().value;
      if (lexer.withinComment > 0)
        throw new SAXParseException ("Syntax error, encountered end of pattern within a comment.", context.locator);
    }
    catch (final SAXParseException e)
    {
      throw e;
    }
    catch (final Exception e)
    {
      if (parser.errorToken.sym == Sym.EOF)
      {
        if (lexer.withinComment > 0)
          throw new SAXParseException (e.getMessage () +
                                       "Encountered end of pattern within a comment.",
                                       context.locator);
        else
          if (lexer.last != null)
            throw new SAXParseException (e.getMessage () +
                                         "Encountered end of pattern after '" +
                                         lexer.last.value +
                                         "'.",
                                         context.locator);
          else
            throw new SAXParseException (e.getMessage () + "Found empty pattern.", context.locator);
      }
      else
        throw new SAXParseException (e.getMessage () + "Found '" + lexer.last.value + "'.", context.locator);
    }
    return pattern;
  }

  /**
   * @see #getRequiredAttribute(String, Attributes, String, ParseContext)
   * @see #parsePattern(String, ParseContext)
   */
  protected static Tree parseRequiredPattern (final String elName,
                                              final Attributes attrs,
                                              final String attName,
                                              final ParseContext context) throws SAXParseException
  {
    return parsePattern (getRequiredAttribute (elName, attrs, attName, context), context);
  }

  /**
   * Parses the string given in <code>string</code> as an expression
   * 
   * @param string
   *        the string to be parsed
   * @param context
   *        the parse context
   * @return a {@link Tree} representation of the expression or
   *         <code>null</code> if <code>string</code> was <code>null</code>
   * @exception SAXParseException
   *            if a parse error occured
   */
  public static Tree parseExpr (final String string, final ParseContext context) throws SAXParseException
  {
    if (string == null)
      return null;

    final StringReader sr = new StringReader (string);
    final Yylex lexer = new Yylex (sr);
    final ExprParser parser = new ExprParser (lexer, context);
    Tree expr;
    try
    {
      expr = (Tree) parser.parse ().value;
      if (lexer.withinComment > 0)
        throw new SAXParseException ("Syntax error, " +
                                     "encountered end of expression within a comment.",
                                     context.locator);
    }
    catch (final SAXParseException e)
    {
      throw e;
    }
    catch (final Exception e)
    {
      if (parser.errorToken.sym == Sym.EOF)
      {
        if (lexer.withinComment > 0)
          throw new SAXParseException (e.getMessage () +
                                       "Encountered end of expression within a comment.",
                                       context.locator);
        else
          if (lexer.last != null)
            throw new SAXParseException (e.getMessage () +
                                         "Encountered end of expression after '" +
                                         lexer.last.value +
                                         "'.",
                                         context.locator);
          else
            throw new SAXParseException (e.getMessage () + "Found empty expression.", context.locator);
      }
      else
        throw new SAXParseException (e.getMessage () + "Found '" + lexer.last.value + "'.", context.locator);
    }
    return expr;
  }

  /**
   * @see #getRequiredAttribute(String, Attributes, String, ParseContext)
   * @see #parseExpr(String, ParseContext)
   */
  protected static Tree parseRequiredExpr (final String elName,
                                           final Attributes attrs,
                                           final String attName,
                                           final ParseContext context) throws SAXParseException
  {
    return parseExpr (getRequiredAttribute (elName, attrs, attName, context), context);
  }

  /**
   * state for the finite state machine implemented in {@link #parseAVT
   * parseAVT}
   */
  private static final int ATT_STATE = 0, LBRACE_STATE = 1, RBRACE_STATE = 2, EXPR_STATE = 3, STR_STATE = 4;

  /**
   * Parses an attribute value template (AVT) and creates a Tree (of AVT nodes)
   * which works similar to the concat function.
   * 
   * @param string
   *        the string to be parsed
   * @param context
   *        the parse context
   * @return a {@link Tree} representation of the AVT or <code>null</code> if
   *         <code>string</code> was <code>null</code>
   * @exception SAXParseException
   *            if a parse error occured
   */
  protected static Tree parseAVT (final String string, final ParseContext context) throws SAXParseException
  {
    if (string == null)
      return null;

    final int length = string.length ();
    final StringBuffer buf = new StringBuffer ();
    Tree tree = null;

    // this is a finite state machine
    int state = ATT_STATE;
    char delimiter = '\0';

    for (int index = 0; index < length; index++)
    {
      final char c = string.charAt (index);
      switch (state)
      {
        case ATT_STATE:
          switch (c)
          {
            case '{':
              state = LBRACE_STATE;
              continue;
            case '}':
              state = RBRACE_STATE;
              continue;
            default:
              buf.append (c);
              continue;
          }
        case LBRACE_STATE:
          if (c == '{')
          {
            buf.append (c);
            state = ATT_STATE;
            continue;
          }
          else
          {
            if (buf.length () != 0)
            {
              tree = new AvtTree (tree, new StringTree (buf.toString ()));
              buf.setLength (0);
            }
            state = EXPR_STATE;
            index--; // put back one character
            continue;
          }
        case RBRACE_STATE:
          if (c == '}')
          {
            buf.append (c);
            state = ATT_STATE;
            continue;
          }
          else
            throw new SAXParseException ("Invalid attribute value template: found unmatched '}' " +
                                         "at position " +
                                         index,
                                         context.locator);
        case EXPR_STATE:
          switch (c)
          {
            case '}':
              tree = new AvtTree (tree, parseExpr (buf.toString (), context));
              buf.setLength (0);
              state = ATT_STATE;
              continue;
            case '\'':
              buf.append (c);
              state = STR_STATE;
              delimiter = c;
              continue;
          }
        case STR_STATE:
          if (c == delimiter)
            state = EXPR_STATE;
          buf.append (c);
          continue;
      }
    }
    switch (state)
    {
      case LBRACE_STATE:
      case EXPR_STATE:
        throw new SAXParseException ("Invalid attribute value template: missing '}'.", context.locator);
      case RBRACE_STATE:
        throw new SAXParseException ("Invalid attribute value template: found single '}' at the end.", context.locator);
      case STR_STATE:
        throw new SAXParseException ("Invalid attribute value template: unterminated string.", context.locator);
    }

    if (buf.length () != 0)
    {
      tree = new AvtTree (tree, new StringTree (buf.toString ()));
    }

    // empty String?
    if (tree == null)
      tree = new StringTree ("");
    return tree;
  }

  /**
   * @see #getRequiredAttribute(String, Attributes, String, ParseContext)
   * @see #parseAVT(String, ParseContext)
   */
  protected static Tree parseRequiredAVT (final String elName,
                                          final Attributes attrs,
                                          final String attName,
                                          final ParseContext context) throws SAXParseException
  {
    return parseAVT (getRequiredAttribute (elName, attrs, attName, context), context);
  }
}
