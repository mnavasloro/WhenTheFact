package oeg.tagger.main;

import java.io.File;
import java.util.List;
import oeg.corpus.FileTempEval3ES;
import oeg.corpus.ManagerTempEval3ES;
import oeg.tagger.eventextractors.ExtractorTIMEXPOSES;

/**
 *
 * @author
 */
public class POSMainESDev {

    public static void main(String[] args) {
        System.out.println("Event Extractor");
        ExtractorTIMEXPOSES ekb = new ExtractorTIMEXPOSES();
//            String output = cf.annotate("The judge refused the appeal.", "");
//        File folderdev = new File("C:\\Users\\mnavas\\DATA\\ERWANA\\testclean\\");
//        String folderworddev = "C:\\Users\\mnavas\\DATA\\ERWANA\\testword\\";
        
        String filename = "";

        try {
            ManagerTempEval3ES mte3 = new ManagerTempEval3ES("C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\datasets\\timeEval\\tempeval3ES\\test\\dev-clean\\", "C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\datasets\\timeEval\\tempeval3ES\\test\\dev\\", "C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\datasets\\timeEval\\tempeval3ES\\test\\devoutputevents\\");
            List<FileTempEval3ES> list = mte3.lista;
            int tot = list.size();
            int i = 0;
            for (FileTempEval3ES f : list) {
                i++;
                String input = f.getTextInput();
                String input2 = input.replaceAll("\\r\\n", "\\n");
                System.out.println("File: " + f.inputFile);
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
