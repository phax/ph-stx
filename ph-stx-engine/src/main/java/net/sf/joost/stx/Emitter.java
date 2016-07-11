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
import java.io.UnsupportedEncodingException;
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
import net.sf.joost.stx.helpers.IMutableAttributes;
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
  public ContentHandler m_aContH;
  private LexicalHandler m_aLexH;
  private final ErrorHandlerImpl m_aErrorHandler; // set in the constructor

  // for namespace handling
  private final NamespaceSupport m_aNSSupport;
  private final Stack <String> m_aNSStack;
  private String m_sNSDefault;

  /** Stack for emitted start events, allows well-formedness check */
  private final Stack <String> m_aOpenedElements;

  /**
   * Previous emitter. A new one will be created for each new result event
   * stream.
   */
  public Emitter m_aPrev;

  // properties of the last element
  private String lastUri, lastLName, lastQName;
  private IMutableAttributes lastAttrs;
  private AbstractNodeBase lastInstruction;

  private boolean insideCDATA = false;
  private boolean dtdAllowed = true;

  public Emitter (final ErrorHandlerImpl errorHandler)
  {
    m_aNSSupport = new NamespaceSupport ();
    m_sNSDefault = "";
    m_aNSStack = new Stack<> ();

    m_aOpenedElements = new Stack<> ();
    this.m_aErrorHandler = errorHandler;
  }

  /** Called from {@link #pushEmitter(IStxEmitter)} */
  protected Emitter (final Emitter prev, final IStxEmitter handler)
  {
    this (prev.m_aErrorHandler);

    this.m_aPrev = prev;
    this.m_aContH = handler;
    this.m_aLexH = handler;
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
    if (handler.getSystemId () == null && m_aContH instanceof IStxEmitter)
    {
      // if the new handler doesn't have its own system identifier set
      // then use the system identifier of the parent
      handler.setSystemId (((IStxEmitter) m_aContH).getSystemId ());
    }
    return new Emitter (this, handler);
  }

  public void setContentHandler (final ContentHandler handler)
  {
    m_aContH = handler;
  }

  public void setLexicalHandler (final LexicalHandler handler)
  {
    m_aLexH = handler;
  }

  /** Process a stored element start tag (from startElement) */
  private void processLastElement () throws SAXException
  {
    try
    {
      m_aContH.startElement (lastUri, lastLName, lastQName, lastAttrs);
      dtdAllowed = false;
    }
    catch (final SAXException se)
    {
      m_aErrorHandler.error (se.getMessage (),
                             lastInstruction.m_sPublicID,
                             lastInstruction.m_sSystemID,
                             lastInstruction.lineNo,
                             lastInstruction.colNo,
                             se);
    }

    m_aOpenedElements.push (lastUri);
    m_aOpenedElements.push (lastQName);

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
      m_aErrorHandler.error ("Can't create an attribute if there's " +
                             "no opened element",
                             instruction.m_sPublicID,
                             instruction.m_sSystemID,
                             instruction.lineNo,
                             instruction.colNo);
      return; // if #errorHandler returns
    }

    if (m_aContH != null)
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
        if (!uri.equals (m_aNSSupport.getURI (prefix)))
        {
          m_aNSSupport.declarePrefix (prefix, uri);
          m_aNSStack.push (prefix);
          m_aContH.startPrefixMapping (prefix, uri);
        }
      }
    }
  }

  public void startDocument () throws SAXException
  {
    if (m_aContH != null)
      m_aContH.startDocument ();
  }

  /**
   * Closes a document.
   *
   * @param instruction
   *        the instruction that causes this method invocation
   */
  public void endDocument (final AbstractInstruction instruction) throws SAXException
  {
    if (m_aContH != null)
    {
      if (lastAttrs != null)
        processLastElement ();
      if (!m_aOpenedElements.isEmpty ())
      {
        m_aErrorHandler.fatalError ("Missing end tag for '" +
                                    m_aOpenedElements.pop () +
                                    "' at the end of the " +
                                    (m_aContH instanceof BufferEmitter ? "buffer" : "document"),
                                    instruction.getNode ().m_sPublicID,
                                    instruction.getNode ().m_sSystemID,
                                    instruction.lineNo,
                                    instruction.colNo);
      }
      m_aContH.endDocument ();
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
    if (m_aContH != null)
    {
      if (lastAttrs != null)
        processLastElement ();

      m_aNSSupport.pushContext ();
      m_aNSStack.push (null); // marker

      // is this element in an undeclared namespace?
      final int colon = qName.indexOf (":");
      if (colon != -1)
      {
        final String prefix = qName.substring (0, colon);
        if (!uri.equals (m_aNSSupport.getURI (prefix)))
        {
          m_aNSSupport.declarePrefix (prefix, uri);
          m_aNSStack.push (prefix);
          m_aContH.startPrefixMapping (prefix, uri);
        }
      }
      else
      {
        if (!uri.equals (m_sNSDefault))
        {
          m_aNSSupport.declarePrefix ("", uri);
          m_sNSDefault = uri;
          m_aNSStack.push ("");
          m_aContH.startPrefixMapping ("", uri);
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
            if (!theUri.equals (m_sNSDefault))
            {
              m_aContH.startPrefixMapping ("", theUri);
              m_aNSSupport.declarePrefix ("", theUri);
              m_sNSDefault = theUri;
              m_aNSStack.push ("");
            }
          }
          else
            if (!theUri.equals (m_aNSSupport.getURI (thePrefix)))
            {
              m_aContH.startPrefixMapping (thePrefix, theUri);
              m_aNSSupport.declarePrefix (thePrefix, theUri);
              m_aNSStack.push (thePrefix);
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
    if (m_aContH != null)
    {
      if (lastAttrs != null)
        processLastElement ();

      if (m_aOpenedElements.isEmpty ())
      {
        m_aErrorHandler.fatalError ("Attempt to emit unmatched end tag " +
                                    (qName != null ? "'" + qName + "' " : "") +
                                    "(no element opened)",
                                    instruction.getNode ().m_sPublicID,
                                    instruction.getNode ().m_sSystemID,
                                    instruction.lineNo,
                                    instruction.colNo);
        return; // if #errorHandler returns
      }
      final String elQName = m_aOpenedElements.pop ();
      final String elUri = m_aOpenedElements.pop ();
      if (!qName.equals (elQName))
      {
        m_aErrorHandler.fatalError ("Attempt to emit unmatched end tag '" +
                                    qName +
                                    "' ('" +
                                    elQName +
                                    "' expected)",
                                    instruction.getNode ().m_sPublicID,
                                    instruction.getNode ().m_sSystemID,
                                    instruction.lineNo,
                                    instruction.colNo);
        return; // if #errorHandler returns
      }
      if (!uri.equals (elUri))
      {
        m_aErrorHandler.fatalError ("Attempt to emit unmatched end tag '{" +
                                    uri +
                                    "}" +
                                    qName +
                                    "' ('{" +
                                    elUri +
                                    "}" +
                                    elQName +
                                    "' expected)",
                                    instruction.getNode ().m_sPublicID,
                                    instruction.getNode ().m_sSystemID,
                                    instruction.lineNo,
                                    instruction.colNo);
        return; // if #errorHandler returns
      }

      m_aContH.endElement (uri, lName, qName);

      // send endPrefixMapping events, prefixes are on #nsStack
      m_aNSSupport.popContext ();
      String thePrefix = m_aNSStack.pop ();
      while (thePrefix != null)
      { // null is the marker for a new context
        m_aContH.endPrefixMapping (thePrefix);
        if (thePrefix == "")
        {
          m_sNSDefault = m_aNSSupport.getURI ("");
          if (m_sNSDefault == null)
            m_sNSDefault = "";
        }
        thePrefix = m_aNSStack.pop ();
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
    if (m_aContH != null)
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
            m_aContH.characters (str.substring (0, index).toCharArray (), 0, index);
            m_aLexH.endCDATA (); // #lexH will be != null,
            m_aLexH.startCDATA (); // because #insideCDATA was true
            str = str.substring (index);
            index = str.indexOf ("]]>");
          }
          m_aContH.characters (str.toCharArray (), 0, str.length ());
        }
        else
          m_aContH.characters (ch, start, length);
      }
      catch (final SAXException ex)
      {
        m_aErrorHandler.fatalError (ex.getMessage (),
                                    instruction.m_sPublicID,
                                    instruction.m_sSystemID,
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
    if (m_aContH != null)
    {
      if (lastAttrs != null)
        processLastElement ();
      try
      {
        m_aContH.processingInstruction (target, data);
      }
      catch (final SAXException se)
      {
        m_aErrorHandler.error (se.getMessage (),
                               instruction.m_sPublicID,
                               instruction.m_sSystemID,
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
    if (m_aContH != null && lastAttrs != null)
      processLastElement ();
    if (m_aLexH != null)
    {
      try
      {
        m_aLexH.comment (ch, start, length);
      }
      catch (final SAXException se)
      {
        m_aErrorHandler.error (se.getMessage (),
                               instruction.m_sPublicID,
                               instruction.m_sSystemID,
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
    if (m_aContH != null && lastAttrs != null)
      processLastElement ();
    if (m_aLexH != null)
    {
      try
      {
        m_aLexH.startCDATA ();
      }
      catch (final SAXException se)
      {
        m_aErrorHandler.error (se.getMessage (),
                               instruction.m_sPublicID,
                               instruction.m_sSystemID,
                               instruction.lineNo,
                               instruction.colNo,
                               se);
      }
      insideCDATA = true;
    }
  }

  public void endCDATA () throws SAXException
  {
    if (m_aLexH != null)
    {
      m_aLexH.endCDATA ();
      insideCDATA = false;
    }
  }

  public void createDTD (final AbstractNodeBase instruction,
                         final String name,
                         final String publicId,
                         final String systemId) throws SAXException
  {
    if (m_aContH != null && lastAttrs != null)
      processLastElement ();
    if (!dtdAllowed)
    {
      m_aErrorHandler.error ("Cannot create a document type declaration for '" +
                             name +
                             "' when an element or another DTD has already " +
                             "been output",
                             instruction.m_sPublicID,
                             instruction.m_sSystemID,
                             instruction.lineNo,
                             instruction.colNo);
      return;
    }
    if (m_aLexH != null)
    {
      try
      {
        m_aLexH.startDTD (name, publicId, systemId);
        m_aLexH.endDTD ();
        dtdAllowed = false;
      }
      catch (final SAXException se)
      {
        m_aErrorHandler.error (se.getMessage (),
                               instruction.m_sPublicID,
                               instruction.m_sSystemID,
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
    if (m_aContH == emitter)
      return true;
    if (m_aPrev != null)
      return m_aPrev.isEmitterActive (emitter);
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
                                 final String encoding,
                                 final String publicId,
                                 final String systemId,
                                 final int lineNo,
                                 final int colNo,
                                 final boolean append) throws java.io.IOException, SAXException, URISyntaxException
  {
    // Note: currently we don't check if a file is already open.
    // Opening a file twice may lead to unexpected results.

    File hrefFile = null; // the file object representing href

    if (m_aContH instanceof IStxEmitter)
    { // we may extract a base URI
      final String base = ((IStxEmitter) m_aContH).getSystemId ();
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
    catch (final UnsupportedEncodingException e)
    {
      final String msg = "Unsupported encoding '" + encoding + "', using " + CSTX.DEFAULT_ENCODING;
      m_aErrorHandler.warning (msg, publicId, systemId, lineNo, colNo, e);
      osw = new OutputStreamWriter (fos, CSTX.DEFAULT_ENCODING);
    }
    return osw;
  }
}
