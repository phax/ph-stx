/*
 * $Id: VariableUtils.java,v 1.2 2007/11/16 17:40:19 obecker Exp $
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

import java.util.Hashtable;
import java.util.Stack;

import net.sf.joost.instruction.GroupBase;
import net.sf.joost.stx.Context;

/**
 * Provides static methods for the handling of variables.
 * @version $Revision: 1.2 $ $Date: 2007/11/16 17:40:19 $
 * @author Oliver Becker
 */
public abstract class VariableUtils
{
   
   private VariableUtils()
   {
      // there are no instances of this class
   }
   
   
   /**
    * @return the group to which the variable declaration belongs to,
    *         <code>null</code> if it is a local variable
    * @throws VariableNotFoundException if the variable couldn't be found
    */
   public static GroupBase findVariableScope(Context context, String expName) throws VariableNotFoundException
   {
      GroupBase groupScope = null;

      Object obj = context.localVars.get(expName);
      if (obj == null) {
         GroupBase group = context.currentGroup;
         while (obj == null && group != null) {
            obj = ((Hashtable) ((Stack) context.groupVars.get(group)).peek()).get(expName);
            groupScope = group;
            group = group.parentGroup;
         }

         if (obj == null) {
            throw new VariableNotFoundException();
         }
      }
      return groupScope;
   }

}
