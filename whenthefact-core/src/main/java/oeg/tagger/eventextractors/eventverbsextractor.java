package oeg.tagger.eventextractors;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.ling.tokensregex.types.Value;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeg.eventRepresentation.Frame;
import oeg.tagger.docHandler.Document;
import oeg.tagger.docHandler.DocumentPart;
import oeg.tagger.docHandler.StructureExtractorECHR;
import oeg.tagger.docHandler.XMLMerger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import static org.joda.time.format.ISODateTimeFormat.dateTime;
import org.slf4j.LoggerFactory;

/**
 * ExtractorTIMEXKeywordBased core class, where the rules are applied and the
 * normalization algorithm is.
 *
 * @author mnavas
 */
public class eventverbsextractor {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(eventverbsextractor.class);

//    PrintWriter out;
    String rules;
    Properties properties = new Properties();
    String posModel;
    String lemmaModel;
    StanfordCoreNLP pipeline;
    
    /* Variables for dependency */
    String modelPath = DependencyParser.DEFAULT_MODEL;
    String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
    MaxentTagger tagger = new MaxentTagger(taggerPath);
    DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

    Map<String, String> map = new HashMap<String, String>();

    String lang = "es";

    String iniSP = "-03-20";
    String iniSU = "-06-21";
    String iniFA = "-09-22";
    String iniWI = "-12-21";

    /**
     * Initializes a instance of the tagger
     *
     * @param lang language (ES - Spanish, EN - English)
     * @return an instance of the tagger
     */
    public eventverbsextractor() {
        init();
    }

    public eventverbsextractor(String language) {
        lang = language;
        init();
    }

    public eventverbsextractor(String pos, String lemma, String rul, String language) {
        posModel = pos;
        lemmaModel = lemma;
        rules = rul;
        lang = language;
        init();
    }

    public eventverbsextractor(String rul, String language) {
        rules = rul;
        lang = language;
        init();
    }
    

    

    public void init() {

        

//        out = new PrintWriter(System.out);
//            properties = StringUtils.argsToProperties(new String[]{"-props", "StanfordCoreNLP-spanish.properties"});
        properties = new Properties();

        properties.setProperty("annotators", "tokenize, ssplit, pos, lemma,ner");
//        properties.setProperty("annotators", "tokenize, ssplit, pos, lemma,ner,parse");
        properties.setProperty("ner.useSUTime", "false");

        properties.setProperty("tokenize.verbose", "false");
        properties.setProperty("TokensRegexNERAnnotator.verbose", "false");

        try {
            pipeline = new StanfordCoreNLP(properties);
        } catch (Exception ex) {
            System.out.println("Error: " + ex.toString());
        }

    }


    public HashMap<String,Frame> annotate(String input, ArrayList<String> types) {
//        ArrayList<String> keywords = new ArrayList<String>();
        HashMap<String,Frame> events = new HashMap<String,Frame>();
       
        try {
            String inp2 = input;
            String inp3 = input;
            int flagRN = 0;

            inp2 = inp2.replaceAll("\\r\\n", "\n");

            Annotation annotation = new Annotation(inp2);

            pipeline.annotate(annotation);


            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            int iterTypes = 0;
            for (CoreMap sentence : sentences) {
                    
                
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        /* We collect the different tags of each token */
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                        String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

                        /* Verb Frame */
//                        for (String k : keywords) {
                            if (pos.contains("V") && !pos.equalsIgnoreCase("VBG") && !lemma.equalsIgnoreCase("be")) {
                                
                                Frame ev = new Frame();
                                ev.core = lemma;
                                ev.typeEvent.add(types.get(iterTypes));
                                iterTypes++;
                                String deppar = dependencyParsing(sentence.toString());
//                                String contspar = constituencyParsing(sentence,word);
                                ev = checkFrame(deppar,word, ev);
                                if(events.containsKey(ev.core)){ 
                                    ev = mergeFrames(ev,events.get(ev.core));
                                }
                                
                                    ev.updatePer();
                                events.put(ev.core, ev);
                                break;
//            System.out.println("token: word=" + word + ",  \t lemma=" + lemma + ",  \t pos=" + pos + ",  \t ne=" + ne + ",  \t normalized=" + normalized);
                            }
//                        }
                    }
                }
                
                System.out.println(events + "\n-------");
            return events;

        } catch (Exception ex) {
            Logger.getLogger(eventverbsextractor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public boolean writeFile(String input, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(w);
            bw.write(input);
            bw.flush();
            bw.close();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(eventverbsextractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     *
     * @param text one sentence (if not, it will just return the result for the
     * first sentcene)
     * @return String with the dependency parsing
     */
    public String dependencyParsing(String text) {

        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
        for (List<HasWord> sentence : tokenizer) { 
            List<TaggedWord> tagged = tagger.tagSentence(sentence);
            GrammaticalStructure gs = parser.predict(tagged);
            return gs.typedDependenciesEnhancedPlusPlus().toString();
        }

        return "";
    }
    
    
        /**
     * We check the arguments of the purchase frame for an event 'sell'
     *
     * @param deppar: dependency parsing string
     * @param word: the event word
     * @param frame
     * @return the frame with new information (or not)
     */
    public Frame checkFrame(String deppar, String word, Frame frame) {

        if(frame == null){
            frame = new Frame();
        }
                
        List<String> x;
        /* We check if i is a passive */
        Pattern pText1 = Pattern.compile("nsubjpass\\(" + word +  "-\\d+, ([^-]+)-\\d+\\)");
        Matcher mText1 = pText1.matcher(deppar);
        if (mText1.find()){ // passive sentence
            frame.subj.add(mText1.group(1));
            Pattern pText2 = Pattern.compile("([a-zA-Z:]+)\\(" + word +  "-\\d+, [^-]+-\\d+\\)");
            Matcher mText2 = pText2.matcher(deppar);
             if (mText2.find()){
            String word2 = mText2.group(1);
               if(!word2.startsWith("nsubj")){
                 frame.passRels.add(mText2.group(1));  
               }
             }frame.obj.add("P");
        }
        
        
        
            
            
        
            
//            pText1 = Pattern.compile("nmod:to\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//            mText1 = pText1.matcher(deppar);        
//            if(mText1.find() && frame.getBuyer().isEmpty()){
//                // Passive sentence
//                frame.setBuyer(mText1.group(1));
//            }
//            return frame;
//        }
        
        /* We check the subject-seller */    
        else{
        pText1 = Pattern.compile("nsubj[^\\(]*\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
        mText1 = pText1.matcher(deppar);
        if (mText1.find()) {
            frame.subj.add(mText1.group(1));
            
        } else{
            frame.subj.add("");
        }
        Pattern pText2 = Pattern.compile("([a-zA-Z:]+)\\(" + word +  "-\\d+, [^-]+-\\d+\\)");
            Matcher mText2 = pText2.matcher(deppar);
            while (mText2.find()){ // passive sentence
                String word2 = mText2.group(1);
               if(!word2.equalsIgnoreCase("dobj") && !word2.startsWith("nsubj")){
                 frame.actRels.add(mText2.group(1));  
               }
               
            }
        /* We check the to-buyer */         
//        pText1 = Pattern.compile("nmod:to\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
//        if (mText1.find() && frame.getBuyer().isEmpty()) {
//            frame.setBuyer(mText1.group(1));
//        }
        
        /* We check the item, that can be an nmod:of or a dobj */         
//        pText1 = Pattern.compile("nmod:of\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
//        if (mText1.find()) {
//            String word2 = mText1.group(1);
//            Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 +  "-\\d+, ([^-]+)-\\d+\\)");
//            Matcher mText2 = pText2.matcher(deppar);
//            while (mText2.find()){ // passive sentence
//               x = frame.Who.arrayEl;
//               x.add(word2);
//               frame.Who.arrayEl = x;
//            }
//        }
        
        pText1 = Pattern.compile("dobj\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
        mText1 = pText1.matcher(deppar);
        if (mText1.find()) {
               frame.obj.add(mText1.group(1));
            
        } else{
            frame.obj.add("");
        }
        
        /* We check the contract, that can be an nmod:by; it can also be checked later the contract mentions in the sentence */         
//        pText1 = Pattern.compile("nmod:by\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
//        if (mText1.find() && frame.getContract().isEmpty()) {
//            frame.setContract(mText1.group(1));
//        }
        
        /* Date and price will be done later */
        
    }
        
        
            
            
        
        return frame;

}
    
    
    /**
     *
     * @param sentence with information about the tree
     * @param word
     * @return String with the parsing tree
     */
    public String constituencyParsing(CoreMap sentence, String word) {

        System.out.println("\n\n----------------\n");
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        
        Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());
    for (Constituent constituent : treeConstituents) {
      if (constituent.label() != null &&
          (constituent.label().toString().equals("VP")) && tree.getLeaves().get(constituent.start()).toString().equalsIgnoreCase(word)) {
        System.err.println("found constituent: "+constituent.toString());
        System.err.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
      }
    }
        
        
        
        
        
        
        System.out.println(tree);
        System.out.println("\n\n----------------\n");
        return tree.toString();
    }

    private Frame mergeFrames(Frame ev, Frame get) {
        for(String e : ev.obj){
            
                get.obj.add(e);
            
        }
        
        for(String e : ev.subj){
            
                get.subj.add(e);
            
        }
        
        for(String e : ev.passRels){
            if(!get.passRels.contains(e)){
                get.passRels.add(e);
            }
        }
        
        for(String e : ev.actRels){
            if(!get.actRels.contains(e)){
                get.actRels.add(e);
            }
        }
        
        for(String e : ev.typeEvent){
            
                get.typeEvent.add(e);
            
        }
        
        return get;
    }
}