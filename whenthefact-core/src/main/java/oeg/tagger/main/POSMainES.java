package oeg.tagger.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import oeg.corpus.FileTempEval3;
import oeg.corpus.FileTempEval3ES;
import oeg.corpus.ManagerTempEval3;
import oeg.corpus.ManagerTempEval3ES;
import oeg.tagger.eventextractors.ExtractorKeywordBased;
import oeg.tagger.eventextractors.ExtractorTIMEXPOS;
import oeg.tagger.main.auxFunctions;
import oeg.tagger.docHandler.Document;
import oeg.tagger.docHandler.StructureExtractorWord;
import oeg.tagger.eventextractors.ExtractorTIMEXPOSES;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author
 */
public class POSMainES {

    public static void main(String[] args) {
        System.out.println("Event Extractor");
        ExtractorTIMEXPOSES ekb = new ExtractorTIMEXPOSES();
//            String output = cf.annotate("The judge refused the appeal.", "");
//        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA\\testclean\\");
//        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA\\testword\\";
        File foldertrain = new File("C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\datasets\\timeEval\\tempeval3ES\\test\\test-clean\\");
        String foutput = "C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\datasets\\timeEval\\tempeval3ES\\test\\outputevents\\";
        File[] files = foldertrain.listFiles();
        String txt = "";
        String filename = "";
        String filenameXML = "";

        try {
            ManagerTempEval3ES mte3 = new ManagerTempEval3ES();
            List<FileTempEval3ES> list = mte3.lista;
            int tot = list.size();
            int i = 0;
            for (FileTempEval3ES f : list) {
                i++;
                String input = f.getTextInput();
                String input2 = input.replaceAll("\\r\\n", "\\n");
                String output = ekb.annotate(input2, f.getDCTInput());
                if (!input.equals(input2)) {
                    output = output.replaceAll("\\n", "\r\n");
                }
                f.writeOutputFile(output);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("error opening file: " + filename);
            return;
        }

    }
}
