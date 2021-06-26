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
 * This class contains a function that merges two XML files, adding the xml
 * TIMEX3 tags to a base XML.
 *
 * @author mnavas
 */
public class HTMLMerger {

    static public String mergeHTML(String baseHTML, String eventAnnotatedXML) {

        try {
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

            if (baseHTML != null && eventAnnotatedXML != null) {
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
                int flaglastone = 0;
//                System.out.println(baseHTML.length());
                while (i < baseHTML.length()) {

                    if (baseHTML.charAt(i) == '<') {
                        while (baseHTML.charAt(i) == '<') {
                            if (baseHTML.indexOf(">", i) + 1 < baseHTML.length()) {
                                i = baseHTML.indexOf(">", i) + 1;
                            } else {
                                return stylestring + baseHTML;
                            }
                        }
                    }
                    if (baseHTML.substring(i, i + 8).matches("&[^;]+;[\\S\\s]*")) {
                        i = baseHTML.indexOf(";", i);
                        j--;
                    } else if (eventAnnotatedXML.startsWith("<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\"",j)) {
                     i--;
                     j = j + eventAnnotatedXML.indexOf(">")-1; 
                     flaglastone = 1;
                    } else if (flaglastone==1 && eventAnnotatedXML.startsWith("</Event_what>",j)) {
                     i--;
                     j = j + "</Event_what>".length()-1;     
                     flaglastone = 0;
                    } else if (j <= (eventAnnotatedXML.length()-13) && eventAnnotatedXML.substring(j, j + 10).equalsIgnoreCase("EVENTTOKEN")) {
                        
//                System.out.println("EMOS ENTRAO en " + j + ": " + eventAnnotatedXML.substring(j, j + 13));
                        baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j + 13) + baseHTML.substring(i);
                        i = i + 12;
                        j = j + 12;
//                    } else if (j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j + 1, j + 6).equalsIgnoreCase("TIMEX")) {
//                        int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
//                        int j2 = eventAnnotatedXML.indexOf("</TIMEX3>", j) + "</TIMEX3>".length();
//                        int j3 = eventAnnotatedXML.indexOf("</TIMEX3>", j);
//                        int lengtnew = j3 - j1;
//                        // in TIMEX3
//                        String intimex = eventAnnotatedXML.substring(j1, j3);
//                        if (!baseHTML.substring(i, i + lengtnew).equalsIgnoreCase(intimex)) {
//                            int lengthint = intimex.length();
//                            int k = 0;
//                            int k2 = 0;
//                            while (k < lengthint) {
//                                while (baseHTML.charAt(i + k2) != intimex.charAt(k)) {
//                                    k2++;
//                                }
//                                k++;
//                                k2++;
//                            }
//                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + k2);
//
//                            i = i + j2 - j;
//                            j = j2 + 1;
//                        } else {
//
//                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + lengtnew);
//                            i = i + j2 - j;
//                            j = j2;
//
//                        }
//                        if (baseHTML.charAt(i) == '<') {
//                            i--;
//                            j--;
//                        }
                    }
                    
//                     else if (j <= (eventAnnotatedXML.length()-13) && eventAnnotatedXML.substring(j, j + 10).equalsIgnoreCase("LINK2TOKEN")) {
//                        
////                System.out.println("EMOS ENTRAO en " + j + ": " + eventAnnotatedXML.substring(j, j + 13));
//                        i = baseHTML.indexOf("</a>", i) + 3;
//                        j = j + 9;
////                    } else if (j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j + 1, j + 6).equalsIgnoreCase("TIMEX")) {
////                        int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
////                        int j2 = eventAnnotatedXML.indexOf("</TIMEX3>", j) + "</TIMEX3>".length();
////                        int j3 = eventAnnotatedXML.indexOf("</TIMEX3>", j);
////                        int lengtnew = j3 - j1;
////                        // in TIMEX3
////                        String intimex = eventAnnotatedXML.substring(j1, j3);
////                        if (!baseHTML.substring(i, i + lengtnew).equalsIgnoreCase(intimex)) {
////                            int lengthint = intimex.length();
////                            int k = 0;
////                            int k2 = 0;
////                            while (k < lengthint) {
////                                while (baseHTML.charAt(i + k2) != intimex.charAt(k)) {
////                                    k2++;
////                                }
////                                k++;
////                                k2++;
////                            }
////                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + k2);
////
////                            i = i + j2 - j;
////                            j = j2 + 1;
////                        } else {
////
////                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + lengtnew);
////                            i = i + j2 - j;
////                            j = j2;
////
////                        }
////                        if (baseHTML.charAt(i) == '<') {
////                            i--;
////                            j--;
////                        }
//                    }
                    
                    
                    
                    
                    
                    else if (j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j + 1, j + 11).equalsIgnoreCase("Event_when")) {
                        int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
                        int j2 = eventAnnotatedXML.indexOf("</Event_when>", j) + "</Event_when>".length();
                        int j3 = eventAnnotatedXML.indexOf("</Event_when>", j);
                        int lengtnew = j3 - j1;
                        String intimex = eventAnnotatedXML.substring(j1, j3);
//                        System.out.println("Event_when - EMOS ENTRAO en " + j + ": " + eventAnnotatedXML.substring(j, j + 38));
                        if (!baseHTML.substring(i, i + lengtnew).equalsIgnoreCase(intimex)) {
                            int lengthint = intimex.length();
                            int k = 0;
                            int k2 = 0;
                            while (k < lengthint) {
                                while (baseHTML.charAt(i + k2) != intimex.charAt(k)) {
                                    k2++;
                                }
                                k++;
                                k2++;
                            }
                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + k2);

                            i = i + j2 - j-1;
                            j = j2-1;
                        } else {
                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + lengtnew);
                            i = i + j2 - j-1;
                            j = j2-1;
                        }
                        if (baseHTML.charAt(i) == '<') {
                            i--;
                            j--;
                        }
                    } else if (j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j + 1, j + 11).equalsIgnoreCase("Event_what")) {
                        
//                        System.out.println("Event_what - EMOS ENTRAO en " + j + ": " + eventAnnotatedXML.substring(j, j + 38));
                        int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
                        int j2 = eventAnnotatedXML.indexOf("</Event_what>", j) + "</Event_what>".length();
                        int j3 = eventAnnotatedXML.indexOf("</Event_what>", j);
                        int lengtnew = j3 - j1;
                        String intimex = eventAnnotatedXML.substring(j1, j3);
                        if (!baseHTML.substring(i, i + lengtnew).equalsIgnoreCase(intimex)) {
                            int lengthint = intimex.length();
                            int k = 0;
                            int k2 = 0;
                            while (k < lengthint) {
                                while (baseHTML.charAt(i + k2) != intimex.charAt(k)) {
                                    k2++;
                                }
                                k++;
                                k2++;
                            }
                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + k2);

                            i = i + j2 - j -1;
                            j = j2 - 1;
                        } else {
                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + lengtnew);
                            i = i + j2 - j -1;
                            j = j2 -1;
                        }
                        if (baseHTML.charAt(i) == '<') {
                            i--;
                            j--;
                        }
                    } else if (j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j + 1, j + 10).equalsIgnoreCase("Event_who")) {
                        
//                        System.out.println("Event_who - EMOS ENTRAO en " + j + ": " + eventAnnotatedXML.substring(j, j + 38));
                        int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
                        int j2 = eventAnnotatedXML.indexOf("</Event_who>", j) + "</Event_who>".length();
                        int j3 = eventAnnotatedXML.indexOf("</Event_who>", j);
                        int lengtnew = j3 - j1;
                        String intimex = eventAnnotatedXML.substring(j1, j3);
                        if (!baseHTML.substring(i, i + lengtnew).equalsIgnoreCase(intimex)) {
                            int lengthint = intimex.length();
                            int k = 0;
                            int k2 = 0;
                            while (k < lengthint) {
                                while (baseHTML.charAt(i + k2) != intimex.charAt(k)) {
                                    k2++;
                                }
                                k++;
                                k2++;
                            }
                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + k2);

                            i = i + j2 - j-1;
                            j = j2-1;
                        } else {
                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + lengtnew);
                            i = i + j2 - j-1;
                            j = j2-1;
                        }
                        if (baseHTML.charAt(i) == '<') {
                            i--;
                            j--;
                        }
//                    } else if (j < eventAnnotatedXML.length() && eventAnnotatedXML.charAt(j) == '<' && eventAnnotatedXML.substring(j + 1, j + 6).equalsIgnoreCase("Event")) {
//                       
//                        System.out.println("Event - EMOS ENTRAO en " + j + ": " + eventAnnotatedXML.substring(j, j + 20));
//                        int j1 = eventAnnotatedXML.indexOf(">", j) + 1;
//                        int j2 = eventAnnotatedXML.indexOf("</Event>", j) + "</Event>".length();
//                        int j3 = eventAnnotatedXML.indexOf("</Event>", j);
//                        int lengtnew = j3 - j1;
//                        String intimex = eventAnnotatedXML.substring(j1, j3);
//                        if (!baseHTML.substring(i, i + lengtnew).equalsIgnoreCase(intimex)) {
//                            int lengthint = intimex.length();
//                            int k = 0;
//                            int k2 = 0;
//                            while (k < lengthint) {
//                                while (baseHTML.charAt(i + k2) != intimex.charAt(k)) {
//                                    k2++;
//                                }
//                                k++;
//                                k2++;
//                            }
//                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + k2);
//
//                            i = i + j2 - j;
//                            j = j2;
//                        } else {
//                            baseHTML = baseHTML.substring(0, i) + eventAnnotatedXML.substring(j, j2) + baseHTML.substring(i + lengtnew);
//                            i = i + j2 - j;
//                            j = j2;
//                        }
//                        if (baseHTML.charAt(i) == '<') {
//                            i--;
//                            j--;
//                        }
                    } else if ((baseHTML.charAt(i) != eventAnnotatedXML.charAt(j)) && (eventAnnotatedXML.charAt(j) == ' ' || eventAnnotatedXML.charAt(j) == '\t')) {
                        i--;
//                        System.out.println("Space detected, skiping");
                    } else if (baseHTML.charAt(i) != eventAnnotatedXML.charAt(j)) {
//                        System.out.println("Weird char: \n" + baseHTML.substring(i, i + 500) + "\n" + "-------------\n" + eventAnnotatedXML.substring(j, j + 500));
                        j--;
//                        System.out.println("-------------");
//                        System.out.print(baseHTML.charAt(i) + "(" + i + ") ");
//                    break;
                    }
                    i++;
                    j++;
                }

                return stylestring + baseHTML;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return null;
    }

    static public String prepareHTML(String eventAnnotatedXML) {

        try {
            File ev1 = new File("ev1.txt");
            File ev2 = new File("ev2.txt");
            File bh1 = new File("bh1.txt");
            File bh2 = new File("bh2.txt");

            if (!writeFile(eventAnnotatedXML, ev1.getAbsolutePath())) {
                System.out.println("ERROR WHILE SAVING IN" + ev1.getAbsolutePath());
            }
            System.out.println("CORRECLY SAVED IN " + ev1.getAbsolutePath());

            if (eventAnnotatedXML != null) {

                int j = 0; //to iterate in timexAnnotatedString
                int i = 0; //to iterate in baseString

                String output = eventAnnotatedXML;
                output = output.replaceAll("<Event [^>]+>", "EVENTTOKENINI");
                output = output.replaceAll("(\\t\\.)?</Event>", "EVENTTOKENEND");

                String pattern = "(<Event_what argument=\"what\"[^>]+>)(.*?)(<\\/Event_what>)";
                String pattern2 = "(.*)(<Event_[^>]+>.*?<\\/Event_[^>]+>)(.*)";
                StringBuffer sb = new StringBuffer(output.length());

                Pattern r2 = Pattern.compile(pattern2);
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(output);
                while (m.find()) {
//                    System.out.println("FOUND " + m.group(0));
                    String ini = m.group(1);
                    String end = m.group(3);
                    String inner = m.group(2);

                    if (inner.contains("<") && inner.contains(">")) {

//                    System.out.println("INSIDE " + inner);
                        String auxst = inner;
                        Matcher m2 = r2.matcher(inner);
                        if (m2.find()) {
                            auxst = m2.group(1) + end + m2.group(2) + ini + m2.group(3);
                            
//                    System.out.println("FOUND INSIDE " + auxst);
                        }

                        m.appendReplacement(sb, ini + auxst + end);

                    }
                }
                m.appendTail(sb);
                output = sb.toString();
                
                
                
                pattern = "(<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\"[^>]*>)(.*?)(<\\/Event_what>)";
//                pattern2 = "(<[^>]*>)";
//                sb = new StringBuffer(output.length());
                
                r = Pattern.compile(pattern);
                m = r.matcher(output);
                if (m.find()) {
//                    System.out.println("FOUNDt0 " + m.group(0));
//                    String ini = m.group(1);
//                    String end = m.group(3);
                    String inner = m.group(2);

                    if (inner.contains("<") && inner.contains(">")) {

                       String auxst = inner.replaceAll("(<[^>]*>)", "</Event_what>$1" + m.group(1));
                    output = output.replace(inner, auxst);
//                    System.out.println("INSIDE " + inner);
//                        String auxst = inner;
//                        Matcher m2 = r2.matcher(inner);
//                        while (m2.find()) {
//                            auxst = "</Event_what>" + m2.group(1) + "<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\">";
//                            
//                    System.out.println("FOUND INSIDE " + auxst);
//                    m.appendReplacement(sb, auxst);
//                        }
//
//                        
//m.appendTail(sb);
                    } else if (inner.contains("\t")) {

//                        System.out.println("NNNN " + inner);
                        
                       String auxst = inner.replaceAll("(\\t\\t+)\\.", "</Event_what>$1" + m.group(1));
                    
//                       System.out.println("INXMSKMSIDE " + auxst);
                       
                       output = output.replace(inner, auxst);
//                    System.out.println("INSIDE " + inner);
//                        String auxst = inner;
//                        Matcher m2 = r2.matcher(inner);
//                        while (m2.find()) {
//                            auxst = "</Event_what>" + m2.group(1) + "<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\">";
//                            
//                    System.out.println("FOUND INSIDE " + auxst);
//                    m.appendReplacement(sb, auxst);
//                        }
//
//                        
//m.appendTail(sb);
                    }
                }
                
//                output = sb.toString();

                
                
                
                output = output.replaceAll("(<Event_what[^>]+><\\/Event_what>)","");
                output = output.replaceAll("( <\\/Event_what>)","<\\/Event_what> ");
                output = output.replaceAll("(<Event_what[^>]+>) "," $1");
                
                
//                    System.out.println("ITS THE FINAL " + output);

                if (!writeFile(output, ev2.getAbsolutePath())) {
                    System.out.println("ERROR WHILE SAVING IN" + ev2.getAbsolutePath());
                }

                System.out.println("CORRECLY SAVED IN " + ev2.getAbsolutePath());
                System.out.println("Event core HTML prepared");
                return output;
            }

        } catch (Exception e) {
            System.out.println("Core preparation failed \n" + e.toString());
        }

        return null;
    }

}
