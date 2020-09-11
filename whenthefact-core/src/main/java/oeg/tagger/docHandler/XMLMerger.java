///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package oeg.tagger.docHandler;
//
///**
// * This class contains a function that merges two XML files, adding the xml
// * TIMEX3 tags to a base XML.
// *
// * @author mnavas
// */
//public class XMLMerger {
//
//    public String mergeXML(String baseXML, String timexAnnotatedXML) {
//        if (baseXML != null && timexAnnotatedXML != null) {
//            int j = 0; //to iterate in timexAnnotatedString
//            int i = 0; //to iterate in baseString
//            while (i < baseXML.length()) {
//                if (baseXML.charAt(i) == '<') {
//                    while (baseXML.charAt(i) == '<') {
//                        if (baseXML.indexOf(">", i) + 1 < baseXML.length()) {
//                            i = baseXML.indexOf(">", i) + 1;
//                        } else {
//                            System.out.println("ERROR WHILE MERGING THE FILES");
//                            return baseXML;
//                        }
//                    }
//                } 
//                if (j < timexAnnotatedXML.length() && timexAnnotatedXML.charAt(j) == '<' && timexAnnotatedXML.substring(j+1,j+7).equalsIgnoreCase("TIMEX3")){
//                    int j1 = timexAnnotatedXML.indexOf(">", j) + 1;
//                    int j2 = timexAnnotatedXML.indexOf("</TIMEX3>", j) + "</TIMEX3>".length();
//                    int j3 = timexAnnotatedXML.indexOf("</TIMEX3>", j);
//                    int lengtnew = j3 - j1;
//                    // in TIMEX3
//                    String intimex = timexAnnotatedXML.substring(j1, j3);
//                    if(!baseXML.substring(i,i+lengtnew).equalsIgnoreCase(intimex)){
//                        int lengthint = intimex.length();
//                        int k = 0;
//                        int k2 = 0;
//                        while(k<lengthint){
//                            while(baseXML.charAt(i+k2)!=intimex.charAt(k)){
//                                k2++;
//                            }                            
//                            k++;
//                            k2++;
//                        }
//                        baseXML = baseXML.substring(0,i) + timexAnnotatedXML.substring(j, j2)+ baseXML.substring(i+k2);
//                        
//                        i = i + j2 - j;
//                        j = j2 + 1;
//                    } else{
//                    
//                    baseXML = baseXML.substring(0,i) + timexAnnotatedXML.substring(j, j2)+ baseXML.substring(i+lengtnew);
//                    i = i + j2 - j;
//                    j = j2;
//                    
//                    }
//                    if(baseXML.charAt(i) == '<'){
//                        i--;
//                        j--;
//                    }
//                }
//                i++;
//                j++;
//            }
//            return baseXML;
//        }
//
//        return null;
//    }
//
//}

//THIS IS THE OLD VERSION; Recover in case the new one provokes any problem
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.docHandler;

/**
 * This class contains a function that merges two XML files, adding the xml TIMEX3 tags to a base XML.
 * 
 * @author mnavas
 */
public class XMLMerger {
    
    public String mergeXML(String baseXML, String timexAnnotatedXML) {
        if(baseXML != null && timexAnnotatedXML != null){
            int j = 0; //to iterate in timexAnnotatedString
            int i = 0; //to iterate in baseString
            while(i < baseXML.length()){
                if(baseXML.charAt(i) == '<'){
                    i = baseXML.indexOf(">", i) + 1;                 
                } else if(j < timexAnnotatedXML.length() && timexAnnotatedXML.charAt(j) == '<'){
                    int j1 = timexAnnotatedXML.indexOf(">", j) + 1;
                    int j2 = timexAnnotatedXML.indexOf("</TIMEX3>", j) + "</TIMEX3>".length();
                    int j3 = timexAnnotatedXML.indexOf("</TIMEX3>", j);
                    int lengtnew = j3 - j1;
                    baseXML = baseXML.substring(0,i) + timexAnnotatedXML.substring(j, j2)+ baseXML.substring(i+lengtnew);
                    i = i + j2 - j;
                    j = j2;
                }
            i++;
            j++;
            }            
            return baseXML;
        } 
        
        return null;
    }
    
    
    public String mergeXML2(String cuerpoMerge, String cuerpoAnnotated) {
        if (cuerpoMerge != null && cuerpoAnnotated != null) {
//            int len = cuerpoMerge.length();
            int j = 0; //to iterate in cuerpoAnnotated
            int i = 0; //to iterate in cuerpoMerge
            while (i < cuerpoMerge.length()) {
//                if((cuerpoMerge.charAt(i) != cuerpoAnnotated.charAt(j)) && (cuerpoMerge.charAt(i) != '<')  && (!cuerpoAnnotated.substring(j,j+7).equalsIgnoreCase("<TIMEX3"))){
//                    System.out.println("EH.\n IN: merge: " + (int) cuerpoMerge.charAt(i) + "\t annotated: " + (int) cuerpoAnnotated.charAt(j));
//                    System.out.println("Estamos en i " + i);
//                    System.out.println("EH.\n PRE: merge: " + (int) cuerpoMerge.charAt(i-1) + "\t annotated: " + (int) cuerpoAnnotated.charAt(j-1));
//                    
//                }
                if (cuerpoMerge.charAt(i) == '<') {
                    while (cuerpoMerge.charAt(i) == '<') {
                        if (cuerpoMerge.charAt(i) == '<' && cuerpoMerge.length() > i + 23 && cuerpoMerge.substring(i, i + 8).equalsIgnoreCase("<vinculo")) {
                            int i2 = cuerpoMerge.indexOf(">", i) + 1;
                            int i3 = cuerpoMerge.indexOf("</vinculo", i);
                            int i4 = cuerpoMerge.indexOf(">", i3) + 1;
                            i = i4 - 1;
//                            if (cuerpoAnnotated.substring(j, j + 6).equalsIgnoreCase("LEYREF1")) {
                            if (cuerpoAnnotated.substring(j, j + 7).equalsIgnoreCase("LEYREF1")) {
//                                j = j + 5;
                                j = cuerpoAnnotated.indexOf("LEYREF2", j) + 6;
                            } else {
                                System.out.println("ERROR WHILE MERGING THE FILES");
                                return cuerpoMerge;
                            }
//                    if(cuerpoAnnotated.substring(j, j + i3 - i2 + "TIMEX3".length()).contains("TIMEX3")){
//                        j = j + i3 - i2 + cuerpoAnnotated.indexOf(">", j) + 1 - cuerpoAnnotated.indexOf("<TIMEX3", j) + "</TIMEX3>".length();
//                    } else{
//                        String inVinc = cuerpoMerge.substring(i2, i3);
//                        inVinc = inVinc.replaceAll("<[^>]*>", "");
//                        j = j + inVinc.length();
//                    }
                        } else if (cuerpoMerge.indexOf(">", i) + 1 < cuerpoMerge.length()) {
                            i = cuerpoMerge.indexOf(">", i);
                            j--;
                        } else {
                            System.out.println("ERROR WHILE MERGING THE FILES");
                            return cuerpoMerge;
                        }
                    }
                }
                if (j < cuerpoAnnotated.length() && cuerpoAnnotated.charAt(j) == '<' && cuerpoAnnotated.substring(j + 1, j + 7).equalsIgnoreCase("TIMEX3")) {
                    int j1 = cuerpoAnnotated.indexOf(">", j) + 1;
                    int j2 = cuerpoAnnotated.indexOf("</TIMEX3>", j) + "</TIMEX3>".length();
                    int j3 = cuerpoAnnotated.indexOf("</TIMEX3>", j);
                    int lengtnew = j3 - j1;
                    // in TIMEX3
                    String intimex = cuerpoAnnotated.substring(j1, j3);
                    String pretimex = cuerpoAnnotated.substring(j, j1);
                    if (!cuerpoMerge.substring(i, i + lengtnew).equalsIgnoreCase(intimex)) {
                        int lengthint = intimex.length();
                        int k = 0;
                        int k2 = 0;
                        while (k < lengthint) {
                            while (cuerpoMerge.charAt(i + k2) != intimex.charAt(k)) {
                                k2++;
                            }
                            k++;
                            k2++;
                        }
                        
                        // Add duplicates
                        String intimexchanged = cuerpoMerge.substring(i, i + k2);
                        if(intimexchanged.contains("<")){
//                            pretimex = pretimex.replaceFirst(">", " duplicated=\"true\">");                            
                            intimexchanged = intimexchanged.replaceAll("(<[^>]+>)", "</TIMEX3>" + "$1" + pretimex);
                        }
                        
                        
                        //
                        
                        cuerpoMerge = cuerpoMerge.substring(0, i) + pretimex + intimexchanged + "</TIMEX3>" + cuerpoMerge.substring(i + k2);
//                        cuerpoMerge = cuerpoMerge.substring(0,i) + cuerpoAnnotated.substring(j, j2)+ cuerpoMerge.substring(i+k2);

//                        i = i + k2 + pretimex.length() + "</TIMEX3>".length();
                        i = i + intimexchanged.length() + pretimex.length() + "</TIMEX3>".length();
//                        i = i + j2 - j;
                        j = j2;
                    } else {

                        cuerpoMerge = cuerpoMerge.substring(0, i) + cuerpoAnnotated.substring(j, j2) + cuerpoMerge.substring(i + lengtnew);
                        i = i + j2 - j;
                        j = j2;

                    }
                    if (cuerpoMerge.charAt(i) == '<') {
                        i--;
                        j--;
                    }
                } else if (j < cuerpoAnnotated.length() && cuerpoAnnotated.charAt(j) == '<' && cuerpoAnnotated.substring(j + 1, j + 6).equalsIgnoreCase("Event")) {
                    String fin = "</Event>";
                    if(cuerpoAnnotated.substring(j + 1, j + 11).equalsIgnoreCase("Event_core")){
                        fin = "</Event_core>";
                    } else if(cuerpoAnnotated.substring(j + 1, j + 10).equalsIgnoreCase("Event_who")){
                        fin = "</Event_who>";
                    }
                    
                    int j1 = cuerpoAnnotated.indexOf(">", j) + 1;
                    int j2 = cuerpoAnnotated.indexOf(fin, j) + fin.length();
                    int j3 = cuerpoAnnotated.indexOf(fin, j);
                    int lengtnew = j3 - j1;
                    // in TIMEX3
                    String intimex = cuerpoAnnotated.substring(j1, j3);
                    String pretimex = cuerpoAnnotated.substring(j, j1);
                    if (!cuerpoMerge.substring(i, i + lengtnew).equalsIgnoreCase(intimex)) {
                        int lengthint = intimex.length();
                        int k = 0;
                        int k2 = 0;
                        while (k < lengthint) {
                            while (cuerpoMerge.charAt(i + k2) != intimex.charAt(k)) {
                                k2++;
                            }
                            k++;
                            k2++;
                        }
                        String intimexchanged = cuerpoMerge.substring(i, i + k2);
                        if(intimexchanged.contains("<")){
//                            pretimex = pretimex.replaceFirst(">", " duplicated=\"true\">");                            
                            intimexchanged = intimexchanged.replaceAll("(<[^>]+>)", fin + "$1" + pretimex);
                        }
                        
                        
                        //
                        
                        cuerpoMerge = cuerpoMerge.substring(0, i) + pretimex + intimexchanged + fin + cuerpoMerge.substring(i + k2);
//                        cuerpoMerge = cuerpoMerge.substring(0,i) + cuerpoAnnotated.substring(j, j2)+ cuerpoMerge.substring(i+k2);

//                        i = i + k2 + pretimex.length() + "</TIMEX3>".length();
                        i = i + intimexchanged.length() + pretimex.length() + fin.length();
//                        i = i + j2 - j;
                        j = j2;
                    } else {

                        cuerpoMerge = cuerpoMerge.substring(0, i) + cuerpoAnnotated.substring(j, j2) + cuerpoMerge.substring(i + lengtnew);
                        i = i + j2 - j;
                        j = j2;

                    }
                    if (cuerpoMerge.charAt(i) == '<') {
                        i--;
                        j--;
                    }
                }
                i++;
                j++;
            }
            return cuerpoMerge;
        }

        return null;
    }
    
    
    public String mergeXML3(String baseXML, String timexAnnotatedXML) {
        if(baseXML != null && timexAnnotatedXML != null){
            int j = 0; //to iterate in timexAnnotatedString
            int i = 0; //to iterate in baseString
            while(i < baseXML.length()){
                if(j < timexAnnotatedXML.length() && timexAnnotatedXML.charAt(j) == '<'){
                    int j1 = timexAnnotatedXML.indexOf(">", j) + 1;
                    baseXML = baseXML.substring(0,i) + timexAnnotatedXML.substring(j, j1) + baseXML.substring(i) ;
                    i = i + j1 - j - 1;
                    j = j1 - 1;
                }
                if(baseXML.charAt(i) == '<'){
                    i = baseXML.indexOf(">", i);   
                    j--;
                }
                
            i++;
            j++;
            }            
            return baseXML;
        } 
        
        return null;
    }
    
}
