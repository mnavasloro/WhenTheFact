package oeg.tagger.core.time.aidCoreNLP;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import oeg.tagger.core.time.annotation.myNER;
import oeg.tagger.core.time.annotation.timex;

/**
 * Test for injecting an Spanish tagger in CoreNLP Adapted from
 * https://github.com/dhfbk/spanish
 *
 * @author mnavas
 */
public class testAid {

    public static void main(String[] args) {

        String text = "La sonda Juno de la NASA, la nave impulsada por energía solar que ha viajado más lejos en el espacio, ha llegado este martes tras cinco años de viaje a la órbita de Júpiter, el planeta más grande del Sistema Solar, a la que dará 37 vueltas antes de estrellarse contra su superficie. El expresidente del Gobierno asegura que el secretario general del PSOE debería dimitir si recibe un revés del comité federal.";
        text = "El 50,2% decidió votar en contra del acuerdo de paz entre el Gobierno y las FARC, por el 49.7% que se decantó por el ‘sí’. La abstención, de más del 60%, y la pésima imagen de la guerrilla han sido determinantes en el resultado de la votación, que ninguna encuesta supo predecir. Evitar que continúe el conflicto armado, que ha atravesado el país durante más de 50 años y ha dejado ocho millones de víctimas, es el primer desafío. El presidente, Juan Manuel Santos, ha asegurado que el cese bilateral del fuego seguirá vigente. Colombia se adentra, no obstante, en un limbo plagado de incertidumbre. Nadie sabe con exactitud qué va a ocurrir a partir de ahora.";
        String posModel = "./src/main/resources/ixa-pipes/morph-models-1.5.0/es/es-pos-perceptron-autodict01-ancora-2.0.bin";
        String lemmaModel = "./src/main/resources/ixa-pipes/morph-models-1.5.0/es/es-lemma-perceptron-ancora-2.0.bin";
        String parseModel = "./src/main/resources/ixa-pipes/morph-models-1.5.0/es/es-parser-chunking.bin";

        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,spanish,readability,ner,tokensregexdemo");
//        properties.setProperty("annotators", "spanish, readability");

        properties.setProperty("spanish.posModel", posModel);
        properties.setProperty("spanish.lemmaModel", lemmaModel);
//        properties.setProperty("spanish.parserModel", parseModel);
        properties.setProperty("readability.language", "es");

        properties.setProperty("customAnnotatorClass.spanish", "oeg.tagger.core.time.aidCoreNLP.BasicAnnotator");
        properties.setProperty("customAnnotatorClass.readability", "eu.fbk.dh.tint.readability.ReadabilityAnnotator");

        properties.setProperty("customAnnotatorClass.tokensregexdemo", "edu.stanford.nlp.pipeline.TokensRegexAnnotator");
            properties.setProperty("tokensregexdemo.rules", "./src/main/resources/rules/rulesES-legal.txt");
            properties.setProperty("tokenize.verbose", "false");
            properties.setProperty("TokensRegexNERAnnotator.verbose", "false");
            
        StanfordCoreNLP corenlp = new StanfordCoreNLP(properties);
        Annotation annotation = new Annotation(text);
        corenlp.annotate(annotation);

//        try {
//            String json = JSONOutputter.jsonPrint(annotation);
//            System.out.println(json);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        PrintWriter out = new PrintWriter(System.out);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

//        Tree tree = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
//    System.out.println("--------\n" + tree + "\n--------\n");
        for (CoreMap sentence : sentences) {
            // NOTE: Depending on what tokensregex rules are specified, there are other annotations
            //       that are of interest other than just the tokens and what we print out here
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Print out words, lemma, ne, and normalized ne
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                String myNe = token.get(myNER.MyNamedEntityTagAnnotation.class);
                String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                String myNeNormalized = token.get(myNER.MyNormalizedNamedEntityTagAnnotation.class);

//        String tnormalized = token.get(temporal.MyNormalizedTemporalAnnotation.class);
//        String myTeNormalized = token.get(temporal.MyTemporalAnnotation.class);
//        Double myTeValue = token.get(temporal.MyValueAnnotation.class);
//        String myTeType = token.get(temporal.MyTypeTemporalAnnotation.class);
                String value = token.get(timex.Value.class);
                String type = token.get(timex.Type.class);
//        out.println("token: " + "word="+word + ", myNe=" + myNe + ", myNenormalized=" + myNeNormalized + "\t\t" + token.value());
//        out.println("token: " + "TIMEX=(" + type + ", " + value + ") \t word=" + word + ", ne=" + ne + ", normalized=" + normalized + ", myNe=" + myNe + ", myNenormalized=" + myNeNormalized + "\t myT=" + tnormalized + "\t  myTnormalized=" + myTeNormalized + "\t  myTValue=" + myTeValue + "\t  myTType=" + myTeType);
                out.println("token: " + "word=" + word + ", lemma=" + lemma + ", pos=" + pos + ", ne=" + ne + ", normalized=" + normalized + ", myNe=" + myNe + ", myNenormalized=" + myNeNormalized);
            }
        }
        out.flush();
    }

}
