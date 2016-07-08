/*
 * $Id: AllTests.java,v 1.3 2009/08/21 12:47:26 obecker Exp $
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
package net.sf.joost.test;

import net.sf.joost.test.stx.StxTest;
import net.sf.joost.test.stx.function.ExtensionFunctionTest;
import net.sf.joost.test.trax.thread.TemplateThreadSafetyTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @version $Revision: 1.3 $ $Date: 2009/08/21 12:47:26 $
 * @author Oliver Becker
 */
public class AllTests extends TestSuite
{
   public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(StxTest.suite());
      suite.addTest(net.sf.joost.test.trax.AllTests.suite());
      suite.addTestSuite(ExtensionFunctionTest.class);
      suite.addTestSuite(TemplateThreadSafetyTest.class);
      return suite;
   }
}
