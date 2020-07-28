/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.tagger.docHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mnavas
 */
public class xmlRelevantPartPruner {

    public static void main(String[] args) throws IOException {
        FileOutputStream fos = null;
        try {
            File finput = new File("C:\\Users\\mnavas\\Desktop\\nonAnnotatedRelevant\\");
            String foutput = "C:\\Users\\mnavas\\Desktop\\output\\";
//            File finput = new File("C:\\Users\\mnavas\\Desktop\\ErwinCorpus\\gate\\");
//            String foutput = "C:\\Users\\mnavas\\Desktop\\ErwinCorpus\\output\\";
//            String foutputHTML = "../annotador-CENDOJ-core/src/main/resources/rules/output.html";
            File[] listF = finput.listFiles();
            for (File f : listF) {
                XMLFileStr fc = new XMLFileStr(f);
                String cuerpo = fc.getCuerpo();
                ArrayList<DocumentPart> relparts = fc.getRelevantParts();
                fc.mergeParts(relparts);

                String outp = fc.setOutput();

                FileOutputStream fos1 = new FileOutputStream(foutput + f.getName());
                OutputStreamWriter w = new OutputStreamWriter(fos1, "UTF-8");
                BufferedWriter bw = new BufferedWriter(w);
                bw.write(outp);
                bw.flush();
                bw.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(xmlRelevantPartPruner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
