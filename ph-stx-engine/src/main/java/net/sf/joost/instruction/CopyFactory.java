/*
 * $Id: CopyFactory.java,v 2.13 2008/10/04 17:13:14 obecker Exp $
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

import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import net.sf.joost.CSTX;
import net.sf.joost.OptionalLog;
import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.SAXEvent;

/**
 * Factory for <code>copy</code> elements, which are represented by the inner
 * Instance class.
 *
 * @version $Revision: 2.13 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public final class CopyFactory extends AbstractFactoryBase
{
  /** allowed attributes for this element. */
  private final HashSet attrNames;

  /** empty attribute list (needed as parameter for startElement) */
  private static Attributes emptyAttList = new AttributesImpl ();

  // Logger initialization
  private static Logger log = OptionalLog.getLog (CopyFactory.class);

  // Constructor
  public CopyFactory ()
  {
    attrNames = new HashSet ();
    attrNames.add ("attributes");
  }

  /** @return <code>"copy"</code> */
  @Override
  public String getName ()
  {
    return "copy";
  }

  @Override
  public AbstractNodeBase createNode (final AbstractNodeBase parent,
                              final String qName,
                              final Attributes attrs,
                              final ParseContext context) throws SAXParseException
  {
    final AbstractTree attributesPattern = parsePattern (attrs.getValue ("attributes"), context);

    checkAttributes (qName, attrs, attrNames, context);
    return new Instance (qName, parent, context, attributesPattern);
  }

  /** Represents an instance of the <code>copy</code> element. */
  public final class Instance extends AbstractNodeBase
  {
    /**
     * the pattern in the <code>attributes</code> attribute, <code>null</code>
     * if this attribute is missing
     */
    private AbstractTree attPattern;

    /**
     * <code>true</code> if {@link #attPattern} is a wildcard (<code>@*</code>)
     */
    private boolean attrWildcard = false;

    /** instruction pointers */
    private AbstractInstruction contents, successor;

    //
    // Constructor
    //

    public Instance (final String qName, final AbstractNodeBase parent, final ParseContext context, final AbstractTree attPattern)
    {
      super (qName, parent, context, true);
      this.attPattern = attPattern;
      if (attPattern != null && attPattern.type == AbstractTree.ATTR_WILDCARD)
        attrWildcard = true;
    }

    /** Store pointers to the contents and the successor */
    @Override
    public boolean compile (final int pass, final ParseContext context)
    {
      if (pass == 0)
        return true; // successor not available yet

      contents = next;
      successor = nodeEnd.next;
      return false;
    }

    /**
     * Copy the begin of the current node to the result stream.
     */
    @Override
    public short process (final Context context) throws SAXException
    {
      final SAXEvent event = (SAXEvent) context.ancestorStack.peek ();
      switch (event.type)
      {
        case SAXEvent.ROOT:
          super.process (context);
          next = contents;
          break;
        case SAXEvent.ELEMENT:
        {
          super.process (context);
          final Attributes attList = attrWildcard ? event.attrs : emptyAttList;
          context.emitter.startElement (event.uri, event.lName, event.qName, attList, event.namespaces, this);
          if (attPattern != null && !attrWildcard)
          {
            // attribute pattern present, but no wildcard (@*)
            final int attrNum = event.attrs.getLength ();
            for (int i = 0; i < attrNum; i++)
            {
              // put attributes on the event stack for matching
              context.ancestorStack.push (SAXEvent.newAttribute (event.attrs, i));
              if (attPattern.matches (context, context.ancestorStack.size (), false))
              {
                final SAXEvent attrEvent = (SAXEvent) context.ancestorStack.peek ();
                context.emitter.addAttribute (attrEvent.uri, attrEvent.qName, attrEvent.lName, attrEvent.value, this);
              }
              // remove attribute
              context.ancestorStack.pop ();
            }
          }
          next = contents;
          break;
        }
        case SAXEvent.TEXT:
          context.emitter.characters (event.value.toCharArray (), 0, event.value.length (), this);
          next = successor;
          break;
        case SAXEvent.CDATA:
          context.emitter.startCDATA (this);
          context.emitter.characters (event.value.toCharArray (), 0, event.value.length (), this);
          context.emitter.endCDATA ();
          next = successor;
          break;
        case SAXEvent.PI:
          context.emitter.processingInstruction (event.qName, event.value, this);
          next = successor;
          break;
        case SAXEvent.COMMENT:
          context.emitter.comment (event.value.toCharArray (), 0, event.value.length (), this);
          next = successor;
          break;
        case SAXEvent.ATTRIBUTE:
          context.emitter.addAttribute (event.uri, event.qName, event.lName, event.value, this);
          next = successor;
          break;
        default:
          log.error ("Unknown SAXEvent type " + event.type);
          throw new SAXParseException ("Unknown SAXEvent type", publicId, systemId, lineNo, colNo);
      }
      return CSTX.PR_CONTINUE;
    }

    /**
     * Copy the end, if the current node is an element.
     */
    @Override
    public short processEnd (final Context context) throws SAXException
    {
      final SAXEvent event = (SAXEvent) context.ancestorStack.peek ();
      if (event.type == SAXEvent.ELEMENT)
        context.emitter.endElement (event.uri, event.lName, event.qName, this);
      return super.processEnd (context);
    }

    @Override
    protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
    {
      super.onDeepCopy (copy, copies);
      final Instance theCopy = (Instance) copy;
      if (contents != null)
        theCopy.contents = contents.deepCopy (copies);
      if (successor != null)
        theCopy.successor = successor.deepCopy (copies);
      if (attPattern != null)
        theCopy.attPattern = attPattern.deepCopy (copies);
    }
  }
}
