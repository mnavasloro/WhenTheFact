package oeg.tagger.docHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.AbstractXWPFSDT;
import org.apache.poi.xwpf.usermodel.BodyType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ICell;
import org.apache.poi.xwpf.usermodel.IRunElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFFootnote;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFSDTCell;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

/**
 *
 * @author vroddon
 */
public class StructureExtractorWord {


    public List<Document> getStructuredDocument(byte[] data, String mimetype) {
        List<Document> list = new ArrayList();

        try {
            InputStream is = new ByteArrayInputStream(data);
            XWPFDocument wdoc = new XWPFDocument(is);
            if (StructureExtractorECHR.isECHR(wdoc))
            {
                Document doc = StructureExtractorECHR.getECHR(wdoc);
                list.add(doc);
                return list;
            }
            
            Document doc = new Document();
            XWPFWordExtractor we = new XWPFWordExtractor(wdoc);
            String texto = we.getText();
       //     System.out.println(texto);
            doc.setText(texto);
            list.add(doc);
            
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    
    
    
    
    public Document getStructuredDoc(File file) {
        Document doc = new Document();
        try {
            byte[] data = FileUtils.readFileToByteArray(file);
            InputStream is = new ByteArrayInputStream(data);
            XWPFDocument wdoc = new XWPFDocument(is);
            
            
            XWPFWordExtractor we = new XWPFWordExtractor(wdoc);
            String texto = we.getText();
       //     System.out.println(texto);
            doc.setText(texto);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public static void main(String[] args) throws IOException {
        //We should read a word document here and pass it to the getStructuredDocument()

        StructureExtractorWord se = new StructureExtractorWord();
        File folder = new File("exampleDecisions");
        for(File file : folder.listFiles()){
            System.out.println("*******");
                System.out.println("** " + file.getName() + " **");
            byte[] data = FileUtils.readFileToByteArray(file);
            List<Document> ldoc = se.getStructuredDocument(data, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            if (!ldoc.isEmpty()) {
                Document doc = ldoc.get(0);
    //            System.out.println(doc.getText());
            for (DocumentPart p : doc.getParts()){
                System.out.println("-------");
                System.out.println(p.id + " (" + p.parent + ")");
                System.out.println(p.offset_ini + " - " + p.offset_end);
                System.out.println(p.title + " (" + p.type + ")");
                System.out.println(p.text);
                System.out.println("-------");
            }
        }
        }
    }
    

    private static List<AbstractXWPFSDT> extractAllSDTs(XWPFDocument doc) {

        List<AbstractXWPFSDT> sdts = new ArrayList<AbstractXWPFSDT>();

        List<XWPFHeader> headers = doc.getHeaderList();
        for (XWPFHeader header : headers) {
            sdts.addAll(extractSDTsFromBodyElements(header.getBodyElements()));
        }
        sdts.addAll(extractSDTsFromBodyElements(doc.getBodyElements()));

        List<XWPFFooter> footers = doc.getFooterList();
        for (XWPFFooter footer : footers) {
            sdts.addAll(extractSDTsFromBodyElements(footer.getBodyElements()));
        }

        for (XWPFFootnote footnote : doc.getFootnotes()) {
            sdts.addAll(extractSDTsFromBodyElements(footnote.getBodyElements()));
        }
        return sdts;
    }

    private static List<AbstractXWPFSDT> extractSDTsFromBodyElements(List<IBodyElement> elements) {
        List<AbstractXWPFSDT> sdts = new ArrayList<AbstractXWPFSDT>();
        for (IBodyElement e : elements) {
            if (e instanceof XWPFSDT) {
                XWPFSDT sdt = (XWPFSDT) e;
                sdts.add(sdt);
            } else if (e instanceof XWPFParagraph) {

                XWPFParagraph p = (XWPFParagraph) e;
                for (IRunElement e2 : p.getIRuns()) {
                    if (e2 instanceof XWPFSDT) {
                        XWPFSDT sdt = (XWPFSDT) e2;
                        sdts.add(sdt);
                    }
                }
            } else if (e instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) e;
                sdts.addAll(extractSDTsFromTable(table));
            }
        }
        return sdts;
    }

    private static List<AbstractXWPFSDT> extractSDTsFromTable(XWPFTable table) {

        List<AbstractXWPFSDT> sdts = new ArrayList<AbstractXWPFSDT>();
        for (XWPFTableRow r : table.getRows()) {
            for (ICell c : r.getTableICells()) {
                if (c instanceof XWPFSDTCell) {
                    sdts.add((XWPFSDTCell) c);
                } else if (c instanceof XWPFTableCell) {
                    sdts.addAll(extractSDTsFromBodyElements(((XWPFTableCell) c).getBodyElements()));
                }
            }
        }
        return sdts;
    }    
}
