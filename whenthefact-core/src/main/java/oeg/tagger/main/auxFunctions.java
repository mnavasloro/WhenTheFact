package oeg.tagger.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Class to run experiments and test the software. 
 * @author Maria
 */
public class auxFunctions {

    /**
     * @param args the command line arguments
     */

    static public boolean writeFile(String input, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(w);
            bw.write(input);
//            bw.write("\uFEFF");
            bw.flush();
            bw.close();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(auxFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
