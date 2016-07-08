/*
 * $Id: URIResolverTest.java,v 1.2 2007/07/18 05:40:28 obecker Exp $
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

package net.sf.joost.samples;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Example class that demonstrates the usage of URI resolvers in Joost.
 * @version $Revision: 1.2 $ $Date: 2007/07/18 05:40:28 $
 * @author Oliver Becker
 */

public class URIResolverTest implements URIResolver
{
   public static void main(String[] args)
   {
      // example transformation
      String testSTX = "URIResolverTest.stx";

      // use Joost as transformation engine
      System.setProperty("javax.xml.transform.TransformerFactory",
                         "net.sf.joost.trax.TransformerFactoryImpl");

      // my custom URIResolver
      URIResolver resolver = new URIResolverTest();

      try {
         TransformerFactory factory = TransformerFactory.newInstance();

         // register the resolver for <stx:include>
         factory.setURIResolver(resolver);

         StreamSource stxSource =
            new StreamSource(URIResolverTest.class.getResourceAsStream(testSTX));
         stxSource.setSystemId(
            URIResolverTest.class.getResource(testSTX).toExternalForm());
         Transformer transformer = factory.newTransformer(stxSource);

         // register the same resolver for <stx:process-document>
         transformer.setURIResolver(resolver);

         transformer.transform(new StreamSource(
                                  URIResolverTest.class.getResourceAsStream(testSTX)),
                               new StreamResult(System.out));
      } catch (TransformerException e) {
         SourceLocator sloc = e.getLocator();
         if (sloc != null)
            System.err.println(sloc.getSystemId() + ":" + 
                               sloc.getLineNumber() + ":" + 
                               sloc.getColumnNumber() + ": " +
                               e.getMessage());
         else
            System.err.println(e.getMessage());
      }
   }


   // ---------------------------------------------------------------------

   //
   // from interface URIResolver
   //


   public Source resolve(String href, String base)
   {
      System.err.println("resolve('" + href + "', '" + base + "')");
      try {
         if ("urn:stream".equals(href)) {
            System.err.println("-> constructing a StreamSource");
            return new StreamSource(new StringReader("<stream no='1'/>"));
         }
         if ("urn:dom".equals(href)) {
            System.err.println("-> constructing a DOMSource");
            DocumentBuilderFactory factory =
               DocumentBuilderFactory.newInstance();
            DocumentBuilder builder =
               factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element el = doc.createElement("dom");
            el.setAttribute("no", "2");
            doc.appendChild(el);
            return new DOMSource(doc);
         }
         if ("urn:import".equals(href)) {
            System.err.println("-> constructing a StreamSource with STX code");
            return new StreamSource(new StringReader(
   "<transform xmlns='http://stx.sourceforge.net/2002/ns' " + 
   "version='1.0' xml:space='preserve'>" +
   "<template match='*'>|  <copy attributes='@*'/>&#xA;" + 
   "</template></transform>"));
         }
      }
      catch (Exception ex) {
         System.err.print(ex);
      }
      System.err.println("-> returning null");
      return null;
   }
}
