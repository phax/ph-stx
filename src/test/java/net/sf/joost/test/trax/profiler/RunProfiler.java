/*
 * $Id: RunProfiler.java,v 1.1 2007/07/15 15:32:28 obecker Exp $
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

package net.sf.joost.test.trax.profiler;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Zubow
 */
public class RunProfiler extends TestSuite{

    // Define a static logger variable so that it references the
    // Logger instance named "RunProfiler".
    static Logger log = Logger.getLogger(RunProfiler.class);
    private static String log4jprop = "conf/log4j.properties";

    static {
        //calling once
        PropertyConfigurator.configure(log4jprop);
    }

    public RunProfiler(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(net.sf.joost.test.trax.profiler.ProfilerTestCases.class);
        return suite;
    }
}
