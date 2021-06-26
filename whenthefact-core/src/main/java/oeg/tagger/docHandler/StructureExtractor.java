package oeg.tagger.docHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;

/**
 * Specific extractor for documents from the ECHR. 3 levels: ECHRTitle1,
 * ECHRHeading1, ECHRPara
 *
 * @author vroddon, mnavas
 */
public class StructureExtractor{
    
    
     public Document extractFromDocument(File f){
        
        try {
            String txt = "";
            if(f.getName().endsWith(".docx")){ //ECHR type
                return extractFromDocumentECHR(f);
            }
            
            txt  = FileUtils.readFileToString(f, "UTF-8");
            
            if(txt==null || txt.contains("pursuant to Rule 77 §§ 2 and 3 of the Rules of Court.")){ //ECHR type
                return extractFromDocumentECHR(f);
            } else if(txt.contains("sum-title-1")){ //ECJ type
                return extractFromDocumentECJ(f);
            } else {
                Document doc =  new Document();
                doc.setText(txt);
                return doc;
            }
            
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    static public Document extractFromDocumentECJ(File f) {
        Document doc = new Document();
        int ini = 0;
        int fin = 0;
        String name = "INTRO";
        ArrayList<DocumentPart> dp = new ArrayList<DocumentPart>();

        try {
            String inhtml = FileUtils.readFileToString(f, "UTF-8");
            String rawhtml = inhtml.replaceAll("<\\/[^>]*>", "");
//            String rawhtml = inhtml.replaceAll("<\\/p>", "\t\\.\t");
//            rawhtml = rawhtml.replaceAll("<\\/[^>]*>", "");
            rawhtml = rawhtml.replaceAll("<[^>]*>", "");
            String regex = "<p class=\\\".*?sum-title-1.*?\\\"[^>]*?>([\\s\\S]*?[\\w ]+[\\s\\S]*?)<\\/p>";
//            String regex = "<p class=\\\".*sum-title-1.*\\\"[^>]*>";
            
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(inhtml);

            while (matcher.find()) {
                fin = matcher.start(1);
                DocumentPart singledp = new DocumentPart();
                
                // substring
//                String subrawhtml = inhtml.substring(0, fin).replaceAll("<\\/[^>]*>", "");
                String subrawhtml = inhtml.substring(0, fin).replaceAll("<\\/p>", "\t\\.\t");
                subrawhtml = subrawhtml.replaceAll("<\\/[^>]*>", "");
                subrawhtml = subrawhtml.replaceAll("<[^>]*>", "");
                
                
                singledp.offset_ini=ini;
                singledp.offset_end= subrawhtml.length();
                singledp.title=name;
                
                dp.add(singledp);
                
                name = matcher.group(1);
                ini = subrawhtml.length() + name.length();          
                
                
            }
            DocumentPart singledp = new DocumentPart();

            singledp.offset_ini=ini;
            singledp.offset_end= rawhtml.length();
            singledp.title=name;
                
            dp.add(singledp);
            
            doc.type="ecj";
            doc.parts=dp;
            doc.text=rawhtml;
            doc.orText = FileUtils.readFileToString(f, "UTF-8");
            
            return doc;
            
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

        
    public Document extractFromDocumentECHR(File f){
        
        try {
            byte[] data = FileUtils.readFileToByteArray(f);
            InputStream is = new ByteArrayInputStream(data);
            XWPFDocument wdoc = new XWPFDocument(is);
//            if (StructureExtractor.isECHR(wdoc))
//            {
                Document doc = StructureExtractor.getECHR(wdoc);
                doc.type="echr";
                return doc;
//            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean isECHR(XWPFDocument wdoc) {
        XWPFStyles styles = wdoc.getStyles();
        XWPFStyle style = styles.getStyle("ECHRTitle1");
        return (style != null);
    }

    public static Document getECHR(XWPFDocument wdoc) {
        Document doc = new Document();
        
        List<XWPFParagraph> parrafos = wdoc.getParagraphs();
        
        //WE GENERATE THE FIRSTS LEVEL ELEMENTS
        int conta = 0;
        String texto = "";
        List<DocumentPart> parts = new ArrayList();
        int icount=0;
        for (XWPFParagraph parrafo : parrafos) {
            String text = parrafo.getText();
            String estilo = parrafo.getStyle();
            if (estilo!=null && estilo.equals("ECHRTitle1")) {
                int ipos= wdoc.getPosOfParagraph(parrafo);
                DocumentPart part = new DocumentPart();
                part.id = "0.t" + conta;
                conta++;
                part.parent = "0";
                part.type = "Title";
                
                if(parrafo.getText().matches("((\\d+\\.)|(\\w+\\.)|(\\(\\w+\\))|(\\(\\d+\\))) *[\\s\\S]+")){                    
                    part.text = parrafo.getText().replaceFirst("((\\d+\\.)|(\\w+\\.)|(\\(\\w+\\))|(\\(\\d+\\))) *","");
                    part.title  = parrafo.getText().substring(0, parrafo.getText().length()-part.text.length());
                } else{
                    part.title  = parrafo.getText();
                    part.text  = parrafo.getText();
                }
                
                part.offset_ini = icount;
                if(!parts.isEmpty()){
                    DocumentPart auxPart = parts.get(parts.size()-1);
                    auxPart.offset_end = part.offset_ini;
                    parts.set(parts.size()-1, auxPart);
                }
                parts.add(part);
        //        System.out.println("CABECERA " + parrafo.getText() + " "+ ipos);
            }
            icount+=text.length()+2;
            texto += text+"\n";
        }
        if(!parts.isEmpty()){
            DocumentPart auxPart = parts.get(parts.size()-1);
            auxPart.offset_end = icount;
            parts.set(parts.size()-1, auxPart);
        }
        doc.setText(texto);
        
        
        //WE GENERATE THE SECOND LEVEL ELEMENTS (HEADINGS)
        for(int num=1; num<8 ; num++){
        conta = 0;
        texto = "";
        List<DocumentPart> partsHeading = new ArrayList();
        icount=0;
        for (XWPFParagraph parrafo : parrafos) {
            String text = parrafo.getText();
            String estilo = parrafo.getStyle();    
            if (estilo!=null && estilo.equals("ECHRHeading" + num)) {
                int ipos= wdoc.getPosOfParagraph(parrafo);
                DocumentPart part = new DocumentPart();
                
                if(parrafo.getText().matches("((\\d+\\.)|(\\w+\\.)|(\\(\\w+\\))|(\\(\\d+\\))) *[\\s\\S]+")){                    
                    part.text = parrafo.getText().replaceFirst("((\\d+\\.)|(\\w+\\.)|(\\(\\w+\\))|(\\(\\d+\\))) *","");
                    part.title  = parrafo.getText().substring(0, parrafo.getText().length()-part.text.length());
                } else{
                    part.title  = parrafo.getText();
                    part.text  = parrafo.getText();
                }
                
                part.offset_ini = icount;
                part.type = "Heading" + num;
                DocumentPart parent = findLowestParent(parts, icount, part.type);
                if(parent == null){
                    part.parent = "0";
                } else{
                    part.parent = parent.id ;
                }
                // We set by default the end of the paragraph as the end of its parent; 
                // Later we check if there are more paragraphs in that parent and correct if needed...
                part.offset_end = parent.offset_end;
                part.id = parent.id + ".h" + num + "-" + conta;
                conta++;
               
                if(!partsHeading.isEmpty()){
                    DocumentPart auxPart = partsHeading.get(partsHeading.size()-1);
                    // There were more children in the previous pragraphs parent! We correct the end
                    if(auxPart.parent.equalsIgnoreCase(part.parent)){
                        auxPart.offset_end = part.offset_ini;
                    }
                    parts.add(auxPart);
                }
                partsHeading.add(part);
        //        System.out.println("CABECERA " + parrafo.getText() + " "+ ipos);
            }
            icount+=text.length()+2;
            texto += text+"\n";
        }
        if(!partsHeading.isEmpty()){
            DocumentPart auxPart = partsHeading.get(partsHeading.size()-1);
//            auxPart.offset_end = icount;
            partsHeading.set(partsHeading.size()-1, auxPart);
            parts.add(auxPart);
        }
            
        }
          
        
        // PARAGRAPH LEVEL
        conta = 0;
        texto = "";
        List<DocumentPart> partsParagraph = new ArrayList();
        icount=0;
        for (XWPFParagraph parrafo : parrafos) {
            String text = parrafo.getText();
            String estilo = parrafo.getStyle();    
            if (estilo!=null && estilo.equals("ECHRPara")) {
                int ipos= wdoc.getPosOfParagraph(parrafo);
                DocumentPart part = new DocumentPart();
                if(parrafo.getText().matches("\\d+\\. *[\\s\\S]+")){                    
                    part.text = parrafo.getText().replaceFirst("((\\d+\\.)|(\\w+\\.)|(\\(\\w+\\))|(\\(\\d+\\))) *","");
                    part.title  = parrafo.getText().substring(0, parrafo.getText().length()-part.text.length());
                } else{
                    part.title  = parrafo.getText();
                    part.text  = parrafo.getText();
                }
                part.type = "Paragraph";
                if(part.title.length() == 0){
                    continue;
                }
                part.offset_ini = icount;
                DocumentPart parent = findLowestParent(parts, icount, part.type);
                if(parent == null){
                    part.parent = "0";
                    part.offset_end = part.offset_ini + part.title.length();
//                    part.offset_end
                } else{
                    part.parent = parent.id ;
                    // We set by default the end of the paragraph as the end of its parent; 
                    // Later we check if there are more paragraphs in that parent and correct if needed...
                    part.offset_end = parent.offset_end;
                }
                
                part.id = part.parent + ".p" + conta;
                conta++;
               
                if(!partsParagraph.isEmpty()){
                    DocumentPart auxPart = partsParagraph.get(partsParagraph.size()-1);
                    // There were more children in the previous pragraphs parent! We correct the end
                    if(auxPart.parent.equalsIgnoreCase(part.parent)){
                        auxPart.offset_end = part.offset_ini;
                    }
                    parts.add(auxPart);
                }
                partsParagraph.add(part);
        //        System.out.println("CABECERA " + parrafo.getText() + " "+ ipos);
            }
            icount+=text.length()+1;
            texto += text+"\n";
        }
        if(!partsParagraph.isEmpty()){
            DocumentPart auxPart = partsParagraph.get(partsParagraph.size()-1);
//            auxPart.offset_end = icount;
            partsParagraph.set(partsParagraph.size()-1, auxPart);
            parts.add(auxPart);
        }
        
        // FINAL DECISION LEVEL
        conta = 0;
        texto = "";
        List<DocumentPart> partsJud = new ArrayList();
        icount=0;
        for (XWPFParagraph parrafo : parrafos) {
            String text = parrafo.getText();
            String estilo = parrafo.getStyle();    
            if (estilo!=null && (estilo.equals("JuList") || estilo.equals("JuLista") || estilo.equals("JuParaLast"))) {// || estilo.equals("JuSigned"))) {
                int ipos= wdoc.getPosOfParagraph(parrafo);
                DocumentPart part = new DocumentPart();
                if(parrafo.getText().matches("((\\d+\\.)|(\\w+\\.)|(\\(\\w+\\))|(\\(\\d+\\))) *[\\s\\S]+")){                    
                    part.text = parrafo.getText().replaceFirst("((\\d+\\.)|(\\w+\\.)|(\\(\\w+\\))|(\\(\\d+\\))) *","");
                    part.title  = parrafo.getText().substring(0, parrafo.getText().length()-part.text.length());
                } else{
                    part.title  = parrafo.getText();
                    part.text  = parrafo.getText();
                }
                part.type = "Decision";
                if(part.title.length() == 0){
                    continue;
                }
                part.offset_ini = icount;
                DocumentPart parent = findLowestParent(parts, icount, part.type);
                if(parent == null){
                    part.parent = "0";
                    part.offset_end = part.offset_ini + part.title.length();
//                    part.offset_end
                } else{
                    part.parent = parent.id ;
                    // We set by default the end of the paragraph as the end of its parent; 
                    // Later we check if there are more paragraphs in that parent and correct if needed...
                    part.offset_end = parent.offset_end;
                }
                
                part.id = part.parent + ".p" + conta;
                conta++;
               
                if(!partsJud.isEmpty()){
                    DocumentPart auxPart = partsJud.get(partsJud.size()-1);
                    // There were more children in the previous pragraphs parent! We correct the end
                    if(auxPart.parent.equalsIgnoreCase(part.parent)){
                        auxPart.offset_end = part.offset_ini;
                    }
                    parts.add(auxPart);
                }
                partsJud.add(part);
        //        System.out.println("CABECERA " + parrafo.getText() + " "+ ipos);
            }
            icount+=text.length()+2;
            texto += text+"\n";
        }
        if(!partsJud.isEmpty()){
            DocumentPart auxPart = partsJud.get(partsJud.size()-1);
//            auxPart.offset_end = icount;
            partsJud.set(partsJud.size()-1, auxPart);
            parts.add(auxPart);
        }
        
        
        
        
        
        
        doc.setParts(parts);
        doc.setPartsParagraph(partsParagraph);
//        System.out.println("==\n"+texto);
        return doc;
    }
    
    
    
    public static DocumentPart findLowestParent(List<DocumentPart> parts, int offset_ini, String type){
        // We iterate the already detected parts backwards to get the lowest-hierarchy parent
            for (int j = parts.size() - 1; j >= 0; j--) { 
                DocumentPart part = parts.get(j);
                if(part.offset_ini < offset_ini && part.offset_end > offset_ini && !type.equalsIgnoreCase(part.type)){
                    return part;
                }
            }
            return null;
    }

}
