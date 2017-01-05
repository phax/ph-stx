/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.charset.CCharset;
import com.helger.commons.io.IHasInputStream;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.io.streamprovider.StringInputStreamProvider;
import com.helger.stx.model.STXPath;
import com.helger.stx.parser.CharStream;
import com.helger.stx.parser.ParseException;
import com.helger.stx.parser.ParserSTX;
import com.helger.stx.parser.ParserSTXConstants;
import com.helger.stx.parser.ParserSTXTokenManager;
import com.helger.stx.parser.STXCharStream;
import com.helger.stx.parser.STXNode;
import com.helger.stx.parser.Token;

/**
 * This is the central user class for reading and parsing XPath2 from different
 * sources.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class STXReader
{
  @PresentForCodeCoverage
  private static final STXReader s_aInstance = new STXReader ();

  private STXReader ()
  {}

  @Nonnull
  @Nonempty
  public static String createLoggingStringParseError (@Nonnull final ParseException ex)
  {
    if (ex.currentToken == null)
    {
      // Is null if the constructor with String only was used
      return ex.getMessage ();
    }
    return createLoggingStringParseError (ex.currentToken, ex.expectedTokenSequences, ex.tokenImage, null);
  }

  @Nonnull
  @Nonempty
  public static String createLoggingStringParseError (@Nonnull final Token aLastValidToken,
                                                      @Nonnull final int [] [] aExpectedTokenSequencesVal,
                                                      @Nonnull final String [] aTokenImageVal,
                                                      @Nullable final Token aLastSkippedToken)
  {
    ValueEnforcer.notNull (aLastValidToken, "LastValidToken");
    ValueEnforcer.notNull (aExpectedTokenSequencesVal, "ExpectedTokenSequencesVal");
    ValueEnforcer.notNull (aTokenImageVal, "TokenImageVal");

    final StringBuilder aExpected = new StringBuilder ();
    int nMaxSize = 0;
    for (final int [] aExpectedTokens : aExpectedTokenSequencesVal)
    {
      if (nMaxSize < aExpectedTokens.length)
        nMaxSize = aExpectedTokens.length;

      if (aExpected.length () > 0)
        aExpected.append (',');
      for (final int nExpectedToken : aExpectedTokens)
        aExpected.append (' ').append (aTokenImageVal[nExpectedToken]);
    }

    final StringBuilder retval = new StringBuilder (1024);
    retval.append ('[')
          .append (aLastValidToken.next.beginLine)
          .append (':')
          .append (aLastValidToken.next.beginColumn)
          .append (']');
    if (aLastSkippedToken != null)
    {
      retval.append ("-[")
            .append (aLastSkippedToken.endLine)
            .append (':')
            .append (aLastSkippedToken.endColumn)
            .append (']');
    }
    retval.append (" Encountered");
    Token aCurToken = aLastValidToken.next;
    for (int i = 0; i < nMaxSize; i++)
    {
      retval.append (' ');
      if (aCurToken.kind == ParserSTXConstants.EOF)
      {
        retval.append (aTokenImageVal[ParserSTXConstants.EOF]);
        break;
      }
      retval.append ("text '")
            .append (aCurToken.image)
            .append ("' corresponding to token ")
            .append (aTokenImageVal[aCurToken.kind]);
      aCurToken = aCurToken.next;
    }
    retval.append (". ");
    if (aLastSkippedToken != null)
      retval.append ("Skipped until token ").append (aLastSkippedToken).append (". ");
    retval.append (aExpectedTokenSequencesVal.length == 1 ? "Was expecting:" : "Was expecting one of:")
          .append (aExpected);
    return retval.toString ();
  }

  /**
   * Main reading of the CSS
   *
   * @param aCharStream
   *        The stream to read from. May not be <code>null</code>.
   * @param eVersion
   *        The CSS version to use. May not be <code>null</code>.
   * @param aCustomErrorHandler
   *        A custom handler for recoverable errors. May be <code>null</code>.
   * @param aCustomExceptionHandler
   *        A custom handler for unrecoverable errors. May not be
   *        <code>null</code>.
   * @return <code>null</code> if parsing failed with an unrecoverable error
   *         (and no throwing exception handler is used), or <code>null</code>
   *         if a recoverable error occurred and no
   *         {@link com.helger.xpath.reader.errorhandler.ThrowingCSSParseErrorHandler}
   *         was used or non-<code>null</code> if parsing succeeded.
   */
  @Nullable
  private static STXNode _readSTXPath (@Nonnull final CharStream aCharStream)
  {
    final ParserSTXTokenManager aTokenHdl = new ParserSTXTokenManager (aCharStream);
    final ParserSTX aParser = new ParserSTX (aTokenHdl);
    try
    {
      // Main parsing
      return aParser.STXPath ();
    }
    catch (final ParseException ex)
    {
      // Unrecoverable error
      System.err.println (createLoggingStringParseError (ex));
      return null;
    }
  }

  @Nullable
  public static STXPath readFromString (@Nonnull final String sData)
  {
    return readFromStream (new StringInputStreamProvider (sData, CCharset.CHARSET_UTF_8_OBJ),
                           CCharset.CHARSET_UTF_8_OBJ);
  }

  @Nullable
  public static STXPath readFromStream (@Nonnull final IHasInputStream aISP, @Nonnull final Charset aCharset)
  {
    ValueEnforcer.notNull (aISP, "InputStreamProvider");
    ValueEnforcer.notNull (aCharset, "Charset");

    final InputStream aIS = aISP.getInputStream ();
    final Reader aReader = StreamHelper.createReader (aIS, aCharset);
    try
    {
      final STXCharStream aCharStream = new STXCharStream (aReader);

      final STXNode aNode = _readSTXPath (aCharStream);

      // Failed to interpret content as CSS?
      if (aNode == null)
        return null;

      // Convert the AST to a domain object
      final STXPath ret = STXNodeToDomainObject.convertToDomainObject (aNode);
      return ret;
    }
    finally
    {
      StreamHelper.close (aReader);
    }
  }
}
