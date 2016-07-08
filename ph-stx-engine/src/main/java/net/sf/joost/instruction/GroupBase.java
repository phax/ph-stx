/*
 * $Id: GroupBase.java,v 2.17 2008/10/04 17:13:14 obecker Exp $
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

package net.sf.joost.instruction;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.Processor;

/**
 * Base class for <code>stx:group</code> (class
 * <code>GroupFactory.Instance</code>) and <code>stx:transform</code> (class
 * <code>TransformFactory.Instance</code>) elements. The
 * <code>stx:transform</code> root element is also a group.
 * 
 * @version $Revision: 2.17 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

abstract public class GroupBase extends NodeBase
{
  // attributes from stx:transform / stx:group

  /**
   * The rule how to process unmatched events (from
   * <code>stx:options' pass-through</code>)
   */
  public byte passThrough = Processor.PASS_THROUGH_NONE;

  /**
   * Should white-space only text nodes be stripped (from
   * <code>stx:options' strip-space</code>)?
   */
  public boolean stripSpace = false;

  /**
   * Should CDATA section be recognized (from
   * <code>stx:options' recognize-cdata</code>)?
   */
  public boolean recognizeCdata = true;

  /**
   * Vector of all contained public templates in this group. Used only
   * temporarily during compiling the transformation sheet.
   */
  private final Vector containedPublicTemplates;

  /**
   * Vector of all contained group templates in this group. Used only
   * temporarily during compiling the transformation sheet.
   */
  private Vector containedGroupTemplates;

  /**
   * Vector of all contained global templates in this group Used only
   * temporarily during compiling the transformation sheet.
   */
  private Vector containedGlobalTemplates;

  /**
   * Visible templates: templates from this group and public templates from
   * subgroups
   */
  public TemplateFactory.Instance [] visibleTemplates;

  /** The templates from {@link #containedGroupTemplates} as array */
  public TemplateFactory.Instance [] groupTemplates;

  /**
   * Table of all contained public and global procedures in this group Used only
   * temporarily during compiling the transformation sheet.
   */
  private final Hashtable containedPublicProcedures;

  /** Table of the group procedures visible for this group */
  Hashtable groupProcedures;

  /**
   * Table of all global procedures in the transformation sheet, stems from the
   * parent group
   */
  Hashtable globalProcedures;

  /**
   * Visible procedures: procedures from this group and public templates from
   * subgroups
   */
  public Hashtable visibleProcedures;

  /** Contained groups in this group */
  protected GroupBase [] containedGroups;

  /**
   * Table of named groups: key = group name, value = group object. All groups
   * will have a reference to the same singleton Hashtable.
   */
  public Hashtable namedGroups;

  /** parent group */
  public GroupBase parentGroup;

  /** Group variables */
  private VariableBase [] groupVariables;

  /** Expanded name of this group */
  public String groupName;

  /** Vector of the children */
  protected Vector children = new Vector ();

  // Constructor
  protected GroupBase (final String qName,
                       final NodeBase parent,
                       final ParseContext context,
                       final byte passThrough,
                       final boolean stripSpace,
                       final boolean recognizeCdata)
  {
    super (qName, parent, context, true);
    this.parentGroup = (GroupBase) parent;
    this.passThrough = passThrough;
    this.stripSpace = stripSpace;
    this.recognizeCdata = recognizeCdata;
    containedPublicTemplates = new Vector ();
    containedGroupTemplates = new Vector ();
    containedGlobalTemplates = new Vector ();
    visibleProcedures = new Hashtable ();
    containedPublicProcedures = new Hashtable ();
    groupProcedures = new Hashtable ();
    if (parentGroup != null)
    {
      namedGroups = parentGroup.namedGroups;
      globalProcedures = parentGroup.globalProcedures;
    }
  }

  @Override
  public void insert (final NodeBase node) throws SAXParseException
  {
    // no call of super.insert(node)
    children.addElement (node);
  }

  /**
   * Determines the visible templates for this group in pass 0 and the array of
   * group templates in pass 1.
   * 
   * @exception SAXException
   *            if conflicts were encountered
   */
  @Override
  public boolean compile (final int pass, final ParseContext context) throws SAXException
  {
    if (pass == 1)
    {
      // create the groupTemplates array
      groupTemplates = new TemplateFactory.Instance [containedGroupTemplates.size ()];
      containedGroupTemplates.toArray (groupTemplates);
      Arrays.sort (groupTemplates);
      containedGroupTemplates = null; // for garbage collection
      return false; // done
    }

    // pass 0

    final Object [] objs = children.toArray ();
    final int length = children.size ();
    // template vector
    final Vector tvec = new Vector ();
    // group vector
    final Vector gvec = new Vector ();
    // variable vector
    final Vector vvec = new Vector ();

    for (int i = 0; i < length; i++)
    {
      if (objs[i] instanceof TemplateFactory.Instance)
      {
        TemplateFactory.Instance t = (TemplateFactory.Instance) objs[i];
        do
        {
          tvec.addElement (t);
          if (t.isPublic)
          {
            containedPublicTemplates.addElement (t);
          }
          if (t.visibility == TemplateBase.GROUP_VISIBLE)
          {
            containedGroupTemplates.addElement (t);
          }
          if (t.visibility == TemplateBase.GLOBAL_VISIBLE)
          {
            containedGlobalTemplates.addElement (t);
          }

          // split templates with unions (|) in their match pattern
        } while ((t = t.split ()) != null);
      }
      else
        if (objs[i] instanceof ProcedureFactory.Instance)
        {
          final ProcedureFactory.Instance p = (ProcedureFactory.Instance) objs[i];
          NodeBase node = (NodeBase) visibleProcedures.get (p.expName);
          if (node != null)
          {
            throw new SAXParseException ("Procedure '" +
                                         p.procName +
                                         "' already defined in line " +
                                         node.lineNo +
                                         (p.systemId.equals (node.systemId) ? (node.lineNo == p.lineNo ? " (possibly several times included)"
                                                                                                       : "")
                                                                            : (" of " + node.systemId)),
                                         p.publicId,
                                         p.systemId,
                                         p.lineNo,
                                         p.colNo);
          }
          else
            visibleProcedures.put (p.expName, p);
          if (p.isPublic)
            containedPublicProcedures.put (p.expName, p);
          if (p.visibility == TemplateBase.GROUP_VISIBLE)
          {
            groupProcedures.put (p.expName, p);
          }
          if (p.visibility == TemplateBase.GLOBAL_VISIBLE)
          {
            node = (NodeBase) globalProcedures.get (p.expName);
            if (node != null)
            {
              throw new SAXParseException ("Global procedure '" +
                                           p.procName +
                                           "' already defined in line " +
                                           node.lineNo +
                                           (p.systemId.equals (node.systemId) ? (node.lineNo == p.lineNo ? " (possibly several times included)"
                                                                                                         : "")
                                                                              : (" of " + node.systemId)),
                                           p.publicId,
                                           p.systemId,
                                           p.lineNo,
                                           p.colNo);
            }
            else
              globalProcedures.put (p.expName, p);
            // global means also group visible
            groupProcedures.put (p.expName, p);
          }
        }
        else
          if (objs[i] instanceof GroupBase)
            gvec.addElement (objs[i]);
          else
            if (objs[i] instanceof VariableBase)
              vvec.addElement (objs[i]);
    }

    // create group array
    containedGroups = new GroupBase [gvec.size ()];
    gvec.toArray (containedGroups);

    // visible templates/procedures: from this group
    // plus public templates/procedures from child groups
    for (final GroupBase containedGroup : containedGroups)
    {
      tvec.addAll (containedGroup.containedPublicTemplates);
      final Hashtable pubProc = containedGroup.containedPublicProcedures;
      for (final Enumeration e = pubProc.keys (); e.hasMoreElements ();)
      {
        Object o;
        if (visibleProcedures.containsKey (o = e.nextElement ()))
        {
          final ProcedureFactory.Instance p1 = (ProcedureFactory.Instance) pubProc.get (o);
          final NodeBase p2 = (NodeBase) visibleProcedures.get (o);
          throw new SAXParseException ("Public procedure '" +
                                       p1.procName +
                                       "' conflicts with the procedure definition in line " +
                                       p2.lineNo +
                                       (p1.systemId.equals (p2.systemId) ? (p1.lineNo == p2.lineNo ? " (possibly several times included)"
                                                                                                   : "")
                                                                         : (" of " + p2.systemId)),
                                       p1.publicId,
                                       p1.systemId,
                                       p1.lineNo,
                                       p1.colNo);
        }
      }
      visibleProcedures.putAll (containedGroup.containedPublicProcedures);
    }

    // create sorted array of visible templates
    visibleTemplates = new TemplateFactory.Instance [tvec.size ()];
    tvec.toArray (visibleTemplates);
    Arrays.sort (visibleTemplates); // in descending priority order

    if (groupName != null)
    {
      // register group
      namedGroups.put (groupName, this);
    }

    // add group and global templates/procedures to all sub-groups
    // (group scope)
    for (final GroupBase containedGroup : containedGroups)
    {
      containedGroup.addGroupTemplates (containedGroupTemplates);
      containedGroup.addGroupTemplates (containedGlobalTemplates);
      containedGroup.addGroupProcedures (groupProcedures);
    }
    // remove the current group procedures in this group
    // (because they are also in visibleProcedures)
    groupProcedures.clear ();

    // add global templates from all sub-groups (global scope)
    // (this removes the global templates in these groups)
    for (final GroupBase containedGroup : containedGroups)
    {
      containedGlobalTemplates.addAll (containedGroup.getGlobalTemplates ());
    }

    // create array of group variables
    groupVariables = new VariableBase [vvec.size ()];
    vvec.toArray (groupVariables);

    return true; // need an additional pass for creating groupTemplates
  }

  /**
   * Initializes recursively the group variables of this group and all contained
   * sub-groups (breadth first).
   */
  public void initGroupVariables (final Context context) throws SAXException
  {
    enterRecursionLevel (context);
    for (final GroupBase containedGroup : containedGroups)
      containedGroup.initGroupVariables (context);
  }

  /**
   * Enters a recursion level by creating a new set of group variable instances.
   */
  public void enterRecursionLevel (final Context context) throws SAXException
  {
    // shadowed variables, needed if keep-value="yes"
    Hashtable shadowed = null;
    if (context.ancestorStack.isEmpty ())
      context.groupVars.put (this, new Stack ());
    else
      shadowed = (Hashtable) ((Stack) context.groupVars.get (this)).peek ();

    // new variable instances
    final Hashtable varTable = new Hashtable ();
    ((Stack) context.groupVars.get (this)).push (varTable);

    context.currentGroup = this;
    for (final VariableBase groupVariable : groupVariables)
      if (groupVariable.keepValue && shadowed != null)
        varTable.put (groupVariable.expName, shadowed.get (groupVariable.expName));
      else
      {
        for (AbstractInstruction inst = groupVariable; inst != null; inst = inst.next)
          inst.process (context);
      }
  }

  /**
   * Exits a recursion level by removing the current group variable instances.
   */
  public void exitRecursionLevel (final Context context)
  {
    ((Stack) context.groupVars.get (this)).pop ();
  }

  /**
   * Add the templates from <code>tVec</code> to the group templates of this
   * group and all sub-groups.
   * 
   * @param tVec
   *        a Vector containing the templates
   */
  protected void addGroupTemplates (final Vector tVec)
  {
    containedGroupTemplates.addAll (tVec);
    for (final GroupBase containedGroup : containedGroups)
      containedGroup.addGroupTemplates (tVec);
  }

  /**
   * Add the procedures from <code>pTable</code> to the group procedures of this
   * group and all sub-groups.
   * 
   * @param pTable
   *        a Hashtable containing the procedures
   * @exception SAXParseException
   *            if one of the procedures is already defined
   */
  protected void addGroupProcedures (final Hashtable pTable) throws SAXParseException
  {
    // compute the real groupProcedures table
    for (final Enumeration e = pTable.keys (); e.hasMoreElements ();)
    {
      final Object key = e.nextElement ();
      if (groupProcedures.containsKey (key))
      {
        final ProcedureFactory.Instance p1 = (ProcedureFactory.Instance) pTable.get (key);
        final NodeBase p2 = (NodeBase) groupProcedures.get (key);
        throw new SAXParseException ("Group procedure '" +
                                     p1.procName +
                                     "' conflicts with the procedure definition in line " +
                                     p2.lineNo +
                                     (p1.systemId.equals (p2.systemId) ? (p1.lineNo == p2.lineNo ? " (possibly several times included)"
                                                                                                 : "")
                                                                       : (" of " + p2.systemId)),
                                     p1.publicId,
                                     p1.systemId,
                                     p1.lineNo,
                                     p1.colNo);
      }
    }
    groupProcedures.putAll (pTable);
    for (final GroupBase containedGroup : containedGroups)
      containedGroup.addGroupProcedures (pTable);
  }

  /**
   * Returns the globally visible templates in this group (and all sub-groups).
   * This method is called from {@link #compile} in the parent group, which adds
   * in turn the returned vector to its vector of the global templates. The
   * field {@link #containedGlobalTemplates} will be set to <code>null</code>
   * afterwards to allow garbage collection.
   */
  public Vector getGlobalTemplates ()
  {
    final Vector tmp = containedGlobalTemplates;
    containedGlobalTemplates = null; // for memory reasons
    return tmp;
  }

  @Override
  public boolean processable ()
  {
    return false;
  }

  // Shouldn't be called
  @Override
  public short process (final Context c) throws SAXException
  {
    throw new SAXParseException ("process called for " + qName, publicId, systemId, lineNo, colNo);
  }

  /** returns the value of {@link #visibleTemplates} */
  public TemplateFactory.Instance [] getVisibleTemplates ()
  {
    return visibleTemplates;
  }

  @Override
  protected void onDeepCopy (final AbstractInstruction copy, final HashMap copies)
  {
    super.onDeepCopy (copy, copies);
    final GroupBase theCopy = (GroupBase) copy;
    if (containedGroups != null)
    {
      theCopy.containedGroups = (GroupBase []) copies.get (containedGroups);
      if (theCopy.containedGroups == null)
      {
        theCopy.containedGroups = new GroupBase [containedGroups.length];
        for (int i = 0; i < containedGroups.length; i++)
        {
          theCopy.containedGroups[i] = (GroupBase) containedGroups[i].deepCopy (copies);
        }
      }
    }
    if (groupVariables != null)
    {
      theCopy.groupVariables = (VariableBase []) copies.get (groupVariables);
      if (theCopy.groupVariables == null)
      {
        theCopy.groupVariables = new VariableBase [groupVariables.length];
        for (int i = 0; i < groupVariables.length; i++)
        {
          theCopy.groupVariables[i] = (VariableBase) groupVariables[i].deepCopy (copies);
        }
      }
    }
    if (groupTemplates != null)
    {
      theCopy.groupTemplates = deepTemplateArrayCopy (groupTemplates, copies);
    }
    if (visibleTemplates != null)
    {
      theCopy.visibleTemplates = deepTemplateArrayCopy (visibleTemplates, copies);
    }
    if (parentGroup != null)
    {
      theCopy.parentGroup = (GroupBase) parentGroup.deepCopy (copies);
    }
    if (namedGroups != null)
    {
      theCopy.namedGroups = deepHashtableCopy (namedGroups, copies);
    }
    if (visibleProcedures != null)
    {
      theCopy.visibleProcedures = deepHashtableCopy (visibleProcedures, copies);
    }
    if (globalProcedures != null)
    {
      theCopy.globalProcedures = deepHashtableCopy (globalProcedures, copies);
    }
    if (groupProcedures != null)
    {
      theCopy.groupProcedures = deepHashtableCopy (groupProcedures, copies);
    }
  }
}
