/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mnavas
 */
public class filesDownloader {

    static public File wordDownloader(String urlS, String id) {

        File f = new File(id + ".docx");
        
            InputStream in = null;
            try {
                System.out.println("opening connection");
                URL url = new URL(urlS);
                in = url.openStream();
                FileOutputStream fos = new FileOutputStream(f);
                System.out.println("reading file...");
                int length = -1;
                byte[] buffer = new byte[1024];// buffer for portion of data from
                // connection
                while ((length = in.read(buffer)) > -1) {
                    fos.write(buffer, 0, length);
                }       fos.close();
                in.close();
                System.out.println("file was downloaded in:");
                System.out.println(f.getAbsolutePath());
            } catch (Exception ex) {
                Logger.getLogger(filesDownloader.class.getName()).log(Level.SEVERE, null, ex);
            }
            return f;
    }
    
    
    
    
    
    
    
    static public File htmlDownloader(String id) {

        File f = new File(id + ".html");
        
            InputStream in = null;
            try {
                System.out.println("opening connection");
                URL url = new URL("https://hudoc.echr.coe.int/app/conversion/docx/html/body?library=ECHR&id=" + id);
                in = url.openStream();
                FileOutputStream fos = new FileOutputStream(f);
                System.out.println("reading file...");
                int length = -1;
                byte[] buffer = new byte[1024];// buffer for portion of data from
                // connection
                while ((length = in.read(buffer)) > -1) {
                    fos.write(buffer, 0, length);
                }       fos.close();
                in.close();
                System.out.println("file was downloaded in:");
                System.out.println(f.getAbsolutePath());
            } catch (Exception ex) {
                Logger.getLogger(filesDownloader.class.getName()).log(Level.SEVERE, null, ex);
            }
            return f;
    }
    
    

    
    static public String htmlDownloader2(String id) {

        try {
            System.out.println("opening connection");
            URL url = new URL("https://hudoc.echr.coe.int/app/conversion/docx/html/body?library=ECHR&id=" + id);
                     
            
            InputStream in = url.openStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            String str = "";
            System.out.println("reading from webpage...");
        while ((line = br.readLine()) != null) {
            str = str + line;
        }
            
            in.close();
            System.out.println("File downloaded");

            return str;
        } catch (Exception ex) {
            Logger.getLogger(filesDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
