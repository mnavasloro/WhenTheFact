/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.docHandler;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mnavas
 */
public class Document {
    List<DocumentPart> parts;
    public String text;

    List<DocumentPart> getParts() {
        return parts;
    }

    void setText(String texto) {
        text = texto;
    }

    void setParts(List<DocumentPart> partsin) {
        parts = partsin;
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
        for(DocumentPart dp : parts){
            if((dp.type.equalsIgnoreCase("Title") || dp.type.contains("Head1")) && (dp.text.contains("PROCEDURE") || dp.text.contains("FOR THESE REASONS, THE COURT"))){
                ldp.add(dp);
            } else if((dp.type.equalsIgnoreCase("Heading1") || dp.type.contains("Head2")) && dp.text.contains("THE CIRCUMSTANCES OF THE CASE")){
                ldp.add(dp);
            }
        }
        return ldp;
    }
    
}
