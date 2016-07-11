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
