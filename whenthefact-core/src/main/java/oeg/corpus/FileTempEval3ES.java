/**
 * Class to process TempEval3 Spanish files
 * 
 */
package oeg.corpus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 * Class File of the Spanish corpus of TempEval3
 *
 * @author mnavas
 */
public class FileTempEval3ES {

    File testFile = null; // File to test against
    File inputFile = null; // Input file (cleaned of TimeML tags)
    File outputFile = null; // File to write the output

    String content = "";

    /**
     * Constructor of FileTempEval3ES
     * 
     * @param file Input file (cleaned of TimeML tags)
     * @param test File to test against
     * @param out File to write the output
     *
     */
    public FileTempEval3ES(File file, File test, File out) {
        inputFile = file;
        testFile = new File(test.getAbsolutePath() + file.getName());
        outputFile = new File(out.getAbsolutePath() + "\\" + file.getName());
    }

    /**
     * Returns the TEXT of the input file (cleaned of TimeML tags)
     *
     * @return the TEXT of the input file (cleaned of TimeML tags)
     */
    public String getTextInput() {
        try {
            String input = FileUtils.readFileToString(inputFile, "UTF-8");
            return getText(input);
        } catch (IOException ex) {
            Logger.getLogger(FileTempEval3ES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns the DTC (Document Creation Time) of the input file (cleaned of
     * TimeML tags)
     *
     * @return the DCT of the input file (cleaned of TimeML tags)
     */
    public String getDCTInput() {
        try {
            String input = FileUtils.readFileToString(inputFile, "UTF-8");
            return getDCT(input);
        } catch (IOException ex) {
            Logger.getLogger(FileTempEval3ES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns the TEXT of the test file
     *
     * @return the TEXT of the test file
     */
    public String getTextTest() {
        try {
            String input = FileUtils.readFileToString(testFile, "UTF-8");
            return getText(input);
        } catch (IOException ex) {
            Logger.getLogger(FileTempEval3ES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns the DTC (Document Creation Time) of the test file
     *
     * @return the DCT of the test file
     */
    public String getDCTTest() {
        try {
            String input = FileUtils.readFileToString(testFile, "UTF-8");
            return getDCT(input);
        } catch (IOException ex) {
            Logger.getLogger(FileTempEval3ES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns the TEXT of string
     *
     * @param input String read in the input file
     * 
     * @return the TEXT of a string
     */
    public String getText(String input) {
        String textRegex = "<TEXT>([^<]*?)<\\/TEXT>";
        Pattern pText = Pattern.compile(textRegex);
        Matcher mText = pText.matcher(input);
        if (mText.find()) {
            return mText.group(1);
        } else {
            return null;
        }
    }

    /**
     * Returns the DTC (Document Creation Time) of string input
     *
     * @param input String read in the input file
     * 
     * @return the DCT of a string
     */
    public String getDCT(String input) {
        String dctRegex = "<DCT><TIMEX3 tid=\"t0\" type=\"DATE\" value=\"(.*)\" temporalFunction.*<\\/TIMEX3><\\/DCT>";
        Pattern pDCT = Pattern.compile(dctRegex);
        String dctRegexSimple = "<DCT>(.*)<\\/DCT>";
        Pattern pDCTSimple = Pattern.compile(dctRegexSimple);
        Matcher mDCT = pDCT.matcher(input);
        Matcher mDCTSimple = pDCTSimple.matcher(input);
        if (mDCT.find()) {
            return mDCT.group(1);
        } else if (mDCTSimple.find()) {
            return mDCTSimple.group(1);
        } else {
            return null;
        }
    }

    /**
     * Writes an output file
     *
     * @param tagged Annotated String
     * 
     * @return boolean TRUE (ended correctly) or FALSE (Exception)
     */
    public boolean writeOutputFile(String tagged) {
        try {
            String input = FileUtils.readFileToString(inputFile, "UTF-8");
            input = input.replaceFirst("<TEXT>([^<]*?)<\\/TEXT>", "<TEXT>" + tagged + "<\\/TEXT>");
            FileOutputStream fos = new FileOutputStream(outputFile.getAbsolutePath());
            OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(w);
            bw.write(input);
            bw.flush();
            bw.close();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(FileTempEval3ES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
