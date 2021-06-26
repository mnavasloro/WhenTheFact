///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package oeg.auxiliary;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import org.apache.commons.io.FileUtils;
//
///**
// *
// * @author mnavas
// */
//public class translateTest {
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        try {
//            // We take taggedHTML, translationHTML, lang (anteriormente, usaremos lang para descargar el HTML)
//            
//            File taggedHTMLf = new File("c.html");
//            File translationHTMLf = new File("es.html");
//            
//            String taggedHTML = FileUtils.readFileToString(taggedHTMLf, "UTF-8");
//            String translationHTML = FileUtils.readFileToString(translationHTMLf, "UTF-8");
//            
//            //1- por cada evento
//            //split por point
//            String[] points = taggedHTML.split("id=\"point\\d+\">");
//            String[] pointsT = translationHTML.split("id=\"point\\d+\">");
//            
//            StringBuilder sb = new StringBuilder("");
//            String pattern2 = "&#9658;([\\S\\s]*?)&#9668;";
//            Pattern r2 = Pattern.compile(pattern2);
//                
//            for(String p : points){                
//                Matcher m2 = r2.matcher(p);
//                while (m2.find()) {
//                    // 1-1 Buscamos el id=point
//                    // 1-1 Buscamos el evento
//                    String innerEvent = m2.group(1);
//                    // borramos los <> de p
//                    String aux = innerEvent.replaceAll("\"", "");
//                    String para = p.replaceAll("<[^>]*>", "");
//                    if(innerEvent.contains("argument=\"when\"")){
//                        String when = aux.replaceAll("<[^>]*argument=\"when\"[^>]*>", "");
//                        when = when.replaceAll("<[^>]*argument=\"when\"[^>]*>", ""); //SIGUE: no, es span no se que
//                    }
//                    
//                    
//
//                    
//                    sb.append(m2.group());
//
//                }
//            }
//            
//            
//            StringBuilder sb = new StringBuilder("");
//            String pattern = "id=\"point\\d+\">\\d+</p>";
//            Pattern r = Pattern.compile(pattern);
//            Matcher m = r.matcher(taggedHTML);
//            while (m.find()) {
//                String point = m.group(1);
//                // 1-1 Buscamos el id=point
//                String pattern2 = "&#9658;([\\S\\s]*?)&#9668;";
//                Pattern r2 = Pattern.compile(pattern2);
//                Matcher m2 = r2.matcher(taggedHTML);
//                while (m2.find()) {
//                    // 1-1 Buscamos el id=point
//                    String innerEvent = m2.group(1);
//                    
//
//                    
//                    sb.append(m.group());
//
//                }
//                
//                id="point1">1</p>
//                sb.append(m.group());
//
//            }
//            String stylestring = sb.toString();
//            
//        } catch (Exception ex) {
//            Logger.getLogger(translateTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    
//}
