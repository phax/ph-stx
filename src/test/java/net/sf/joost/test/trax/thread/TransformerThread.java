/*
 * $Id: TransformerThread.java,v 1.1 2007/07/15 15:32:35 obecker Exp $
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
 * The Initial Developer of the Original Code is Anatolij Zubow.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */

package net.sf.joost.test.trax.thread;


import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
/**
 *  Transformationthread
 * @author Zubow
 */

public class TransformerThread extends Thread{

    //private Templates templates;
    private Transformer transformer;
    private String name;

    private final static String xmlId = "test/flat.xml";
    private final static String out   = "testdata/thread/";
    private final static String stxId = "test/flat.stx";

    private int counter;

    //sharing a Transformer object
    //result : ok !!!
    public TransformerThread(Transformer transformer, String name) {
        super(name);
        this.transformer = transformer;
        this.name = name;
        this.counter = 0;

    }

    //sharing a Templates object
    //result : failed !!!
    public TransformerThread(Templates templates, String name) {
        super(name);
        this.name = name;
        this.counter = 0;

        try {

            this.transformer = templates.newTransformer();

        }
        catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    //sharing a TransformerFactory object
    public TransformerThread(TransformerFactory tfactory, String name) {
        super(name);
        this.name = name;
        this.counter = 0;

        try {

            InputStream stxIS = new BufferedInputStream(new FileInputStream(stxId));
            StreamSource stxSource = new StreamSource(stxIS);
            stxSource.setSystemId(stxId);

            //get Transformer from Factory
            this.transformer = tfactory.newTransformer(stxSource);

            //Templates templates = tfactory.newTemplates(stxSource);

            //this.transformer = templates.newTransformer();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Transform some stuff
    public void run() {

        while(counter < 5) {

            System.out.println("-->" + name);

            String filename = out + name + (new Integer(counter)).toString() + ".xml";

            try {

                // Transform the source XML to System.out.
                transformer.transform( new StreamSource(xmlId),
                                       new StreamResult(filename));
            }
            catch (TransformerException ex) {
                ex.printStackTrace();
            }

            counter++;
        }
    }

}
