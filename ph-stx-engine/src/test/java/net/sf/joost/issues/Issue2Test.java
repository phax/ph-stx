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
 *  are Copyright (C) 2016-2017 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.issues;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import com.helger.commons.io.resource.ClassPathResource;

import net.sf.joost.trax.TransformerFactoryImpl;

public class Issue2Test
{
  @Test
  public void test () throws TransformerConfigurationException, IOException
  {
    final TransformerFactoryImpl impl = new TransformerFactoryImpl ();
    try (final InputStream templateStream = ClassPathResource.getInputStream ("issues/issue2.stx"))
    {
      final Source transformSource = new StreamSource (templateStream);
      final Templates template = impl.newTemplates (transformSource);
    }
  }
}
