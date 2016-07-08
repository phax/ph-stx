/*
 * $Id: JDK15RegexTranslator.java,v 1.1 2007/06/04 19:57:37 obecker Exp $
 *
 * Copied from Michael Kay's Saxon 8.9
 * Local changes (excluding package declarations and imports) marked as // OB
 */

package net.sf.joost.util.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.joost.util.om.FastStringBuffer;
import net.sf.joost.util.om.Whitespace;

/**
 * This class translates XML Schema regex syntax into JDK 1.5 regex syntax. This
 * differs from the JDK 1.4 translator because JDK 1.5 handles non-BMP
 * characters (wide characters) in places where JDK 1.4 does not, for example in
 * a range such as [X-Y]. This enables much of the code from the 1.4 translator
 * to be removed. Author: James Clark Modified by Michael Kay (a) to integrate
 * the code into Saxon, and (b) to support XPath additions to the XML Schema
 * regex syntax. This version also removes most of the complexities of handling
 * non-BMP characters, since JDK 1.5 handles these natively.
 */
public class JDK15RegexTranslator extends RegexTranslator
{

  // TODO: retrofit the changes for handling caseBlind comparison to the JDK 1.4
  // translator

  /**
   * Translates XML Schema and XPath regexes into <code>java.util.regex</code>
   * regexes.
   *
   * @see java.util.regex.Pattern
   * @see <a href="http://www.w3.org/TR/xmlschema-2/#regexs">XML Schema Part
   *      2</a>
   */

  public static final CharClass [] categoryCharClasses = new CharClass [RegexData.categories.length ()];
  public static final CharClass [] subCategoryCharClasses = new CharClass [RegexData.subCategories.length () / 2];

  /**
   * CharClass for each block name in specialBlockNames.
   */
  public static final CharClass [] specialBlockCharClasses = { new CharRange (0x10300, 0x1032F),
                                                               new CharRange (0x10330, 0x1034F),
                                                               new CharRange (0x10400, 0x1044F),
                                                               new CharRange (0x1D000, 0x1D0FF),
                                                               new CharRange (0x1D100, 0x1D1FF),
                                                               new CharRange (0x1D400, 0x1D7FF),
                                                               new CharRange (0x20000, 0x2A6D6),
                                                               new CharRange (0x2F800, 0x2FA1F),
                                                               new CharRange (0xE0000, 0xE007F),
                                                               new Union (new CharClass [] { new CharRange (0xE000,
                                                                                                            0xF8FF),
                                                                                             new CharRange (0xF0000,
                                                                                                            0xFFFFD),
                                                                                             new CharRange (0x100000,
                                                                                                            0x10FFFD) }),
                                                               Empty.getInstance (),
                                                               Empty.getInstance (),
                                                               Empty.getInstance () };

  private static final CharClass DOT_SCHEMA = new Complement (new Union (new CharClass [] { new SingleChar ('\n'),
                                                                                            new SingleChar ('\r') }));

  private static final CharClass ESC_d = new Property ("Nd");

  private static final CharClass ESC_D = new Complement (ESC_d);

  private static final CharClass ESC_W = new Union (new CharClass [] { computeCategoryCharClass ('P'),
                                                                       computeCategoryCharClass ('Z'),
                                                                       computeCategoryCharClass ('C') });
  // was: new Property("P"), new Property("Z"), new Property("C") }

  private static final CharClass ESC_w = new Complement (ESC_W);

  private static final CharClass ESC_s = new Union (new CharClass [] { new SingleChar (' '),
                                                                       new SingleChar ('\n'),
                                                                       new SingleChar ('\r'),
                                                                       new SingleChar ('\t') });

  private static final CharClass ESC_S = new Complement (ESC_s);

  private static final CharClass ESC_i = makeCharClass (RegexData.NMSTRT_CATEGORIES,
                                                        RegexData.NMSTRT_INCLUDES,
                                                        RegexData.NMSTRT_EXCLUDE_RANGES);

  private static final CharClass ESC_I = new Complement (ESC_i);

  private static final CharClass ESC_c = makeCharClass (RegexData.NMCHAR_CATEGORIES,
                                                        RegexData.NMCHAR_INCLUDES,
                                                        RegexData.NMCHAR_EXCLUDE_RANGES);

  private static final CharClass ESC_C = new Complement (ESC_c);

  private JDK15RegexTranslator (final CharSequence regExp)
  {
    this.regExp = regExp;
    this.length = regExp.length ();
  }

  /**
   * Translates a regular expression in the syntax of XML Schemas Part 2 into a
   * regular expression in the syntax of <code>java.util.regex.Pattern</code>.
   * The translation assumes that the string to be matched against the regex
   * uses surrogate pairs correctly. If the string comes from XML content, a
   * conforming XML parser will automatically check this; if the string comes
   * from elsewhere, it may be necessary to check surrogate usage before
   * matching.
   *
   * @param regexp
   *        a String containing a regular expression in the syntax of XML
   *        Schemas Part 2
   * @param xpath
   *        a boolean indicating whether the XPath 2.0 F+O extensions to the
   *        schema regex syntax are permitted
   * @throws net.sf.saxon.regex.RegexSyntaxException
   *         if <code>regexp</code> is not a regular expression in the syntax of
   *         XML Schemas Part 2, or XPath 2.0, as appropriate
   * @see java.util.regex.Pattern
   * @see <a href="http://www.w3.org/TR/xmlschema-2/#regexs">XML Schema Part
   *      2</a>
   */
  public static String translate (final CharSequence regexp,
                                  final boolean xpath,
                                  final boolean ignoreWhitespace,
                                  final boolean caseBlind) throws RegexSyntaxException
  {

    // System.err.println("Input regex: " + regexp);
    final JDK15RegexTranslator tr = new JDK15RegexTranslator (regexp);
    tr.isXPath = xpath;
    tr.ignoreWhitespace = ignoreWhitespace;
    tr.caseBlind = caseBlind;
    tr.advance ();
    tr.translateTop ();
    // System.err.println("Output regex: " + tr.result.toString());
    return tr.result.toString ();
  }

  static abstract class CharClass
  {

    protected CharClass ()
    {}

    abstract void output (FastStringBuffer buf);

    abstract void outputComplement (FastStringBuffer buf);

    int getSingleChar ()
    {
      return -1;
    }

  }

  static abstract class SimpleCharClass extends CharClass
  {
    SimpleCharClass ()
    {

    }

    @Override
    void output (final FastStringBuffer buf)
    {
      buf.append ('[');
      inClassOutput (buf);
      buf.append (']');
    }

    @Override
    void outputComplement (final FastStringBuffer buf)
    {
      buf.append ("[^");
      inClassOutput (buf);
      buf.append (']');
    }

    abstract void inClassOutput (FastStringBuffer buf);
  }

  static class SingleChar extends SimpleCharClass
  {
    private final int c;

    SingleChar (final int c)
    {
      this.c = c;
    }

    @Override
    int getSingleChar ()
    {
      return c;
    }

    @Override
    void output (final FastStringBuffer buf)
    {
      inClassOutput (buf);
    }

    @Override
    void inClassOutput (final FastStringBuffer buf)
    {
      if (isJavaMetaChar (c))
      {
        buf.append ('\\');
        buf.append ((char) c);
      }
      else
      {
        switch (c)
        {
          case '\r':
            buf.append ("\\r");
            break;
          case '\n':
            buf.append ("\\n");
            break;
          case '\t':
            buf.append ("\\t");
            break;
          case ' ':
            buf.append ("\\x20");
            break;
          default:
            buf.appendWideChar (c);
        }
      }
      return;
    }

  }

  static class Empty extends SimpleCharClass
  {
    private static final Empty instance = new Empty ();

    private Empty ()
    {

    }

    static Empty getInstance ()
    {
      return instance;
    }

    @Override
    void output (final FastStringBuffer buf)
    {
      buf.append ("\\x00"); // no character matches
    }

    @Override
    void outputComplement (final FastStringBuffer buf)
    {
      buf.append ("[^\\x00]"); // every character matches
    }

    @Override
    void inClassOutput (final FastStringBuffer buf)
    {
      throw new RuntimeException ("BMP output botch");
    }

  }

  static class CharRange extends SimpleCharClass
  {
    private final int lower;
    private final int upper;

    CharRange (final int lower, final int upper)
    {
      this.lower = lower;
      this.upper = upper;
    }

    @Override
    void inClassOutput (final FastStringBuffer buf)
    {
      if (isJavaMetaChar (lower))
      {
        buf.append ('\\');
      }
      buf.appendWideChar (lower);
      buf.append ('-');
      if (isJavaMetaChar (upper))
      {
        buf.append ('\\');
      }
      buf.appendWideChar (upper);
    }

  }

  static class Property extends SimpleCharClass
  {
    private final String name;

    Property (final String name)
    {
      this.name = name;
    }

    @Override
    void inClassOutput (final FastStringBuffer buf)
    {
      buf.append ("\\p{");
      buf.append (name);
      buf.append ('}');
    }

    @Override
    void outputComplement (final FastStringBuffer buf)
    {
      buf.append ("\\P{");
      buf.append (name);
      buf.append ('}');
    }
  }

  static class Subtraction extends CharClass
  {
    private final CharClass cc1;
    private final CharClass cc2;

    Subtraction (final CharClass cc1, final CharClass cc2)
    {
      // min corresponds to intersection
      // complement corresponds to negation
      this.cc1 = cc1;
      this.cc2 = cc2;
    }

    @Override
    void output (final FastStringBuffer buf)
    {
      buf.append ('[');
      cc1.output (buf);
      buf.append ("&&");
      cc2.outputComplement (buf);
      buf.append (']');
    }

    @Override
    void outputComplement (final FastStringBuffer buf)
    {
      buf.append ('[');
      cc1.outputComplement (buf);
      cc2.output (buf);
      buf.append (']');
    }
  }

  static class Union extends CharClass
  {
    private final List members;

    Union (final CharClass [] v)
    {
      this (toList (v));
    }

    private static List toList (final CharClass [] v)
    {
      final List members = new ArrayList (5);
      for (final CharClass element : v)
        members.add (element);
      return members;
    }

    Union (final List members)
    {
      this.members = members;
    }

    @Override
    void output (final FastStringBuffer buf)
    {
      buf.append ('[');
      for (int i = 0, len = members.size (); i < len; i++)
      {
        final CharClass cc = (CharClass) members.get (i);
        cc.output (buf);
      }
      buf.append (']');
    }

    @Override
    void outputComplement (final FastStringBuffer buf)
    {
      boolean first = true;
      final int len = members.size ();
      for (int i = 0; i < len; i++)
      {
        final CharClass cc = (CharClass) members.get (i);
        if (cc instanceof SimpleCharClass)
        {
          if (first)
          {
            buf.append ("[^");
            first = false;
          }
          ((SimpleCharClass) cc).inClassOutput (buf);
        }
      }
      for (int i = 0; i < len; i++)
      {
        final CharClass cc = (CharClass) members.get (i);
        if (!(cc instanceof SimpleCharClass))
        {
          if (first)
          {
            buf.append ('[');
            first = false;
          }
          else
          {
            buf.append ("&&");
          }
          cc.outputComplement (buf);
        }
      }
      if (first == true)
      {
        // empty union, so the complement is everything
        buf.append ("[\u0001-");
        buf.appendWideChar (RegexData.NONBMP_MAX);
        buf.append ("]");
      }
      else
      {
        buf.append (']');
      }
    }
  }

  static class BackReference extends CharClass
  {
    private final int i;

    BackReference (final int i)
    {
      this.i = i;
    }

    @Override
    void output (final FastStringBuffer buf)
    {
      inClassOutput (buf);
    }

    @Override
    void outputComplement (final FastStringBuffer buf)
    {
      inClassOutput (buf);
    }

    void inClassOutput (final FastStringBuffer buf)
    {
      if (i != -1)
      {
        buf.append ("(?:\\" + i + ")"); // terminate the back-reference with a
                                        // syntactic separator
      }
      else
      {
        buf.append ("(?:)"); // matches a zero-length string, while allowing a
                             // quantifier
      }
    }
  }

  static class Complement extends CharClass
  {
    private final CharClass cc;

    Complement (final CharClass cc)
    {
      this.cc = cc;
    }

    @Override
    void output (final FastStringBuffer buf)
    {
      cc.outputComplement (buf);
    }

    @Override
    void outputComplement (final FastStringBuffer buf)
    {
      cc.output (buf);
    }
  }

  @Override
  protected boolean translateAtom () throws RegexSyntaxException
  {
    switch (curChar)
    {
      case RegexData.EOS:
        if (!eos)
          break;
        // else fall through
      case '?':
      case '*':
      case '+':
      case ')':
      case '{':
      case '}':
      case '|':
      case ']':
        return false;
      case '(':
        copyCurChar ();
        final int thisCapture = ++currentCapture;
        translateRegExp ();
        expect (')');
        captures.add (thisCapture);
        copyCurChar ();
        return true;
      case '\\':
        advance ();
        parseEsc ().output (result);
        return true;
      case '[':
        inCharClassExpr = true;
        advance ();
        parseCharClassExpr ().output (result);
        return true;
      case '.':
        if (isXPath)
        {
          // under XPath, "." has the same meaning as in JDK 1.5
          break;
        }
        else
        {
          // under XMLSchema, "." means anything except \n or \r, which is
          // different from the XPath/JDK rule
          DOT_SCHEMA.output (result);
          advance ();
          return true;
        }
      case '$':
      case '^':
        if (isXPath)
        {
          copyCurChar ();
          return true;
        }
        result.append ('\\');
        break;
      default:
        if (caseBlind)
        {
          final int thisChar = absorbSurrogatePair ();
          final int [] variants = CaseVariants.getCaseVariants (thisChar);
          if (variants.length > 0)
          {
            final CharClass [] chars = new CharClass [variants.length + 1];
            chars[0] = new SingleChar (thisChar);
            for (int i = 0; i < variants.length; i++)
            {
              chars[i + 1] = new SingleChar (variants[i]);
            }
            final Union union = new Union (chars);
            union.output (result);
            advance ();
            return true;
          }
          // else fall through
        }
        // else fall through
    }
    copyCurChar ();
    return true;
  }

  private static CharClass makeCharClass (final String categories, final String includes, final String excludeRanges)
  {
    final List includeList = new ArrayList (5);
    for (int i = 0, len = categories.length (); i < len; i += 2)
      includeList.add (new Property (categories.substring (i, i + 2)));
    for (int i = 0, len = includes.length (); i < len; i++)
    {
      int j = i + 1;
      for (; j < len && includes.charAt (j) - includes.charAt (i) == j - i; j++)
        ;
      --j;
      if (i == j - 1)
        --j;
      if (i == j)
        includeList.add (new SingleChar (includes.charAt (i)));
      else
        includeList.add (new CharRange (includes.charAt (i), includes.charAt (j)));
      i = j;
    }
    final List excludeList = new ArrayList (5);
    for (int i = 0, len = excludeRanges.length (); i < len; i += 2)
    {
      final char min = excludeRanges.charAt (i);
      final char max = excludeRanges.charAt (i + 1);
      if (min == max)
        excludeList.add (new SingleChar (min));
      else
        if (min == max - 1)
        {
          excludeList.add (new SingleChar (min));
          excludeList.add (new SingleChar (max));
        }
        else
          excludeList.add (new CharRange (min, max));
    }
    return new Subtraction (new Union (includeList), new Union (excludeList));
  }

  private CharClass parseEsc () throws RegexSyntaxException
  {
    switch (curChar)
    {
      case 'n':
        advance ();
        return new SingleChar ('\n');
      case 'r':
        advance ();
        return new SingleChar ('\r');
      case 't':
        advance ();
        return new SingleChar ('\t');
      case '\\':
      case '|':
      case '.':
      case '-':
      case '^':
      case '?':
      case '*':
      case '+':
      case '(':
      case ')':
      case '{':
      case '}':
      case '[':
      case ']':
        break;
      case 's':
        advance ();
        return ESC_s;
      case 'S':
        advance ();
        return ESC_S;
      case 'i':
        advance ();
        return ESC_i;
      case 'I':
        advance ();
        return ESC_I;
      case 'c':
        advance ();
        return ESC_c;
      case 'C':
        advance ();
        return ESC_C;
      case 'd':
        advance ();
        return ESC_d;
      case 'D':
        advance ();
        return ESC_D;
      case 'w':
        advance ();
        return ESC_w;
      case 'W':
        advance ();
        return ESC_W;
      case 'p':
        advance ();
        return parseProp ();
      case 'P':
        advance ();
        return new Complement (parseProp ());
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        if (isXPath)
        {
          final char c = curChar;
          final int c0 = (c - '0');
          advance ();
          final int c1 = "0123456789".indexOf (curChar);
          if (c1 >= 0)
          {
            // limit a back-reference to two digits, but only allow two if there
            // is such a capture
            // TODO: the spec does not limit the number of digits in a
            // back-reference to two.
            final int n = c0 * 10 + c1;
            advance ();
            if (captures.contains (n))
            {
              // treat it as a two-digit back-reference
              return new BackReference (n);
            }
            else
            {
              recede ();
            }
          }
          if (captures.contains (c0))
          {
            return new BackReference (c0);
          }
          else
          {
            // match a zero-length string
            return new BackReference (-1);
          }
        }
        else
        {
          throw makeException ("digit not allowed after \\");
        }
      case '$':
        if (isXPath)
        {
          break;
        }
        // otherwise fall through
      default:
        throw makeException ("invalid escape sequence");
    }
    final CharClass tem = new SingleChar (curChar);
    advance ();
    return tem;
  }

  private CharClass parseProp () throws RegexSyntaxException
  {
    expect ('{');
    final int start = pos;
    for (;;)
    {
      advance ();
      if (curChar == '}')
        break;
      if (!isAsciiAlnum (curChar) && curChar != '-')
        expect ('}');
    }
    CharSequence propertyNameCS = regExp.subSequence (start, pos - 1);
    if (ignoreWhitespace && !inCharClassExpr)
    {
      propertyNameCS = Whitespace.removeAllWhitespace (propertyNameCS);
    }
    final String propertyName = propertyNameCS.toString ();
    advance ();
    switch (propertyName.length ())
    {
      case 0:
        throw makeException ("empty property name");
      case 2:
        final int sci = RegexData.subCategories.indexOf (propertyName);
        if (sci < 0 || sci % 2 == 1)
          throw makeException ("unknown category");
        return getSubCategoryCharClass (sci / 2);
      case 1:
        final int ci = RegexData.categories.indexOf (propertyName.charAt (0));
        if (ci < 0)
          throw makeException ("unknown category", propertyName);
        return getCategoryCharClass (ci);
      default:
        if (!propertyName.startsWith ("Is"))
          break;
        final String blockName = propertyName.substring (2);
        for (int i = 0; i < RegexData.specialBlockNames.length; i++)
          if (blockName.equals (RegexData.specialBlockNames[i]))
            return specialBlockCharClasses[i];
        if (!isBlock (blockName))
          throw makeException ("invalid block name", blockName);
        return new Property ("In" + blockName);
    }
    throw makeException ("invalid property name", propertyName);
  }

  private CharClass parseCharClassExpr () throws RegexSyntaxException
  {
    boolean compl;
    if (curChar == '^')
    {
      advance ();
      compl = true;
    }
    else
    {
      compl = false;
    }
    final List members = new ArrayList (10);
    // boolean firstOrLast = true;
    do
    {
      final CharClass lower = parseCharClassEscOrXmlChar (true);
      members.add (lower);
      if (curChar == ']' || eos)
      {
        addCaseVariant (lower, members);
        break;
      }
      // firstOrLast = isLastInGroup();
      if (curChar == '-')
      {
        final char next = regExp.charAt (pos);
        if (next == '[')
        {
          // hyphen denotes subtraction
          // TODO: subtraction can't be used in a negative character group
          addCaseVariant (lower, members);
          advance ();
          break;
        }
        else
          if (next == ']')
          {
            // hyphen denotes a regular character - no need to do anything
            addCaseVariant (lower, members);
            // TODO: the spec rules are unclear here. We are allowing hyphen to
            // represent
            // itself in contexts like [A-Z-0-9]
          }
          else
          {
            // hyphen denotes a character range
            advance ();
            final CharClass upper = parseCharClassEscOrXmlChar (true);
            if (lower.getSingleChar () < 0 || upper.getSingleChar () < 0)
              throw makeException ("multi_range");
            if (lower.getSingleChar () > upper.getSingleChar ())
              throw makeException ("invalid range (start > end)");
            members.set (members.size () - 1, new CharRange (lower.getSingleChar (), upper.getSingleChar ()));
            if (caseBlind)
            {
              // Special-case A-Z and a-z
              if (lower.getSingleChar () == 'a' && upper.getSingleChar () == 'z')
              {
                members.add (new CharRange ('A', 'Z'));
                for (final int element : CaseVariants.ROMAN_VARIANTS)
                {
                  members.add (new SingleChar (element));
                }
              }
              else
                if (lower.getSingleChar () == 'A' && upper.getSingleChar () == 'Z')
                {
                  members.add (new CharRange ('a', 'z'));
                  for (final int element : CaseVariants.ROMAN_VARIANTS)
                  {
                    members.add (new SingleChar (element));
                  }
                }
                else
                {
                  for (int k = lower.getSingleChar (); k <= upper.getSingleChar (); k++)
                  {
                    final int [] variants = CaseVariants.getCaseVariants (k);
                    for (final int variant : variants)
                    {
                      members.add (new SingleChar (variant));
                    }
                  }
                }
            }
            // look for a subtraction
            if (curChar == '-' && regExp.charAt (pos) == '[')
            {
              advance ();
              // expect('[');
              break;
            }
          }
      }
      else
      {
        addCaseVariant (lower, members);
      }
    } while (curChar != ']');
    if (eos)
    {
      expect (']');
    }
    CharClass result;
    if (members.size () == 1)
      result = (CharClass) members.get (0);
    else
      result = new Union (members);
    if (compl)
      result = new Complement (result);
    if (curChar == '[')
    {
      advance ();
      result = new Subtraction (result, parseCharClassExpr ());
      expect (']');
    }
    inCharClassExpr = false;
    advance ();
    return result;
  }

  private void addCaseVariant (final CharClass lower, final List members)
  {
    if (caseBlind)
    {
      final int [] variants = CaseVariants.getCaseVariants (lower.getSingleChar ());
      for (final int variant : variants)
      {
        members.add (new SingleChar (variant));
      }
    }
  }

  private boolean isLastInGroup ()
  {
    // look ahead at the next character
    final char c = regExp.charAt (pos);
    return (c == ']' || c == '[');
  }

  private CharClass parseCharClassEscOrXmlChar (final boolean firstOrLast) throws RegexSyntaxException
  {
    switch (curChar)
    {
      case RegexData.EOS:
        if (eos)
          expect (']');
        break;
      case '\\':
        advance ();
        return parseEsc ();
      case '[':
      case ']':
        throw makeException ("character must be escaped", new String (new char [] { curChar }));
      case '-':
        // if (!firstOrLast) {
        // throw makeException("character must be escaped", new String(new
        // char[]{curChar}));
        // }
        break;
    }
    final CharClass tem = new SingleChar (absorbSurrogatePair ());
    advance ();
    return tem;
  }

  private static synchronized CharClass getCategoryCharClass (final int ci)
  {
    if (categoryCharClasses[ci] == null)
      categoryCharClasses[ci] = computeCategoryCharClass (RegexData.categories.charAt (ci));
    return categoryCharClasses[ci];
  }

  private static synchronized CharClass getSubCategoryCharClass (final int sci)
  {
    if (subCategoryCharClasses[sci] == null)
      subCategoryCharClasses[sci] = computeSubCategoryCharClass (RegexData.subCategories.substring (sci *
                                                                                                    2,
                                                                                                    (sci + 1) * 2));
    return subCategoryCharClasses[sci];
  }

  private static CharClass computeCategoryCharClass (final char code)
  {
    final List classes = new ArrayList (5);
    classes.add (new Property (new String (new char [] { code })));
    for (int ci = RegexData.CATEGORY_NAMES.indexOf (code); ci >= 0; ci = RegexData.CATEGORY_NAMES.indexOf (code,
                                                                                                           ci + 1))
    {
      final int [] addRanges = RegexData.CATEGORY_RANGES[ci / 2];
      for (int i = 0; i < addRanges.length; i += 2)
        classes.add (new CharRange (addRanges[i], addRanges[i + 1]));
    }
    if (code == 'P')
      classes.add (makeCharClass (RegexData.CATEGORY_Pi + RegexData.CATEGORY_Pf));
    if (code == 'L')
    {
      classes.add (new SingleChar (RegexData.UNICODE_3_1_ADD_Ll));
      classes.add (new SingleChar (RegexData.UNICODE_3_1_ADD_Lu));
    }
    if (code == 'C')
    {
      // JDK 1.4 leaves Cn out of C?
      classes.add (new Subtraction (new Property ("Cn"),
                                    new Union (new CharClass [] { new SingleChar (RegexData.UNICODE_3_1_ADD_Lu),
                                                                  new SingleChar (RegexData.UNICODE_3_1_ADD_Ll) })));
      final List assignedRanges = new ArrayList (5);
      for (final int [] element : RegexData.CATEGORY_RANGES)
        for (int j = 0; j < element.length; j += 2)
          assignedRanges.add (new CharRange (element[j], element[j + 1]));
      classes.add (new Subtraction (new CharRange (RegexData.NONBMP_MIN, RegexData.NONBMP_MAX),
                                    new Union (assignedRanges)));
    }
    if (classes.size () == 1)
      return (CharClass) classes.get (0);
    return new Union (classes);
  }

  private static CharClass computeSubCategoryCharClass (final String name)
  {
    final CharClass base = new Property (name);
    final int sci = RegexData.CATEGORY_NAMES.indexOf (name);
    if (sci < 0)
    {
      if (name.equals ("Cn"))
      {
        // Unassigned
        final List assignedRanges = new ArrayList (5);
        assignedRanges.add (new SingleChar (RegexData.UNICODE_3_1_ADD_Lu));
        assignedRanges.add (new SingleChar (RegexData.UNICODE_3_1_ADD_Ll));
        for (final int [] element : RegexData.CATEGORY_RANGES)
          for (int j = 0; j < element.length; j += 2)
            assignedRanges.add (new CharRange (element[j], element[j + 1]));
        return new Subtraction (new Union (new CharClass [] { base,
                                                              new CharRange (RegexData.NONBMP_MIN,
                                                                             RegexData.NONBMP_MAX) }),
                                new Union (assignedRanges));
      }
      if (name.equals ("Pi"))
        return makeCharClass (RegexData.CATEGORY_Pi);
      if (name.equals ("Pf"))
        return makeCharClass (RegexData.CATEGORY_Pf);
      return base;
    }
    final List classes = new ArrayList (5);
    classes.add (base);
    final int [] addRanges = RegexData.CATEGORY_RANGES[sci / 2];
    for (int i = 0; i < addRanges.length; i += 2)
      classes.add (new CharRange (addRanges[i], addRanges[i + 1]));
    if (name.equals ("Lu"))
      classes.add (new SingleChar (RegexData.UNICODE_3_1_ADD_Lu));
    else
      if (name.equals ("Ll"))
        classes.add (new SingleChar (RegexData.UNICODE_3_1_ADD_Ll));
      else
        if (name.equals ("Nl"))
          classes.add (new CharRange (RegexData.UNICODE_3_1_CHANGE_No_to_Nl_MIN,
                                      RegexData.UNICODE_3_1_CHANGE_No_to_Nl_MAX));
        else
          if (name.equals ("No"))
            return new Subtraction (new Union (classes),
                                    new CharRange (RegexData.UNICODE_3_1_CHANGE_No_to_Nl_MIN,
                                                   RegexData.UNICODE_3_1_CHANGE_No_to_Nl_MAX));
    return new Union (classes);
  }

  private static CharClass makeCharClass (final String members)
  {
    final List list = new ArrayList (5);
    for (int i = 0, len = members.length (); i < len; i++)
      list.add (new SingleChar (members.charAt (i)));
    return new Union (list);
  }

  public static void main (final String [] args) throws RegexSyntaxException
  {
    final String s = translate (args[0], args[1].equals ("xpath"), false, true);
    for (int i = 0, len = s.length (); i < len; i++)
    {
      final char c = s.charAt (i);
      if (c >= 0x20 && c <= 0x7e)
        System.err.print (c);
      else
      {
        System.err.print ("\\u");
        for (int shift = 12; shift >= 0; shift -= 4)
          System.err.print ("0123456789ABCDEF".charAt ((c >> shift) & 0xF));
      }
    }
    try
    {
      Pattern.compile (s);
    }
    catch (final Exception err)
    {
      System.err.println ("Error: " + err.getMessage ());
    }
    System.err.println ();
  }

  // }

}

//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file except changes marked.
//
// The Initial Developer of the Original Code is James Clark
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): Michael Kay
//
