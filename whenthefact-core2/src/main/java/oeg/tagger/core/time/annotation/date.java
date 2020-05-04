package oeg.tagger.core.time.annotation;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;

/**
 * Annotations fields for class DATE
 *
 * @author mnavas
 */
public class date {

    private date() {
    }

    public static class MyTokensAnnotation implements CoreAnnotation<List<? extends CoreMap>> {

        @Override
        public Class<List<? extends CoreMap>> getType() {
            return ErasureUtils.<Class<List<? extends CoreMap>>>uncheckedCast(List.class);
        }
    }

    public static class MyDay implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyMonth implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyYear implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyDayWeek implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyWeek implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

}
