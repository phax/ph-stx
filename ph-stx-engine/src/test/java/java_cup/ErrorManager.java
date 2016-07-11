
package java_cup;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java_cup.runtime.Symbol;

public class ErrorManager
{
  private static ErrorManager errorManager;
  private int errors = 0;
  private int warnings = 0;
  private int fatals = 0;

  public int getFatalCount ()
  {
    return fatals;
  }

  public int getErrorCount ()
  {
    return errors;
  }

  public int getWarningCount ()
  {
    return warnings;
  }

  static
  {
    errorManager = new ErrorManager ();
  }

  public static ErrorManager getManager ()
  {
    return errorManager;
  }

  private ErrorManager ()
  {}

  // TODO: migrate to java.util.logging
  /**
   * Error message format: ERRORLEVEL at (LINE/COLUMN)@SYMBOL: MESSAGE
   * ERRORLEVEL : MESSAGE
   **/
  public void emit_fatal (final String message)
  {
    System.err.println ("Fatal : " + message);
    fatals++;
  }

  public void emit_fatal (final String message, final Symbol sym)
  {
    // System.err.println("Fatal at
    // ("+sym.left+"/"+sym.right+")@"+convSymbol(sym)+" : "+message);
    System.err.println ("Fatal: " + message + " @ " + sym);
    fatals++;
  }

  public void emit_warning (final String message)
  {
    System.err.println ("Warning : " + message);
    warnings++;
  }

  public void emit_warning (final String message, final Symbol sym)
  {
    // System.err.println("Warning at
    // ("+sym.left+"/"+sym.right+")@"+convSymbol(sym)+" : "+message);
    System.err.println ("Fatal: " + message + " @ " + sym);
    warnings++;
  }

  public void emit_error (final String message)
  {
    System.err.println ("Error : " + message);
    errors++;
  }

  public void emit_error (final String message, final Symbol sym)
  {
    // System.err.println("Error at
    // ("+sym.left+"/"+sym.right+")@"+convSymbol(sym)+" : "+message);
    System.err.println ("Error: " + message + " @ " + sym);
    errors++;
  }

  private static String convSymbol (final Symbol symbol)
  {
    final String result = (symbol.m_aValue == null) ? "" : " (\"" + symbol.m_aValue.toString () + "\")";
    final Field [] fields = sym.class.getFields ();
    for (int i = 0; i < fields.length; i++)
    {
      if (!Modifier.isPublic (fields[i].getModifiers ()))
        continue;
      try
      {
        if (fields[i].getInt (null) == symbol.sym)
          return fields[i].getName () + result;
      }
      catch (final Exception ex)
      {}
    }
    return symbol.toString () + result;
  }

}
