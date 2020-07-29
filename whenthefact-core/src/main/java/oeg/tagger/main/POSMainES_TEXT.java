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
public class POSMainES_TEXT {

    public static void main(String[] args) {
        System.out.println("Event Extractor");
        ExtractorTIMEXPOSES ekb = new ExtractorTIMEXPOSES();
        String input2 = "Ese Comité Ejecutivo está formado por representantes del BID, de la Organización de Estados Americanos ( OEA) y de la Comisión Económica de las Naciones Unidas para América Latina y el Caribe ( CEPAL).";

        try {
           
                String output = ekb.annotate(input2, "2020-07-28");
                System.out.println(output);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("error processing: " + input2);
            return;
        }

    }
}
