/*
 * $Id: ResultDocumentFactory.java,v 2.23 2009/08/21 12:46:17 obecker Exp $
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

import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.AbstractStreamEmitter;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.trax.TrAXHelper;

/**
 * Factory for <code>result-document</code> elements, which are represented by
 * the inner Instance class.
 *
 * @version $Revision: 2.23 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

public final class ResultDocumentFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element */
  private final HashSet <String> attrNames;

  // Constructor
  public ResultDocumentFactory ()
  {
    attrNames = new HashSet<> ();
    attrNames.add ("href");
    attrNames.add ("output-encoding");
    attrNames.add ("output-method");
    attrNames.add ("append");
  }

  /** @return <code>"result-document"</code> */
  @Override
  public String getName ()
  {
    return "result-document";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                                      final String qName,
                                      final Attributes attrs,
                                      final ParseContext context) throws SAXParseException
  {
    final AbstractTree href = parseRequiredAVT (qName, attrs, "href", context);

    final String encodingAtt = attrs.getValue ("output-encoding");

    String methodAtt = attrs.getValue ("output-method");
    if (methodAtt != null)
    {
      if (methodAtt.indexOf (':') != -1)
        methodAtt = getExpandedName (methodAtt, context);
      else
        if (!methodAtt.equals ("text") && !methodAtt.equals ("xml"))
          throw new SAXParseException ("Value of attribute 'output-method' must be 'xml', 'text', " +
                                       "or a qualified name. Found '" +
                                       methodAtt +
                                       "'",
                                       context.locator);
    }

    // default is "no" (false)
    final boolean append = getEnumAttValue ("append", attrs, YESNO_VALUES, context) == YES_VALUE;

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, href, encodingAtt, methodAtt, append);
  }

  /** Represents an instance of the <code>result-document</code> element. */
  public static final class Instance extends AbstractNodeBase
  {
    private AbstractTree href;
    private String encoding;
    private final String method;
    private final boolean append;

    protected Instance (final String qName,
                        final AbstractNodeBase parent,
                        final ParseContext context,
                        final AbstractTree href,
                        final String encoding,
                        final String method,
                        final boolean append)
    {
      super (qName, parent, context, true);
      this.href = href;
      this.encoding = encoding;
      this.method = method;
      this.append = append;
    }

    /**
     * Redirects the result stream to the specified URI
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      super.process (context);
      if (encoding == null) // no encoding attribute specified
        // use global encoding att
        encoding = context.currentProcessor.getOutputEncoding ();

      final String filename = href.evaluate (context, this).getString ();

      final Properties props = (Properties) context.currentProcessor.m_aOutputProperties.clone ();
      props.setProperty (OutputKeys.ENCODING, encoding);
      if (method != null)
        props.setProperty (OutputKeys.METHOD, method);

      IStxEmitter emitter = null;
      try
      {
        if (context.outputUriResolver != null)
        {
          final Result result = context.outputUriResolver.resolve (filename, m_sSystemID, props, append);
          if (result != null)
          {
            emitter = TrAXHelper.initStxEmitter (result, context.currentProcessor, props);
            if (emitter == null)
            {
              throw new SAXParseException ("Unsupported Result type " +
                                           result.getClass ().getName (),
                                           m_sPublicID,
                                           m_sSystemID,
                                           lineNo,
                                           colNo);
            }
            if (append && (emitter instanceof AbstractStreamEmitter))
            {
              ((AbstractStreamEmitter) emitter).setOmitXmlDeclaration (true);
            }
            m_aLocalFieldStack.push (result);
          }
        }

        if (emitter == null)
        {
          // either there's no outputUriResolver or it returned null
          final Writer osw = context.m_aEmitter.getResultWriter (filename,
                                                              encoding,
                                                              m_sPublicID,
                                                              m_sSystemID,
                                                              lineNo,
                                                              colNo,
                                                              append);

          final AbstractStreamEmitter se = AbstractStreamEmitter.newEmitter (osw, encoding, props);
          if (append)
            se.setOmitXmlDeclaration (true);
          m_aLocalFieldStack.push (osw);
          emitter = se;
        }
      }
      catch (final java.io.IOException ex)
      {
        context.m_aErrorHandler.error (ex.toString (), m_sPublicID, m_sSystemID, lineNo, colNo, ex);
        // if the errorHandler returns
        return CSTX.PR_CONTINUE;
      }
      catch (final URISyntaxException ex)
      {
        context.m_aErrorHandler.error (ex.toString (), m_sPublicID, m_sSystemID, lineNo, colNo, ex);
        // if the errorHandler returns
        return CSTX.PR_CONTINUE;
      }
      catch (final TransformerException ex)
      {
        context.m_aErrorHandler.error (ex);
      }

      context.pushEmitter (emitter);
      context.m_aEmitter.startDocument ();
      return CSTX.PR_CONTINUE;
    }

    /** Close the current result stream */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      context.m_aEmitter.endDocument (m_aNodeEnd);
      context.popEmitter ();
      final Object object = m_aLocalFieldStack.pop ();
      try
      {
        if (object instanceof Writer)
        {
          ((Writer) object).close ();
        }
        else
        {
          // must be a Result from the OutputURIResolver
          context.outputUriResolver.close ((Result) object);
        }
      }
      catch (final java.io.IOException ex)
      {
        context.m_aErrorHandler.error (ex.toString (),
                                       m_sPublicID,
                                       m_sSystemID,
                                       m_aNodeEnd.lineNo,
                                       m_aNodeEnd.colNo,
                                       ex);
      }
      catch (final TransformerException ex)
      {
        context.m_aErrorHandler.error (ex);
      }

      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (href != null)
        theCopy.href = href.deepCopy (copies);
    }

  }
}
