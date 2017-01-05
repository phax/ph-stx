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
package net.sf.joost;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.joost.emitter.AbstractStreamEmitter;
import net.sf.joost.emitter.FOPEmitter;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Processor;

/**
 * Command line interface for Joost.
 *
 * @version $Revision: 1.31 $ $Date: 2008/10/06 13:31:41 $
 * @author Oliver Becker
 */
public class Main
{
  // the logger object if available
  private static final Logger log = LoggerFactory.getLogger (Main.class);

  /**
   * Entry point
   *
   * @param args
   *        array of strings containing the parameter for Joost and at least two
   *        URLs addressing xml-source and stx-sheet
   */
  public static void main (final String [] args)
  {
    // input filename
    String xmlFile = null;

    // the currently last processor (as XMLFilter)
    Processor processor = null;

    // output filename (optional)
    String outFile = null;

    // custom message emitter class name (optional)
    String meClassname = null;

    // set to true if a command line parameter was wrong
    boolean wrongParameter = false;

    // set to true if -help was specified on the command line
    boolean printHelp = false;

    // set to true if -pdf was specified on the command line
    boolean doFOP = false;

    // set to true if -nodecl was specified on the command line
    boolean nodecl = false;

    // set to true if -noext was specified on the command line
    boolean noext = false;

    // set to true if -doe was specified on the command line
    boolean doe = false;

    // debugging
    boolean dontexit = false;

    // timings
    boolean measureTime = false;
    long timeStart = 0, timeEnd = 0;

    // needed for evaluating parameter assignments
    int index;

    // serializer SAX -> XML text
    AbstractStreamEmitter emitter = null;

    // filenames for the usage and version info
    final String USAGE = "usage.txt";
    final String VERSION = "version.txt";

    try
    {

      // parse command line argument list
      for (int i = 0; i < args.length; i++)
      {
        if (args[i].trim ().length () == 0)
        {
          // empty parameter? ingore
        }
        // all options start with a '-', but a single '-' means stdin
        else
          if (args[i].charAt (0) == '-' && args[i].length () > 1)
          {
            if ("-help".equals (args[i]))
            {
              printHelp = true;
              continue;
            }
            if ("-version".equals (args[i]))
            {
              _printResource (VERSION);
              _logInfo ();
              return;
            }
            if ("-pdf".equals (args[i]))
            {
              doFOP = true;
              continue;
            }
            if ("-nodecl".equals (args[i]))
            {
              nodecl = true;
              continue;
            }
            if ("-noext".equals (args[i]))
            {
              noext = true;
              continue;
            }
            if ("-doe".equals (args[i]))
            {
              doe = true;
              continue;
            }
            if ("-wait".equals (args[i]))
            {
              dontexit = true; // undocumented
              continue;
            }
            if ("-time".equals (args[i]))
            {
              measureTime = true;
              continue;
            }
            if ("-o".equals (args[i]))
            {
              // this option needs a parameter
              if (++i < args.length && args[i].charAt (0) != '-')
              {
                if (outFile != null)
                {
                  System.err.println ("Option -o already specified with " + outFile);
                  wrongParameter = true;
                }
                else
                  outFile = args[i];
                continue;
              }
              if (outFile != null)
                System.err.println ("Option -o already specified with " + outFile);
              else
                System.err.println ("Option -o requires a filename");
              i--;
              wrongParameter = true;
            }
            else
              if ("-m".equals (args[i]))
              {
                // this option needs a parameter
                if (++i < args.length && args[i].charAt (0) != '-')
                {
                  if (meClassname != null)
                  {
                    System.err.println ("Option -m already specified with " + meClassname);
                    wrongParameter = true;
                  }
                  else
                    meClassname = args[i];
                  continue;
                }
                if (meClassname != null)
                  System.err.println ("Option -m already specified with " + meClassname);
                else
                  System.err.println ("Option -m requires a classname");
                i--;
                wrongParameter = true;
              }
              else
              {
                System.err.println ("Unknown option " + args[i]);
                wrongParameter = true;
              }
          }
          // command line argument is not an option with a leading '-'
          else
            if ((index = args[i].indexOf ('=')) != -1)
            {
              // parameter assignment
              if (processor != null)
                processor.setParameter (args[i].substring (0, index), args[i].substring (index + 1));
              else
              {
                System.err.println ("Assignment " + args[i] + " must follow an stx-sheet parameter");
                wrongParameter = true;
              }
              continue;
            }
            else
              if (xmlFile == null)
              {
                xmlFile = args[i];
                continue;
              }
              else
              {
                // xmlFile != null, i.e. this is an STX sheet
                final ParseContext pContext = new ParseContext ();
                pContext.allowExternalFunctions = !noext;
                if (measureTime)
                  timeStart = System.currentTimeMillis ();
                final Processor proc = new Processor (new InputSource (args[i]), pContext);
                if (measureTime)
                {
                  timeEnd = System.currentTimeMillis ();
                  System.err.println ("Parsing " + args[i] + ": " + (timeEnd - timeStart) + " ms");
                }

                if (processor != null)
                  proc.setParent (processor); // XMLFilter chain
                processor = proc;
              }
      }

      // PDF creation requested
      if (doFOP && outFile == null)
      {
        System.err.println ("Option -pdf requires option -o");
        wrongParameter = true;
      }

      // missing filenames
      if (!printHelp && processor == null)
      {
        if (xmlFile == null)
          System.err.println ("Missing filenames for XML source and " + "STX transformation sheet");
        else
          System.err.println ("Missing filename for STX transformation " + "sheet");
        wrongParameter = true;
      }

      if (meClassname != null && !wrongParameter)
      {
        // create object
        IStxEmitter messageEmitter = null;
        try
        {
          messageEmitter = (IStxEmitter) Class.forName (meClassname).newInstance ();
        }
        catch (final ClassNotFoundException ex)
        {
          System.err.println ("Class not found: " + ex.getMessage ());
          wrongParameter = true;
        }
        catch (final InstantiationException ex)
        {
          System.err.println ("Instantiation failed: " + ex.getMessage ());
          wrongParameter = true;
        }
        catch (final IllegalAccessException ex)
        {
          System.err.println ("Illegal access: " + ex.getMessage ());
          wrongParameter = true;
        }
        catch (final ClassCastException ex)
        {
          System.err.println ("Wrong message emitter: " + meClassname + " doesn't implement the " + IStxEmitter.class);
          wrongParameter = true;
        }
        if (messageEmitter != null)
        { // i.e. no exception occurred
          // set message emitter for all processors in the filter chain
          Processor p = processor;
          do
          {
            p.setMessageEmitter (messageEmitter);
            final Object o = p.getParent ();
            if (o instanceof Processor)
              p = (Processor) o;
            else
              p = null;
          } while (p != null);
        }
      }

      if (printHelp)
      {
        _printResource (VERSION);
        _printResource (USAGE);
        _logInfo ();
        return;
      }

      if (wrongParameter)
      {
        System.err.println ("Specify -help to get a detailed help message");
        System.exit (1);
      }

      // The first processor re-uses its XMLReader for parsing the input
      // xmlFile.
      // For a real XMLFilter usage you have to call
      // processor.setParent(yourXMLReader)

      // Connect a SAX consumer
      if (doFOP)
      {
        // pass output events to FOP
        // // Version 1: use a FOPEmitter object as XMLFilter
        // processor.setContentHandler(
        // new FOPEmitter(
        // new java.io.FileOutputStream(outFile)));

        // Version 2: use a static method to retrieve FOP's content
        // handler and use it directly
        processor.setContentHandler (FOPEmitter.getFOPContentHandler (new java.io.FileOutputStream (outFile)));
      }
      else
      {
        // Create XML output
        if (outFile != null)
        {
          emitter = AbstractStreamEmitter.newEmitter (outFile, processor.m_aOutputProperties);
          emitter.setSystemId (new File (outFile).toURI ().toString ());
        }
        else
          emitter = AbstractStreamEmitter.newEmitter (System.out, processor.m_aOutputProperties);
        processor.setContentHandler (emitter);
        processor.setLexicalHandler (emitter);
        // the previous line is a short-cut for
        // processor.setProperty(
        // "http://xml.org/sax/properties/lexical-handler", emitter);

        emitter.setOmitXmlDeclaration (nodecl);
        emitter.setSupportDisableOutputEscaping (doe);
      }

      InputSource is;
      if (xmlFile.equals ("-"))
      {
        is = new InputSource (System.in);
        is.setSystemId ("<stdin>");
        is.setPublicId ("");
      }
      else
        is = new InputSource (xmlFile);

      // Ready for take-off
      if (measureTime)
        timeStart = System.currentTimeMillis ();

      processor.parse (is);

      if (measureTime)
      {
        timeEnd = System.currentTimeMillis ();
        System.err.println ("Processing " + xmlFile + ": " + (timeEnd - timeStart) + " ms");
      }

      // // check if the Processor copy constructor works
      // Processor pr = new Processor(processor);
      // java.util.Properties props = new java.util.Properties();
      // props.put("encoding", "ISO-8859-2");
      // StreamEmitter em =
      // StreamEmitter.newEmitter(System.err, props);
      // pr.setContentHandler(em);
      // pr.setLexicalHandler(em);
      // pr.parse(is);
      // // end check

      // this is for debugging with the Java Memory Profiler
      if (dontexit)
      {
        System.err.println ("Press Enter to exit");
        System.in.read ();
      }
    }
    catch (final IOException ex)
    {
      System.err.println (ex.toString ());
      System.exit (1);
    }
    catch (final SAXException ex)
    {
      if (emitter != null)
      {
        try
        {
          // flushes the internal BufferedWriter, i.e. outputs
          // the intermediate result
          emitter.endDocument ();
        }
        catch (final SAXException exx)
        {
          // ignore
        }
      }
      final Exception embedded = ex.getException ();
      if (embedded != null)
      {
        if (embedded instanceof TransformerException)
        {
          final TransformerException te = (TransformerException) embedded;
          final SourceLocator sl = te.getLocator ();
          String systemId;
          // ensure that systemId is not null; is this a bug?
          if (sl != null && (systemId = sl.getSystemId ()) != null)
          {
            // remove the "file://" scheme prefix if it is present
            if (systemId.startsWith ("file://"))
              systemId = systemId.substring (7);
            else
              if (systemId.startsWith ("file:"))
                // bug in JDK 1.4 / Crimson?
                // (see rfc1738)
                systemId = systemId.substring (5);
            System.err.println (systemId +
                                ":" +
                                sl.getLineNumber () +
                                ":" +
                                sl.getColumnNumber () +
                                ": " +
                                te.getMessage ());
          }
          else
            System.err.println (te.getMessage ());
        }
        else
        {
          // Fatal: this mustn't happen
          embedded.printStackTrace (System.err);
        }
      }
      else
        System.err.println (ex.toString ());
      System.exit (1);
    }
  }

  /**
   * Outputs the contents of a resource info file.
   *
   * @param filename
   *        the name of the file containing the info to output
   */
  private static void _printResource (final String filename)
  {
    try
    {
      // find the file resource
      final InputStream is = Main.class.getResourceAsStream (filename);
      if (is == null)
        throw new java.io.FileNotFoundException (filename);
      final BufferedReader br = new BufferedReader (new InputStreamReader (is));
      boolean doOutput = true;
      String line;
      while ((line = br.readLine ()) != null)
      {
        if (line.startsWith ("@@@ "))
        {
          // special control line
          if (line.equals ("@@@ START DEBUG ONLY"))
            doOutput = CSTX.DEBUG;
          else
            if (line.equals ("@@@ END DEBUG ONLY"))
              doOutput = true;
          // else: ignore
          continue;
        }
        if (doOutput)
          System.err.println (line);
      }
      System.err.println ("");
    }
    catch (final IOException ex)
    {
      log.error ("Exception", ex);
    }
  }

  /**
   * Output logging availability info and exit Joost
   */
  private static void _logInfo ()
  {
    System.err.println ("Logging is enabled using " + log.getClass ().getName ());
  }
}
