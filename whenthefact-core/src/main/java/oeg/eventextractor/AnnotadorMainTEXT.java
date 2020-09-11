package oeg.eventextractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
//import oeg.contractFrames.ExtractorKeywordBased;
import oeg.tagger.extractors.ExtractorTIMEXKeywordBased;
import static oeg.tagger.extractors.writer.writeFile;
import oeg.tagger.docHandler.Document;
import oeg.tagger.docHandler.StructureExtractorWord;
import oeg.tagger.eventextractors.ExtractorTIMEXKeywordBasedNE;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author 
 */
public class AnnotadorMainTEXT {
        public static void main(String[] args) {
          System.out.println("Event Extractor");
        ExtractorTIMEXKeywordBasedNE ekb = new ExtractorTIMEXKeywordBasedNE();
//            String output = cf.annotate("The judge refused the appeal.", "");
//        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA\\testclean\\");
//        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA\\testword\\";
        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA2\\trainclean\\");
        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA2\\word\\";
        String foutput = "C:\\Users\\mnavas\\DATA\\ERWANA2\\outTIMEX\\";
        File[] files = foldertrain.listFiles();
        String txt = "";
        String filename = "";
        String filenameXML = "";
        for (File f : files) {
            try {
                txt = "While the essential object of Article 8 of the Convention is to protect the individual against arbitrary interference by the public authorities, it does not merely compel the State to abstain from such interference: in addition to this negative undertaking, there may be positive obligations inherent in effective respect for private or family life.";
                filename = folderwordtrain + f.getName().replaceFirst("\\.docx.*", "\\.docx");
                filename = filename.replaceFirst("\\.xml.*", "\\.docx");
                filenameXML = f.getName().replaceFirst("\\.docx.*", "\\.docx").replaceFirst("\\.docx", "\\.xml");
                
                File word = new File(filename);
                String output = ekb.annotate(txt, "2012-02-20", word, f.getName());
                System.out.println(output);
                
//                String outf = foutput + filenameXML;
                
//                System.out.println("Writing to: " + outf + "\n----");
//                if (!writeFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<TEXT>" + output + "</TEXT>", outf)) {
//            System.out.println("ERROR WHILE SAVING IN" + outf);
//        }
               
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("error opening file: " + filename);
                return;
            }
            
        }
    }
}


