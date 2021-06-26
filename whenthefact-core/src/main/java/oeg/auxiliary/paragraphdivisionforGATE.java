///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package oeg.auxiliary;
//
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.util.CoreMap;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import oeg.tagger.docHandler.Document;
//import oeg.tagger.docHandler.DocumentPart;
//import oeg.tagger.docHandler.StructureExtractorECHR;
//import oeg.tagger.eventextractors.ExtractorTIMEXKeywordBasedNE;
//import static oeg.tagger.extractors.writer.writeFile;
//import org.apache.commons.io.FileUtils;
//
///**
// *
// * @author mnavas
// */
//public class paragraphdivisionforGATE {
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
////        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA2\\testclean\\");
////        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA2\\word\\";
//
////        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\TempCourt\\TempCourt_originalcorpus\\ECHR\\");
////        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\TempCourt\\TempCourt_originalcorpus\\word\\";
////        String foutput = "C:\\Users\\mnavas\\DATA\\TempCourt\\outPARA\\";
//
//        File foldertrain = new File("C:\\Users\\mnavas\\DATA\\ERWANA2-submission\\allclean\\");
//        String folderwordtrain = "C:\\Users\\mnavas\\DATA\\ERWANA2-submission\\word\\";
//        String foutput = "C:\\Users\\mnavas\\DATA\\ERWANA2-submission\\outPARA\\";
//        File[] files = foldertrain.listFiles();
//        String txt = "";
//        String filename = "";
//        String filenameXML = "";
//        for (File f : files) {
//            try {
//                txt = FileUtils.readFileToString(f, "UTF-8");
//
//                String output = txt;
//
//                filename = folderwordtrain + f.getName().replaceFirst("\\.docx.*", "\\.docx");
//                filename = filename.replaceFirst("\\.xml.*", "\\.docx");
//                filenameXML = f.getName().replaceFirst("\\.txt", "\\.xml");
//
//                File wordfile = new File(filename);
//
//                StructureExtractorECHR seECHR = new StructureExtractorECHR();
//                Document doc = seECHR.extractFromDocument(wordfile);
////                List<DocumentPart> eventRelevantParts = doc.getPartsParagraph();
//                List<DocumentPart> eventRelevantParts = doc.getPartsParagraph();
//
//                int offset = 0;
//                String end = "</PARAGRAPH>";
//                int offend = end.length();
//                int sizeoutput = output.length();
//                for (DocumentPart erp : eventRelevantParts) {
//                    if (!(erp.type.equalsIgnoreCase("Title") || erp.type.startsWith("Heading")) ) {
//                        
//                        if(offset + erp.offset_ini < sizeoutput){
//                        
//                        String ini = "<PARAGRAPH ID=\"" + erp.id + "\" PARENT=\"" + erp.parent + "\" TYPE=\"" + erp.type + "\">";
//                        output = output.substring(0, offset + erp.offset_ini) + ini + output.substring(offset + erp.offset_ini, offset + erp.offset_end) + end + output.substring(offset + erp.offset_end);
//                        System.out.println(offset);
//                        
//                        offset = offset + offend + ini.length();
//                            
//                        }
//                    }
//
//                }
//
//                System.out.println(output);
//
//                String outf = foutput + filenameXML;
//
//                System.out.println("Writing to: " + outf + "\n----");
//                if (!writeFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<TEXT>" + output + "</TEXT>", outf)) {
//                    System.out.println("ERROR WHILE SAVING IN" + outf);
//                }
//
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
