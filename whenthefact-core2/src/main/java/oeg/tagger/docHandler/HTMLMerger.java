/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.docHandler;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static oeg.tagger.extractors.writer.writeFile;

/**
 * This class contains a function that merges two XML files, adding the xml TIMEX3 tags to a base XML.
 * 
 * @author mnavas
 */
public class HTMLMerger {
    
    static public String mergeHTML(String baseHTML, String eventAnnotatedXML) {
//        File ev1 = new File("ev1.txt");
//        File ev2 = new File("ev2.txt");
//        File bh1 = new File("bh1.txt");
//        File bh2 = new File("bh2.txt");
//        
//        if (!writeFile(baseHTML, bh1.getAbsolutePath())) {
//            System.out.println("ERROR WHILE SAVING IN" + bh1.getAbsolutePath());
//        }
//        
//        if (!writeFile(eventAnnotatedXML, ev1.getAbsolutePath())) {
//            System.out.println("ERROR WHILE SAVING IN" + ev1.getAbsolutePath());
//        }
        
        
        if(baseHTML != null && eventAnnotatedXML != null){
             String stylestring = "";
                String pattern = "<(style|script)>[\\s\\S]*<\\/(style|script)>";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(baseHTML);
                while (m.find()) {
                    stylestring = stylestring + m.group();
                    
                }
                baseHTML = baseHTML.replaceAll(pattern, "");
            baseHTML = baseHTML.replaceAll("\\r\\n", "");
            baseHTML = baseHTML.replaceAll("\\n", "");
            baseHTML = baseHTML.replaceAll("\\r", "");
            eventAnnotatedXML = eventAnnotatedXML.replaceAll("\\r\\n", "");
            eventAnnotatedXML = eventAnnotatedXML.replaceAll("\\n", "");
            eventAnnotatedXML = eventAnnotatedXML.replaceAll("\\r", "");
            eventAnnotatedXML = eventAnnotatedXML.replaceAll("\\t", "");
//            baseHTML = baseHTML.replaceAll("↵", "");
            
//if (!writeFile(eventAnnotatedXML, ev2.getAbsolutePath())) {
//            System.out.println("ERROR WHILE SAVING IN" + ev2.getAbsolutePath());
//        }
//
//if (!writeFile(eventAnnotatedXML, bh2.getAbsolutePath())) {
//            System.out.println("ERROR WHILE SAVING IN" + bh2.getAbsolutePath());
//        }
            int j = 0; //to iterate in timexAnnotatedString
            int i = 0; //to iterate in baseString
            System.out.println(baseHTML.length());
            while (i < baseHTML.length()) {
                
                if (baseHTML.charAt(i) == '<') {
                    while (baseHTML.charAt(i) == '<') {
                        if(baseHTML.indexOf(">", i) + 1 < baseHTML.length()){                            
                        i = baseHTML.indexOf(">", i) + 1;
                        } else
                        {
                            return stylestring + baseHTML;
                        }
                }
                }
              if(baseHTML.substring(i, i+8).matches("&[^;]+;[\\S\\s]*")){
                    i = baseHTML.indexOf(";", i);
                    j--;
                } else if(j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j+1,j+6).equalsIgnoreCase("TIMEX")){
                    int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
                    int j2 = eventAnnotatedXML.indexOf("</TIMEX3>", j) + "</TIMEX3>".length();
                    int j3 = eventAnnotatedXML.indexOf("</TIMEX3>", j);
                    int lengtnew = j3 - j1;
                    // in TIMEX3
                    String intimex = eventAnnotatedXML.substring(j1, j3);
                    if(!baseHTML.substring(i,i+lengtnew).equalsIgnoreCase(intimex)){
                        int lengthint = intimex.length();
                        int k = 0;
                        int k2 = 0;
                        while(k<lengthint){
                            while(baseHTML.charAt(i+k2)!=intimex.charAt(k)){
                                k2++;
                            }                            
                            k++;
                            k2++;
                        }
                        baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+k2);
                        
                        i = i + j2 - j;
                        j = j2 + 1;
                    } else{
                    
                    baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+lengtnew);
                    i = i + j2 - j;
                    j = j2;
                    
                    }
                    if(baseHTML.charAt(i) == '<'){
                        i--;
                        j--;
                    }
                } else if(j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j+1,j+11).equalsIgnoreCase("Event_when")){
                    int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
                    int j2 = eventAnnotatedXML.indexOf("</Event_when>", j) + "</Event_when>".length();
                    int j3 = eventAnnotatedXML.indexOf("</Event_when>", j);
                    int lengtnew = j3 - j1;
                    String intimex = eventAnnotatedXML.substring(j1, j3);
                    if(!baseHTML.substring(i,i+lengtnew).equalsIgnoreCase(intimex)){
                        int lengthint = intimex.length();
                        int k = 0;
                        int k2 = 0;
                        while(k<lengthint){
                            while(baseHTML.charAt(i+k2)!=intimex.charAt(k)){
                                k2++;
                            }                            
                            k++;
                            k2++;
                        }
                        baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+k2);
                        
                        i = i + j2 - j;
                        j = j2;
                    } else{
                    baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+lengtnew);
                    i = i + j2 - j;
                    j = j2;
                }
                    if(baseHTML.charAt(i) == '<'){
                        i--;
                        j--;
                    }
                } else if(j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j+1,j+11).equalsIgnoreCase("Event_what")){
                    int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
                    int j2 = eventAnnotatedXML.indexOf("</Event_what>", j) + "</Event_what>".length();
                    int j3 = eventAnnotatedXML.indexOf("</Event_what>", j);
                    int lengtnew = j3 - j1;
                    String intimex = eventAnnotatedXML.substring(j1, j3);
                    if(!baseHTML.substring(i,i+lengtnew).equalsIgnoreCase(intimex)){
                        int lengthint = intimex.length();
                        int k = 0;
                        int k2 = 0;
                        while(k<lengthint){
                            while(baseHTML.charAt(i+k2)!=intimex.charAt(k)){
                                k2++;
                            }                            
                            k++;
                            k2++;
                        }
                        baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+k2);
                        
                        i = i + j2 - j;
                        j = j2;
                    } else{
                    baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+lengtnew);
                    i = i + j2 - j;
                    j = j2;
                }
                    if(baseHTML.charAt(i) == '<'){
                        i--;
                        j--;
                    }
                } else if(j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j+1,j+11).equalsIgnoreCase("Event_core")){
                    int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
                    int j2 = eventAnnotatedXML.indexOf("</Event_core>", j) + "</Event_core>".length();
                    int j3 = eventAnnotatedXML.indexOf("</Event_core>", j);
                    int lengtnew = j3 - j1;
                    String intimex = eventAnnotatedXML.substring(j1, j3);
                    if(!baseHTML.substring(i,i+lengtnew).equalsIgnoreCase(intimex)){
                        int lengthint = intimex.length();
                        int k = 0;
                        int k2 = 0;
                        while(k<lengthint){
                            while(baseHTML.charAt(i+k2)!=intimex.charAt(k)){
                                k2++;
                            }                            
                            k++;
                            k2++;
                        }
                        baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+k2);
                        
                        i = i + j2 - j;
                        j = j2;
                    } else{
                    baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+lengtnew);
                    i = i + j2 - j;
                    j = j2;
                }
                    if(baseHTML.charAt(i) == '<'){
                        i--;
                        j--;
                    }
                } else if(j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j+1,j+6).equalsIgnoreCase("Event")){
                    int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
                    int j2 = eventAnnotatedXML.indexOf("</Event>", j) + "</Event>".length();
                    int j3 = eventAnnotatedXML.indexOf("</Event>", j);
                    int lengtnew = j3 - j1;
                    String intimex = eventAnnotatedXML.substring(j1, j3);
                    if(!baseHTML.substring(i,i+lengtnew).equalsIgnoreCase(intimex)){
                        int lengthint = intimex.length();
                        int k = 0;
                        int k2 = 0;
                        while(k<lengthint){
                            while(baseHTML.charAt(i+k2)!=intimex.charAt(k)){
                                k2++;
                            }                            
                            k++;
                            k2++;
                        }
                        baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+k2);
                        
                        i = i + j2 - j;
                        j = j2;
                    } else{
                    baseHTML = baseHTML.substring(0,i) + eventAnnotatedXML.substring(j, j2)+ baseHTML.substring(i+lengtnew);
                    i = i + j2 - j;
                    j = j2;
                }
                    if(baseHTML.charAt(i) == '<'){
                        i--;
                        j--;
                    }
                }
                else if((baseHTML.charAt(i) != eventAnnotatedXML.charAt(j)) && (eventAnnotatedXML.charAt(j) == ' ' || eventAnnotatedXML.charAt(j) == '\t')){
                    i--;
                    System.out.println("Space detected, skiping");
                }
                else if(baseHTML.charAt(i) != eventAnnotatedXML.charAt(j)){
                    System.out.println("Weird char: \n" + baseHTML.substring(i, i+500) + "\n" + "-------------\n" + eventAnnotatedXML.substring(j, j+500));
                    j--;
                    System.out.println("-------------");
//                    break;
                }
            i++;
            j++;
            }
            
            return stylestring + baseHTML;
        } 
        
        return null;
    }
    
}
