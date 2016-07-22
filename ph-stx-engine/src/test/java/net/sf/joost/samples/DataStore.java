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
package net.sf.joost.samples;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.helger.xml.transform.StringStreamSource;

/**
 * Self-containing example class that demonstrates the usage of STX for creating
 * an XML representation of data that is stored in Java objects. The "Java 2
 * XML" functionality is completely expressed in the STX sheet and therefore
 * separated from the application logic. However, the Java extension mechanism
 * for Joost is required for accessing the data.
 * <p>
 * In this case the STX sheet is part of the application as a String object,
 * which seems reasonable because there is of course a strong correlation
 * between the <code>DataStore</code> class and the STX code. On the other hand
 * this makes it a little bit difficult to edit the STX code.
 * <p>
 * This example class comprises
 * <ul>
 * <li><code>DataStore</code> as a class that models certain data
 * <li>a {@link #main main} method that demonstrates its usage
 * <li>a proper STX sheet {@link #STX_SHEET}, contained as a String object, as
 * well as a dummy XML source {@link #XML_DUMMY}
 * </ul>
 *
 * @version $Revision: 1.1 $ $Date: 2007/07/15 15:32:29 $
 * @author Oliver Becker
 */

public class DataStore
{
  // Member fields
  private final String m_sGiven;
  private final String m_sName;
  private final String m_sSubject;
  private String m_sHeadOf;

  // Constructors
  DataStore (final String given, final String name, final String subject, final String headOf)
  {
    this (given, name, subject);
    this.m_sHeadOf = headOf;
  }

  DataStore (final String given, final String name, final String subject)
  {
    this.m_sGiven = given;
    this.m_sName = name;
    this.m_sSubject = subject;
  }

  // Getter methods
  public String getGiven ()
  {
    return m_sGiven;
  }

  public String getName ()
  {
    return m_sName;
  }

  public String getSubject ()
  {
    return m_sSubject;
  }

  public String getHeadOf ()
  {
    return m_sHeadOf;
  }

  public boolean isHead ()
  {
    return m_sHeadOf != null;
  }

  // ********************************************************************

  /**
   * main: Create some objects and put them into a HashSet. Construct an STX
   * transformation object, pass an iterator as external parameter and run the
   * transformation.
   *
   * @param args
   *        not used
   */
  public static void main (final String [] args)
  {
    // example data (taken from http://www.hp-lexicon.org/)
    final Set <DataStore> teachers = new LinkedHashSet<> ();
    teachers.add (new DataStore ("Minerva", "McGonagall", "Transfiguration", "Gryffindor"));
    teachers.add (new DataStore ("Severus", "Snape", "Potions", "Slytherin"));
    teachers.add (new DataStore ("Filius", "Flitwick", "Charms", "Ravenclaw"));
    teachers.add (new DataStore ("Sibyll", "Trelawney", "Divination"));
    teachers.add (new DataStore ("Pomona", "Sprout", "Herbology", "Hufflepuff"));
    teachers.add (new DataStore ("Rolanda", "Hooch", "Flying"));

    // use Joost as transformation engine
    System.setProperty ("javax.xml.transform.TransformerFactory", "net.sf.joost.trax.TransformerFactoryImpl");
    try
    {
      final TransformerFactory factory = TransformerFactory.newInstance ();

      final Transformer transformer = factory.newTransformer (new StringStreamSource (STX_SHEET));

      // pass the iterator as a global parameter to the transformation
      transformer.setParameter ("iter", teachers.iterator ());

      // run the transformation
      transformer.transform (new StreamSource (new ByteArrayInputStream (XML_DUMMY.getBytes ())),
                             new StreamResult (System.out));
    }
    catch (final TransformerException e)
    {
      System.err.println (e.getMessage ());
    }
  }

  // ********************************************************************

  // the STX transformation that generates the XML code
  private static final String STX_SHEET = "<?xml version='1.0'?>" +
                                          "<stx:transform xmlns:stx='http://stx.sourceforge.net/2002/ns'" +
                                          "               xmlns:d='java:net.sf.joost.samples.DataStore'" +
                                          "               xmlns:i='java:java.util.Iterator'" +
                                          "               version='1.0'" +
                                          "               exclude-result-prefixes='d i'>" +
                                          // iter is the global parameter that
                                          // should be initialized with
                                          // a java.util.Iterator object
                                          "  <stx:param name='iter' />" +

                                          "  <stx:template match='/'>" +
                                          "    <staff>" +
                                          // iterate over $iter
                                          "      <stx:while test='i:has-next($iter)'>" +
                                          "        <stx:variable name='d' select='i:next($iter)' />" +
                                          "        <stx:text>&#xA;</stx:text>" +
                                          // generate XML for the objects
                                          "        <teacher>" +
                                          "          <stx:if test='d:isHead($d)'>" +
                                          "            <stx:attribute name='headOf' select='d:getHeadOf($d)' />" +
                                          "          </stx:if>" +
                                          "          <given><stx:value-of select='d:getGiven($d)' /></given>" +
                                          "          <name><stx:value-of select='d:getName($d)' /></name>" +
                                          "          <teaches>" +
                                          "            <stx:value-of select='d:getSubject($d)' />" +
                                          "          </teaches>" +
                                          "        </teacher>" +
                                          "      </stx:while>" +
                                          "      <stx:text>&#xA;</stx:text>" +
                                          "    </staff>" +
                                          "  </stx:template>" +

                                          "</stx:transform>";

  // a dummy input XML source
  private static final String XML_DUMMY = "<?xml version='1.0'?>" + "<x/>";
}

/*
 * Expected Result: <?xml version="1.0" encoding="UTF-8"?> <staff>
 * <teacher><given>Sibyll</given><name>Trelawney</name><teaches>Divination</
 * teaches></teacher> <teacher
 * headOf="Ravenclaw"><given>Filius</given><name>Flitwick</name><teaches>Charms<
 * /teaches></teacher> <teacher
 * headOf="Gryffindor"><given>Minerva</given><name>McGonagall</name><teaches>
 * Transfiguration</teaches></teacher>
 * <teacher><given>Rolanda</given><name>Hooch</name><teaches>Flying</teaches></
 * teacher> <teacher
 * headOf="Hufflepuff"><given>Pomona</given><name>Sprout</name><teaches>
 * Herbology</teaches></teacher> <teacher
 * headOf="Slytherin"><given>Severus</given><name>Snape</name><teaches>Potions</
 * teaches></teacher> </staff>
 */
