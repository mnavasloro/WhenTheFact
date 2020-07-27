/**
 * Class that manages TempEval3 files
 *  
 * See also
 * @ref FileTempEval3
 * 
 */
package oeg.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager of the corpus TempEval 3
 *
 * @author mnavas
 */
public class ManagerTempEval3 {

    String rootData = "C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\";
    File cleanDocs = new File(rootData + "datasets\\timeEval\\tempeval3\\test\\test-clean\\"); // Path to the folder with the input (clean) files
    File testDocs = new File(rootData + "datasets\\timeEval\\tempeval3\\test\\test\\"); // Path to the folder with the test files
    File outDocs = new File(rootData + "datasets\\timeEval\\tempeval3\\test\\outputevents\\"); // Path to the folder to store the output files

    public List<FileTempEval3> lista = new ArrayList<FileTempEval3>();

    /**
     * Constructor of ManagerTempEval3
     * 
     * @param fInput Path of the plain text files folder
     * @param fTest Path of the test folder
     * @param fOut Path of the output folder
     *
     */
    public ManagerTempEval3(String fInput, String fTest, String fOut) {
        cleanDocs = new File(fInput);
        testDocs = new File(fTest);
        outDocs = new File(fOut);

        for (File f : cleanDocs.listFiles()) {
            FileTempEval3 ft3 = new FileTempEval3(f, testDocs, outDocs);
            lista.add(ft3);
        }
    }

    /**
     * Constructor of ManagerTempEval3
     *
     */
    public ManagerTempEval3() {

        for (File f : cleanDocs.listFiles()) {
            FileTempEval3 ft3 = new FileTempEval3(f, testDocs, outDocs);
            lista.add(ft3);
        }
    }

}
