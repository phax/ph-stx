package net.sf.joost.test.trax;

import net.sf.joost.trax.TrAXConstants;
import net.sf.joost.trax.TransformerFactoryImpl;

import java.io.InputStream;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ExternalErrorListenerTest extends TestCase
{
   private Properties props;

   public static Test suite()
   {
      return new TestSuite(ExternalErrorListenerTest.class);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      props = System.getProperties();
      System.setProperty(TrAXConstants.KEY_XSLT_FACTORY,
                         "org.apache.xalan.processor.TransformerFactoryImpl");
   }

   protected void tearDown() throws Exception
   {
      System.setProperties(props);
      super.tearDown();
   }

   public void testErrorListener() throws Exception
   {
      String xmlFileString = "errorDocument.xml";
      String stxFileString = "errorMapping.stx";

      InputStream xmlStream = ExternalErrorListenerTest.class
            .getResourceAsStream(xmlFileString);
      InputStream stxStream = ExternalErrorListenerTest.class
            .getResourceAsStream(stxFileString);

      Source xmlSource = new StreamSource(xmlStream);
      Source xsltSource = new StreamSource(stxStream);

      TransformerFactory transFact = new TransformerFactoryImpl();

      Listener listener1 = new Listener();
      transFact.setErrorListener(listener1);

      Transformer trans = transFact.newTransformer(xsltSource);

      Listener listener2 = new Listener();
      trans.setErrorListener(listener2);

      try {
         trans.transform(xmlSource, new StreamResult(System.out));
         fail();
      }
      catch (TransformerException te) {
         System.err.println("transformation aborted: " + te.getMessage());
      }

      assertFalse(listener1.fatalCalled);
      assertFalse(listener1.errorCalled);
      assertFalse(listener1.warningCalled);

      assertTrue(listener2.fatalCalled);
      assertFalse(listener2.errorCalled);  // depends on the actual XSLT impl
      assertFalse(listener2.warningCalled);
   }

   private static class Listener implements ErrorListener
   {
      boolean fatalCalled, errorCalled, warningCalled;

      public void error(TransformerException exception)
            throws TransformerException
      {
         errorCalled = true;
         System.err.println("[ERROR] " + exception.getMessage());
         throw new TransformerException("stylesheet aborted: "
               + exception.getMessage() + ": " + exception.getMessage());
      }

      public void fatalError(TransformerException exception)
            throws TransformerException
      {
         fatalCalled = true;
         System.err.println("[FATAL] " + exception.getMessage());
         throw new TransformerException("stylesheet aborted: "
               + exception.getMessage() + ": " + exception.getMessage());
      }

      public void warning(TransformerException exception)
      {
         warningCalled = true;
         System.out.println("[WARN ] " + exception.getMessage());
      }
   }

}