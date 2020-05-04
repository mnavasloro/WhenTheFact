package oeg.tagger.docHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;

/**
 * Specific extractsor for documents from the ECHR. 3 levels: ECHRTitle1,
 * ECHRHeading1, ECHRPara
 *
 * @author vroddon, mnavas
 */
public class StructureExtractorECHR {

    public static void main(String[] args) throws IOException {
        //Read documents in folder and convert them into CSV for UniBo

        StructureExtractorWord se = new StructureExtractorWord();
        File folder;
        if(args.length != 1){
            folder = new File("exampleDecisions");
        } else{
            folder = new File(args[0]);
        }
        for (File file : folder.listFiles()) {
            try (PrintWriter writer = new PrintWriter(new File("output_csv/" + file.getName() + ".csv"))) {
                StringBuilder sb = new StringBuilder();

                System.out.println("*******");
                System.out.println("** " + file.getName() + " **");
                byte[] data = FileUtils.readFileToByteArray(file);
                List<Document> ldoc = se.getStructuredDocument(data, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                if (!ldoc.isEmpty()) {
                    Document doc = ldoc.get(0);
                    //            System.out.println(doc.getText());
                    for (DocumentPart p : doc.getParts()) {
                        System.out.println(p.id + " (" + p.parent + ")");
                        System.out.println(p.offset_ini + " - " + p.offset_end);
                        System.out.println(p.title + " (" + p.type + ")");
                        System.out.println(p.text);
                        System.out.println("-------");


                        sb.append(StringEscapeUtils
    .escapeCsv(p.id));
                        sb.append(',');
                        sb.append(StringEscapeUtils
    .escapeCsv(p.text));
                        sb.append(',');
                        sb.append("http://example.org/parent=" + StringEscapeUtils
    .escapeCsv(p.parent));
                        sb.append(',');
                        sb.append("http://example.org/type=" + StringEscapeUtils
    .escapeCsv(p.type) + "|" + "http://example.org/title=\"" + StringEscapeUtils.escapeCsv(p.title) + "\"");
                        sb.append('\n');

                    } 
                    
                        writer.write(sb.toString());
                        writer.close();
                        System.out.println("done!");
                }
                
                       
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    
    public Document extractFromDocument(File f){
        
        try {
            byte[] data = FileUtils.readFileToByteArray(f);
            InputStream is = new ByteArrayInputStream(data);
            XWPFDocument wdoc = new XWPFDocument(is);
//            if (StructureExtractorECHR.isECHR(wdoc))
//            {
                Document doc = StructureExtractorECHR.getECHR(wdoc);
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
            icount+=text.length()+2;
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
