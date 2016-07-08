/*
 * $Id: TrAXConstants.java,v 1.15 2008/10/06 13:31:41 obecker Exp $
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


package net.sf.joost.trax;

import net.sf.joost.Constants;

import javax.xml.transform.Result;

/**
 * Common interface for TrAX related constants.
 * @version $Revision: 1.15 $ $Date: 2008/10/06 13:31:41 $
 * @author Anatolij Zubow, Oliver Becker
 */
public interface TrAXConstants extends Constants {

    /**
     * Internally used for the identity transformation.
     */
    public final static String IDENTITY_TRANSFORM =
        "<?xml version='1.0'?>" +
        "<stx:transform xmlns:stx='" + STX_NS + "'" +
        " version='1.0' pass-through='all' />";


    /**
     * Key for the Joost property
     * {@link net.sf.joost.TransformerHandlerResolver}
     * @see javax.xml.transform.TransformerFactory#setAttribute
     */
    public static String KEY_TH_RESOLVER =
        "http://joost.sf.net/attributes/transformer-handler-resolver";


    /**
     * Key for the Joost property
     * {@link net.sf.joost.OutputURIResolver}
     * @see javax.xml.transform.TransformerFactory#setAttribute
     */
    public static String KEY_OUTPUT_URI_RESOLVER =
        "http://joost.sf.net/attributes/output-uri-resolver";


    /**
     * Key for the Joost XSLT factory property
     * @see javax.xml.transform.TransformerFactory#setAttribute
     */
    public static String KEY_XSLT_FACTORY =
        "http://joost.sf.net/attributes/xslt-factory";


    /**
     * Key for the Joost property
     * {@link net.sf.joost.trax.TransformerFactoryImpl#debugmode}
     * @see javax.xml.transform.TransformerFactory#setAttribute
     */
    public final static String DEBUG_FEATURE =
        "http://joost.sf.net/attributes/debug-feature";

    /**
     * Key for the Joost message emitter property (must be the class name or
     * an instance of an STXEmitter)
     * {@link net.sf.joost.trax.TransformerFactoryImpl#msgEmitter}
     * @see javax.xml.transform.TransformerFactory#setAttribute
     */
    public final static String MESSAGE_EMITTER_CLASS =
        "http://joost.sf.net/attributes/messageEmitterClass";

    /**
     * Key for a Joost property that determines whether calls to Java
     * extension functions are allowed. Its property value must be a Boolean.
     * @see javax.xml.transform.TransformerFactory#setAttribute
     */
    public final static String ALLOW_EXTERNAL_FUNCTIONS =
       "http://joost.sf.net/attributes/allow-external-functions";

    /**
     * Key for a Joost output property that determines whether the PIs for
     * controlling disable-output-escaping
     * ({@link Result#PI_DISABLE_OUTPUT_ESCAPING} and
     * {@link Result#PI_ENABLE_OUTPUT_ESCAPING}) will be interpreted or not.
     * Its property value must be a String, either "yes" or "no"
     * @see javax.xml.transform.Transformer#setOutputProperty(String, String)
     */
    public final static String OUTPUT_KEY_SUPPORT_DISABLE_OUTPUT_ESCAPING =
       "{" + JOOST_EXT_NS + "}support-disable-output-escaping";
}
