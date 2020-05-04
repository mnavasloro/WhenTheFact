package oeg.tagger.core.time.annotation;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

/**
 * Annotation fields for class myNER
 *
 * @author mnavas
 */
public class myNER {

    private myNER() {
    }

    public static class MyNamedEntityTagAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyNormalizedNamedEntityTagAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyValueAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyNumTagAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }
}
