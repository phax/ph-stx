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
package net.sf.joost.grammar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.string.StringHelper;

import java_cup.internal_error;

public class MainCreatePatternParser
{
  public static void main (final String [] args) throws internal_error, IOException, Exception
  {
    final Charset aCharset = StandardCharsets.ISO_8859_1;
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
