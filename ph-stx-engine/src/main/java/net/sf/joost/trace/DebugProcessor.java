/*
 * $Id: DebugProcessor.java,v 1.22 2008/10/04 17:13:14 obecker Exp $
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
 * The Initial Developer of the Original Code is Anatolij Zubow.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): Oliver Becker.
 */

package net.sf.joost.trace;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.instruction.AbstractNodeBase;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.IParserListener;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Parser;
import net.sf.joost.stx.Processor;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.trax.TransformerImpl;

/**
 * Extends the {@link net.sf.joost.stx.Processor} with debug features.
 *
 * @version $Revision: 1.22 $ $Date: 2008/10/04 17:13:14 $
 * @author Zubow
 */
public class DebugProcessor extends Processor
{

  /** the TraceManager for dynamic tracing */
  private TraceManager m_aTraceMgr;
  /** the TrAX-Transformer */
  private TransformerImpl m_aTransformer;
  /** the ParserListener for static tracing */
  private IParserListener parserListener;

  private Locator m_aLocator;

  /** logger */
  private static Logger log = LoggerFactory.getLogger (DebugProcessor.class);

  /**
   * See
   * {@link net.sf.joost.stx.Processor#Processor(net.sf.joost.stx.Processor)}
   *
   * @throws SAXException
   */
  public DebugProcessor (final Processor proc) throws SAXException
  {
    super (proc);
  }

  /**
   * See {@link net.sf.joost.stx.Processor#Processor(net.sf.joost.stx.Parser)}
   */
  public DebugProcessor (final Parser stxParser) throws SAXException
  {
    super (stxParser);
  }

  /**
   * See
   * {@link net.sf.joost.stx.Processor#Processor(org.xml.sax.InputSource, ParseContext)}
   */
  public DebugProcessor (final InputSource src, final ParseContext pContext) throws IOException, SAXException
  {
    super (src, pContext);
  }

  /**
   * See
   * {@link net.sf.joost.stx.Processor#Processor(XMLReader, InputSource, ParseContext)}
   */
  public DebugProcessor (final XMLReader reader,
                         final InputSource src,
                         final ParseContext pContext,
                         final IStxEmitter messageEmitter) throws IOException, SAXException
  {
    super (reader, src, pContext);
    setMessageEmitter (messageEmitter);
  }

  /**
   * See
   * {@link net.sf.joost.stx.Processor#Processor(XMLReader, InputSource, ParseContext)}
   */
  public DebugProcessor (final XMLReader reader, final InputSource src, final ParseContext pContext) throws IOException,
                                                                                                     SAXException
  {
    super (reader, src, pContext);
  }

  /**
   * See {@link net.sf.joost.stx.Processor#copy()}
   *
   * @throws SAXException
   */
  @Override
  public Processor copy () throws SAXException
  {
    return new DebugProcessor (this);
  }

  // ------------------------------------------------------------------------
  // Methods
  // ------------------------------------------------------------------------

  @Override
  public void setDocumentLocator (final Locator locator)
  {
    super.setDocumentLocator (locator);
    this.m_aLocator = locator;
  }

  public Locator getDocumentLocator ()
  {
    return m_aLocator;
  }

  /**
   * Overriden method for debug purpose
   */
  @Override
  protected Emitter initializeEmitter (final Context ctx)
  {
    log.info ("initialize DebugProcessor ...");
    return new DebugEmitter (ctx.m_aErrorHandler);
  }

  /**
   * Overriden method for the execution of a given instruction.
   *
   * @param inst
   *        the instruction to be executed
   * @param event
   *        the current saxevent from source-document
   * @return return codes, see {@link CSTX}
   * @throws SAXException
   *         in case of errors.
   */
  @Override
  protected int processInstruction (final AbstractInstruction inst, final SAXEvent event) throws SAXException
  {

    boolean atomicnode = false;
    int ret = -1;

    // check, if transformation should be cancelled
    if (m_aTransformer.cancelTransformation)
    {
      return CSTX.PR_ERROR;
    }

    // found end element
    if (inst instanceof AbstractNodeBase.End)
    {
      // end node
      m_aTraceMgr.fireLeaveInstructionNode (inst, event);
    }
    else
    {
      // no corresponding endElement
      if (inst.getNode ().getNodeEnd () == null)
      {
        // remind this
        atomicnode = true;
      }
      // fire callback on tracemanager
      m_aTraceMgr.fireEnterInstructionNode (inst, event);
    }

    // process instruction
    ret = inst.process (getContext ());

    if (atomicnode && m_aTraceMgr != null)
    {
      // fire callback on tracemanager
      m_aTraceMgr.fireLeaveInstructionNode (inst, event);
      atomicnode = false;
    }
    return ret;
  }

  /**
   * getter for property {@link #parserListener}
   */
  public IParserListener getParserListener ()
  {
    return parserListener;
  }

  /**
   * setter for property {@link #m_aTraceMgr}
   */
  public void setTraceManager (final TraceManager tmgr)
  {
    this.m_aTraceMgr = tmgr;
  }

  /**
   * getter for property {@link #m_aTraceMgr}
   */
  public TraceManager getTraceManager ()
  {
    return this.m_aTraceMgr;
  }

  /**
   * getter for property {@link #m_aTransformer}
   */
  public TransformerImpl getTransformer ()
  {
    return m_aTransformer;
  }

  /**
   * setter for property {@link #m_aTransformer}
   */
  public void setTransformer (final TransformerImpl transformer)
  {
    this.m_aTransformer = transformer;
  }

  // --------------------------------------------------------------
  // Sax-callback methods
  // --------------------------------------------------------------

  /**
   * overloaded method of ContentHandler for debug information
   */
  @Override
  public void startDocument () throws SAXException
  {
    // process event
    super.startDocument ();
    // fire startprocessing event to tracelistener
    this.m_aTraceMgr.fireStartSourceDocument ();
  }

  /**
   * overloaded method of ContentHandler for debug information
   */
  @Override
  public void endDocument () throws SAXException
  {
    // process event
    super.endDocument ();
    // fire endprocessing event to tracelistener
    this.m_aTraceMgr.fireEndSourceDocument ();
  }

  /**
   * overloaded method of ContentHandler for debug information
   */
  @Override
  public void startElement (final String uri,
                            final String lName,
                            final String qName,
                            final Attributes attrs) throws SAXException
  {
    SAXEvent saxevent;

    // todo - namespace support - remove null value
    saxevent = SAXEvent.newElement (uri, lName, qName, attrs, false, null);

    // process event
    super.startElement (uri, lName, qName, attrs);
    // inform debugger
    this.m_aTraceMgr.fireStartSourceElement (saxevent);
  }

  /**
   * overloaded method of ContentHandler for debug information
   */
  @Override
  public void endElement (final String uri, final String lName, final String qName) throws SAXException
  {
    SAXEvent saxevent;

    // todo - namespace support - remove null value
    saxevent = SAXEvent.newElement (uri, lName, qName, null, false, null);

    // process event
    super.endElement (uri, lName, qName);
    // inform debugger
    this.m_aTraceMgr.fireEndSourceElement (saxevent);
  }

  /**
   * overloaded method of ContentHandler for debug information
   */
  @Override
  public void characters (final char [] ch, final int start, final int length) throws SAXException
  {
    SAXEvent saxevent;

    saxevent = SAXEvent.newText (new String (ch, start, length));
    // process event
    super.characters (ch, start, length);
    // inform debugger
    this.m_aTraceMgr.fireSourceText (saxevent);
  }

  /**
   * overloaded method of ContentHandler for debug information
   */
  @Override
  public void processingInstruction (final String target, final String data) throws SAXException
  {
    SAXEvent saxevent;

    saxevent = SAXEvent.newPI (target, data);
    // process event
    super.processingInstruction (target, data);
    // inform debugger
    this.m_aTraceMgr.fireSourcePI (saxevent);
  }

  /**
   * overloaded method of ContentHandler for debug information
   */
  @Override
  public void startPrefixMapping (final String prefix, final String uri) throws SAXException
  {
    SAXEvent saxevent;

    saxevent = SAXEvent.newMapping (prefix, uri);
    // process event
    super.startPrefixMapping (prefix, uri);
    // inform debugger
    this.m_aTraceMgr.fireSourceMapping (saxevent);
  }

  /**
   * overloaded method of LexicalHandler for debug information
   */
  @Override
  public void comment (final char [] ch, final int start, final int length) throws SAXException
  {
    SAXEvent saxevent;

    saxevent = SAXEvent.newComment (new String (ch, start, length));
    // process event
    super.comment (ch, start, length);
    // inform debugger
    this.m_aTraceMgr.fireSourceComment (saxevent);
  }

  /**
   * overloaded method of LexicalHandler for debug information
   */
  // public void startCDATA() {
  // problem - bestimme characters, die in einem CDATA-Abschnitt liegen
  // SAXEvent saxevent = SAXEvent.newCDATA()
  // }
  // public void endCDATA()
  // public void ignorableWhitespace(char[] ch, int start, int length)
  // public void startDTD(String name, String publicId, String systemId)
  // public void endDTD()
}
