//package oeg.eventextractor;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import oeg.contractFrames.ExtractorKeywordBased;
//import oeg.docHandler.Document;
//import oeg.docHandler.StructureExtractorWord;
//import org.apache.commons.io.FileUtils;
//
///**
// *
// * @author 
// */
//public class Main {
//        public static void main(String[] args) {
//          System.out.println("Event Extractor");
//        ExtractorKeywordBased ekb = new ExtractorKeywordBased();
////            String output = cf.annotate("The judge refused the appeal.", "");
////        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA\\testclean\\");
////        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA\\testword\\";
//        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA\\trainclean\\");
//        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA\\trainword\\";
//        File[] files = foldertrain.listFiles();
//        String txt = "";
//        String filename = "";
//        for (File f : files) {
//            try {
//                txt = FileUtils.readFileToString(f, "UTF-8");
//                filename = folderwordtrain + f.getName().replaceFirst("\\.docx.*", "\\.docx");
//                filename = filename.replaceFirst("\\.xml", "\\.docx");
//                
//                File word = new File(filename);
//                String output = ekb.annotate(txt, word, f.getName());
//                filename = "";
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println("error opening file: " + filename);
//                return;
//            }
//            
//        }
//    }
//
//}
