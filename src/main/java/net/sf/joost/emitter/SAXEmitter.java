/*
 * $Id: SAXEmitter.java,v 1.9 2007/11/15 13:29:51 obecker Exp $
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
 * Contributor(s): ______________________________________.
 */


package net.sf.joost.emitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;


/**
 *  This class implements the common interface <code>StxEmitter</code>.
 *  Is is designed for using <code>SAXResult</code>.
 *  So this class outputs a SAX2-event-stream to the output target -
 *  {@link #saxContentHandler} (e.g. the registered ContentHandler).
 *  @author Zubow
 */
public class SAXEmitter extends StxEmitterBase {

    // Define a static logger variable so that it references the
    // Logger instance named "SAXEmitter".
    private static Log log;
    static {
        if (DEBUG)
            log = LogFactory.getLog(SAXEmitter.class);
    }

    /**
     * The {@link SAXEmitter} acts as a proxy und propagates SAX2 events to
     * this handler.
     */
    private ContentHandler saxContentHandler = null;
    
    /**
     * If present, the {@link SAXEmitter} propagates lexical SAX2 events this
     * handler.
     */
    private LexicalHandler saxLexicalHandler = null;


    /**
     * Constructor
     * @param saxSourceHandler A ContentHandler for the SAXResult
     */
    public SAXEmitter(ContentHandler saxSourceHandler) {

        if (DEBUG)
            log.debug("init SAXEmitter");
        this.saxContentHandler = saxSourceHandler;
        if (saxSourceHandler instanceof LexicalHandler)
        	saxLexicalHandler = (LexicalHandler) saxSourceHandler;

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void startDocument() throws SAXException {

        //act as proxy
        saxContentHandler.startDocument();

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void endDocument() throws SAXException {

        //act as proxy
        saxContentHandler.endDocument();

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void startElement(String uri, String local, String raw,
                            Attributes attrs)
        throws SAXException {

        saxContentHandler.startElement(uri, local, raw, attrs);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void endElement(String uri, String local, String raw)
        throws SAXException {

        saxContentHandler.endElement(uri, local, raw);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {

        saxContentHandler.characters(ch, start, length);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {

        saxContentHandler.startPrefixMapping(prefix, uri);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void endPrefixMapping(String prefix) throws SAXException {

        saxContentHandler.endPrefixMapping(prefix);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void processingInstruction(String target, String data)
        throws SAXException {

        saxContentHandler.processingInstruction(target, data);

    }

    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void skippedEntity(String value)
        throws SAXException {

        saxContentHandler.skippedEntity(value);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void ignorableWhitespace(char[] p0, int p1, int p2)
        throws SAXException {

        saxContentHandler.ignorableWhitespace(p0, p1, p2);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxContentHandler}
     */
    public void setDocumentLocator(Locator locator) {

        saxContentHandler.setDocumentLocator(locator);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxLexicalHandler}
     */
    public void startDTD(String name, String publicId, String systemId)
        throws SAXException {

    	if (saxLexicalHandler != null)
    		saxLexicalHandler.startDTD(name, publicId, systemId);

    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxLexicalHandler}
     */
    public void endDTD() throws SAXException {
    	
    	if (saxLexicalHandler != null)
    		saxLexicalHandler.endDTD();
    	
    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxLexicalHandler}
     */
    public void startEntity(String name) throws SAXException {
    	
    	if (saxLexicalHandler != null)
    		saxLexicalHandler.startEntity(name);
    	
    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxLexicalHandler}
     */
    public void endEntity(String name) throws SAXException {
    	
    	if (saxLexicalHandler != null)
    		saxLexicalHandler.endEntity(name);
    	
    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxLexicalHandler}
     */
    public void startCDATA() throws SAXException {
    	
    	if (saxLexicalHandler != null)
    		saxLexicalHandler.startCDATA();
    	
    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxLexicalHandler}
     */
    public void endCDATA() throws SAXException {
    	
    	if (saxLexicalHandler != null)
    		saxLexicalHandler.endCDATA();
    	
    }


    /**
     * SAX2-Callback - Simply propagates the Call to the registered output
     * target - here the {@link #saxLexicalHandler}
     */
    public void comment(char[] ch, int start, int length)
        throws SAXException {
    	
    	if (saxLexicalHandler != null)
    		saxLexicalHandler.comment(ch, start, length);
    	
    }
}
