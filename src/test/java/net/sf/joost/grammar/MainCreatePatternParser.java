package net.sf.joost.grammar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.helger.commons.charset.CCharset;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.string.StringHelper;

import java_cup.internal_error;

public class MainCreatePatternParser
{
  public static void main (final String [] args) throws internal_error, IOException, Exception
  {
    final Charset aCharset = CCharset.CHARSET_ISO_8859_1_OBJ;
    final String sBasePath = "src/main/resources/net/sf/joost/grammar/";
    java_cup.Main.main (new String [] { "-parser",
                                        "PatternParser",
                                        "-symbols",
                                        "Sym",
                                        "-package",
                                        "net.sf.joost.grammar.javacup",
                                        "-interface",
                                        "-compact_red",
                                        // "-nowarn",
                                        sBasePath + "PatternResolved.cup" });

    final String sDst = "src/main/java/net/sf/joost/grammar/";
    File aDst = new File (sDst, "PatternParser.java");
    aDst.delete ();
    new File (aDst.getName ()).renameTo (aDst);

    String s = SimpleFileIO.getFileAsString (aDst, aCharset);
    s = StringHelper.replaceAll (s, "java_cup.runtime.", "net.sf.joost.grammar.cup.");
    SimpleFileIO.writeFile (aDst, s, aCharset);

    aDst = new File (sDst, "Sym.java");
    aDst.delete ();
    new File (aDst.getName ()).renameTo (aDst);
  }
}
