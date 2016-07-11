/*
 * $Id: Emitter.java,v 1.38 2009/08/21 12:46:17 obecker Exp $
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is: this file
 *
 * The Initial Developer of the Original Code is Oliver Becker.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): Thomas Behrends.
 */

package net.sf.joost.stx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.NamespaceSupport;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.BufferEmitter;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.stx.helpers.MutableAttributes;
import net.sf.joost.stx.helpers.MutableAttributesImpl;

/**
 * Emitter acts as a filter between the Processor and the real SAX output
 * handler. It maintains a stack of in-scope namespaces and sends corresponding
 * events to the real output handler.
 *
 * @version $Revision: 1.38 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

public class Emitter
{
  public ContentHandler contH;
  private LexicalHandler lexH;
  private final ErrorHandlerImpl errorHandler; // set in the constructor

  // for namespace handling
  private final NamespaceSupport nsSupport;
  private final Stack <String> nsStack;
  private String nsDefault;

  /** Stack for emitted start events, allows well-formedness check */
  private final Stack <String> openedElements;

  /**
   * Previous emitter. A new one will be created for each new result event
   * stream.
   */
  public Emitter prev;

  // properties of the last element
  private String lastUri, lastLName, lastQName;
  private MutableAttributes lastAttrs;
  private AbstractNodeBase lastInstruction;

  private boolean insideCDATA = false;
  private boolean dtdAllowed = true;

  public Emitter (final ErrorHandlerImpl errorHandler)
  {
    nsSupport = new NamespaceSupport ();
    nsDefault = "";
    nsStack = new Stack<> ();

    openedElements = new Stack<> ();
    this.errorHandler = errorHandler;
  }

  /** Called from {@link #pushEmitter(IStxEmitter)} */
  protected Emitter (final Emitter prev, final IStxEmitter handler)
  {
    this (prev.errorHandler);

    this.prev = prev;
    this.contH = handler;
    this.lexH = handler;
  }

  /**
   * Put the current emitter object on a stack and return a new emitter, which
   * uses the given handler. This method may be overridden.
   *
   * @param handler
   *        the STX handler for the new emitter
   * @return a new emitter object
   */
  public Emitter pushEmitter (final IStxEmitter handler)
  {
    if (handler.getSystemId () == null && contH instanceof IStxEmitter)
    {
      // if the new handler doesn't have its own system identifier set
      // then use the system identifier of the parent
      handler.setSystemId (((IStxEmitter) contH).getSystemId ());
    }
    return new Emitter (this, handler);
  }

  public void setContentHandler (final ContentHandler handler)
  {
    contH = handler;
  }

  public void setLexicalHandler (final LexicalHandler handler)
  {
    lexH = handler;
  }

  /** Process a stored element start tag (from startElement) */
  private void processLastElement () throws SAXException
  {
    try
    {
      contH.startElement (lastUri, lastLName, lastQName, lastAttrs);
      dtdAllowed = false;
    }
    catch (final SAXException se)
    {
      errorHandler.error (se.getMessage (),
                          lastInstruction.publicId,
                          lastInstruction.systemId,
                          lastInstruction.lineNo,
                          lastInstruction.colNo,
                          se);
    }

    openedElements.push (lastUri);
    openedElements.push (lastQName);

    lastAttrs = null; // flag: there's no startElement pending
  }

  /**
   * Adds a dynamic created attribute (via <code>stx:attribute</code>)
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void addAttribute (final String uri,
                            final String qName,
                            final String lName,
                            final String value,
                            final AbstractNodeBase instruction) throws SAXException
  {
    if (lastAttrs == null)
    {
      errorHandler.error ("Can't create an attribute if there's " +
                          "no opened element",
                          instruction.publicId,
                          instruction.systemId,
                          instruction.lineNo,
                          instruction.colNo);
      return; // if #errorHandler returns
    }

    if (contH != null)
    {

      final int index = lastAttrs.getIndex (uri, lName);
      if (index != -1)
      { // already there
        lastAttrs.setValue (index, value);
      }
      else
      {
        lastAttrs.addAttribute (uri, lName, qName, "CDATA", value);
      }

      // is this attribute in an undeclared namespace?
      final int colon = qName.indexOf (":");
      if (colon != -1)
      { // look only at prefixed attributes
        final String prefix = qName.substring (0, colon);
        if (!uri.equals (nsSupport.getURI (prefix)))
        {
          nsSupport.declarePrefix (prefix, uri);
          nsStack.push (prefix);
          contH.startPrefixMapping (prefix, uri);
        }
      }
    }
  }

  public void startDocument () throws SAXException
  {
    if (contH != null)
      contH.startDocument ();
  }

  /**
   * Closes a document.
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void endDocument (final AbstractInstruction instruction) throws SAXException
  {
    if (contH != null)
    {
      if (lastAttrs != null)
        processLastElement ();
      if (!openedElements.isEmpty ())
      {
        errorHandler.fatalError ("Missing end tag for '" +
                                 openedElements.pop () +
                                 "' at the end of the " +
                                 (contH instanceof BufferEmitter ? "buffer" : "document"),
                                 instruction.getNode ().publicId,
                                 instruction.getNode ().systemId,
                                 instruction.lineNo,
                                 instruction.colNo);
      }
      contH.endDocument ();
    }
  }

  /**
   * Opens a new element.
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void startElement (final String uri,
                            final String lName,
                            final String qName,
                            final Attributes attrs,
                            final Map <String, String> namespaces,
                            final AbstractNodeBase instruction) throws SAXException
  {
    if (contH != null)
    {
      if (lastAttrs != null)
        processLastElement ();

      nsSupport.pushContext ();
      nsStack.push (null); // marker

      // is this element in an undeclared namespace?
      final int colon = qName.indexOf (":");
      if (colon != -1)
      {
        final String prefix = qName.substring (0, colon);
        if (!uri.equals (nsSupport.getURI (prefix)))
        {
          nsSupport.declarePrefix (prefix, uri);
          nsStack.push (prefix);
          contH.startPrefixMapping (prefix, uri);
        }
      }
      else
      {
        if (!uri.equals (nsDefault))
        {
          nsSupport.declarePrefix ("", uri);
          nsDefault = uri;
          nsStack.push ("");
          contH.startPrefixMapping ("", uri);
        }
      }
      // no need to check also the attributes
      // their namespaces should appear in #namespaces
      // (hopefully)

      // We store the properties of this element, because following
      // addAttribute() calls may create additional attributes. This
      // element will be reported to the next emitter in processLastElement
      lastUri = uri;
      lastLName = lName;
      lastQName = qName;
      lastAttrs = new MutableAttributesImpl (attrs);

      if (namespaces != null)
      {
        // does #namespaces contain undeclared namespaces?
        for (final Map.Entry <String, String> e : namespaces.entrySet ())
        {
          final String thePrefix = e.getKey ();
          final String theUri = e.getValue ();
          if ("".equals (thePrefix))
          { // default namespace
            if (!theUri.equals (nsDefault))
            {
              contH.startPrefixMapping ("", theUri);
              nsSupport.declarePrefix ("", theUri);
              nsDefault = theUri;
              nsStack.push ("");
            }
          }
          else
            if (!theUri.equals (nsSupport.getURI (thePrefix)))
            {
              contH.startPrefixMapping (thePrefix, theUri);
              nsSupport.declarePrefix (thePrefix, theUri);
              nsStack.push (thePrefix);
            }
        }
      }
      // else: happens for dynamically created elements
      // e.g. <stx:start-element name="foo" />

      lastInstruction = instruction;
    }
  }

  /**
   * Closes an element.
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void endElement (final String uri,
                          final String lName,
                          final String qName,
                          final AbstractInstruction instruction) throws SAXException
  {
    if (contH != null)
    {
      if (lastAttrs != null)
        processLastElement ();

      if (openedElements.isEmpty ())
      {
        errorHandler.fatalError ("Attempt to emit unmatched end tag " +
                                 (qName != null ? "'" + qName + "' " : "") +
                                 "(no element opened)",
                                 instruction.getNode ().publicId,
                                 instruction.getNode ().systemId,
                                 instruction.lineNo,
                                 instruction.colNo);
        return; // if #errorHandler returns
      }
      final String elQName = openedElements.pop ();
      final String elUri = openedElements.pop ();
      if (!qName.equals (elQName))
      {
        errorHandler.fatalError ("Attempt to emit unmatched end tag '" +
                                 qName +
                                 "' ('" +
                                 elQName +
                                 "' expected)",
                                 instruction.getNode ().publicId,
                                 instruction.getNode ().systemId,
                                 instruction.lineNo,
                                 instruction.colNo);
        return; // if #errorHandler returns
      }
      if (!uri.equals (elUri))
      {
        errorHandler.fatalError ("Attempt to emit unmatched end tag '{" +
                                 uri +
                                 "}" +
                                 qName +
                                 "' ('{" +
                                 elUri +
                                 "}" +
                                 elQName +
                                 "' expected)",
                                 instruction.getNode ().publicId,
                                 instruction.getNode ().systemId,
                                 instruction.lineNo,
                                 instruction.colNo);
        return; // if #errorHandler returns
      }

      contH.endElement (uri, lName, qName);

      // send endPrefixMapping events, prefixes are on #nsStack
      nsSupport.popContext ();
      String thePrefix = nsStack.pop ();
      while (thePrefix != null)
      { // null is the marker for a new context
        contH.endPrefixMapping (thePrefix);
        if (thePrefix == "")
        {
          nsDefault = nsSupport.getURI ("");
          if (nsDefault == null)
            nsDefault = "";
        }
        thePrefix = nsStack.pop ();
      }
    }
  }

  /**
   * Emits characters.
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void characters (final char [] ch,
                          final int start,
                          final int length,
                          final AbstractNodeBase instruction) throws SAXException
  {
    if (length == 0)
      return;
    if (contH != null)
    {
      if (lastAttrs != null)
        processLastElement ();
      try
      {
        if (insideCDATA)
        { // prevent output of "]]>" in this CDATA section
          String str = new String (ch, start, length);
          int index = str.indexOf ("]]>");
          while (index != -1)
          {
            // "]]>" found; split between "]]" and ">"
            index += 2;
            contH.characters (str.substring (0, index).toCharArray (), 0, index);
            lexH.endCDATA (); // #lexH will be != null,
            lexH.startCDATA (); // because #insideCDATA was true
            str = str.substring (index);
            index = str.indexOf ("]]>");
          }
          contH.characters (str.toCharArray (), 0, str.length ());
        }
        else
          contH.characters (ch, start, length);
      }
      catch (final SAXException ex)
      {
        errorHandler.fatalError (ex.getMessage (),
                                 instruction.publicId,
                                 instruction.systemId,
                                 instruction.lineNo,
                                 instruction.colNo,
                                 ex);
      }
    }
  }

  /**
   * Creates a processing instruction.
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void processingInstruction (final String target,
                                     final String data,
                                     final AbstractNodeBase instruction) throws SAXException
  {
    if (contH != null)
    {
      if (lastAttrs != null)
        processLastElement ();
      try
      {
        contH.processingInstruction (target, data);
      }
      catch (final SAXException se)
      {
        errorHandler.error (se.getMessage (),
                            instruction.publicId,
                            instruction.systemId,
                            instruction.lineNo,
                            instruction.colNo,
                            se);
      }
    }
  }

  /**
   * Creates a comment.
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void comment (final char [] ch,
                       final int start,
                       final int length,
                       final AbstractNodeBase instruction) throws SAXException
  {
    if (contH != null && lastAttrs != null)
      processLastElement ();
    if (lexH != null)
    {
      try
      {
        lexH.comment (ch, start, length);
      }
      catch (final SAXException se)
      {
        errorHandler.error (se.getMessage (),
                            instruction.publicId,
                            instruction.systemId,
                            instruction.lineNo,
                            instruction.colNo,
                            se);
      }
    }
  }

  /**
   * Creates a CDATA section.
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void startCDATA (final AbstractNodeBase instruction) throws SAXException
  {
    if (contH != null && lastAttrs != null)
      processLastElement ();
    if (lexH != null)
    {
      try
      {
        lexH.startCDATA ();
      }
      catch (final SAXException se)
      {
        errorHandler.error (se.getMessage (),
                            instruction.publicId,
                            instruction.systemId,
                            instruction.lineNo,
                            instruction.colNo,
                            se);
      }
      insideCDATA = true;
    }
  }

  public void endCDATA () throws SAXException
  {
    if (lexH != null)
    {
      lexH.endCDATA ();
      insideCDATA = false;
    }
  }

  public void createDTD (final AbstractNodeBase instruction,
                         final String name,
                         final String publicId,
                         final String systemId) throws SAXException
  {
    if (contH != null && lastAttrs != null)
      processLastElement ();
    if (!dtdAllowed)
    {
      errorHandler.error ("Cannot create a document type declaration for '" +
                          name +
                          "' when an element or another DTD has already " +
                          "been output",
                          instruction.publicId,
                          instruction.systemId,
                          instruction.lineNo,
                          instruction.colNo);
      return;
    }
    if (lexH != null)
    {
      try
      {
        lexH.startDTD (name, publicId, systemId);
        lexH.endDTD ();
        dtdAllowed = false;
      }
      catch (final SAXException se)
      {
        errorHandler.error (se.getMessage (),
                            instruction.publicId,
                            instruction.systemId,
                            instruction.lineNo,
                            instruction.colNo,
                            se);
      }
    }
  }

  /**
   * @return true if this emitter is in use or on the stack
   */
  public boolean isEmitterActive (final IStxEmitter emitter)
  {
    if (contH == emitter)
      return true;
    if (prev != null)
      return prev.isEmitterActive (emitter);
    return false;
  }

  /**
   * Provides a <code>Writer</code> object that will be used for
   * <code>stx:result-document</code> instructions.
   *
   * @param href
   *        the filename
   * @param encoding
   *        the requested encoding
   * @param publicId
   *        public ID of the transformation sheet
   * @param systemId
   *        system ID of the transformation sheet
   * @param lineNo
   *        line number of the <code>stx:result-document</code> instruction
   * @param colNo
   *        column number of the <code>stx:result-document</code> instruction
   * @param append
   *        flag that determines, whether the new XML should be appended to an
   *        existing file
   */
  public Writer getResultWriter (final String href,
                                 String encoding,
                                 final String publicId,
                                 final String systemId,
                                 final int lineNo,
                                 final int colNo,
                                 final boolean append) throws java.io.IOException, SAXException, URISyntaxException
  {
    // Note: currently we don't check if a file is already open.
    // Opening a file twice may lead to unexpected results.

    File hrefFile = null; // the file object representing href

    if (contH instanceof IStxEmitter)
    { // we may extract a base URI
      final String base = ((IStxEmitter) contH).getSystemId ();
      if (base != null)
        hrefFile = new File (new URI (base).resolve (href));
    }
    if (hrefFile == null)
    { // still null (means: no base available)
      if (href.indexOf (':') != -1) // href is a URI
        hrefFile = new File (new URI (href));
      else // href is just a path
        hrefFile = new File (href);
    }

    // create missing directories
    // (say: simply create them, don't check if there are really missing)
    final String absFilename = hrefFile.getAbsolutePath ();
    final int dirPos = absFilename.lastIndexOf (File.separator);
    if (dirPos != -1)
      new File (absFilename.substring (0, dirPos)).mkdirs ();

    final FileOutputStream fos = new FileOutputStream (hrefFile, append);
    OutputStreamWriter osw;
    try
    {
      osw = new OutputStreamWriter (fos, encoding);
    }
    catch (final java.io.UnsupportedEncodingException e)
    {
      final String msg = "Unsupported encoding '" + encoding + "', using " + CSTX.DEFAULT_ENCODING;
      errorHandler.warning (msg, publicId, systemId, lineNo, colNo, e);
      osw = new OutputStreamWriter (fos, encoding = CSTX.DEFAULT_ENCODING);
    }
    return osw;
  }
}
