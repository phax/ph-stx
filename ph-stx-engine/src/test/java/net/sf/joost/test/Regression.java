package net.sf.joost.test;

import net.sf.joost.trax.TransformerFactoryImpl;
import org.junit.Test;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class Regression {

    @Test
    public void stxRegression() throws TransformerConfigurationException {
        TransformerFactoryImpl impl = new TransformerFactoryImpl();
        InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream("regression.stx");
        Source transformSource = new StreamSource(templateStream);
        Templates template = impl.newTemplates(transformSource);
    }

}
