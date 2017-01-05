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
// taken from Michael Kay's Saxon, see http://saxon.sourceforge.net
package net.sf.joost.trax;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * DOMDriver.java: (pseudo-)SAX driver for DOM.<BR>
 * This class simulates the action of a SAX Parser, taking an
 * already-constructed DOM Document and walking around it in a depth-first
 * traversal, calling a SAX-compliant ContentHandler to process the children as
 * it does so.
 *
 * @author MHK, 5 Jun 1998
 * @version 20 Jan 1999 modified to use AttributeListWrapper class
 * @version 3 February 2000 modified to use AttributeCollection class
 * @version 24 February 2000 modified to drive SAX2, which means it has to do
 *          namespace handling
 */

public class DOMDriver implements Locator, XMLReader
{

  protected ContentHandler contentHandler = new DefaultHandler ();
  private LexicalHandler lexicalHandler = null;
  private final NamespaceSupport nsSupport = new NamespaceSupport ();
  private final AttributesImpl attlist = new AttributesImpl ();
  private final String [] parts = new String [3];
  private final String [] elparts = new String [3];
  private final Hashtable <String, String> nsDeclarations = new Hashtable<> ();
  protected Node root = null;
  protected String m_sSystemID;

  /**
   * Set the content handler.
   *
   * @param handler
   *        The object to receive content events. If this also implements
   *        LexicalHandler, it will also be notified of comments.
   */

  public void setContentHandler (final ContentHandler handler)
  {
    this.contentHandler = handler;
    if (handler instanceof LexicalHandler)
    {
      lexicalHandler = (LexicalHandler) handler;
    }
  }

  public ContentHandler getContentHandler ()
  {
    return contentHandler;
  }

  /**
   * <b>SAX1</b>: Sets the locale used for diagnostics; currently, only locales
   * using the English language are supported.
   *
   * @param locale
   *        The locale for which diagnostics will be generated
   */

  public void setLocale (final Locale locale)
  {}

  /**
   * <b>SAX2</b>: Returns the object used when resolving external entities
   * during parsing (both general and parameter entities).
   */

  public EntityResolver getEntityResolver ()
  {
    return null;
  }

  /**
   * <b>SAX1, SAX2</b>: Set the entity resolver for this parser.
   */

  public void setEntityResolver (final EntityResolver resolver)
  {}

  /**
   * <b>SAX2</b>: Returns the object used to process declarations related to
   * notations and unparsed entities.
   */

  public DTDHandler getDTDHandler ()
  {
    return null;
  }

  /**
   * <b>SAX1, SAX2</b>: Set the DTD handler for this parser.
   *
   * @param handler
   *        The object to receive DTD events.
   */
  public void setDTDHandler (final DTDHandler handler)
  {}

  /**
   * <b>SAX1</b>: Set the document handler for this parser. If a content handler
   * was set, this document handler will supplant it. The parser is set to
   * report all XML 1.0 names rather than to filter out "xmlns" attributes (the
   * "namespace-prefixes" feature is set to true).
   *
   * @deprecated SAX2 programs should use the XMLReader interface and a
   *             ContentHandler.
   * @param handler
   *        The object to receive document events.
   */

  // public void setDocumentHandler (DocumentHandler handler) {}

  /**
   * <b>SAX1, SAX2</b>: Set the error handler for this parser.
   *
   * @param handler
   *        The object to receive error events.
   */

  public void setErrorHandler (final ErrorHandler handler)
  {}

  /**
   * <b>SAX2</b>: Returns the object used to receive callbacks for XML errors of
   * all levels (fatal, nonfatal, warning); this is never null;
   */

  public ErrorHandler getErrorHandler ()
  {
    return null;
  }

  /**
   * Set the DOM Document that will be walked
   */

  public void setDocument (final Document doc)
  {
    root = doc;
  }

  /**
   * Parse from InputSource. The InputSource is ignored; it's there only to
   * satisfy the XMLReader interface
   */

  public void parse (final InputSource source) throws SAXException
  {
    parse ();
  }

  /**
   * Parse from SystemId. The SystemId is ignored; it's there only to satisfy
   * the XMLReader interface
   */

  public void parse (final String source) throws SAXException
  {
    parse ();
  }

  /**
   * Walk a document (traversing the nodes depth first)
   *
   * @exception SAXException
   *            On any error in the document
   */

  public void parse () throws SAXException
  {
    if (root == null)
    {
      throw new SAXException ("DOMDriver: no start node defined");
    }
    if (contentHandler == null)
    {
      throw new SAXException ("DOMDriver: no content handler defined");
    }

    contentHandler.setDocumentLocator (this);
    contentHandler.startDocument ();
    walkNode (root); // walk the root node
    contentHandler.endDocument ();
  }

  /**
   * Walk a node of a document (traversing the children depth first)
   *
   * @param node
   *        The DOM Node object to walk
   * @exception SAXException
   *            On any error in the document
   */

  private void walkNode (final Node node) throws SAXException
  {
    if (node.hasChildNodes ())
    {
      final NodeList nit = node.getChildNodes ();
      for (int i = 0; i < nit.getLength (); i++)
      {
        final Node child = nit.item (i);
        switch (child.getNodeType ())
        {
          case Node.DOCUMENT_NODE:
            break; // should not happen
          case Node.ELEMENT_NODE:
            final Element element = (Element) child;
            nsSupport.pushContext ();
            attlist.clear ();
            nsDeclarations.clear ();

            // we can't rely on namespace declaration attributes being present -
            // there may be undeclared namespace prefixes. (If the DOM is a
            // Saxon
            // tree, there will be no namespace declaration attributes.) So we
            // declare all namespaces encountered, to be on the safe side.

            // String elname = element.getTagName();
            // if (elname.indexOf(':')>=0) {
            // also need to do this for default namespace
            try
            {
              final String prefix = element.getPrefix ();
              final String uri = element.getNamespaceURI ();
              // System.err.println("Implicit Namespace: " + prefix + "=" +
              // uri);
              if (prefix != null && nsDeclarations.get (prefix) == null)
              {
                nsSupport.declarePrefix (prefix, uri);
                contentHandler.startPrefixMapping (prefix, uri);
                nsDeclarations.put (prefix, uri);
              }
            }
            catch (final Throwable err)
            {
              // it must be a level 1 DOM
            }
            // }

            final NamedNodeMap atts = element.getAttributes ();
            for (int a1 = 0; a1 < atts.getLength (); a1++)
            {
              final Attr att = (Attr) atts.item (a1);
              final String attname = att.getName ();
              if (attname.equals ("xmlns"))
              {
                // System.err.println("Default namespace: " + att.getValue());
                if (nsDeclarations.get ("") == null)
                {
                  final String uri = att.getValue ();
                  nsSupport.declarePrefix ("", uri);
                  contentHandler.startPrefixMapping ("", uri);
                  nsDeclarations.put ("", uri);
                }
              }
              else
                if (attname.startsWith ("xmlns:"))
                {
                  // System.err.println("Namespace: " + attname.substring(6) +
                  // "=" + att.getValue());
                  final String prefix = attname.substring (6);
                  if (nsDeclarations.get (prefix) == null)
                  {
                    final String uri = att.getValue ();
                    nsSupport.declarePrefix (prefix, uri);
                    contentHandler.startPrefixMapping (prefix, uri);
                    nsDeclarations.put (prefix, uri);
                  }
                }
                else
                  if (attname.indexOf (':') >= 0)
                  {
                    try
                    {
                      final String prefix = att.getPrefix ();
                      final String uri = att.getNamespaceURI ();
                      // System.err.println("Implicit Namespace: " + prefix +
                      // "=" + uri);
                      if (nsDeclarations.get (prefix) == null)
                      {
                        nsSupport.declarePrefix (prefix, uri);
                        contentHandler.startPrefixMapping (prefix, uri);
                        nsDeclarations.put (prefix, uri);
                      }
                    }
                    catch (final Throwable err)
                    {
                      // it must be a level 1 DOM
                    }
                  }
            }
            for (int a2 = 0; a2 < atts.getLength (); a2++)
            {
              final Attr att = (Attr) atts.item (a2);
              final String attname = att.getName ();
              if (!attname.equals ("xmlns") && !attname.startsWith ("xmlns:"))
              {
                // System.err.println("Processing attribute " + attname);
                final String [] parts2 = nsSupport.processName (attname, parts, true);
                if (parts2 == null)
                {
                  throw new SAXException ("Undeclared namespace in " + attname);
                }
                attlist.addAttribute (parts2[0], parts2[1], parts2[2], "CDATA", att.getValue ());
              }
            }
            final String [] elparts2 = nsSupport.processName (element.getTagName (), elparts, false);
            if (elparts2 == null)
            {
              throw new SAXException ("Undeclared namespace in " + element.getTagName ());
            }
            final String uri = elparts2[0];
            final String local = elparts2[1];
            final String raw = elparts2[2];

            contentHandler.startElement (uri, local, raw, attlist);

            walkNode (element);

            contentHandler.endElement (uri, local, raw);
            final Enumeration <String> prefixes = nsSupport.getDeclaredPrefixes ();
            while (prefixes.hasMoreElements ())
            {
              final String prefix = prefixes.nextElement ();
              contentHandler.endPrefixMapping (prefix);
            }
            nsSupport.popContext ();
            break;
          case Node.ATTRIBUTE_NODE: // have already dealt with attributes
            break;
          case Node.PROCESSING_INSTRUCTION_NODE:
            contentHandler.processingInstruction (((ProcessingInstruction) child).getTarget (),
                                                  ((ProcessingInstruction) child).getData ());
            break;
          case Node.COMMENT_NODE:
            if (lexicalHandler != null)
            {
              final String text = ((Comment) child).getData ();
              if (text != null)
              {
                lexicalHandler.comment (text.toCharArray (), 0, text.length ());
              }
            }
            break;
          case Node.TEXT_NODE:
          case Node.CDATA_SECTION_NODE:
            final String text = ((CharacterData) child).getData ();
            if (text != null)
            {
              contentHandler.characters (text.toCharArray (), 0, text.length ());
            }
            break;
          case Node.ENTITY_REFERENCE_NODE:
            walkNode (child);
            break;
          default:
            break; // should not happen
        }
      }
    }

  }

  //
  // Implementation of org.xml.sax.Locator.
  //

  public void setSystemId (final String systemId)
  {
    this.m_sSystemID = systemId;
  }

  public String getPublicId ()
  {
    return null; // TODO
  }

  public String getSystemId ()
  {
    return m_sSystemID;
  }

  public int getLineNumber ()
  {
    return -1;
  }

  public int getColumnNumber ()
  {
    return -1;
  }

  // Features and properties

  static final String FEATURE = "http://xml.org/sax/features/";
  static final String HANDLER = "http://xml.org/sax/properties/";

  /**
   * <b>SAX2</b>: Tells the value of the specified feature flag.
   *
   * @exception SAXNotRecognizedException
   *            thrown if the feature flag is neither built in, nor yet
   *            assigned.
   */

  public boolean getFeature (final String featureId) throws SAXNotRecognizedException
  {
    if ((FEATURE + "validation").equals (featureId))
      return false;

    // external entities (both types) are currently always excluded
    if ((FEATURE + "external-general-entities").equals (featureId) ||
        (FEATURE + "external-parameter-entities").equals (featureId))
      return false;

    // element/attribute names are as namespace-sensitive
    if ((FEATURE + "namespace-prefixes").equals (featureId))
      return false;

    // report element/attribute namespaces?
    if ((FEATURE + "namespaces").equals (featureId))
      return true;

    // always interns: no
    if ((FEATURE + "string-interning").equals (featureId))
      return false;

    throw new SAXNotRecognizedException (featureId);
  }

  /**
   * <b>SAX2</b>: Returns the specified property.
   *
   * @exception SAXNotRecognizedException
   *            thrown if the property value is neither built in, nor yet
   *            stored.
   */

  public Object getProperty (final String name) throws SAXNotRecognizedException
  {
    if (name.equals ("http://xml.org/sax/properties/lexical-handler"))
    {
      return lexicalHandler;
    }
    throw new SAXNotRecognizedException (name);
  }

  /**
   * <b>SAX2</b>: Sets the state of feature flags in this parser. Some built-in
   * feature flags are mutable; all flags not built-in are motable.
   */
  public void setFeature (final String featureId, final boolean on) throws SAXNotRecognizedException,
                                                                    SAXNotSupportedException
  {
    if ((FEATURE + "validation").equals (featureId))
    {
      if (on)
        throw new SAXNotSupportedException (featureId + " feature cannot be switched on");
      return;
    }

    // external entities (both types) are currently always excluded
    if ((FEATURE + "external-general-entities").equals (featureId) ||
        (FEATURE + "external-parameter-entities").equals (featureId))
    {
      if (on)
        throw new SAXNotSupportedException (featureId + " feature cannot be switched on");
      return;
    }

    // element/attribute names are as namespace-sensitive
    if ((FEATURE + "namespace-prefixes").equals (featureId))
    {
      if (on)
        throw new SAXNotSupportedException (featureId + " feature cannot be switched on");
      return;
    }

    // report element/attribute namespaces?
    if ((FEATURE + "namespaces").equals (featureId))
    {
      if (!on)
        throw new SAXNotSupportedException (featureId + " feature cannot be switched off");
      return;
    }

    // always interns: no
    if ((FEATURE + "string-interning").equals (featureId))
    {
      if (on)
        throw new SAXNotSupportedException (featureId + " feature cannot be switched on");
      return;
    }

    throw new SAXNotRecognizedException ("Feature not recognized: " + featureId);
  }

  /**
   * <b>SAX2</b>: Assigns the specified property. Like SAX1 handlers, these may
   * be changed at any time.
   */

  public void setProperty (final String propertyId, final Object property) throws SAXNotRecognizedException,
                                                                           SAXNotSupportedException
  {
    if (propertyId.equals ("http://xml.org/sax/properties/lexical-handler"))
    {
      if (property instanceof LexicalHandler)
      {
        lexicalHandler = (LexicalHandler) property;
      }
      else
      {
        throw new SAXNotSupportedException ("Lexical Handler must be instance of org.xml.sax.ext.LexicalHandler");
      }
    }
    else
    {
      throw new SAXNotRecognizedException (propertyId);
    }
  }

}
//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (mhkay@iclway.co.uk).
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
