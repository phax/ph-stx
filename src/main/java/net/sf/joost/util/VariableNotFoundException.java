/*
 * $Id: VariableNotFoundException.java,v 1.2 2007/11/16 17:40:19 obecker Exp $
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

package net.sf.joost.util;

/**
 * Will be thrown if a variable is not declared.
 * @see VariableUtils#findVariableScope(net.sf.joost.stx.Context, String)
 * @version $Revision: 1.2 $ $Date: 2007/11/16 17:40:19 $
 * @author Oliver Becker
 */
public class VariableNotFoundException extends Exception
{

}
