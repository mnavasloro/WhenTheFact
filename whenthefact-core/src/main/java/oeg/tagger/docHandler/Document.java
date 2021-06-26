/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.docHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mnavas
 */
public class Document {
    List<DocumentPart> parts;
    List<DocumentPart> parParts;
    ArrayList<String> topicParts = new ArrayList<String>();
    public String type = "";
    public String text;
    public String orText;

    public List<DocumentPart> getParts() {
        return parts;
    }

    void setText(String texto) {
        text = texto;
    }

    void setParts(List<DocumentPart> partsin) {
        parts = partsin;
    }
    
    public List<DocumentPart> getPartsParagraph() {
        return parParts;
    }

    void setPartsParagraph(List<DocumentPart> partsin) {
        parParts = partsin;
    }
    
    public List<DocumentPart> getTitleParts(){
        List<DocumentPart> ldp = new ArrayList<DocumentPart>();
        for(DocumentPart dp : parts){
            if(dp.type.equalsIgnoreCase("Title")){
                ldp.add(dp);
            }
        }
        return ldp;
    }
    
    public List<DocumentPart> getEventRelevantParts(){
        List<DocumentPart> ldp = new ArrayList<DocumentPart>();
        if(type.equalsIgnoreCase("ecj")){
            for(DocumentPart dp : parts){
                if(!dp.title.contains("href") && !dp.title.contains("JUDGMENT OF THE COURT") && !dp.title.contains("INTRO") && !dp.title.contains("Legal")  && !dp.title.contains("Consideration of the questions") && !dp.title.contains("Law")){// && (dp.text.contains("PROCEDURE") || dp.text.contains("FOR THESE REASONS, THE COURT"))){
                    ldp.add(dp);
                }
            }
        }
        else{
            for(DocumentPart dp : parts){
                if((dp.type.equalsIgnoreCase("Title") || dp.type.contains("Head1"))  && !dp.text.contains("PROCEDURE") && !dp.title.contains("LAW") && !dp.title.contains("THE FACTS")){// && (dp.text.contains("PROCEDURE") || dp.text.contains("FOR THESE REASONS, THE COURT"))){
                    ldp.add(dp);
                } else if((dp.type.equalsIgnoreCase("Heading1") || dp.type.contains("Head2")) && dp.text.contains("THE CIRCUMSTANCES OF THE CASE") && !dp.text.contains("PROCEDURE")  && !dp.title.contains("LAW") && !dp.title.contains("THE FACTS")){
                    ldp.add(dp);
    //            } else if( dp.title.contains("PROCEDURE") && dp.text.startsWith("The case originated in")){
    //                ldp.add(dp);
                }
            }
        }
        return ldp;
    }
    
    
    
    
    public DocumentPart getProcedure(){
        for(DocumentPart dp : parts){
            if( dp.title.contains("PROCEDURE")){
                return dp;
            }
        }
        return null;
    }
    
    
    public ArrayList<String> getTopicParts(){
        if(type.equalsIgnoreCase("ecj") && orText!=null){
            
            final String regex = "<p class=\\\"[^\\\"]*index[^\\\"]*\\\">([^<]*)\\)<\\/p>";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(orText);
            String index = "";

            if (matcher.find()) {                
                index = matcher.group(1);
                String[] splitIndex = index.split("[—–]");
                
                for(String s: splitIndex){
                    
                   if(!s.contains("Article")){
                       topicParts.add(s.substring(1, s.length()-1));
                   }                    
                    
                }
                
                
            }
            return topicParts;
        }
        return null;
    }
    
    
    
    
    
}
