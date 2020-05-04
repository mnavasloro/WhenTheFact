/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mnavas
 */
public class testDownload {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<String> links = new ArrayList<String>();

    links.add("https://hudoc.echr.coe.int/app/conversion/docx/?library=ECHR&id=001-202121&filename=001-202121.docx");

    for (int i = 0; i < links.size(); i++) {
        InputStream in = null;
            try {
                System.out.println("opening connection");
                URL url = new URL(links.get(i));
                in = url.openStream();
                FileOutputStream fos = new FileOutputStream(new File(i + ".docx"));
                System.out.println("reading file...");
                int length = -1;
                byte[] buffer = new byte[1024];// buffer for portion of data from
                // connection
                while ((length = in.read(buffer)) > -1) {
                    fos.write(buffer, 0, length);
                }       fos.close();
                in.close();
                System.out.println("file was downloaded");
            } catch (IOException ex) {
                Logger.getLogger(testDownload.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(testDownload.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
    }
    
}
