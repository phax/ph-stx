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
