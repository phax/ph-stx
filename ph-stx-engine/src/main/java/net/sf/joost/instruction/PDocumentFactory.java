/*
 * $Id: PDocumentFactory.java,v 2.20 2009/08/21 12:46:17 obecker Exp $
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
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.instruction;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import net.sf.joost.CSTX;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Processor;
import net.sf.joost.stx.Value;
import net.sf.joost.trax.TrAXHelper;

/**
 * Factory for <code>process-document</code> elements, which are represented by
 * the inner Instance class.
 *
 * @version $Revision: 2.20 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

public class PDocumentFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final HashSet <String> attrNames;

  // Logger initialization
  private static final Logger log = LoggerFactory.getLogger (PDocumentFactory.class);

  //
  // Constructor
  //
  public PDocumentFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("href");
    attrNames.add ("base");
    attrNames.add ("group");
    attrNames.add ("filter-method");
    attrNames.add ("filter-src");
  }

  /** @return <code>"process-document"</code> */
  @Override
  public String getName ()
  {
    return "process-document";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final AbstractTree href = parseRequiredExpr (qName, attrs, "href", context);

    final AbstractTree baseAVT = parseAVT (attrs.getValue ("base"), context);

    final String groupAtt = attrs.getValue ("group");

    final String filterMethodAtt = attrs.getValue ("filter-method");

    if (groupAtt != null && filterMethodAtt != null)
      throw new SAXParseException ("It's not allowed to use both 'group' and 'filter-method' " +
                                   "attributes",
                                   context.locator);

    final String filterSrcAtt = attrs.getValue ("filter-src");

    if (filterSrcAtt != null && filterMethodAtt == null)
      throw new SAXParseException ("Missing 'filter-method' attribute in '" +
                                   qName +
                                   "' ('filter-src' is present)",
                                   context.locator);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, href, baseAVT, groupAtt, filterMethodAtt, filterSrcAtt);
  }

  /** The inner Instance class */
  public static final class Instance extends AbstractProcessBase
  {
    private AbstractTree m_aHref, m_aBaseUri;

    // Constructor
    public Instance (final String qName,
                     final AbstractNodeBase parent,
                     final ParseContext context,
                     final AbstractTree href,
                     final AbstractTree baseUri,
                     final String groupQName,
                     final String method,
                     final String src) throws SAXParseException
    {
      super (qName, parent, context, groupQName, method, src);
      this.m_aBaseUri = baseUri;
      this.m_aHref = href;
    }

    /**
     * Processes an external document.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      Value v = m_aHref.evaluate (context, this);
      if (v.type == Value.EMPTY)
        return CSTX.PR_CONTINUE; // nothing to do

      final Processor proc = context.currentProcessor;
      ContentHandler contH = proc;
      LexicalHandler lexH = proc;
      if (hasFilter ())
      {
        // use external SAX filter (TransformerHandler)
        final TransformerHandler handler = getProcessHandler (context);
        if (handler == null)
          return CSTX.PR_ERROR;
        contH = handler;
        lexH = handler;
      }

      String base;
      if (m_aBaseUri == null)
      {
        // determine default base URI
        if (v.type == Value.NODE)
        {
          // use #input
          base = context.locator.getSystemId ();
          // TODO: take the node's base. The result differs if the
          // node in v comes from a different document
          // (for example, it was stored in a variable)
        }
        else // use #sheet
          base = m_sSystemID;
      }
      else
      { // use specified base URI
        base = m_aBaseUri.evaluate (context, this).getString ();
        if ("#input".equals (base) && context.locator != null)
          base = context.locator.getSystemId ();
        else
          if ("#sheet".equals (base))
            base = m_sSystemID;
      }

      final Locator prevLoc = context.locator;
      context.locator = null;
      proc.startInnerProcessing ();

      try
      {
        Value nextVal;
        XMLReader defaultReader = null;
        do
        {
          XMLReader reader;
          InputSource iSource;
          Source source;
          nextVal = v.next;
          v.next = null;
          final String hrefURI = v.getStringValue ();
          // ask URI resolver if present
          if (context.m_aURIResolver != null && (source = context.m_aURIResolver.resolve (hrefURI, base)) != null)
          {
            final SAXSource saxSource = TrAXHelper.getSAXSource (source, null);
            reader = saxSource.getXMLReader ();
            if (reader != null)
            {
              reader.setErrorHandler (context.m_aErrorHandler);
              reader.setContentHandler (contH);
              try
              {
                reader.setProperty ("http://xml.org/sax/properties/lexical-handler", lexH);
              }
              catch (final SAXException ex)
              {
                log.warn ("Accessing " + reader + ": " + ex);
                context.m_aErrorHandler.warning ("Accessing " +
                                                 reader +
                                                 ": " +
                                                 ex,
                                                 m_sPublicID,
                                                 m_sSystemID,
                                                 lineNo,
                                                 colNo,
                                                 ex);
              }
            }
            else
              reader = defaultReader;
            iSource = saxSource.getInputSource ();
          }
          else
          {
            // construct href relatively to base
            // (base must be an absolut URI)
            iSource = new InputSource (new URL (new URL (base), hrefURI).toExternalForm ());
            reader = defaultReader;
          }

          if (reader == null)
          { // i.e. defaultReader == null
            // construct a default XML reader,
            // happens at most once per process-document invocation
            reader = defaultReader = Processor.createXMLReader ();
            reader.setErrorHandler (context.m_aErrorHandler);
            reader.setContentHandler (contH);
            try
            {
              reader.setProperty ("http://xml.org/sax/properties/lexical-handler", lexH);
            }
            catch (final SAXException ex)
            {
              log.warn ("Accessing " + reader + ": " + ex);
              context.m_aErrorHandler.warning ("Accessing " +
                                               reader +
                                               ": " +
                                               ex,
                                               m_sPublicID,
                                               m_sSystemID,
                                               lineNo,
                                               colNo,
                                               ex);
            }
          }

          reader.parse (iSource);
          v = nextVal;
        } while (v != null);
      }
      catch (final java.io.IOException ex)
      {
        // TODO: better error handling
        context.m_aErrorHandler.error (new SAXParseException (ex.toString (), m_sPublicID, m_sSystemID, lineNo, colNo));
      }
      catch (final TransformerException te)
      {
        context.m_aErrorHandler.error (te);
      }
      proc.endInnerProcessing ();
      context.locator = prevLoc;
      return CSTX.PR_CONTINUE;
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (m_aBaseUri != null)
        theCopy.m_aBaseUri = m_aBaseUri.deepCopy (copies);
      if (m_aHref != null)
        theCopy.m_aHref = m_aHref.deepCopy (copies);
    }
  }
}
