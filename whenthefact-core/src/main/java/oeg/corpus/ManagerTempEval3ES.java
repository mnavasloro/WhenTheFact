/**
 * Class that manages TempEval3ES files
 *  
 * See also
 * @ref FileTempEval3ES
 * 
 */
package oeg.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager of the Spanish corpus TempEval 3
 *
 * @author mnavas
 */
public class ManagerTempEval3ES {

    String rootData = "C:/Users/mnavas/CODE/OLD_CODE/data";
    File cleanDocs = new File(rootData + "/datasets/timeEval/tempeval3ES/test/test-clean/"); // Path to the folder with the input (clean) files
    File testDocs = new File(rootData + "/datasets/timeEval/tempeval3ES/test/test/"); // Path to the folder with the test files
    File outDocs = new File(rootData + "/datasets/timeEval/tempeval3ES/test/outputevents/"); // Path to the folder to store the output files

    public List<FileTempEval3ES> lista = new ArrayList<FileTempEval3ES>();

    /**
     * Constructor of ManagerTempEval3ES
     * 
     * @param fInput Path of the plain text files folder
     * @param fTest Path of the test folder
     * @param fOut Path of the output folder
     *
     */
    public ManagerTempEval3ES(String fInput, String fTest, String fOut) {
        cleanDocs = new File(fInput);
        testDocs = new File(fTest);
        outDocs = new File(fOut);

        for (File f : cleanDocs.listFiles()) {
            FileTempEval3ES ft3 = new FileTempEval3ES(f, testDocs, outDocs);
            lista.add(ft3);
        }
    }

    /**
     * Constructor of ManagerTempEval3ES
     *
     */
    public ManagerTempEval3ES() {

        for (File f : cleanDocs.listFiles()) {
            FileTempEval3ES ft3 = new FileTempEval3ES(f, testDocs, outDocs);
            lista.add(ft3);
        }
    }

}
