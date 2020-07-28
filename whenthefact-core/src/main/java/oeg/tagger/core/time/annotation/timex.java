package oeg.tagger.core.time.annotation;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;

/**
 * Annotation fields for class TIMEX
 *
 * @author mnavas
 */
public class timex {

    private timex() {
    }

    public static class MyTimexAnnotation implements CoreAnnotation<List<? extends CoreMap>> {

        @Override
        public Class<List<? extends CoreMap>> getType() {
            return ErasureUtils.<Class<List<? extends CoreMap>>>uncheckedCast(List.class);
        }
    }

    public static class Value implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class Type implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class Freq implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class Quant implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class Mod implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

}
