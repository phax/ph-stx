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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.util.regex;

import java.util.regex.Pattern;

import org.junit.Test;

public class JDK15RegexTranslatorTest
{
  @Test
  public void testBasic () throws RegexSyntaxException
  {
    final String sXPathRegEx = "^.*?doubles.*$";
    final boolean bXPath2FOExtensions = true;

    final String s = JDK15RegexTranslator.translate (sXPathRegEx, bXPath2FOExtensions, false, true);
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

}
