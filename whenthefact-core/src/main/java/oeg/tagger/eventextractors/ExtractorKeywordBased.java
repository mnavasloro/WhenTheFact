package oeg.tagger.eventextractors;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeg.tagger.docHandler.Document;
import oeg.tagger.docHandler.DocumentPart;
import oeg.tagger.docHandler.StructureExtractorECHR;
import org.slf4j.LoggerFactory;

/**
 * Class with the methods to annotate English texts referring to contracts. 
 * The output of this method is:
 * - A set of frames derived from the text
 * - PROLEG expressions
 * 
 * @author mnavas
 */
public class ExtractorKeywordBased {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExtractorKeywordBased.class);

    static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ExtractorKeywordBased.class);
    
    StanfordCoreNLP pipeline;
    PrintWriter out;

    Map<String, String> corefsubs = new HashMap<String, String>();

    /* Variables for dependency */
//    String modelPath = DependencyParser.DEFAULT_MODEL;
    String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
    MaxentTagger tagger = new MaxentTagger(taggerPath);
//    DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

    /* Variables for preprocessing */
    StanfordCoreNLP preprocessPipeline;
    
    /* Variables for logicoutput */
    Map<String,String> refItem = new HashMap<String,String>();
    List<String> logical = new ArrayList<String>();
    
    
    public int numindex = 1;

    /**
     * Initializes a instance of the tagger
     *
     */
    public ExtractorKeywordBased() {
        init();
    }

    /**
     * Initialization routines.
     * Initializes Stanford Core NLP library.
     * Sets up a pipeline.
     */
    public void init() {

        /* Path to the file with the rules */
//        String rules = "../ContractFrames/src/main/resources/rules/rulesENframesContract.txt";

        out = new PrintWriter(System.out);

        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma");//,ner,parse");//,tokensregexdemo");
//        properties.setProperty("customAnnotatorClass.tokensregexdemo", "edu.stanford.nlp.pipeline.TokensRegexAnnotator");
//        properties.setProperty("tokensregexdemo.rules", rules);
        logger.info(properties);

        /* Initialization of CoreNLP pipeline */
        pipeline = new StanfordCoreNLP(properties);

        /* Initialization of the preprocessing pipeline */
//        Properties propertiesPreproc = new Properties();
//        propertiesPreproc.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
//        preprocessPipeline = new StanfordCoreNLP(propertiesPreproc);
    }

    
//    public String annotate(String text) {
//        return annotate(text, "proleg");
//    }

    
    //TODO: change output to string, so it returns the annotated string
    /**
     * Annotates a String text
     *
     * @param text
     *
     * EXAMPLES OF INPUT text: "The contract entered into force yesterday. The
     * establishment of the purchase contract was done the day after."
     *
     * "The contract entered into force yesterday."
     *
     * "A established a purchase contract with B to buy a land, L on April 1,
     * 2018. The establishment of the contract wasn't done the next day. A
     * rescinded the purchase contract by a fraud by the thrid party C on May 1,
     * 2018."
     *
     * "A established a purchase contract with B to buy a land, L on April"
     * @return Annotated String in PROLEG
     *
     */
    public String annotate(String text, File wordfile, String filename) {

        ArrayList<String> keywords = new ArrayList<String>();
        keywords.addAll(Arrays.asList("lodge" , "appeal" , "reject" , "issue" , "uphold" , "convict" , //"inform" , 
                "order" , "apply" , "institute" , "refuse" , "quash" , "submit" , 
                //"find" , 
                "grant" , "reply" , "amend" , "authorise" , "conclude" , 
//"free" , 
                "liberate" , "release" , "unloose" , "unloosen" , "loose" , "report" , "accept" , "collar" , "nail" , "apprehend" , "arrest" , "pick up" , "nab" , "cop" , "complain" , "declare" , "defer" , "render" , "deliver" , "return" , "confine" , "detain" , "discontinue" , "extend" , "hear" , "hold" , //"judgment" , 
                "notify" , "object" , "oppose" , "receive" , "request" , "rule" , "sentence" , "condemn" , "doom" , "stay" , "summon" , "cite" , "suspend" , "take into custody" , "adopt" , "amendment" , "annul" , "attempt" , "claim" , "consider" , "consult" , "contend" , "contest" , "demand" , "due" , "file" , "register" , "forward" , //"give" , 
                "instigate" , "invalidate" , "invit" , "invite" , "keep" , //"make" , 
                "notice" , "overthrow" , "subvert" , "overturn" , "bring down" , "pass" , "place" , "protest" , //"put" , 
                "question" , "recruit" , "resume" , "revoke" , "seek" , "serve" , "process" , "swear out" , //"sit" , "take" , 
                "win" , "witness", "write", "marry", "ask", "start", "born", "carry"));
        ArrayList<String> keywordsN = new ArrayList<String>();
        keywordsN.addAll(Arrays.asList("application" , "request" , "claim" , "action" , "appeal" , "complain" , "objection" , "complaint" , //"case" , "law" , "initiate charges" , 
                "comment" , "opinion" , "decision" , "permit" , "notice" , "argument" , "order" , //"judgment" , "pay" , "expulsion" , "reduction" , "arrest" , "release" , "hearing" , 
                "authorisation" , "proceedings" , "grant" , "leave" , "guilty" , "file" , "claims" , "visit" , "detention" , "lawful" , "counterclaim" , "detained" , "evidence" , "imprisonment" , "lawyer" , "deport" , "arguments" , "confirmation" , "attention" , "agreement" , "amendments" , "enforcement" , "examination"));
        
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        String output = text;
        int offset = 0;
        int offsetEvent = "<Event argument=\"core\">".length() + "</Event>".length();
        out.println();
        out.println(annotation.toShorterString());
        
        StructureExtractorECHR seECHR = new StructureExtractorECHR();
        Document doc = seECHR.extractFromDocument(wordfile);
        List<DocumentPart> eventRelevantParts = doc.getEventRelevantParts();
        
        List<CoreMap> sentencesAll = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        List<CoreMap> sentences = new ArrayList<CoreMap>();
        if(eventRelevantParts.isEmpty()){
            sentences = sentencesAll;
        } else{
            for (CoreMap sentence : sentencesAll) {
                List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                Integer beginSentence = toks.get(0).beginPosition();
                 for(DocumentPart erp : eventRelevantParts){
                     if(beginSentence >= erp.offset_ini && beginSentence < erp.offset_end){
                         sentences.add(sentence);
                         break;
                     }
                 }
            }
        }
           
        
        for (CoreMap sentence : sentences) {
            // DEPENDENCY
            //String deppar = dependencyParsing(sentence.toString());
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                /* We collect the different tags of each token */
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
//                String contractEvent = token.get(contractEvents.ContractEventTagAnnotation.class);
//                String date = token.get(contractEvents.DateTagAnnotation.class);
//                Timex timex = token.get(TimeAnnotations.TimexAnnotation.class);
//                if (contractEvent != null) {
                    /* Verb Event */
                    int flag=0;
                    for(String k : keywords){
                    if (lemma.contains(k) && pos.contains("V") && !pos.equalsIgnoreCase("VBG")){                         
                        output = output.substring(0, offset + token.beginPosition()) + "<Event argument=\"core\">" + output.substring(offset + token.beginPosition(), offset + token.endPosition()) + "</Event>" + output.substring(offset + token.endPosition());
                        offset = offset + offsetEvent;
//                        flag = 1;
                        break;
//            System.out.println("token: word=" + word + ",  \t lemma=" + lemma + ",  \t pos=" + pos + ",  \t ne=" + ne + ",  \t normalized=" + normalized);
                    } }                    
//                    if(flag==0){
//                    for(String k : keywordsN){ if (lemma.contains(k) && pos.contains("NN")){                         
//                        output = output.substring(0, offset + token.beginPosition()) + "<Event argument=\"core\">" + output.substring(offset + token.beginPosition(), offset + token.endPosition()) + "</Event>" + output.substring(offset + token.endPosition());
//                        offset = offset + offsetEvent;
//                        break;
////            System.out.println("token: word=" + word + ",  \t lemma=" + lemma + ",  \t pos=" + pos + ",  \t ne=" + ne + ",  \t normalized=" + normalized);
// 
//                    }} flag=0;
//           }
             }
        }
        out.flush();        
        output = output.replaceAll(" , ", ", ");
        output = output.replaceAll("THE COURT, UNANIMOUSLY", "<Event argument=\"core\">THE COURT, UNANIMOUSLY</Event>");


        /* We create a nlp.xml file as output, with the tags per token and parse trees/constituents */
        try {
            FileOutputStream os = new FileOutputStream(new File("nlp" + numindex + ".xml"));
            pipeline.xmlPrint(annotation, os);
        } catch (Exception ex) {
            Logger.getLogger(ExtractorKeywordBased.class.getName()).log(Level.SEVERE, null, ex);
        }
 numindex++;
        
        if (!writeFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<TEXT>" + output + "</TEXT>", filename)) {//"outputOR" + numindex + ".xml")) {
            logger.error("ERROR WHILE SAVING AS INLINE IN outputOR" + numindex + ".xml");
        }

        return output;
    }

    /**
     *
     * @param text one sentence (if not, it will just return the result for the
     * first sentcene)
     * @return String with the dependency parsing
     */
//    public String dependencyParsing(String text) {
//
//        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
//        for (List<HasWord> sentence : tokenizer) { 
//            List<TaggedWord> tagged = tagger.tagSentence(sentence);
//            GrammaticalStructure gs = parser.predict(tagged);
//            return gs.typedDependenciesEnhancedPlusPlus().toString();
//        }
//
//        return "";
//    }

    /**
     *
     * @param text input text
     * @param annotation with information about the tree
     * @return String with the parsing tree
     */
    public String constituencyParsing(String text, Annotation annotation) {

        System.out.println("\n\n----------------\n");
        Tree tree2 = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
        System.out.println(tree2);
        System.out.println("\n\n----------------\n");
        return tree2.toString();
    }

    /**
     *
     * @param constituency the contituency tree of the string to annotate
     * @param output the string to annotate
     * @return String with the output annotated
     */
    public String constituencyContract(String constituency, String output) {

        Pattern pContract = Pattern.compile("\\(NP ((\\([^)]+\\) )*\\(NN contract\\)( \\([^)]+\\))*)\\)");
        Matcher mText = pContract.matcher(constituency);
        while (mText.find()) {
            String outp = mText.group(1);
            while (outp.contains(")")) {
                outp = outp.replaceAll("\\(\\w+ (\\w+)\\)", "$1");
            }
            output = output.replaceFirst(outp, "<CONTRACT>" + outp + "</CONTRACT>");
        }
        return output;
    }

    /**
     *
     * @param text text to mark up mentions to contracts
     * @param annotation with information about coref
     * @return String with the mentions to contracts marked up
     */
    public String corefParsing(String text, Annotation annotation) {
        String output = text;

        int ids = 0;
        for (CorefChain cc : annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            String current = "";
            for (CorefChain.CorefMention aux : cc.getMentionsInTextualOrder()) {
                String[] out = preprocessACoref(aux.mentionSpan, output, ids, current);
                output = out[0];
                if (!out[1].equals("") && !current.equals(out[1])) {
                    current = out[1];
                }
            }

        }

        System.out.println("---");
        System.out.println("coref chains");
        for (CorefChain cc : annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            System.out.println("\t" + cc);
        }
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("---");
            System.out.println("mentions");
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                System.out.println("\t" + m);
            }
        }

        return output;
    }

    /**
     *
     * @param text text to mark up mentions to contracts
     * @param annotation with information about coref
     * @return String with the mentions to contracts marked up
     */

    /**
     * We check if there are any negations in the scope of the word (an event)
     *
     * @param deppar: dependency parsing string
     * @param word: the word to which the
     * @param te: temporal expression
     * @return boolean true: if the event is negated false: if there is no
     * negation in the scope
     */
    public String checkRelationEventTE(String deppar, String word, String te) {

        String value = "";
        String inTimex = "";

        Pattern pText1 = Pattern.compile("<TIMEX3[^>]* value=\"([^\"]*)\"[^>]*>([^<]+)<");
        Matcher mText1 = pText1.matcher(te);
        if (mText1.find()) {
            value = mText1.group(1);
            inTimex = mText1.group(2);
        } else {
            System.out.println("NO VALUE FOUND FOR THE TIMEX");
            return "";
        }

        //TOKENIZE WORDS AND KEEP THE VALUE
        Annotation annotation = new Annotation(inTimex);
        preprocessPipeline.annotate(annotation);
        CoreMap firstSentence = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        List<String> listTE = new ArrayList<String>();
        // this for loop will print out all of the tokens and the character offset info
        for (CoreLabel token : firstSentence.get(CoreAnnotations.TokensAnnotation.class)) {
            listTE.add(token.word());
        }

        /* There are two possible events:
            - A verb event (we therefore look for the pattern nmod(word-\d, te-\d)
                - Sometimes with :tmod, :on, or advmod
            - An event with a passive verb [we look for neg(X, Y) ... nsubjpass(X, word-\d)
         */
        String patternNMOD = "nmod(:tmod|:on)?\\(([^-]+-\\d+), ([^-]+-\\d+)\\)"; // For NounEvents
        String patternADVMOD = "advmod(:\\w+)?\\(([^-]+-\\d+), ([^-]+-\\d+)\\)"; // For NounEvents

        String patternNounEvents21 = "nsubjpass\\(";
        String patternNounEvents22 = ", ";

        for (String tePart : listTE) {
            Pattern pText = Pattern.compile(patternNMOD);
            Matcher mText = pText.matcher(deppar);
            while (mText.find()) {
                String g1 = mText.group(2);
                String g2 = mText.group(3);
                if (g1.contains(word) & g2.contains(tePart)) { // the event is a passive-negated noun, therefore negated
                    return value;
                } else if (!g1.contains(word) & g2.contains(tePart)) { // the event is a passive-negated noun, therefore negated
                    String patText = patternNounEvents21 + word + "-\\d+" + patternNounEvents22 + g2 + "\\)";
                    Pattern pText2 = Pattern.compile(patText);
                    Matcher mText2 = pText2.matcher(deppar);
                    if (mText2.find()) {
                        return value;
                    }

                }
            }

            Pattern pText3 = Pattern.compile(patternADVMOD);
            Matcher mText3 = pText3.matcher(deppar);
            while (mText.find()) {
                String g1 = mText3.group(3);
                String g2 = mText3.group(3);
                if (g1.contains(word) & g2.contains(tePart)) { // the event is a passive-negated noun, therefore negated
                    return value;
                } else if (!g1.contains(word) & g2.contains(tePart)) { // the event is a passive-negated noun, therefore negated
                    String patText = patternNounEvents21 + word + "-\\d+" + patternNounEvents22 + g2 + "\\)";
                    Pattern pText2 = Pattern.compile(patText);
                    Matcher mText2 = pText2.matcher(deppar);
                    if (mText2.find()) {
                        return value;
                    }

                }
            }
        }
        return "";
    }

    /**
     * We check if there are any negations in the scope of the word (an event)
     *
     * @param deppar: dependency parsing string
     * @param word: the word whose scope may include negation
     * @return boolean true: if the event is negated false: if there is no
     * negation in the scope
     */
    public boolean checkNeg(String deppar, String word) {
        /* There are two possible events:
            - A negated verb event (we therefore look for the pattern neg(word-\d, X)
            - An event negated by a passive verb [we look for neg(X, Y) ... nsubjpass(X, word-\d)
         */
        String patternNounEvents1 = "neg\\(([^-]+-\\d+), ([^-]+-\\d+)\\)";
        //"nsubjpass\\("+g1+", ("+word+"-\\d+)\\)";
        String patternNounEvents21 = "nsubjpass(";
        String patternNounEvents22 = ", ";

        if (deppar.contains("neg(")) { //if there is any negation, we check
            Pattern pText = Pattern.compile(patternNounEvents1);
            Matcher mText = pText.matcher(deppar);
            while (mText.find()) {
                String g1 = mText.group(1);
//                System.out.println(patternNounEvents21 + g1 + patternNounEvents22);
                if (g1.contains(word)) { // the event is a negated verb, therefore negated
                    return true;
                } else if (deppar.contains(patternNounEvents21 + g1 + patternNounEvents22)) { // the event is a passive-negated noun, therefore negated
                    return true;
                }
            }

        }
        return false;
    }
    
    
    /**
     * We check if there are the event is subordinated (xcomp), such as in "would like to X", "decide to X"
     * We check if there are the event is a hypothesis, having WOULD as aux "In that case, Part A would X"
     *
     * @param deppar: dependency parsing string
     * @param word: the word whose scope may include possibility
     * @return boolean true: if the event didn't happen false: if it did
     */
    public boolean checkXCOMP(String deppar, String word) {
        /* If the verb is subordinated, we don't care about it */
        String patternXCOMP = "xcomp\\(([^-]+-\\d+), (" + word + "-\\d+)\\)";
        Pattern pText = Pattern.compile(patternXCOMP);
        Matcher mText = pText.matcher(deppar);
        if (mText.find()) {
            return true;
        }
        
        /* If the verb is has a would, we don't care about it */
        String patternAUX = "aux\\((would-\\d+), (" + word + "-\\d+)\\)";
        pText = Pattern.compile(patternAUX);
        mText = pText.matcher(deppar);
        if (mText.find()) {
            return true;
        }

        return false;
    }
    
    

    public String preprocessInput(String input) {
        String output = input;

        /* We check for using a capital letter for parties, e.g. "A buys a land L to B" */
 /* We check the isolated letters; we change them:
            - If they are capital and not an A,        
            - If they are capital A without being after a point,
            - If it is an A before a verb that is not after a noun */
 
        /* We check for the pattern 'personA' " */
        Pattern pText = Pattern.compile("'?[L|l]and ([A-Z])'?");
        Matcher mText = pText.matcher(output);
        StringBuffer sb = new StringBuffer(output.length());
        while (mText.find()) {
            output = output.replaceFirst(mText.group(0), "Land" + mText.group(1));
            refItem.put("Land" + mText.group(1), mText.group(1));
        }
        
        /* We check for the pattern 'personA' " */
        pText = Pattern.compile("'person[ ]?([A-Z])'");
        mText = pText.matcher(output);
        while (mText.find()) {
            output = output.replaceAll(mText.group(0), "person" + mText.group(1));
//            refItem.put("Person" + mText.group(0), mText.group(1));
        }
        
        pText = Pattern.compile("\\b([B-Z])\\b");
        mText = pText.matcher(output);
        while (mText.find()) {
            mText.appendReplacement(sb,"Part" + mText.group(1));
            refItem.put("Part" + mText.group(1), mText.group(1));
        }
        mText.appendTail(sb);
        output = sb.toString();
        
        pText = Pattern.compile(".?.?\\b(A)\\b");
        mText = pText.matcher(output);
        sb = new StringBuffer(output.length());
        boolean flagDoubtA = false;
        while (mText.find()) {
            String g1 = mText.group(0);
            if (g1.length() < 2 || g1.startsWith(".")) {
                /* The begining of a paragraph or a sentence, we should check it is not a DT */
                flagDoubtA = true;
            } else {
                /* The kind of A we are looking for */
                String aux =  g1.replaceFirst("\\bA\\b", "PartA");
                refItem.put("PartA", "A");
                mText.appendReplacement(sb,aux);
            }
        }
        mText.appendTail(sb);
        output = sb.toString();

        if (flagDoubtA) {
            /* If there was a doubtful A, we use POS for deciding */
            Annotation annotation = new Annotation(output);
            preprocessPipeline.annotate(annotation);
            String chain = "";
            String globalChain = "";
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                chain = sentence.toString();
                TokenSequencePattern p = TokenSequencePattern.compile("(/A/) ([ !{ tag:\"NN\"} & !{tag:\"NNP\"} & !{ tag:\"VB\" } ]+ [ { tag:\"DT\" } | { word:/\\w+[A-Z]\\b/ } ]) /.*/*");
                TokenSequenceMatcher m = p.getMatcher(sentence.get(CoreAnnotations.TokensAnnotation.class));
                if (m.find()) {
                    CoreMap aa = m.mergeGroup();
                    chain = aa.toString().replaceFirst("A", "PartA");
                    refItem.put("PartA", "A");
                }
                globalChain = globalChain + chain + " ";
            }
            output = globalChain.substring(0, globalChain.length() - 1);
        }

        /* We check for the pattern 'X_X_X' " */
        int ids = 1;
        Pattern pText1 = Pattern.compile("((\\w+)_)+(\\w+)");
        Matcher mText1 = pText1.matcher(output);
        while (mText1.find()) {
            output = output.replaceAll(mText1.group(0), "Item" + ids);
            refItem.put("Item" + ids, mText1.group(0));
            ids++;
        }
        
        /* We check for the pattern 'dd/MM/yyyy " */
        output = output.replaceAll("'?(\\d?\\d) ?/ ?(January|February|March|April|May|June|July|August|September|October|November|December) ?/ ?(\\d?\\d?\\d\\d)'?", "$1 $2 $3");
        
//        checkMinor();
        Annotation annotation = new Annotation(output);
        preprocessPipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            TokenSequencePattern p = TokenSequencePattern.compile("/because/? ([ { tag:\"NN\"} | {tag:\"NNP\"} ]) [ { lemma:\"be\" } ] [ { tag:\"DT\" } ]? [ { lemma:\"minor\" } ]");
            TokenSequenceMatcher m = p.getMatcher(sentence.get(CoreAnnotations.TokensAnnotation.class));
            while (m.find()) {
                output = output.replaceFirst(m.group(0), "");
                logical.add("minor(" + m.group(1) + ").\n");
            }
            p = TokenSequencePattern.compile("/because/? ([ { tag:\"NN\"} | {tag:\"NNP\"} ]) [ { lemma:\"be\" } ] [ { word:\"under\" } ]? ([ { ner:\"NUMBER\" } ])");
            m = p.getMatcher(sentence.get(CoreAnnotations.TokensAnnotation.class));
            while (m.find()) {
                CoreMap age = m.mergeGroup(2);
                for (CoreLabel token : age.get(CoreAnnotations.TokensAnnotation.class)) {
                    String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                    pText = Pattern.compile("<(\\d+)");
                    mText = pText.matcher(normalized);
                    if (mText.find()) {
                        int ageint = Integer.parseInt(mText.group(1));
                        if(ageint < 20){
                            logical.add("minor(" + m.group(1) + ").\n");
                        }
                        break;
                    }
                }
                output = output.replaceFirst(m.group(0), "");
            }
            /* We replace contract synonyms for contract */
            p = TokenSequencePattern.compile("([ { lemma:\"agreement\" } ])");
            m = p.getMatcher(sentence.get(CoreAnnotations.TokensAnnotation.class));
            while (m.find()) {
                output = output.replaceFirst(m.group(0), "contract");
            }
            p = TokenSequencePattern.compile("/because/? ([ { tag:\"NN\"} | {tag:\"NNP\"} ]) [ { lemma:\"be\" } ] [ { tag:\"DT\" } ]? ([ { ner:\"DURATION\" } ]+)");
            m = p.getMatcher(sentence.get(CoreAnnotations.TokensAnnotation.class));
            while (m.find()) {
                CoreMap age = m.mergeGroup(2);
                for (CoreLabel token : age.get(CoreAnnotations.TokensAnnotation.class)) {
                    String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                    pText = Pattern.compile("P(\\d+)Y");
                    mText = pText.matcher(normalized);
                    if (mText.find()) {
                        int ageint = Integer.parseInt(mText.group(1));
                        if(ageint < 20){
                            logical.add("minor(" + m.group(1) + ").\n");
                        }
                        break;
                    }
                }
                output = output.replaceFirst(m.group(0), "");
            }
        }

        return output;
    }

    /* Functions of TicTag, maybe useful for further evaluation */
//    public boolean evaluateTE3() {
//        try {
//            ManagerTempEval3 mte3 = new ManagerTempEval3();
//            List<FileTempEval3> list = mte3.lista;
//            for (FileTempEval3 f : list) {
//                String input = f.getTextInput();
//                String output = annotate(input);
//                f.writeOutputFile(output);
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(LegalWhen.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return false;
//    }
//
//    public boolean evaluateTE3ES() {
//        try {
//            ManagerTempEval3ES mte3 = new ManagerTempEval3ES();
//            List<FileTempEval3ES> list = mte3.lista;
//            for (FileTempEval3ES f : list) {
//                String input = f.getTextInput();
//                String output = annotate(input);
//                f.writeOutputFile(output);
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(LegalWhen.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return false;
//    }
//
//    public boolean evaluateTimeBank() {
//        try {
//            ManagerTimeBank mtb = new ManagerTimeBank();
//            List<FileTimeBank> list = mtb.lista;
//            for (FileTimeBank f : list) {
//                String input = f.getTextInput();
//                String output = annotate(input);
//                f.writeOutputFile(output);
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(LegalWhen.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return false;
//    }
//
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
            Logger.getLogger(ExtractorKeywordBased.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private String[] preprocessACoref(String mentionSpan, String output, int newMention, String current) {
        String[] out = {"", ""};
        out[0] = output;
        String toRep = mentionSpan.replaceAll("\\b(\\w+)\\b", "(?<!\\\\w)(<[^>]+>)?$1(<[^>]+>)?(?!\\\\w)");
//        String toRep = mentionSpan.replaceAll("\\b", "(<[^>]+>)?");
        StringBuffer sb = new StringBuffer(output.length());
        
        Pattern pContract = Pattern.compile(toRep);
        Matcher mText = pContract.matcher(output);
        if (mText.find()) {
            int help = mText.groupCount();
            int i = 1;
            while (mText.group(i) == null && i < help) {
                i++;
                if (i == help) {
                    if (!current.isEmpty()) {
                        mText.appendReplacement(sb,current);
                        out[1] = current;
                    }
                    else{
                        return out;
                    }
                }
            }      
            
            if(out[1].isEmpty()){
//                String aux = output.replaceFirst(mText.group(0), "\\$" + mText.group(i).replaceAll(">", "").replaceAll("<", "").replaceAll("/", "") + newMention);
                out[1] = "\\$" + mText.group(i).replaceAll(">", "").replaceAll("<", "").replaceAll("/", "") + newMention;
                mText.appendReplacement(sb,"\\$" + mText.group(i).replaceAll(">", "").replaceAll("<", "").replaceAll("/", "") + newMention);
                
            }
            
            mText.appendTail(sb);
            out[0] = sb.toString();
            
            return out;
        }

        return out;
    }

    public boolean writeLogicFile(String input2, String path) {
        String input = "";
        String initiate = "initiate(";
        String terminate = "terminate(";
//        initiate("contract"; end; date)
//        terminate("contract"; end; date)

        // For each sentence, s
        String[] output2 = input2.split("\\.");
        for (String s : output2) {
            String date = "UNKNOWN";
            String what = "";
            String type = "";
            Pattern pTE = Pattern.compile("\\$([^\\s]*)");
            Matcher mTE = pTE.matcher(s);
            while (mTE.find()) {
                String in = mTE.group(1);
                if (in.startsWith("DATE")) {
                    date = in.substring(4);
                } else if (in.equals("EVENTEND")) {
                    type = terminate;
                } else if (in.startsWith("EVENTEND")) {
                    what = in;
                } else if (in.equals("EVENTESTABLISH")) {
                    type = initiate;
                } else if (in.startsWith("EVENTESTABLISH")) {
                    what = in;
                } else {
                    what = in;
                }
            }
//            if (!date.equals("") && !what.equals("") && !type.equals("")) {
            if (!what.equals("") && !type.equals("")) {
                input = input + type + what + "," + date + ");\n";
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(w);
            bw.write(input);
            bw.flush();
            bw.close();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(ExtractorKeywordBased.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    
    
        
        
//        /* We check the contract, that can be an nmod:by; it can also be checked later the contract mentions in the sentence */         
//        pText1 = Pattern.compile("nmod:by\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
//        if (mText1.find() && frame.getContract().isEmpty()) {
//            frame.setContract(mText1.group(1));
//        }
        
        /* Date and price will be done later */
        


}
