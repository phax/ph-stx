/*
 * $Id: ErrorListenerImpl.java,v 1.1 2007/07/15 15:32:28 obecker Exp $
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

package net.sf.joost.test.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * ErrorListener implementation
 *
 * @author Zubow
 */
public class ErrorListenerImpl implements ErrorListener {

    private String name;

    /**
     * Constructor
     */
    public ErrorListenerImpl(String name) {
        this.name = name;
    }

    /**
     * Receive notification of a warning.
     *
     * {@link javax.xml.transform.ErrorListener#warning(TransformerException)}
     *
     * <p>{@link javax.xml.transform.Transformer} can use this method to report
     * conditions that are not errors or fatal errors.  The default behaviour
     * is to take no action.</p>
     *
     * <p>After invoking this method, the Transformer must continue with
     * the transformation. It should still be possible for the
     * application to process the document through to the end.</p>
     *
     * @param exception The warning information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */
    public void warning(TransformerException exception)
        throws TransformerException {

        System.err.println("WARNING occured - ErrorListenerImpl " + name);
    }

    /**
     * Receive notification of a recoverable error.
     *
     * {@link javax.xml.transform.ErrorListener#error(TransformerException)}
     *
     * <p>The transformer must continue to try and provide normal transformation
     * after invoking this method.  It should still be possible for the
     * application to process the document through to the end if no other errors
     * are encountered.</p>
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */
    public void error(TransformerException exception)
        throws TransformerException {

        System.err.println("ERROR occured - ErrorListenerImpl " + name);
        System.err.println(exception.getMessageAndLocation());
        throw exception;
    }

    /**
     * Receive notification of a non-recoverable error.
     *
     * {@link javax.xml.transform.ErrorListener#fatalError(TransformerException)}
     *
     * <p>The transformer must continue to try and provide normal transformation
     * after invoking this method.  It should still be possible for the
     * application to process the document through to the end if no other errors
     * are encountered, but there is no guarantee that the output will be
     * useable.</p>
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */
    public void fatalError(TransformerException exception)
        throws TransformerException {

        System.err.println("FATALERROR occured - ErrorListenerImpl " + name);
        System.err.println(exception.getMessage());
        // cancel transformation
        throw exception;
    }
}
