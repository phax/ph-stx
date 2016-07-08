/*
 * $Id: AllTests.java,v 1.3 2009/09/22 21:13:44 obecker Exp $
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/*
 *	Datei: $RCSfile: AllTests.java,v $
 *	JUnit-Test-Suite for TraX-Transformers
 *	$Id: AllTests.java,v 1.3 2009/09/22 21:13:44 obecker Exp $
 */
public class AllTests extends TestSuite {

    // Define a static logger variable so that it references the
    // Logger instance named "RunTests".
    static Logger log = Logger.getLogger(AllTests.class);
    private static String log4jprop = "conf/log4j.properties";

    static {
        //calling once
        PropertyConfigurator.configure(log4jprop);
    }

    public AllTests(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        suite.addTestSuite(net.sf.joost.test.trax.TestTestCases.class);
        suite.addTest(ExternalErrorListenerTest.suite());
        return suite;
    }
}
