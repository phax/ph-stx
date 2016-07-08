/*
 * $Id: ExtensionFunction.java,v 1.6 2009/08/21 12:46:17 obecker Exp $
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

package net.sf.joost.stx.function;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.Instance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An instance of this class represents a Java extension function. Parts of this
 * code are taken from Michael Kay's Saxon XSLT processor implementation.
 *
 * @version $Revision: 1.6 $ $Date: 2009/08/21 12:46:17 $
 * @author Oliver Becker
 */
final public class ExtensionFunction implements Instance
{
   /** the target class, identified by the namespace */
   private Class targetClass;

   /** possible methods, should differ at most in formal parameter types */
   private ArrayList candidateMethods = new ArrayList();

   /** the number of provided parameters in the function call */
   private int paramCount = 0;

   /**
    * <code>true</code> if this function call is a constructor invocation
    */
   private boolean isConstructor;


   /**
    * Constructs a Java extension function.
    * @param className the name of the Java class the function belongs to
    *        (taken from the namespace URI of the function call)
    * @param lName the local name of the function call
    *        (may contain hyphens)
    * @param args the supplied function parameters
    * @param locator the Locator object
    * @exception SAXParseException if there's no proper function
    */
   ExtensionFunction(String className, String lName, Tree args,
                             Locator locator)
      throws SAXParseException
   {
      // identify the requested class
      try {
         targetClass = Class.forName(className, true,
               Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException ex) {
         throw new SAXParseException(
            "Can't find Java class " + ex.getMessage(), locator);
      }

      // Count parameters in args
      // Future: use static type information to preselect candidate methods
      if (args != null) {
         paramCount = 1;
         while (args.type == Tree.LIST) {
            args = args.left;
            paramCount++;
         }
      }

      String fName = lName;
      // check function name
      if (lName.equals("new")) {
         // request to construct a new object
         isConstructor = true;
         // first: check the class
         int mod = targetClass.getModifiers();
         if (Modifier.isAbstract(mod))
            throw new SAXParseException(
               "Cannot create an object, class " + targetClass +
               " is abstract", locator);
         else if (Modifier.isInterface(mod))
            throw new SAXParseException(
               "Cannot create an object, " + targetClass +
               " is an interface", locator);
         else if (Modifier.isPrivate(mod))
            throw new SAXParseException(
               "Cannot create an object, class " + targetClass +
               " is private", locator);
         else if (Modifier.isProtected(mod))
            throw new SAXParseException(
               "Cannot create an object, class " + targetClass +
               " is protected", locator);

         // look for a matching constructor
         Constructor[] constructors = targetClass.getConstructors();
         for (int i=0; i<constructors.length; i++) {
            Constructor theConstructor = constructors[i];
            if (!Modifier.isPublic(theConstructor.getModifiers()))
               continue; // constructor is not public
            if (theConstructor.getParameterTypes().length != paramCount)
               continue; // wrong number of parameters
            candidateMethods.add(theConstructor);
         }

         if (candidateMethods.size() == 0)
            throw new SAXParseException(
               "No constructor found with " + paramCount +
               " parameter" + (paramCount != 1 ? "s" : "") +
               " in class " + className,
               locator);
      }
      else {
         // turn a hyphenated function-name into camelCase
         if (lName.indexOf('-') >= 0) {
            StringBuffer buff = new StringBuffer();
            boolean afterHyphen = false;
            for (int n=0; n<lName.length(); n++) {
               char c = lName.charAt(n);
               if (c=='-')
                  afterHyphen = true;
               else {
                  if (afterHyphen)
                     buff.append(Character.toUpperCase(c));
                  else
                     buff.append(c);
                  afterHyphen = false;
               }
            }
            fName = buff.toString();
         }

         Method[] methods = targetClass.getMethods();
         for (int i=0; i<methods.length; i++) {
            Method theMethod = methods[i];
            if (!theMethod.getName().equals(fName))
               continue; // method with a different name
            int modifiers = theMethod.getModifiers();
            if (!Modifier.isPublic(modifiers))
               continue; // method is not public
            int significantParams = paramCount;
            if (!Modifier.isStatic(modifiers))
               significantParams--; // method is not static,
                                    // first param is the target object
            if (theMethod.getParameterTypes().length != significantParams)
               continue; // wrong number of parameters
            candidateMethods.add(theMethod);
         }

         if (candidateMethods.size() == 0)
            throw new SAXParseException(
               "No function found matching '" + fName + "' " +
               (lName.equals(fName) ? "" : "(" + lName + ") ") +
               "with " + paramCount + " parameter" +
               (paramCount != 1 ? "s" : "") +
               " in class " + className,
               locator);
      }
   }


   /** find and call the correct Java method */
   public Value evaluate(Context context, int top, Tree args)
      throws SAXException, EvalException
   {
      // evaluate current parameters
      Value[] values = null;
      if (paramCount > 0) {
         values = new Value[paramCount];

         for (int i=paramCount-1; i>0; i--) {
            values[i] = args.right.evaluate(context, top);
            args = args.left;
         }
         values[0] = args.evaluate(context, top);
      }

      if (isConstructor) {
         // this is a constructor call
         Constructor theConstructor = null;
         int methodNum = candidateMethods.size();
         if (methodNum == 1)
            theConstructor = (Constructor)candidateMethods.get(0);
         else {
            // choose the best constructor depending on current parameters
            // This algorithm simply adds the distance values of all
            // parameters and chooses the candidate with the lowest value.
            // (Saxon's algorithm is more complicated, presumably there's
            // a good reason for that ...)
            double minDistance = -1;
            boolean ambigous = false;
            for (int i=0; i<methodNum; i++) {
               Constructor c = (Constructor)candidateMethods.get(i);
               double distance = 0;
               Class[] paramTypes = c.getParameterTypes();
               for (int j=0; j<paramTypes.length; j++)
                  distance += values[j].getDistanceTo(paramTypes[j]);
               // better fit?
               if (distance < minDistance || minDistance < 0) {
                  minDistance = distance;
                  theConstructor = c;
                  ambigous = false;
               }
               else if (distance == minDistance)
                  ambigous = true;
            }
            if (minDistance == Double.POSITIVE_INFINITY)
               throw new EvalException(
                  "None of the Java constructors in " +
                  targetClass.getName() +
                  " matches this function call to 'new'");
            if (ambigous)
               throw new EvalException(
                  "There are several Java constructors in " +
                  targetClass.getName() +
                  " that match the function call to 'new' equally well ");
         } // end else (choose best constructor)

         // set current parameters
         Class[] formalParams = theConstructor.getParameterTypes();
         Object[] currentParams = new Object[formalParams.length];
         for (int i=0; i<formalParams.length; i++)
            currentParams[i] = values[i].toJavaObject(formalParams[i]);

         // call constructor
         try {
            Object obj = theConstructor.newInstance(currentParams);
            return new Value(obj);
         }
         catch (InstantiationException err0) {
            throw new EvalException("Cannot instantiate class " +
                                    err0.getMessage());
         }
         catch (IllegalAccessException err1) {
            throw new EvalException("Constructor access is illegal " +
                                    err1.getMessage());
         }
         catch (IllegalArgumentException err2) {
            throw new EvalException("Argument is of wrong type " +
                                    err2.getMessage());
         }
         catch (InvocationTargetException err3) {
            throw new EvalException(
               "Exception in extension constructor " +
               theConstructor.getName() +
               ": " + err3.getTargetException().toString());
         }
      } // end else (constructor invocation)

      else { // method invocation
         Method theMethod = null;
         int methodNum = candidateMethods.size();
         if (methodNum == 1)
            theMethod = (Method)candidateMethods.get(0);
         else {
            // choose the best method depending on current parameters
            // (see comment for constructors above)
            double minDistance = -1;
            boolean ambigous = false;
            for (int i=0; i<methodNum; i++) {
               Method m = (Method)candidateMethods.get(i);
               double distance = 0;
               Class[] paramTypes = m.getParameterTypes();
               if (Modifier.isStatic(m.getModifiers())) {
                  for (int j=0; j<paramTypes.length; j++)
                     distance += values[j].getDistanceTo(paramTypes[j]);
               }
               else {
                  // first argument is the target object
                  distance = values[0].getDistanceTo(targetClass);
                  for (int j=0; j<paramTypes.length; j++)
                     distance += values[j+1].getDistanceTo(paramTypes[j]);
               }
               // better fit?
               if (distance < minDistance || minDistance < 0) {
                  minDistance = distance;
                  theMethod = m;
                  ambigous = false;
               }
               else if (distance == minDistance)
                  ambigous = true;
            }
            if (minDistance == Double.POSITIVE_INFINITY)
               throw new EvalException(
                  "None of the Java methods in " +
                  targetClass.getName() +
                  " matches this function call to '" +
                  theMethod.getName() + "'");
            if (ambigous)
               throw new EvalException(
                  "There are several Java methods in " +
                  targetClass.getName() + " that match function '" +
                  theMethod.getName() + "' equally well");
         } // end else (choose best method)

         // set current parameters
         Object theInstance = null;
         Class[] formalParams = theMethod.getParameterTypes();
         Object[] currentParams = new Object[formalParams.length];
         if (Modifier.isStatic(theMethod.getModifiers())) {
            for (int i=0; i<formalParams.length; i++)
               currentParams[i] = values[i].toJavaObject(formalParams[i]);
         }
         else {
            // perform this additional check for the first parameter,
            // because otherwise the error message is a little but
            // misleading ("Conversion to ... is not supported")
            if (methodNum == 1 && // haven't done this check in this case
                values[0].getDistanceTo(targetClass) ==
                          Double.POSITIVE_INFINITY)
               throw new EvalException(
                  "First parameter in the function call to '" +
                  theMethod.getName() + "' must be the object instance");

            theInstance = values[0].toJavaObject(targetClass);

            if (theInstance == null)
               throw new EvalException(
                  "Target object (first parameter) in the function call " +
                  "to '" + theMethod.getName() + "' is null");

            for (int i=0; i<formalParams.length; i++) {
               currentParams[i] =
                  values[i+1].toJavaObject(formalParams[i]);
            }
         }

         // call method
         try {
            return new Value(theMethod.invoke(theInstance, currentParams));
         }
         catch (IllegalAccessException err1) {
            throw new EvalException("Method access is illegal " +
                                    err1.getMessage(), err1);
         }
         catch (IllegalArgumentException err2) {
            throw new EvalException("Argument is of wrong type " +
                                    err2.getMessage(), err2);
         }
         catch (InvocationTargetException err3) {
            throw new EvalException(
               "Exception in extension method '" +
               theMethod.getName() + "': " +
               err3.getTargetException().toString(), err3);
         }
      }
   }


   // These functions will never be called.
   // However, they are required by the Instance interface.

   /** Not called */
   public int getMinParCount() { return 0; }

   /** Not called */
   public int getMaxParCount() { return 0; }

   /** Not called */
   public String getName() { return null; }

   /** @return <code>false</code> (we don't know) */
   public boolean isConstant() { return false; }

}