/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.main;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static oeg.tagger.extractors.writer.writeFile;

/**
 *
 * @author mnavas
 */
public class testCharsets {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        charset("Ã“NODI v. HUNGARY");
    }

    public static void charset(String value) {
        String concat = "";
        try{
        Set<String> charsets = Charset.availableCharsets().keySet();
        String probe = "windows-1252";
//        String probe = StandardCharsets.UTF_8.name();
        for (String c : charsets) {
            Charset charset = Charset.forName(c);
            System.out.print(c);
            if (charset != null) {
//                if (value.equals(convert(convert(value, charset.name(), probe), probe, charset.name()))) {
                String out = convert(value, charset.name(), probe);
                System.out.println(" - " + out);
                concat = concat + "<p>" + out + "</p>";
               

                
//                }
            }
        }
        } catch(Exception ex){
            System.out.println("Error con "); //windows-1252
        } finally{
         if (!writeFile("<html class=\"gr__lkg_lynx-project_eu\">\n" +
"\n" +
"<head>\n" +
"	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">\n" +
"	<title>EventFinder</title>\n" +
"	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body>\n" + concat + "</body>\n", "C:\\Users\\mnavas\\Desktop\\ds2.html")) {
            System.out.println("ERROR WHILE SAVING IN" + "C:\\Users\\mnavas\\Desktop\\ds2.html");
         }
        }
    }

    public static String convert(String value, String fromEncoding, String toEncoding) {
        try {
            return new String(value.getBytes(fromEncoding), toEncoding);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(testCharsets.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
