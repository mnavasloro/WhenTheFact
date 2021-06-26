/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.docHandler;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mnavas
 */
public class LawORDateEN {

    String subText = "";
    HashMap<String, String> mapaRefs = new HashMap<String, String>();
    HashMap<String, String> listaRegex = new HashMap();

    public String replaceCite(String copiaText) {

        Integer count;
        subText = copiaText;
        
        Set<String> iterar = listaRegex.keySet();

        for (String cadena : iterar) {

            count = 0;

            Pattern pText = Pattern.compile(cadena);
            Matcher m = pText.matcher(subText);
            String shortcut = listaRegex.get(cadena);
            StringBuffer buf = new StringBuffer();

            while (m.find()) {
                String original = m.group(0);
                String cambio = shortcut + String.format("%08d", count) + shortcut;
                mapaRefs.put(cambio, original);
                m.appendReplacement(buf, cambio);
                count++;

            }
            
            subText = m.appendTail(buf).toString();

        }
        return subText;
    }

    public String returnCite(String txt) {

        Set<String> keySet = mapaRefs.keySet();
        
        for (String cadena : keySet) {            
            Pattern pText = Pattern.compile(cadena);
            Matcher m = pText.matcher(txt);
            StringBuffer buf = new StringBuffer();

            if (m.find()) {
                m.appendReplacement(buf, mapaRefs.get(cadena));
            } else{
                System.out.println("Notfound " + cadena);
            }
            
            txt = m.appendTail(buf).toString();

        }

        return txt;
    }

    public LawORDateEN() {
        listaRegex.put("No[ \\xa0]\\d+\\/\\d+[ \\xa0]of[ \\xa0]\\d{1,2}[ \\xa0]\\w+[ \\xa0]\\d{4}", "ECN31UNO");
        listaRegex.put("OJ[ \\xa0]\\d{4}[ \\xa0]\\w+[ \\xa0]\\d+,[ \\xa0]p\\.[ \\xa0]\\d+", "OJCIT4TION");
        listaRegex.put("judgment[ \\xa0]of[ \\xa0]\\d{1,2}[ \\xa0]\\w+[ \\xa0]\\d{4},[ \\xa0](\\w+ )+v([ \\xa0]\\w+)+", "PR3VJVD6MNT");
        listaRegex.put("Council[ \\xa0]of[ \\xa0]\\d{1,2}[ \\xa0]\\w+[ \\xa0]\\d{4}", "C0UNC1LPr3v");
        listaRegex.put("Regulation[ \\xa0]\\d+\\/\\d+,[ \\xa0]on[ \\xa0]\\d{1,2}[ \\xa0]\\w+[ \\xa0]\\d{4},", "C0UNC1L4ft3r");
        
    }


}
