package oeg.tagger.core.time.annotation;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;

/**
 * Annotations fields for class temporal
 *
 * @author mnavas
 */
public class temporal {

    private temporal() {
    }

    public static class MyTokensAnnotation implements CoreAnnotation<List<? extends CoreMap>> {

        @Override
        public Class<List<? extends CoreMap>> getType() {
            return ErasureUtils.<Class<List<? extends CoreMap>>>uncheckedCast(List.class);
        }
    }

    public static class MyTemporalAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyNormalizedTemporalAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyTypeTemporalAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyRuleAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(String.class);
        }
    }

    public static class MyValueAnnotation implements CoreAnnotation<Number> {

        @Override
        public Class<Number> getType() {
            return ErasureUtils.<Class<Number>>uncheckedCast(Number.class);
        }
    }

    public static class MyStringValueAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return ErasureUtils.<Class<String>>uncheckedCast(Number.class);
        }
    }

}
