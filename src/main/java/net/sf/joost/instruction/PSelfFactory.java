/*
 * $Id: PSelfFactory.java,v 2.4 2007/11/25 14:18:01 obecker Exp $
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.HashSet;

import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;


/**
 * Factory for <code>process-self</code> elements, which are represented by 
 * the inner Instance class.
 * @version $Revision: 2.4 $ $Date: 2007/11/25 14:18:01 $
 * @author Oliver Becker
 */

public class PSelfFactory extends FactoryBase
{
   /** allowed attributes for this element */
   private HashSet attrNames;

   // Constructor
   public PSelfFactory()
   {
      attrNames = new HashSet();
      attrNames.add("group");
      attrNames.add("filter-method");
      attrNames.add("filter-src");
   }


   /** @return <code>"process-self"</code> */
   public String getName()
   {
      return "process-self";
   }

   public NodeBase createNode(NodeBase parent, String qName, 
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      String groupAtt = attrs.getValue("group");

      String filterMethodAtt = attrs.getValue("filter-method");

      if (groupAtt != null && filterMethodAtt != null)
         throw new SAXParseException(
            "It's not allowed to use both 'group' and 'filter-method' attributes",
            context.locator);

      String filterSrcAtt = attrs.getValue("filter-src");

      if (filterSrcAtt != null && filterMethodAtt == null)
         throw new SAXParseException(
            "Missing 'filter-method' attribute in '" + qName + 
            "' ('filter-src' is present)",
            context.locator);

      checkAttributes(qName, attrs, attrNames, context);

      return new Instance(qName, parent, context, groupAtt,
                          filterMethodAtt, filterSrcAtt);
   }


   /** The inner Instance class */
   public class Instance extends ProcessBase
   {
      // Constructor
      public Instance(String qName, NodeBase parent, ParseContext context,
                      String groupQName, String method, String src)
         throws SAXParseException
      {
         super(qName, parent, context, groupQName, method, src);
      }


      /** 
       * @return {@link #PR_SELF}
       */
      public short processEnd(Context context)
         throws SAXException
      {
         // no need to call super.processEnd(), there are no local
         // variable declarations
         if (filter != null) {
            // use external SAX filter (TransformerHandler)
            context.targetHandler = getProcessHandler(context);
            if (context.targetHandler == null)
               return PR_ERROR;
         }
         return PR_SELF;
      }
   }
}
