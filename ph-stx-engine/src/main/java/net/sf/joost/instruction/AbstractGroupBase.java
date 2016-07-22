/**
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.1 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is: this file
 *
 *  The Initial Developer of the Original Code is Oliver Becker.
 *
 *  Portions created by Philip Helger
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
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
import net.sf.joost.stx.Value;

/**
 * Base class for <code>stx:group</code> (class
 * <code>GroupFactory.Instance</code>) and <code>stx:transform</code> (class
 * <code>TransformFactory.Instance</code>) elements. The
 * <code>stx:transform</code> root element is also a group.
 *
 * @version $Revision: 2.17 $ $Date: 2008/10/04 17:13:14 $
 * @author Oliver Becker
 */

public abstract class AbstractGroupBase extends AbstractNodeBase
{
  // attributes from stx:transform / stx:group

  /**
   * The rule how to process unmatched events (from
   * <code>stx:options' pass-through</code>)
   */
  public byte m_nPassThrough = Processor.PASS_THROUGH_NONE;

  /**
   * Should white-space only text nodes be stripped (from
   * <code>stx:options' strip-space</code>)?
   */
  public boolean m_bStripSpace = false;

  /**
   * Should CDATA section be recognized (from
   * <code>stx:options' recognize-cdata</code>)?
   */
  public boolean m_bRecognizeCdata = true;

  /**
   * Vector of all contained public templates in this group. Used only
   * temporarily during compiling the transformation sheet.
   */
  private final Vector <TemplateFactory.Instance> m_aContainedPublicTemplates;

  /**
   * Vector of all contained group templates in this group. Used only
   * temporarily during compiling the transformation sheet.
   */
  private Vector <TemplateFactory.Instance> m_aContainedGroupTemplates;

  /**
   * Vector of all contained global templates in this group Used only
   * temporarily during compiling the transformation sheet.
   */
  private Vector <TemplateFactory.Instance> m_aContainedGlobalTemplates;

  /**
   * Visible templates: templates from this group and public templates from
   * subgroups
   */
  public TemplateFactory.Instance [] m_aVisibleTemplates;

  /** The templates from {@link #m_aContainedGroupTemplates} as array */
  public TemplateFactory.Instance [] m_aGroupTemplates;

  /**
   * Table of all contained public and global procedures in this group Used only
   * temporarily during compiling the transformation sheet.
   */
  private final Hashtable <String, ProcedureFactory.Instance> m_aContainedPublicProcedures;

  /** Table of the group procedures visible for this group */
  Hashtable <String, ProcedureFactory.Instance> m_aGroupProcedures;

  /**
   * Table of all global procedures in the transformation sheet, stems from the
   * parent group
   */
  Hashtable <String, ProcedureFactory.Instance> m_aGlobalProcedures;

  /**
   * Visible procedures: procedures from this group and public templates from
   * subgroups
   */
  public Hashtable <String, ProcedureFactory.Instance> m_aVisibleProcedures;

  /** Contained groups in this group */
  protected AbstractGroupBase [] m_aContainedGroups;

  /**
   * Table of named groups: key = group name, value = group object. All groups
   * will have a reference to the same singleton Hashtable.
   */
  public Hashtable <String, Object> m_aNamedGroups;

  /** parent group */
  public AbstractGroupBase m_aParentGroup;

  /** Group variables */
  private AbstractVariableBase [] m_aGroupVariables;

  /** Expanded name of this group */
  public String m_sGroupName;

  /** Vector of the children */
  protected Vector <AbstractNodeBase> m_aChildren = new Vector<> ();

  // Constructor
  protected AbstractGroupBase (final String qName,
                               final AbstractNodeBase parent,
                               final ParseContext context,
                               final byte passThrough,
                               final boolean stripSpace,
                               final boolean recognizeCdata)
  {
    super (qName, parent, context, true);
    this.m_aParentGroup = (AbstractGroupBase) parent;
    this.m_nPassThrough = passThrough;
    this.m_bStripSpace = stripSpace;
    this.m_bRecognizeCdata = recognizeCdata;
    m_aContainedPublicTemplates = new Vector<> ();
    m_aContainedGroupTemplates = new Vector<> ();
    m_aContainedGlobalTemplates = new Vector<> ();
    m_aVisibleProcedures = new Hashtable<> ();
    m_aContainedPublicProcedures = new Hashtable<> ();
    m_aGroupProcedures = new Hashtable<> ();
    if (m_aParentGroup != null)
    {
      m_aNamedGroups = m_aParentGroup.m_aNamedGroups;
      m_aGlobalProcedures = m_aParentGroup.m_aGlobalProcedures;
    }
  }

  @Override
  public void insert (final AbstractNodeBase node) throws SAXParseException
  {
    // no call of super.insert(node)
    m_aChildren.addElement (node);
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
      m_aGroupTemplates = new TemplateFactory.Instance [m_aContainedGroupTemplates.size ()];
      m_aContainedGroupTemplates.toArray (m_aGroupTemplates);
      Arrays.sort (m_aGroupTemplates);
      m_aContainedGroupTemplates = null; // for garbage collection
      return false; // done
    }

    // pass 0

    final Object [] objs = m_aChildren.toArray ();
    final int length = m_aChildren.size ();
    // template vector
    final Vector <TemplateFactory.Instance> tvec = new Vector<> ();
    // group vector
    final Vector <AbstractGroupBase> gvec = new Vector<> ();
    // variable vector
    final Vector <AbstractVariableBase> vvec = new Vector<> ();

    for (int i = 0; i < length; i++)
    {
      if (objs[i] instanceof TemplateFactory.Instance)
      {
        TemplateFactory.Instance t = (TemplateFactory.Instance) objs[i];
        do
        {
          tvec.addElement (t);
          if (t.m_bIsPublic)
          {
            m_aContainedPublicTemplates.addElement (t);
          }
          if (t.m_nVisibility == AbstractTemplateBase.GROUP_VISIBLE)
          {
            m_aContainedGroupTemplates.addElement (t);
          }
          if (t.m_nVisibility == AbstractTemplateBase.GLOBAL_VISIBLE)
          {
            m_aContainedGlobalTemplates.addElement (t);
          }

          // split templates with unions (|) in their match pattern
        } while ((t = t.split ()) != null);
      }
      else
        if (objs[i] instanceof ProcedureFactory.Instance)
        {
          final ProcedureFactory.Instance p = (ProcedureFactory.Instance) objs[i];
          AbstractNodeBase node = m_aVisibleProcedures.get (p.m_sExpName);
          if (node != null)
          {
            throw new SAXParseException ("Procedure '" +
                                         p.m_sProcName +
                                         "' already defined in line " +
                                         node.lineNo +
                                         (p.m_sSystemID.equals (node.m_sSystemID) ? (node.lineNo == p.lineNo ? " (possibly several times included)"
                                                                                                             : "")
                                                                                  : (" of " + node.m_sSystemID)),
                                         p.m_sPublicID,
                                         p.m_sSystemID,
                                         p.lineNo,
                                         p.colNo);
          }
          m_aVisibleProcedures.put (p.m_sExpName, p);
          if (p.m_bIsPublic)
            m_aContainedPublicProcedures.put (p.m_sExpName, p);
          if (p.m_nVisibility == AbstractTemplateBase.GROUP_VISIBLE)
          {
            m_aGroupProcedures.put (p.m_sExpName, p);
          }
          if (p.m_nVisibility == AbstractTemplateBase.GLOBAL_VISIBLE)
          {
            node = m_aGlobalProcedures.get (p.m_sExpName);
            if (node != null)
            {
              throw new SAXParseException ("Global procedure '" +
                                           p.m_sProcName +
                                           "' already defined in line " +
                                           node.lineNo +
                                           (p.m_sSystemID.equals (node.m_sSystemID) ? (node.lineNo == p.lineNo ? " (possibly several times included)"
                                                                                                               : "")
                                                                                    : (" of " + node.m_sSystemID)),
                                           p.m_sPublicID,
                                           p.m_sSystemID,
                                           p.lineNo,
                                           p.colNo);
            }
            m_aGlobalProcedures.put (p.m_sExpName, p);
            // global means also group visible
            m_aGroupProcedures.put (p.m_sExpName, p);
          }
        }
        else
          if (objs[i] instanceof AbstractGroupBase)
            gvec.addElement ((AbstractGroupBase) objs[i]);
          else
            if (objs[i] instanceof AbstractVariableBase)
              vvec.addElement ((AbstractVariableBase) objs[i]);
    }

    // create group array
    m_aContainedGroups = new AbstractGroupBase [gvec.size ()];
    gvec.toArray (m_aContainedGroups);

    // visible templates/procedures: from this group
    // plus public templates/procedures from child groups
    for (final AbstractGroupBase containedGroup : m_aContainedGroups)
    {
      tvec.addAll (containedGroup.m_aContainedPublicTemplates);
      final Hashtable <String, net.sf.joost.instruction.ProcedureFactory.Instance> pubProc = containedGroup.m_aContainedPublicProcedures;
      for (final Enumeration <String> e = pubProc.keys (); e.hasMoreElements ();)
      {
        Object o;
        if (m_aVisibleProcedures.containsKey (o = e.nextElement ()))
        {
          final ProcedureFactory.Instance p1 = pubProc.get (o);
          final AbstractNodeBase p2 = m_aVisibleProcedures.get (o);
          throw new SAXParseException ("Public procedure '" +
                                       p1.m_sProcName +
                                       "' conflicts with the procedure definition in line " +
                                       p2.lineNo +
                                       (p1.m_sSystemID.equals (p2.m_sSystemID) ? (p1.lineNo == p2.lineNo ? " (possibly several times included)"
                                                                                                         : "")
                                                                               : (" of " + p2.m_sSystemID)),
                                       p1.m_sPublicID,
                                       p1.m_sSystemID,
                                       p1.lineNo,
                                       p1.colNo);
        }
      }
      m_aVisibleProcedures.putAll (containedGroup.m_aContainedPublicProcedures);
    }

    // create sorted array of visible templates
    m_aVisibleTemplates = new TemplateFactory.Instance [tvec.size ()];
    tvec.toArray (m_aVisibleTemplates);
    Arrays.sort (m_aVisibleTemplates); // in descending priority order

    if (m_sGroupName != null)
    {
      // register group
      m_aNamedGroups.put (m_sGroupName, this);
    }

    // add group and global templates/procedures to all sub-groups
    // (group scope)
    for (final AbstractGroupBase containedGroup : m_aContainedGroups)
    {
      containedGroup.addGroupTemplates (m_aContainedGroupTemplates);
      containedGroup.addGroupTemplates (m_aContainedGlobalTemplates);
      containedGroup.addGroupProcedures (m_aGroupProcedures);
    }
    // remove the current group procedures in this group
    // (because they are also in visibleProcedures)
    m_aGroupProcedures.clear ();

    // add global templates from all sub-groups (global scope)
    // (this removes the global templates in these groups)
    for (final AbstractGroupBase containedGroup : m_aContainedGroups)
    {
      m_aContainedGlobalTemplates.addAll (containedGroup.getGlobalTemplates ());
    }

    // create array of group variables
    m_aGroupVariables = new AbstractVariableBase [vvec.size ()];
    vvec.toArray (m_aGroupVariables);

    return true; // need an additional pass for creating groupTemplates
  }

  /**
   * Initializes recursively the group variables of this group and all contained
   * sub-groups (breadth first).
   */
  public void initGroupVariables (final Context context) throws SAXException
  {
    enterRecursionLevel (context);
    for (final AbstractGroupBase containedGroup : m_aContainedGroups)
      containedGroup.initGroupVariables (context);
  }

  /**
   * Enters a recursion level by creating a new set of group variable instances.
   */
  public void enterRecursionLevel (final Context context) throws SAXException
  {
    // shadowed variables, needed if keep-value="yes"
    Hashtable <String, Value> shadowed = null;
    if (context.ancestorStack.isEmpty ())
      context.groupVars.put (this, new Stack<> ());
    else
      shadowed = context.groupVars.get (this).peek ();

    // new variable instances
    final Hashtable <String, Value> varTable = new Hashtable<> ();
    context.groupVars.get (this).push (varTable);

    context.currentGroup = this;
    for (final AbstractVariableBase groupVariable : m_aGroupVariables)
      if (groupVariable.m_bKeepValue && shadowed != null)
        varTable.put (groupVariable.m_sExpName, shadowed.get (groupVariable.m_sExpName));
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
    context.groupVars.get (this).pop ();
  }

  /**
   * Add the templates from <code>tVec</code> to the group templates of this
   * group and all sub-groups.
   *
   * @param tVec
   *        a Vector containing the templates
   */
  protected void addGroupTemplates (final Vector <TemplateFactory.Instance> tVec)
  {
    m_aContainedGroupTemplates.addAll (tVec);
    for (final AbstractGroupBase containedGroup : m_aContainedGroups)
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
  protected void addGroupProcedures (final Hashtable <String, ProcedureFactory.Instance> pTable) throws SAXParseException
  {
    // compute the real groupProcedures table
    for (final Enumeration <String> e = pTable.keys (); e.hasMoreElements ();)
    {
      final Object key = e.nextElement ();
      if (m_aGroupProcedures.containsKey (key))
      {
        final ProcedureFactory.Instance p1 = pTable.get (key);
        final AbstractNodeBase p2 = m_aGroupProcedures.get (key);
        throw new SAXParseException ("Group procedure '" +
                                     p1.m_sProcName +
                                     "' conflicts with the procedure definition in line " +
                                     p2.lineNo +
                                     (p1.m_sSystemID.equals (p2.m_sSystemID) ? (p1.lineNo == p2.lineNo ? " (possibly several times included)"
                                                                                                       : "")
                                                                             : (" of " + p2.m_sSystemID)),
                                     p1.m_sPublicID,
                                     p1.m_sSystemID,
                                     p1.lineNo,
                                     p1.colNo);
      }
    }
    m_aGroupProcedures.putAll (pTable);
    for (final AbstractGroupBase containedGroup : m_aContainedGroups)
      containedGroup.addGroupProcedures (pTable);
  }

  /**
   * Returns the globally visible templates in this group (and all sub-groups).
   * This method is called from {@link #compile} in the parent group, which adds
   * in turn the returned vector to its vector of the global templates. The
   * field {@link #m_aContainedGlobalTemplates} will be set to <code>null</code>
   * afterwards to allow garbage collection.
   */
  public Vector <TemplateFactory.Instance> getGlobalTemplates ()
  {
    final Vector <TemplateFactory.Instance> tmp = m_aContainedGlobalTemplates;
    m_aContainedGlobalTemplates = null; // for memory reasons
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
    throw new SAXParseException ("process called for " + m_sQName, m_sPublicID, m_sSystemID, lineNo, colNo);
  }

  /** returns the value of {@link #m_aVisibleTemplates} */
  public TemplateFactory.Instance [] getVisibleTemplates ()
  {
    return m_aVisibleTemplates;
  }

  @Override
  protected void onDeepCopy (final AbstractInstruction copy, final HashMap <Object, Object> copies)
  {
    super.onDeepCopy (copy, copies);
    final AbstractGroupBase theCopy = (AbstractGroupBase) copy;
    if (m_aContainedGroups != null)
    {
      theCopy.m_aContainedGroups = (AbstractGroupBase []) copies.get (m_aContainedGroups);
      if (theCopy.m_aContainedGroups == null)
      {
        theCopy.m_aContainedGroups = new AbstractGroupBase [m_aContainedGroups.length];
        for (int i = 0; i < m_aContainedGroups.length; i++)
        {
          theCopy.m_aContainedGroups[i] = (AbstractGroupBase) m_aContainedGroups[i].deepCopy (copies);
        }
      }
    }
    if (m_aGroupVariables != null)
    {
      theCopy.m_aGroupVariables = (AbstractVariableBase []) copies.get (m_aGroupVariables);
      if (theCopy.m_aGroupVariables == null)
      {
        theCopy.m_aGroupVariables = new AbstractVariableBase [m_aGroupVariables.length];
        for (int i = 0; i < m_aGroupVariables.length; i++)
          theCopy.m_aGroupVariables[i] = (AbstractVariableBase) m_aGroupVariables[i].deepCopy (copies);
      }
    }
    if (m_aGroupTemplates != null)
      theCopy.m_aGroupTemplates = deepTemplateArrayCopy (m_aGroupTemplates, copies);
    if (m_aVisibleTemplates != null)
      theCopy.m_aVisibleTemplates = deepTemplateArrayCopy (m_aVisibleTemplates, copies);
    if (m_aParentGroup != null)
      theCopy.m_aParentGroup = (AbstractGroupBase) m_aParentGroup.deepCopy (copies);
    if (m_aNamedGroups != null)
      theCopy.m_aNamedGroups = deepHashtableCopy (m_aNamedGroups, copies);
    if (m_aVisibleProcedures != null)
      theCopy.m_aVisibleProcedures = deepHashtableCopy (m_aVisibleProcedures, copies);
    if (m_aGlobalProcedures != null)
      theCopy.m_aGlobalProcedures = deepHashtableCopy (m_aGlobalProcedures, copies);
    if (m_aGroupProcedures != null)
      theCopy.m_aGroupProcedures = deepHashtableCopy (m_aGroupProcedures, copies);
  }
}
