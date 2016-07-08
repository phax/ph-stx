package net.sf.joost.grammar;

import jflex.Main;

public class MainCreateLex
{
  public static void main (final String [] args) throws Exception
  {
    final String sBasePath = "src/main/resources/net/sf/joost/grammar/";
    Main.generate (new String [] { "-d", "src/main/java/net/sf/joost/grammar/", sBasePath + "Yylex" });
  }
}
