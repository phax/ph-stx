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

import net.sf.joost.emitter.StreamEmitter;
import net.sf.joost.emitter.StxEmitter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.trax.TrAXHelper;

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


/**
 * Factory for <code>result-document</code> elements, which are represented by
 * the inner Instance class.
 * @version $Revision: 2.23 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */

final public class ResultDocumentFactory extends FactoryBase
{
   /** allowed attributes for this element */
   private HashSet attrNames;

   // Constructor
   public ResultDocumentFactory()
   {
      attrNames = new HashSet();
      attrNames.add("href");
      attrNames.add("output-encoding");
      attrNames.add("output-method");
      attrNames.add("append");
   }


   /** @return <code>"result-document"</code> */
   public String getName()
   {
      return "result-document";
   }

   public NodeBase createNode(NodeBase parent, String qName,
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      Tree href = parseRequiredAVT(qName, attrs, "href", context);

      String encodingAtt = attrs.getValue("output-encoding");

      String methodAtt = attrs.getValue("output-method");
      if (methodAtt != null) {
         if (methodAtt.indexOf(':') != -1)
            methodAtt = getExpandedName(methodAtt, context);
         else if (!methodAtt.equals("text") && !methodAtt.equals("xml"))
            throw new SAXParseException(
               "Value of attribute 'output-method' must be 'xml', 'text', " +
               "or a qualified name. Found '" + methodAtt + "'",
               context.locator);
      }

      // default is "no" (false)
      boolean append =
         getEnumAttValue("append", attrs, YESNO_VALUES, context) == YES_VALUE;

      checkAttributes(qName, attrs, attrNames, context);
      return new Instance(qName, parent, context, href, encodingAtt,
                          methodAtt, append);
   }


   /** Represents an instance of the <code>result-document</code> element. */
   final public class Instance extends NodeBase
   {
      private Tree href;
      private String encoding, method;
      private boolean append;

      protected Instance(String qName, NodeBase parent, ParseContext context,
                         Tree href, String encoding, String method,
                         boolean append)
      {
         super(qName, parent, context, true);
         this.href = href;
         this.encoding = encoding;
         this.method = method;
         this.append = append;
      }


      /**
       * Redirects the result stream to the specified URI
       */
      public short process(Context context)
         throws SAXException
      {
         super.process(context);
         if (encoding == null) // no encoding attribute specified
            // use global encoding att
            encoding = context.currentProcessor.getOutputEncoding();

         String filename = href.evaluate(context, this).getString();

         Properties props =
            (Properties)context.currentProcessor.outputProperties.clone();
         props.setProperty(OutputKeys.ENCODING, encoding);
         if (method != null)
            props.setProperty(OutputKeys.METHOD, method);

         StxEmitter emitter = null;
         try {
            if (context.outputUriResolver != null) {
               Result result =
                  context.outputUriResolver.resolve(filename, systemId,
                                                    props, append);
               if (result != null) {
                  emitter = TrAXHelper.initStxEmitter(result,
                                                      context.currentProcessor,
                                                      props);
                  if (emitter == null) {
                     throw new SAXParseException("Unsupported Result type "
                                                 + result.getClass().getName(),
                                                 publicId, systemId, lineNo, colNo);
                  }
                  if (append && (emitter instanceof StreamEmitter)) {
                     ((StreamEmitter) emitter).setOmitXmlDeclaration(true);
                  }
                  localFieldStack.push(result);
               }
            }

            if (emitter == null) {
               // either there's no outputUriResolver or it returned null
               Writer osw = context.emitter.getResultWriter(
                               filename, encoding,
                               publicId, systemId, lineNo, colNo, append);

               StreamEmitter se = StreamEmitter.newEmitter(osw, encoding, props);
               if (append)
                  se.setOmitXmlDeclaration(true);
               localFieldStack.push(osw);
               emitter = se;
            }
         }
         catch (java.io.IOException ex) {
            context.errorHandler.error(ex.toString(),
                                       publicId, systemId, lineNo, colNo, ex);
            return PR_CONTINUE; // if the errorHandler returns
         }
         catch (URISyntaxException ex) {
            context.errorHandler.error(ex.toString(),
                                       publicId, systemId, lineNo, colNo, ex);
            return PR_CONTINUE; // if the errorHandler returns
         }
         catch (TransformerException ex) {
            context.errorHandler.error(ex);
         }


         context.pushEmitter(emitter);
         context.emitter.startDocument();
         return PR_CONTINUE;
      }


      /** Close the current result stream */
      public short processEnd(Context context)
         throws SAXException
      {
         context.emitter.endDocument(nodeEnd);
         context.popEmitter();
         Object object = localFieldStack.pop();
         try {
            if (object instanceof Writer) {
               ((Writer)object).close();
            }
            else {
               // must be a Result from the OutputURIResolver
               context.outputUriResolver.close((Result)object);
            }
         }
         catch (java.io.IOException ex) {
            context.errorHandler.error(ex.toString(),
                                       publicId, systemId,
                                       nodeEnd.lineNo, nodeEnd.colNo,
                                       ex);
         }
         catch (TransformerException ex) {
            context.errorHandler.error(ex);
         }

         return super.processEnd(context);
      }


      protected void onDeepCopy(AbstractInstruction copy, HashMap copies)
      {
         super.onDeepCopy(copy, copies);
         Instance theCopy = (Instance) copy;
         if (href != null)
            theCopy.href = href.deepCopy(copies);
      }

   }
}
