package oeg.tagger.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import oeg.tagger.eventextractors.ExtractorTIMEXKeywordBasedNEFrames;
import oeg.tagger.eventextractors.ExtractorTIMEXKeywordBasedNE;
import oeg.tagger.main.auxFunctions;
import oeg.tagger.docHandler.Document;
import oeg.tagger.docHandler.StructureExtractorWord;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author 
 */
public class AnnotadorMainTest {
        public static void main(String[] args) {
          System.out.println("Event Extractor");
        ExtractorTIMEXKeywordBasedNE ekb = new ExtractorTIMEXKeywordBasedNE();
//            String output = cf.annotate("The judge refused the appeal.", "");
//        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA\\testclean\\");
//        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA\\testword\\";
        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA\\testclean\\");
        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA\\testword\\";
        String foutput = "C:\\Users\\mnavas\\DATA\\ERWANA\\outtestTIMEX\\";
        File[] files = foldertrain.listFiles();
        String txt = "";
        String filename = "";
        String filenameXML = "";
        for (File f : files) {
            try {
                txt = FileUtils.readFileToString(f, "UTF-8");
                filename = folderwordtrain + f.getName().replaceFirst("\\.docx.*", "\\.docx");
                filename = filename.replaceFirst("\\.xml", "\\.docx");
                filenameXML = f.getName().replaceFirst("\\.docx.*", "\\.docx").replaceFirst("\\.docx", "\\.xml");
                
                File word = new File(filename);
                String output = ekb.annotate(txt, "2012-02-20", word, f.getName());
                System.out.println(output);
                
                String outf = foutput + filenameXML;
                output = output.replaceAll("TIMEX3", "Eventwhen");
                System.out.println("Writing to: " + outf + "\n----");
                if (!auxFunctions.writeFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<TEXT>" + output + "</TEXT>", outf)) {
            System.out.println("ERROR WHILE SAVING IN" + outf);
        }
               
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("error opening file: " + filename);
                return;
            }
            
        }
    }

}
