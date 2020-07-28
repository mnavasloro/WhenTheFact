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
public class XMLMerger2 {
    
    public String mergeXML(String baseXML, String timexAnnotatedXML) {
        if(baseXML != null && timexAnnotatedXML != null){
            int j = 0; //to iterate in timexAnnotatedString
            int i = 0; //to iterate in baseString
            while(i < baseXML.length()){
                if(baseXML.charAt(i) == '<' && timexAnnotatedXML.charAt(j) != '<'){
                    i = baseXML.indexOf(">", i);     
                    j--;
                } else if(baseXML.charAt(i) == '<' && timexAnnotatedXML.charAt(j) == '<'){
                    int j1 = timexAnnotatedXML.indexOf(">", j) + 1;
                    int j2 = timexAnnotatedXML.indexOf("</TIMEX3>", j) + "</TIMEX3>".length();
                    int j3 = timexAnnotatedXML.indexOf("</TIMEX3>", j);
                    int lengtnew = j3 - j1;
                    int i2 = baseXML.indexOf(">", i)+1 + "</EVENT>".length() + lengtnew;
                    baseXML = baseXML.substring(0,i) + timexAnnotatedXML.substring(j, j2)+ baseXML.substring(i2) ;
                    i = i + j2 - j;
                    j = j2;
                
                } else if(j < timexAnnotatedXML.length() && timexAnnotatedXML.charAt(j) == '<'){
                    int j1 = timexAnnotatedXML.indexOf(">", j) + 1;
                    int j2 = timexAnnotatedXML.indexOf("</TIMEX3>", j) + "</TIMEX3>".length();
                    int j3 = timexAnnotatedXML.indexOf("</TIMEX3>", j);
                    int lengtnew = j3 - j1;
                    // If there is a event tag in the way
                    if(baseXML.substring(i,Integer.min(i+lengtnew+"<EVENT>".length(),baseXML.length())).contains("<EVENT")){
                        String inTIMEX = timexAnnotatedXML.substring(j1, j3);
                        int k = 0;
                        int h = i;
                        while(k<inTIMEX.length()){
                            if(inTIMEX.charAt(k)!=baseXML.charAt(h) && baseXML.charAt(h)=='<'){
                                int h1 = baseXML.indexOf(">", h) + 1;
                                int h2 = baseXML.indexOf("<", h1);
                                h = baseXML.indexOf("</EVENT>", h) + "</EVENT>".length();
                                k+= h2 - h1;
                            }
                            else{
                                h++;
                                k++; 
                            }
                                                      
                        }                                                
                        baseXML = baseXML.substring(0,i) + timexAnnotatedXML.substring(j, j2)+ baseXML.substring(h);
                    }
                    else{
                       baseXML = baseXML.substring(0,i) + timexAnnotatedXML.substring(j, j2)+ baseXML.substring(i+lengtnew);
                     
                    }
                    i = i + j2 - j;
                    j = j2;
                } else if(baseXML.charAt(i) != timexAnnotatedXML.charAt(j)){
                    System.out.println(i + " " + j);
                }
            i++;
            j++;
            }            
            return baseXML;
        } 
        
        return null;
    }
    
}
