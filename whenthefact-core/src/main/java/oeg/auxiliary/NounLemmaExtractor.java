package oeg.auxiliary;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import oeg.tagger.main.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeg.corpus.FileTempEval3ES;
import oeg.corpus.ManagerTempEval3ES;
import oeg.tagger.eventextractors.ExtractorTIMEXPOSES;

/**
 *
 * @author
 */
public class NounLemmaExtractor {

    public static void main(String[] args) {
        System.out.println("Noun Lemma Extractor");
        Set<String> listEVENTS = new HashSet<String>();
        Set<String> listEVENTSlemma = new HashSet<String>();
        
        String aux = ("{");
        // Read train
        try {
            ManagerTempEval3ES mte3 = new ManagerTempEval3ES("C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\datasets\\timeEval\\tempeval3ES\\test\\train-clean\\", "C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\datasets\\timeEval\\tempeval3ES\\test\\train\\", "C:\\Users\\mnavas\\CODE\\OLD_CODE\\data\\datasets\\timeEval\\tempeval3ES\\test\\trainoutputevents\\");
            List<FileTempEval3ES> list = mte3.lista;
            
            for (FileTempEval3ES f : list) {
                
                String input = f.getTextTest();
                
            String textRegex = "<EVENT [^>]*>([^<]*)<\\/EVENT>";
            Pattern pText = Pattern.compile(textRegex);
            Matcher mText = pText.matcher(input);
            while (mText.find()) {
                listEVENTS.add(mText.group(1));
            }
            }    
            System.out.println(listEVENTS);
            
            
            Properties properties = StringUtils.argsToProperties(new String[]{"-props", "StanfordCoreNLP-spanish.properties"});
            String posModel = "./src/main/resources/ixa-pipes/morph-models-1.5.0/es/es-pos-perceptron-autodict01-ancora-2.0.bin";
            
            String lemmaModel = "./src/main/resources/ixa-pipes/morph-models-1.5.0/es/es-lemma-perceptron-ancora-2.0.bin";
            

            properties.setProperty("annotators", "tokenize,ssplit,spanish,readability");
            properties.setProperty("spanish.posModel", posModel);
            properties.setProperty("spanish.lemmaModel", lemmaModel);
            properties.setProperty("readability.language", "es");

            properties.setProperty("customAnnotatorClass.spanish", "oeg.tagger.core.time.aidCoreNLP.BasicAnnotator");
            properties.setProperty("customAnnotatorClass.readability", "eu.fbk.dh.tint.readability.ReadabilityAnnotator");

            properties.setProperty("customAnnotatorClass.tokensregexdemo", "edu.stanford.nlp.pipeline.TokensRegexAnnotator");
            properties.setProperty("tokenize.verbose", "false");
            properties.setProperty("TokensRegexNERAnnotator.verbose", "false");
            
            StanfordCoreNLP pipeline;
        try {
            pipeline = new StanfordCoreNLP(properties);
            
            for(String e : listEVENTS){
                Annotation annotation = new Annotation(e);
                pipeline.annotate(annotation);
                
                List<CoreMap> sentencesAll = annotation.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence : sentencesAll) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        /* We collect the different tags of each token */
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        
                        
                        /* Verb Event */
                        if (!pos.startsWith("Y") && !pos.startsWith("M") && !pos.startsWith("S") && !pos.startsWith("D") && !pos.startsWith("F") && !pos.startsWith("VM") && !pos.startsWith("VS") && !pos.matches("AQ.+P") ){// && !pos.equalsIgnoreCase("VBG")) {
                            listEVENTSlemma.add(lemma);
                            aux += "\"" + lemma + "\", ";
                        }
                        
                }
                }
                
            }
            
//            System.out.println(listEVENTSlemma);
            System.out.print(aux.substring(0, aux.length()-2) + "}");
            
        } catch (Exception ex) {
            System.out.println("Error: " + ex.toString());
        }
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        
        // Coger lemas dentro de eventos que sean N
        
        
        

    }
}
