package oeg.tagger.core.time.aidCoreNLP;

import eus.ixa.ixa.pipe.pos.Annotate;
import java.io.IOException;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Adapted from https://github.com/dhfbk/spanish, code for injecting an Spanish tagger in CoreNLP (License GPLv3)
 *
 */
public class SpanishModel {
    
private static final Logger LOGGER = Logger.getLogger(SpanishModel.class.getName());

    private static SpanishModel ourInstance = null;

    private Annotate posAnnotator;

    public SpanishModel(Annotate posAnnotator) {
        this.posAnnotator = posAnnotator;
    }

    public Annotate getPosAnnotator() {
        return posAnnotator;
    }

    public void setPosAnnotator(Annotate posAnnotator) {
        this.posAnnotator = posAnnotator;
    }

    public static SpanishModel getInstance(Properties properties) {
        if (ourInstance == null) {

            final Properties posProperties = new Properties();
            posProperties.setProperty("model", properties.getProperty("posModel"));
            posProperties.setProperty("lemmatizerModel", properties.getProperty("lemmaModel"));
            posProperties.setProperty("language", "es");
            posProperties.setProperty("multiwords", "no");
            posProperties.setProperty("dictag", "false");

            try {
                ourInstance = new SpanishModel(new Annotate(posProperties));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return ourInstance;
    }
}
