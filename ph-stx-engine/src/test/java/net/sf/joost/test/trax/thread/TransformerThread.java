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
package net.sf.joost.test.trax.thread;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.helger.commons.io.resource.ClassPathResource;
import com.helger.xml.transform.ResourceStreamSource;

/**
 * Transformationthread
 *
 * @author Zubow
 */

public class TransformerThread extends Thread
{

  // private Templates templates;
  private Transformer m_aTransformer;
  private final String m_sName;

  private final static String xmlId = "examples/flat.xml";
  private final static String out = "testdata/thread/";
  private final static String stxId = "examples/flat.stx";

  private int counter;

  // sharing a Transformer object
  // result : ok !!!
  public TransformerThread (final Transformer transformer, final String name)
  {
    super (name);
    this.m_aTransformer = transformer;
    this.m_sName = name;
    this.counter = 0;

  }

  // sharing a Templates object
  // result : failed !!!
  public TransformerThread (final Templates templates, final String name)
  {
    super (name);
    this.m_sName = name;
    this.counter = 0;

    try
    {

      this.m_aTransformer = templates.newTransformer ();

    }
    catch (final TransformerConfigurationException ex)
    {
      ex.printStackTrace ();
    }
  }

  // sharing a TransformerFactory object
  public TransformerThread (final TransformerFactory tfactory, final String name)
  {
    super (name);
    this.m_sName = name;
    this.counter = 0;

    try
    {

      final InputStream stxIS = new BufferedInputStream (new FileInputStream (stxId));
      final StreamSource stxSource = new StreamSource (stxIS);
      stxSource.setSystemId (stxId);

      // get Transformer from Factory
      this.m_aTransformer = tfactory.newTransformer (stxSource);

      // Templates templates = tfactory.newTemplates(stxSource);

      // this.transformer = templates.newTransformer();

    }
    catch (final Exception ex)
    {
      ex.printStackTrace ();
    }
  }

  // Transform some stuff
  @Override
  public void run ()
  {

    while (counter < 5)
    {

      System.out.println ("-->" + m_sName);

      final String filename = out + m_sName + Integer.toString (counter) + ".xml";

      try
      {

        // Transform the source XML to System.out.
        m_aTransformer.transform (new ResourceStreamSource (new ClassPathResource (xmlId)),
                                  new StreamResult (filename));
      }
      catch (final TransformerException ex)
      {
        ex.printStackTrace ();
      }

      counter++;
    }
  }

}
