package net.sf.joost.grammar;

import java.io.File;
import java.io.IOException;

import java_cup.internal_error;

public class MainCreateGrammar
{
  public static void main (final String [] args) throws internal_error, IOException, Exception
  {
    final String sBasePath = "src/main/resources/net/sf/joost/grammar/";
    final String [] aArgs = new String [] { "-parser",
                                            "PatternParser",
                                            "-symbols",
                                            "Sym",
                                            "-package",
                                            "net.sf.joost.grammar.javacup",
                                            "-interface",
                                            "-compact_red",
                                            // "-nowarn",
                                            sBasePath + "PatternResolved.cup" };
    java_cup.Main.main (aArgs);

    final String sDst = "src/main/java/net/sf/joost/grammar/";
    File aDst = new File (sDst, "PatternParser.java");
    aDst.delete ();
    new File (aDst.getName ()).renameTo (aDst);
    aDst = new File (sDst, "Sym.java");
    aDst.delete ();
    new File (aDst.getName ()).renameTo (aDst);
  }
}
