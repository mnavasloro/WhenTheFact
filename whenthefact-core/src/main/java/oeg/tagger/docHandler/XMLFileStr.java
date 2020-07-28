/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.docHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mnavas
 */
public class XMLFileStr {

    public String cuerpo = null;
    public String cuerpoClean = null;
    public String cuerpoAnnotated = null;
    public String cuerpoMerge = null;
    public String input = null;
    public String output = null;
    public File file = null;

    public XMLFileStr(File filename) {
        file = filename;
    }

    /**
     * Returns the CUERPO of the input file
     *
     * @return the CUERPO of the input file
     */
    public String getCuerpo() {
        try {
            input = FileUtils.readFileToString(file, "UTF-8");
            String textRegex = "<body[^>]*>([\\s\\S]*)<\\/body>";
            Pattern pText = Pattern.compile(textRegex);
            Matcher mText = pText.matcher(input);
            if (mText.find()) {
                cuerpo = mText.group(1);
                return cuerpo;
            } else {
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(XMLFileStr.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns the CUERPO of the input file and deletes all the xml tags in it
     *
     * @return the CUERPO of the input file wothout xml tags
     */
    public ArrayList<DocumentPart> getParts() {
        ArrayList<DocumentPart> lp = new ArrayList<DocumentPart>();
        try {
            if (cuerpo == null) {
                getCuerpo();
            }
            if (cuerpo != null) {              
               
            String textRegex = "<p gate:gateId=\"(\\d+)\" class=\"([^\"]*)\">([^<]*)<\\/p>";
            Pattern pText = Pattern.compile(textRegex, Pattern.MULTILINE);
            Matcher mText = pText.matcher(cuerpo);
            while (mText.find()) {
                DocumentPart dp = new DocumentPart();
                dp.id = mText.group(1);
                dp.type = mText.group(2);
                dp.text = mText.group(3);
                dp.offset_ini = mText.start();
                dp.offset_end = mText.end(); 
                
                lp.add(dp);
                
            }               
                
                return lp;
            }
        } catch (Exception ex) {
            Logger.getLogger(XMLFileStr.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    public ArrayList<DocumentPart> getRelevantParts() {
        ArrayList<DocumentPart> parts = getParts();
        ArrayList<DocumentPart> ldp = new ArrayList<DocumentPart>();

        for (DocumentPart dp : parts) {
//            System.out.println(dp.type);
//            if (((dp.type.equalsIgnoreCase("Title") || dp.type.contains("Head1")) && (dp.text.contains("PROCEDURE") || dp.text.contains("FOR THESE REASONS, THE COURT"))) || ((dp.type.equalsIgnoreCase("Heading1")) && dp.text.contains("THE CIRCUMSTANCES OF THE CASE"))) {
//            if (((dp.type.equalsIgnoreCase("ECHRTitle1") || dp.type.contains("ECHRHeading")) && (dp.text.contains("PROCEDURE") || dp.text.contains("FOR THESE REASONS, THE COURT"))) || ((dp.type.equalsIgnoreCase("Heading1")) && dp.text.contains("THE CIRCUMSTANCES OF THE CASE"))) {
            if (((dp.type.equalsIgnoreCase("eCHR_Title_1") || dp.type.contains("eCHR_Heading_1")) && (dp.text.contains("PROCEDURE") || dp.text.contains("FOR THESE REASONS, THE COURT"))) || ((dp.type.equalsIgnoreCase("eCHR_Heading_1")) && dp.text.contains("THE CIRCUMSTANCES OF THE CASE"))) {
                String textRegex = "<p gate:gateId=\"(\\d+)\" class=\"(" + dp.type + "|eCHR_Title_1)\">([^<]*)<\\/p>";
                Pattern pText = Pattern.compile(textRegex);
                Matcher mText = pText.matcher(cuerpo.substring(dp.offset_end));
                if (mText.find()) {
                    dp.offset_end += mText.start();
                } else {
                    dp.offset_end = -1;
                }
                ldp.add(dp);
            }
        }

        return ldp;
    }
    
    public String setOutput() {
        try {
            output = input.replaceFirst("(<body[^>]*>)([\\s\\S]*)<\\/body>", "$1" + cuerpoMerge + "<\\/body>");
            return output;
        } catch (Exception ex) {
            Logger.getLogger(XMLFileStr.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public String mergeParts(ArrayList<DocumentPart> relparts){
        
        cuerpoMerge = "";
        for(DocumentPart p : relparts){
            if(p.offset_end != -1){
                cuerpoMerge = cuerpoMerge + cuerpo.substring(p.offset_ini, p.offset_end);
            }
            else{
                cuerpoMerge = cuerpoMerge + cuerpo.substring(p.offset_ini);
                break;
            }
        }
        
        return cuerpoMerge;
        
    }


}
