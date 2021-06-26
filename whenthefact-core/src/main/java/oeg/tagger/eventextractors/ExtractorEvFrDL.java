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
//import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.GrammaticalStructure;
//import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
//import edu.stanford.nlp.trees.Tree;
//import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeg.eventRepresentation.Event;
import oeg.eventRepresentation.Frame;
import oeg.eventRepresentation.FrameFrame;
import oeg.tagger.docHandler.Document;
import oeg.tagger.docHandler.DocumentPart;
import oeg.tagger.docHandler.StructureExtractor;
import oeg.tagger.docHandler.XMLMerger;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;

/**
 * ExtractorTIMEXKeywordBased core class, where the rules are applied and the
 * normalization algorithm is.
 *
 * @author mnavas
 */
public class ExtractorEvFrDL {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExtractorEvFrDL.class);

//    PrintWriter out;
    String eventFile;
    String framesFile;
    String rules;
    Properties properties = new Properties();
    String posModel;
    String lemmaModel;
    StanfordCoreNLP pipeline;

    HashMap<String, Frame> eventFrames;
    HashMap<String, FrameFrame> frameFrames;

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
    public ExtractorEvFrDL() {
        init();
    }

    public ExtractorEvFrDL(String language) {
        lang = language;
        init();
    }

    public ExtractorEvFrDL(String pos, String lemma, String rul, String language, String events, String fevents) {
        posModel = pos;
        lemmaModel = lemma;
        rules = rul;
        lang = language;
        eventFile = events;
        framesFile = fevents;
        init();
    }

    public ExtractorEvFrDL(String rul, String language) {
        rules = rul;
        lang = language;
        init();
    }

    public void init() {

        FileInputStream fileIn = null;

        if (eventFile == null) {
            eventFile = ".\\src\\main\\resources\\events.ser";
        }

        if (framesFile == null) {
            framesFile = ".\\src\\main\\resources\\frames.ser";
        }

        try {
            fileIn = new FileInputStream(eventFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            eventFrames = (HashMap<String, Frame>) in.readObject();
            in.close();
            fileIn.close();

            fileIn = new FileInputStream(framesFile);
            in = new ObjectInputStream(fileIn);
            frameFrames = (HashMap<String, FrameFrame>) in.readObject();
            in.close();
            fileIn.close();

//            System.out.println(eventFrames);
            if (rules == null) {
                rules = "./src/main/resources/rules/rulesEN.txt";
            }
            //        out = new PrintWriter(System.out);
//            properties = StringUtils.argsToProperties(new String[]{"-props", "StanfordCoreNLP-spanish.properties"});
            properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit, pos, lemma,ner,tokensregexdemo");
//properties.setProperty("annotators", "tokenize, ssplit, pos, lemma,ner,parse,tokensregexdemo");
            properties.setProperty("ner.useSUTime", "false");
            properties.setProperty("customAnnotatorClass.tokensregexdemo", "edu.stanford.nlp.pipeline.TokensRegexAnnotator");
            properties.setProperty("tokensregexdemo.rules", rules);
            properties.setProperty("tokenize.verbose", "false");
            properties.setProperty("TokensRegexNERAnnotator.verbose", "false");
//    properties.setProperty("regexner.verbose", "false");
            try {
                pipeline = new StanfordCoreNLP(properties);
            } catch (Exception ex) {
                System.out.println("Error: " + ex.toString());
            }
        } catch (Exception ex) {
            Logger.getLogger(ExtractorEvFrDL.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileIn.close();
            } catch (IOException ex) {
                Logger.getLogger(ExtractorEvFrDL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public DateTime getNextMonth(DateTime dt, int monthS) {
        int current = dt.getMonthOfYear();
        if (monthS <= current) {
            monthS += 12;
        }
        DateTime next = dt.plusMonths(monthS - current);
        return next;
    }

    public DateTime getLastMonth(DateTime dt, int monthS) {
        int current = dt.getMonthOfYear();
        if (monthS < current) {
            monthS = current - monthS;
        } else {
            monthS = 12 - monthS + current;
        }
        DateTime next = dt.minusMonths(monthS);
        return next;
    }

    public DateTime getNextDayWeek(DateTime dt, int dayW) {
        int current = dt.getDayOfWeek();
        if (dayW <= current) {
            dayW += 7;
        }
        DateTime next = dt.plusDays(dayW - current);
        return next;
    }

    public DateTime getLastDayWeek(DateTime dt, int dayW) {
        int current = dt.getDayOfWeek();
        if (dayW < current) {
            dayW = current - dayW;
        } else {
            dayW = 7 - dayW + current;
        }
        DateTime next = dt.minusDays(dayW);
        return next;
    }

    public String getNextMonthS(DateTime dt, String monthSS) {
        int current = dt.getMonthOfYear();
        String a = monthSS.replaceAll("MONTHS", "");
        int monthS = Integer.valueOf(a);
        String next;
        if (monthS <= current) {
            next = (dt.getYear() + 1) + "-" + String.format("%02d", monthS);
        } else {
            next = dt.getYear() + "-" + String.format("%02d", monthS);
        }
        return next;
    }

    public String getLastMonthS(DateTime dt, String monthSS) {
        int current = dt.getMonthOfYear();
        String a = monthSS.replaceAll("MONTHS", "");
        int monthS = Integer.valueOf(a);
        String next;
        if (monthS >= current) {
            next = (dt.getYear() + 1) + "-" + String.format("%02d", monthS);
        } else {
            next = dt.getYear() + "-" + String.format("%02d", monthS);
        }
        return next;
    }

    public String getNextDate(String dt, String refD) {
        DateTime dtDT = new DateTime(dt);
        if (refD.matches("\\d\\d\\d\\d-\\d\\d(-\\d\\d)?")) {
            return refD;
        } else if (refD.matches("XXXX-\\d\\d-\\d\\d")) {
            refD = refD.replaceAll("XXXX", dt.substring(0, 4));
            DateTime refDDT = new DateTime(refD);
            if (refDDT.isAfter(dtDT)) {
                return refD;
            } else {
                return refDDT.plusYears(1).toString("YYYY-MM-dd");
            }
        } else if (refD.matches("XXXX-XX-\\d\\d")) {
            refD = refD.replaceAll("XXXX", dt.substring(0, 4));
            refD = refD.replaceAll("XX", dt.substring(5, 7));
            DateTime refDDT = new DateTime(refD);
            if (refDDT.isAfter(dtDT)) {
                return refD;
            } else {
                return refDDT.plusMonths(1).toString("YYYY-MM-dd");
            }
        }
        return refD;
    }

    public String getLastDate(String dt, String refD) {
        DateTime dtDT = new DateTime(dt);
        if (refD.matches("\\d\\d\\d\\d-\\d\\d(-\\d\\d)?")) {
            return refD;
        } else if (refD.matches("XXXX-\\d\\d-\\d\\d")) {
            refD = refD.replaceAll("XXXX", dt.substring(0, 4));
            DateTime refDDT = new DateTime(refD);
            if (refDDT.isBefore(dtDT)) {
                return refD;
            } else {
                return refDDT.minusYears(1).toString("YYYY-MM-dd");
            }
        } else if (refD.matches("XXXX-XX-\\d\\d")) {
            refD = refD.replaceAll("XXXX", dt.substring(0, 4));
            refD = refD.replaceAll("XX", dt.substring(5, 7));
            DateTime refDDT = new DateTime(refD);
            if (refDDT.isBefore(dtDT)) {
                return refD;
            } else {
                return refDDT.minusMonths(1).toString("YYYY-MM-dd");
            }
        }

        return refD;
    }

    public String getNextSeason(String dt, String refD) {
        if (refD.matches("\\d\\d\\d\\d-[A-Z][A-Z]")) {
            return refD;
        }
        String year = dt.substring(0, 4);
        String season = refD.substring(4, 7);
        String seasondate = year;
        DateTime dtDT = new DateTime(dt);
        if (season.equalsIgnoreCase("-SU")) {
            seasondate = seasondate + iniSU;
        } else if (season.equalsIgnoreCase("-SP")) {
            seasondate = seasondate + iniSP;
        } else if (season.equalsIgnoreCase("-WI")) {
            seasondate = seasondate + iniWI;
        } else if (season.equalsIgnoreCase("-FA")) {
            seasondate = seasondate + iniFA;
        }
        DateTime refDDT = new DateTime(seasondate);

        if (refDDT.isAfter(dtDT)) {
            return year + season;
        } else {
            return refDDT.plusYears(1).toString("YYYY") + season;
        }
    }

    public String getLastSeason(String dt, String refD) {
        if (refD.matches("\\d\\d\\d\\d-[A-Z][A-Z]")) {
            return refD;
        }
        String year = dt.substring(0, 4);
        String season = refD.substring(4, 7);
        String seasondate = year;
        DateTime dtDT = new DateTime(dt);
        if (season.equalsIgnoreCase("-SU")) {
            seasondate = seasondate + iniSU;
        } else if (season.equalsIgnoreCase("-SP")) {
            seasondate = seasondate + iniSP;
        } else if (season.equalsIgnoreCase("-WI")) {
            seasondate = seasondate + iniWI;
        } else if (season.equalsIgnoreCase("-FA")) {
            seasondate = seasondate + iniFA;
        }
        DateTime refDDT = new DateTime(seasondate);

        if (refDDT.isBefore(dtDT)) {
            return year + season;
        } else {
            return refDDT.minusYears(1).toString("YYYY") + season;
        }
    }

    public LinkedHashMap<String, String> parseDuration(String input) {
        LinkedHashMap<String, String> durations = new LinkedHashMap<String, String>();
        Pattern pAnchor = Pattern.compile("(\\d*\\.?\\d+|X)([a-zA-Z]+)");

        Matcher m = pAnchor.matcher(input);
        while (m.find()) {
            String numb = m.group(1);
            String unit = m.group(2);
            durations.put(unit, numb);

        }
//        Pattern pAnchor = Pattern.compile("anchor\\((\\w+),([+-]?\\d+),(\\w+)\\)");

        return durations;
    }

    public String annotate(String input, String anchorDate, File filename) {

        try {
            DateTime a = new DateTime(anchorDate);
        } catch (Exception e) {
            Date dct = Calendar.getInstance().getTime();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            anchorDate = df.format(dct);
        }

        Pattern pAnchor = Pattern.compile("anchor\\((\\w+),(.),([^\\)]+)\\)");
        String lastfullDATE = anchorDate; // Where we keep the last full date, in case we have to normalize
        String backupAnchor = anchorDate;
        String lastDATE = anchorDate; // Where we keep the last date, in case we have to normalize
//        Pattern pAnchor = Pattern.compile("anchor\\((\\w+),([+-]?\\d+),(\\w+)\\)");
        try {
            String inpWhen = input;  //TIMEX

            inpWhen = inpWhen.replaceAll("\\r\\n", "\n");
            String inpCore = inpWhen;  //CORE
            String inpWho = inpWhen;  //WHO
            String inpWhat = inpWhen;  //WHAT

            int offsetdelay = 0;
            int numval = 2;
            Annotation annotation = new Annotation(inpWhen);

            int offset = 0;
            int offsetwho = 0;
            int offsetev = 0;

            int offsetEvent = "<Event argument=\"what\">".length() + "</Event>".length();
            int offsetEventEv = "<Event argument=\"ev\">".length() + "</Event>".length();

            StructureExtractor se = new StructureExtractor();
            Document doc = se.extractFromDocument(filename);
            List<DocumentPart> eventRelevantParts = doc.getEventRelevantParts();

            if (doc.type.equalsIgnoreCase("echr")) {
                offsetdelay = 1;
                offset = 1;
                offsetwho = 1;
                offsetev = 1;
            }

            // GET KEY SENTENCE AND LOOK FOR SYNONYMS
            ArrayList<String> topic = getTopic(doc);

            // LOOK FOR SEMANTIC FIELD WITH THE WORDS FROM TOPIC
            ArrayList<String> topicWords = getSemanticField(topic);

            pipeline.annotate(annotation);

//            String testeo = annotation.get(CoreAnnotations.TextAnnotation.class);
//            if(inpWhen.equals(testeo)){
//                System.out.println("yeaaaaah");
//            }
            // GET RELEVANT SENTENCES
            List<CoreMap> sentencesAll = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            List<CoreMap> sentences = new ArrayList<CoreMap>();
            if (eventRelevantParts.isEmpty()) {
                sentences = sentencesAll;
            } else {
                DocumentPart p = doc.getProcedure();
                for (CoreMap sentence : sentencesAll) {
                    List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                    Integer beginSentence = toks.get(0).beginPosition();

                    for (DocumentPart erp : eventRelevantParts) {
                        if (beginSentence >= erp.offset_ini && beginSentence < erp.offset_end) {
                            sentences.add(sentence);
                            break;
                        }
                    }

                }

            }

            lastfullDATE = backupAnchor;
//            for (CoreMap sentence : sentencesAll) {
            for (CoreMap sentence : sentences) {

                lastDATE = backupAnchor;
                CoreMapExpressionExtractor<MatchedExpression> extractor = CoreMapExpressionExtractor
                        .createExtractorFromFiles(TokenSequencePattern.getNewEnv(), rules);
                List<MatchedExpression> matchedExpressions = extractor.extractExpressions(sentence);
                int flagTIMEX = 0;
                HashMap<Float, Integer> positionsTIMEX = new HashMap<Float, Integer>();
                for (MatchedExpression matched : matchedExpressions) {

                    CoreMap cm = matched.getAnnotation();

                    Value v = matched.getValue();

                    ArrayList<edu.stanford.nlp.ling.tokensregex.types.Expressions.PrimitiveValue> a = (ArrayList<edu.stanford.nlp.ling.tokensregex.types.Expressions.PrimitiveValue>) v.get();
                    String typ = (String) a.get(0).get();
                    String val = (String) a.get(1).get();
                    String freq = (String) a.get(2).get();
                    String rul = (String) a.get(4).get();

                    System.out.println(typ + " | " + val + " | " + freq + " | " + rul);

                    // TO DO: el get? poner los values!
                    numval++;
                    int ini = cm.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    String text = cm.get(CoreAnnotations.TextAnnotation.class);
//        out.println(matched.getText() + " - " + matched.getCharOffsets());

                    // To adapt to TE3 format - news mode
                    if ((typ.equalsIgnoreCase("DATE") || typ.equalsIgnoreCase("TIME")) && val.startsWith("XXXX-XX") && anchorDate != null) {
                        DateTime dt = new DateTime(lastfullDATE);
//                        DateTime dt = new DateTime(anchorDate);
                        int month = dt.getMonthOfYear();
                        int year = dt.getYear();
                        val = year + "-" + String.format("%02d", month) + val.substring(7, val.length());
                    } else if ((typ.equalsIgnoreCase("DATE") || typ.equalsIgnoreCase("TIME")) && val.startsWith("XXXX") && anchorDate != null) {
                        DateTime dt = new DateTime(lastfullDATE);
//                        DateTime dt = new DateTime(anchorDate);
                        int year = dt.getYear();
                        val = year + val.substring(4, val.length());
                    }

                    // To adapt to TE3 format
                    val = val.replaceAll("-X+", "");

                    // TODO: also, use the dependency parsing to find modifiers
                    // TODO: the ref can be other day...
                    if (val.startsWith("Danchor(+,") && anchorDate != null) {
                        String refDate = val.substring(10, val.length() - 1);
                        val = getNextDate(anchorDate, refDate);
                    } else if (val.startsWith("Danchor(-,") && anchorDate != null) {
                        String refDate = val.substring(10, val.length() - 1);
                        val = getLastDate(anchorDate, refDate);
                    } else if (val.startsWith("Sanchor(+,") && anchorDate != null) {
                        String refDate = val.substring(10, val.length() - 1);
                        val = getNextSeason(anchorDate, refDate);
                    } else if (val.startsWith("Sanchor(-,") && anchorDate != null) {
                        String refDate = val.substring(10, val.length() - 1);
                        val = getLastSeason(anchorDate, refDate);
                    } else if (val.startsWith("Ranchor(+,") && anchorDate != null) {
                        String gran = val.substring(10, val.length() - 1);
                        DateTime dat = new DateTime(anchorDate);
                        if (gran.equalsIgnoreCase("M")) {
                            int day = dat.getDayOfMonth();
                            int maxM = dat.dayOfMonth().getMaximumValue();
                            val = (maxM - day) + "D";
                        } else if (gran.equalsIgnoreCase("Y")) {
                            int day = dat.getDayOfMonth();
                            int maxM = dat.dayOfMonth().getMaximumValue();
                            if (dat.getMonthOfYear() != 12) {
                                val = (12 - dat.getMonthOfYear()) + "M" + (maxM - day) + "D";
                            } else {
                                val = (maxM - day) + "D";
                            }
                        }
                    } else if (val.startsWith("Ranchor(-,") && anchorDate != null) {
                        String gran = val.substring(10, val.length() - 1);
                        DateTime dat = new DateTime(anchorDate);
                        if (gran.equalsIgnoreCase("M")) {
                            int day = dat.getDayOfMonth();
                            val = day + "D";
                        } else if (gran.equalsIgnoreCase("Y")) {
                            int day = dat.getDayOfMonth();
                            if (dat.getMonthOfYear() != 1) {
                                if (day == 1) {
                                    val = (dat.getMonthOfYear() - 1) + "M";
                                } else {
                                    val = (dat.getMonthOfYear() - 1) + "M" + (day - 1) + "D";
                                }
                            } else {
                                val = (day - 1) + "D";
                            }
                        }
                    } else if (val.startsWith("DWanchor(+,") && anchorDate != null) {
                        String refDate = val.substring(11, val.length() - 1);
                        val = getNextMonthS(new DateTime(anchorDate), refDate);
                    } else if (val.startsWith("DWanchor(-,") && anchorDate != null) {
                        String refDate = val.substring(11, val.length() - 1);
                        val = getLastMonthS(new DateTime(anchorDate), refDate);
                    } else if (val.startsWith("anchor") && anchorDate != null) {
                        DateTime dt = new DateTime(anchorDate);

                        Matcher m = pAnchor.matcher(val);
                        m.find();
                        String ref = m.group(1);
                        String plus = m.group(2);
                        String duration = m.group(3);

                        LinkedHashMap<String, String> durations = new LinkedHashMap<String, String>();
                        // If it is an anchor for a date (eg, "this month")
                        if (plus.equalsIgnoreCase("x")) {
                            durations.put(duration, "0");
                        } else {
                            durations = parseDuration(duration);
                        }

                        for (Entry<String, String> entry : durations.entrySet()) {
                            String gran = entry.getKey();
                            int plusI = Integer.valueOf(entry.getValue());

                            // Needs to be more general, check if today, proceed otherwise if not
                            if (gran.equalsIgnoreCase("D")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = dt.plusDays(plusI);
                                } else if (plus.equalsIgnoreCase("-")) {
                                    dt = dt.minusDays(plusI);
                                } else {
                                    dt = new DateTime(lastfullDATE);
                                    val = dt.toString("YYYY-MM-dd") + val.substring(val.lastIndexOf(")") + 1);

                                }
                            } else if (gran.equalsIgnoreCase("M")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = dt.plusMonths(plusI);
                                } else if (plus.equalsIgnoreCase("-")) {
                                    dt = dt.minusMonths(plusI);
                                } else {
                                    dt = new DateTime(lastfullDATE);
                                    val = dt.toString("YYYY-MM");
                                }
                            } else if (gran.equalsIgnoreCase("Y")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = dt.plusYears(plusI);
                                } else if (plus.equalsIgnoreCase("-")) {
                                    dt = dt.minusYears(plusI);
                                } else {
                                    dt = new DateTime(lastfullDATE);
                                    val = dt.toString("YYYY");
                                }
                            } else if (gran.equalsIgnoreCase("CENT")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = dt.plusYears(plusI * 100);
                                } else if (plus.equalsIgnoreCase("-")) {
                                    dt = dt.minusYears(plusI * 100);
                                } else {
                                    val = (dt.plusYears(100)).toString("YYYY");
                                    if (val.length() == 4) {
                                        val = val.substring(0, 2);
                                    } else if (val.length() == 3) {
                                        val = "0" + val.substring(0, 1);
                                    } else {
                                        val = "00";
                                    }
                                }
                            } else if (gran.equalsIgnoreCase("W")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = dt.plusWeeks(plusI);
                                } else if (plus.equalsIgnoreCase("-")) {
                                    dt = dt.minusWeeks(plusI);
                                } else {
                                    val = dt.toString("YYYY") + "-W" + String.format("%02d", dt.getWeekOfWeekyear());
                                }
                            } else if (gran.equalsIgnoreCase("H")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = dt.plusHours(plusI);
                                } else {
                                    dt = dt.minusHours(plusI);
                                }
                            } else if (gran.equalsIgnoreCase("MIN")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = dt.plusMinutes(plusI);
                                } else {
                                    dt = dt.minusMinutes(plusI);
                                }
                            } else if (gran.equalsIgnoreCase("S")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = dt.plusSeconds(plusI);
                                } else {
                                    dt = dt.minusSeconds(plusI);
                                }

                            } else if (gran.equalsIgnoreCase("DAYW")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = getNextDayWeek(dt, plusI);
                                } else if (plus.equalsIgnoreCase("-")) {
                                    dt = getLastDayWeek(dt, plusI);
                                } else if (plus.equalsIgnoreCase("z")) {
                                    int current = dt.getDayOfWeek();
                                    if (plusI <= current) {
                                        dt = dt.minusDays(current - plusI);
                                    } else {
                                        dt = dt.plusDays(plusI - current);
                                    }
                                }
                            } else if (gran.startsWith("Q")) {
                                if (plus.equalsIgnoreCase("x") && plus.matches("Q\\d+")) {
                                    val = dt.toString("YYYY") + "-" + gran;
                                } else {
                                    if (plus.equalsIgnoreCase("+")) {
                                        dt = dt.plusMonths(3 * plusI);
                                    } else if (plus.equalsIgnoreCase("-")) {
                                        dt = dt.minusMonths(3 * plusI);
                                    }
                                    if (dt.getMonthOfYear() < 4) {
                                        val = dt.toString("YYYY") + "-Q1";
                                    } else if (dt.getMonthOfYear() < 7) {
                                        val = dt.toString("YYYY") + "-Q2";
                                    } else if (dt.getMonthOfYear() < 10) {
                                        val = dt.toString("YYYY") + "-Q3";
                                    } else {
                                        val = dt.toString("YYYY") + "-Q4";
                                    }
                                }
                            } else if (gran.startsWith("HALF")) {
                                if (plus.equalsIgnoreCase("x") && plus.matches("HALF\\d+")) {
                                    val = dt.toString("YYYY") + "-" + gran.replaceFirst("ALF", "");
                                } else {
                                    if (plus.equalsIgnoreCase("+")) {
                                        dt = dt.plusMonths(6 * plusI);
                                    } else if (plus.equalsIgnoreCase("-")) {
                                        dt = dt.minusMonths(6 * plusI);
                                    }
                                    if (dt.getMonthOfYear() < 7) {
                                        val = dt.toString("YYYY") + "-H1";
                                    } else {
                                        val = dt.toString("YYYY") + "-H2";
                                    }
                                }
                            } else if (gran.startsWith("T")) {
                                if (plus.equalsIgnoreCase("x") && plus.matches("T\\d+")) {
                                    val = dt.toString("YYYY") + "-" + gran;
                                } else {
                                    if (plus.equalsIgnoreCase("+")) {
                                        dt = dt.plusMonths(4 * plusI);
                                    } else if (plus.equalsIgnoreCase("-")) {
                                        dt = dt.minusMonths(4 * plusI);
                                    }
                                    if (dt.getMonthOfYear() < 5) {
                                        val = dt.toString("YYYY") + "-T1";
                                    } else if (dt.getMonthOfYear() < 9) {
                                        val = dt.toString("YYYY") + "-T2";
                                    } else {
                                        val = dt.toString("YYYY") + "-T3";
                                    }
                                }
                            } else if (gran.equalsIgnoreCase("MONTHS")) {
                                if (plus.equalsIgnoreCase("+")) {
                                    dt = getNextMonth(dt, plusI);
                                } else {
                                    dt = getLastMonth(dt, plusI);
                                }
                            }
                        }

                        if (val.matches("anchor\\([A-Z]+,.,.*(\\d+)W\\)")) {
                            val = dt.getYear() + "-W" + String.format("%02d", dt.getWeekOfWeekyear());
                        } else if (val.matches("anchor\\([A-Z]+,.,.*(\\d+)Y\\)")) {
                            val = dt.toString("YYYY");
                        } else if (val.matches("anchor\\([A-Z]+,.,.*(\\d+)M\\)")) {
                            val = dt.toString("YYYY-MM");
                        } else if (val.matches("\\d{0,4}-[H|T|Q]\\d")) {
                        } else if (!plus.equalsIgnoreCase("x")) {
                            val = dt.toString("YYYY-MM-dd") + val.substring(val.lastIndexOf(")") + 1);
                        } else {

                        }
                    }

                    if ((typ.equalsIgnoreCase("DURATION") || typ.equalsIgnoreCase("SET"))) {
                        LinkedHashMap<String, String> auxVal = parseDuration(val);
                        String auxfin = "P";
                        int flagT = 0;
                        int mins = 0;
                        for (Entry<String, String> entry : auxVal.entrySet()) {
                            String gran = entry.getKey();
                            String gran2 = entry.getValue();
                            if ((gran.equalsIgnoreCase("AF") || gran.equalsIgnoreCase("MO") || gran.equalsIgnoreCase("MI") || gran.equalsIgnoreCase("EV") || gran.equalsIgnoreCase("NI")) && flagT == 0) {
                                flagT = 1;
                                auxfin = auxfin + "T" + gran2.replaceFirst("\\.0", "") + gran;
                            } else if (gran.equalsIgnoreCase("H") && flagT == 0) {
                                flagT = 1;
                                auxfin = auxfin + "T" + gran2.replaceFirst("\\.0", "") + gran;
                            } else if (gran.equalsIgnoreCase("MIN") && flagT == 0) {
                                flagT = 1;
                                auxfin = auxfin + "T" + gran2.replaceFirst("\\.0", "") + "M";
                            } else if (gran.equalsIgnoreCase("HALF")) {
                                flagT = 1;
                                auxfin = auxfin + gran2.replaceFirst("\\.0", "") + "H";
                            } else if (gran.equalsIgnoreCase("S") && flagT == 0) {
                                flagT = 1;
                                auxfin = auxfin + "T" + gran2.replaceFirst("\\.0", "") + gran;
                            } else {
                                auxfin = auxfin + gran2.replaceFirst("\\.0", "") + gran;
                            }
                        }
                        val = auxfin;
                        val = val.replaceFirst("MIN", "M");
                        val = val.replaceFirst("HALF", "H");

                    }
                    if (typ.equalsIgnoreCase("TIME") && val.startsWith("T")) {
                        val = lastfullDATE + val;
                    }
                    if (typ.equalsIgnoreCase("TIME") && val.matches("....-..-..(Tanchor\\(.*,.*,.*\\))*.*")) { //for date + time anchorbug
                        val = val.replaceAll("(anchor\\(.*,.*,.*\\))", "");
                        val = val.replaceAll("T+", "T");
                    }

                    if (typ.equalsIgnoreCase("DATE") && val.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
                        lastfullDATE = val;
                    }

                    if (typ.equalsIgnoreCase("TIME") && val.startsWith("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
                        lastfullDATE = val.substring(0, 10);
                    }

                    if (typ.equalsIgnoreCase("DATE")) {
                        lastDATE = val;
                    }
                    String addini = "<TIMEX3 tid=\"t" + numval + "\" type=\"" + typ + "\" value=\"" + val + "\">";
                    if (!freq.isEmpty()) {
                        addini = "<TIMEX3 tid=\"t" + numval + "\" type=\"" + typ + "\" value=\"" + val + "\" freq=\"" + freq + "\">";
                    }
                    String addfin = "</TIMEX3>";

                    String toAdd = addini + text + addfin;
                    if (text.endsWith(" ,")) {
                        toAdd = addini + text.substring(0, text.length() - 2) + addfin + " ,";
                    } else if (text.endsWith(",")) {
                        toAdd = addini + text.substring(0, text.length() - 1) + addfin + ",";
                    } else if (text.endsWith(" .")) {
                        toAdd = addini + text.substring(0, text.length() - 2) + addfin + " .";
                    } else if (text.endsWith(".")) {
                        toAdd = addini + text.substring(0, text.length() - 1) + addfin + ".";
                    } else if (text.endsWith(" ;")) {
                        toAdd = addini + text.substring(0, text.length() - 2) + addfin + " ;";
                    } else if (text.endsWith(";")) {
                        toAdd = addini + text.substring(0, text.length() - 1) + addfin + ";";
                    }

//                     if (typ.equalsIgnoreCase("DATE")) {
////                    if (typ.equalsIgnoreCase("DATE") && !inp2.substring(ini + offsetdelay - " of ".length(), ini + offsetdelay).equalsIgnoreCase(" of ")) {
//                        flagTIMEX = 1;
//                        inpWhen = inpWhen.substring(0, ini + offsetdelay) + toAdd + inpWhen.substring(ini + text.length() + offsetdelay);
//                        offsetdelay = offsetdelay + toAdd.length() - text.length();
//
//                    }
                    if (typ.equalsIgnoreCase("DATE") && (ini + offsetdelay < "European Parliament and of the Council of ".length())) {
//                    if (typ.equalsIgnoreCase("DATE") && !inp2.substring(ini + offsetdelay - " of ".length(), ini + offsetdelay).equalsIgnoreCase(" of ")) {
                        flagTIMEX++;
                        inpWhen = inpWhen.substring(0, ini + offsetdelay) + toAdd + inpWhen.substring(ini + text.length() + offsetdelay);
                        offsetdelay = offsetdelay + toAdd.length() - text.length();

                    } else if (typ.equalsIgnoreCase("DATE") && !inpWhen.substring(ini + offsetdelay - "European Parliament and of the Council of ".length(), ini + offsetdelay).equalsIgnoreCase("European Parliament and of the Council of ")) {
//                    if (typ.equalsIgnoreCase("DATE") && !inp2.substring(ini + offsetdelay - " of ".length(), ini + offsetdelay).equalsIgnoreCase(" of ")) {
                        flagTIMEX++;
                        inpWhen = inpWhen.substring(0, ini + offsetdelay) + toAdd + inpWhen.substring(ini + text.length() + offsetdelay);
                        offsetdelay = offsetdelay + toAdd.length() - text.length();

                    }

//                    if (typ.equalsIgnoreCase("DATE") && (ini + offsetdelay < "decision of ".length())) {
////                    if (typ.equalsIgnoreCase("DATE") && !inp2.substring(ini + offsetdelay - " of ".length(), ini + offsetdelay).equalsIgnoreCase(" of ")) {
//                        flagTIMEX = 1;
//                        inpWhen = inpWhen.substring(0, ini + offsetdelay) + toAdd + inpWhen.substring(ini + text.length() + offsetdelay);
//                        offsetdelay = offsetdelay + toAdd.length() - text.length();
//
//                    } else if (typ.equalsIgnoreCase("DATE") && !inpWhen.substring(ini + offsetdelay - "decision of ".length(), ini + offsetdelay).equalsIgnoreCase("decision of ") && !inpWhen.substring(ini + offsetdelay - "judgment of ".length(), ini + offsetdelay).equalsIgnoreCase("judgment of ")) {
////                    if (typ.equalsIgnoreCase("DATE") && !inp2.substring(ini + offsetdelay - " of ".length(), ini + offsetdelay).equalsIgnoreCase(" of ")) {
//                        flagTIMEX = 1;
//                        inpWhen = inpWhen.substring(0, ini + offsetdelay) + toAdd + inpWhen.substring(ini + text.length() + offsetdelay);
//                        offsetdelay = offsetdelay + toAdd.length() - text.length();
//
//                    } else if (typ.equalsIgnoreCase("DATE") && (inpWhen.substring(ini + offsetdelay - "by decision of ".length(), ini + offsetdelay).equalsIgnoreCase("by decision of ") || inpWhen.substring(ini + offsetdelay - "by judgment of ".length(), ini + offsetdelay).equalsIgnoreCase("by judgment of "))) {
////                    if (typ.equalsIgnoreCase("DATE") && !inp2.substring(ini + offsetdelay - " of ".length(), ini + offsetdelay).equalsIgnoreCase(" of ")) {
//                        flagTIMEX = 1;
//                        inpWhen = inpWhen.substring(0, ini + offsetdelay) + toAdd + inpWhen.substring(ini + text.length() + offsetdelay);
//                        offsetdelay = offsetdelay + toAdd.length() - text.length();
//
//                    }
//                    if (flagTIMEX == 1) {  // Solo una expresion temporal
//                        break;
//                    }
                    if (flagTIMEX > 0) {  // we save the position of the timexes
                        positionsTIMEX.put((float) (ini + (text.length() / 2)), numval);
                    }

                }
                int flag = 0;
                if (flagTIMEX > 1) { // Otra opcion es sumar uno por cada TIMEX, pero tampoco es una relacion uno a uno...
                    int numvalClose = numval;
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        /* We collect the different tags of each token */
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
////                    System.out.println("w: " + word);
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                        String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//                        String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

                        /* Verb Event */
                        flag = 0;

                        for (Entry<String, Frame> entry : eventFrames.entrySet()) {
                            String k = entry.getKey();
                            if (lemma.contains(k) && pos.contains("V") && !pos.equalsIgnoreCase("VBG")) {
                                String deppar = dependencyParsing(sentence.toString());
                                int wordpos = token.index();
                                Pattern pText1 = Pattern.compile("[^\\)]*aux[^\\(]*\\(([^-]+)-(\\d+), " + word + "-" + wordpos + "\\)");
                                Matcher mText1 = pText1.matcher(deppar);
                                if (mText1.find()) {
                                    // It is an auxiliary verb, continue parsing!
                                } else {
//                                String contspar = constituencyParsing(sentence,word);
                                    Frame frame = entry.getValue();
                                    Event ev = checkEvent(deppar, word, frame, token.index());
//                                if (!lemma.contains("bear") && ev.who.arrayEl.contains("applicant")) {
                                    if (!(lemma.equalsIgnoreCase("bear") && (ev.who.arrayEl.contains("applicant") || ev.who.arrayEl.contains("applicants")))) {
                                        // ADD CORE TO INP3
                                        // coger sentence, buscar los tokens min y max en positions, y sus begin position y end position, y hacer lo de abajo
                                        ArrayList<Integer> positions = searchPositionInSentence(sentence, ev);

                                        //TODO annotate sentence
                                        List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                                        int beg = toks.get(0).beginPosition();
                                        int end = toks.get(toks.size() - 1).endPosition();
                                        //CHOOSE TYPE
                                        String typ = "circumstance";
                                        if (frame.percProc > 0.5) {
                                            typ = "procedure";
                                        }

                                        if (positions.isEmpty()) {
                                            positions.add(token.beginPosition() + 1);
                                            positions.add(token.endPosition() + 1);

                                        }
                                        // We check the closest timex
                                        if (flagTIMEX != 1) {
                                            float dist = 1000000;
                                            float corepos = (float) (positions.get(1) + positions.get(0)) / 2;
                                            for (Entry<Float, Integer> ent : positionsTIMEX.entrySet()) {
                                                float newdist = Math.abs(corepos - ent.getKey());
                                                if (newdist < dist) {
                                                    numvalClose = ent.getValue();
                                                    dist = newdist;
                                                }

                                            }
                                        }

                                        if (positions.get(0) != -1) {
                                            inpCore = inpCore.substring(0, offset + positions.get(0)) + "<Event argument=\"what\"" + " tid=\"t" + numvalClose + "\" type=\"" + typ + "\" prov=\"eventsmattertrain" + "\">" + inpCore.substring(offset + positions.get(0), offset + positions.get(1)) + "</Event>" + inpCore.substring(offset + positions.get(1));
                                            offset = offset + offsetEvent + (" tid=\"t" + numvalClose + "\" type=\"" + typ + "\" prov=\"eventsmattertrain" + "\"").length();
                                        }
                                        if (positions.size() == 4) {

                                            // ADD WHO TO INP4
                                            inpWho = inpWho.substring(0, offsetwho + positions.get(2)) + "<Event argument=\"who\"" + " tid=\"t" + numvalClose + "\">" + inpWho.substring(offsetwho + positions.get(2), offsetwho + positions.get(3)) + "</Event>" + inpWho.substring(offsetwho + positions.get(3));
//                               inp3 = inp3.substring(0, offset + token.beginPosition() + 1) + "<Event argument=\"what\"" + " tid=\"t" + numval + "\">" + inp3.substring(offset + token.beginPosition() + 1, offset + token.endPosition() + 1) + "</Event>" + inp3.substring(offset + token.endPosition() + 1);
                                            offsetwho = offsetwho + offsetEvent - 1 + (" tid=\"t" + numvalClose + "\"").length(); // por el ev -> who
                                        }

                                        inpWhat = inpWhat.substring(0, offsetev + beg) + "<Event argument=\"ev\"" + " tid=\"t" + numvalClose + "\" type=\"" + typ + "\">" + inpWhat.substring(offsetev + beg, offsetev + end) + "</Event>" + inpWhat.substring(offsetev + end);

                                        offsetev = offsetev + offsetEventEv + "\" type=\"".length() + typ.length() + (" tid=\"t" + numvalClose + "\"").length();
                                        numval++;
                                        flag = 1;

//                        flag = 1;
                                        break;
                                    } else {
                                        flag = 1;
                                        break;
                                    }
                                }

//            System.out.println("token: word=" + word + ",  \t lemma=" + lemma + ",  \t pos=" + pos + ",  \t ne=" + ne + ",  \t normalized=" + normalized);
                            }

                        }
                        if (flag == 1) {
                            break;
                        }

                        for (Entry<String, FrameFrame> entry : frameFrames.entrySet()) {
                            String k = entry.getKey();
                            FrameFrame va  = entry.getValue();
                            if (lemma.contains(k) && ((pos.contains("V") && !pos.equalsIgnoreCase("VBG") && va.pos.equalsIgnoreCase("v")))) {// || (pos.equalsIgnoreCase("NN") && va.pos.equalsIgnoreCase("n")))) {
                                String deppar = dependencyParsing(sentence.toString());
                                int wordpos = token.index();
                                Pattern pText1 = Pattern.compile("[^\\)]*aux[^\\(]*\\(([^-]+)-(\\d+), " + word + "-" + wordpos + "\\)");
                                Matcher mText1 = pText1.matcher(deppar);
                                if (mText1.find()) {
                                    // It is an auxiliary verb, continue parsing!
                                } else {
//                                String contspar = constituencyParsing(sentence,word);
                                    FrameFrame frame = entry.getValue();
                                    Event ev = checkEventF(deppar, word, frame, token.index());
//                                if (!lemma.contains("bear") && ev.who.arrayEl.contains("applicant")) {
                                    if (!(lemma.equalsIgnoreCase("bear") && (ev.who.arrayEl.contains("applicant") || ev.who.arrayEl.contains("applicants")))) {
                                        // ADD CORE TO INP3
                                        // coger sentence, buscar los tokens min y max en positions, y sus begin position y end position, y hacer lo de abajo
                                        ArrayList<Integer> positions = searchPositionInSentence(sentence, ev);

                                        //TODO annotate sentence
                                        List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                                        int beg = toks.get(0).beginPosition();
                                        int end = toks.get(toks.size() - 1).endPosition();
                                        //CHOOSE TYPE
                                        String typ = "procedure";
//                                    String typ = "circumstance";
//                                    if(frame.percProc > 0.5){
//                                        typ = "procedure";
//                                    }

                                        if (positions.isEmpty()) {
                                            positions.add(token.beginPosition() + 1);
                                            positions.add(token.endPosition() + 1);

                                        }
                                        // We check the closest timex
                                        if (flagTIMEX != 1) {
                                            float dist = 1000000;
                                            float corepos = (float) (positions.get(1) + positions.get(0)) / 2;
                                            for (Entry<Float, Integer> ent : positionsTIMEX.entrySet()) {
                                                float newdist = Math.abs(corepos - ent.getKey());
                                                if (newdist < dist) {
                                                    numvalClose = ent.getValue();
                                                    dist = newdist;
                                                }

                                            }

                                        }

                                        if (positions.get(0) != -1) {
                                            inpCore = inpCore.substring(0, offset + positions.get(0)) + "<Event argument=\"what\"" + " tid=\"t" + numvalClose + "\" type=\"" + typ + "\" prov=\"framenet" + "\">" + inpCore.substring(offset + positions.get(0), offset + positions.get(1)) + "</Event>" + inpCore.substring(offset + positions.get(1));
                                            offset = offset + offsetEvent + (" tid=\"t" + numvalClose + "\" type=\"" + typ + "\" prov=\"framenet" + "\"").length();
                                        }
                                        if (positions.size() == 4) {

                                            // ADD WHO TO INP4
                                            inpWho = inpWho.substring(0, offsetwho + positions.get(2)) + "<Event argument=\"who\"" + " tid=\"t" + numvalClose + "\">" + inpWho.substring(offsetwho + positions.get(2), offsetwho + positions.get(3)) + "</Event>" + inpWho.substring(offsetwho + positions.get(3));
//                               inp3 = inp3.substring(0, offset + token.beginPosition() + 1) + "<Event argument=\"what\"" + " tid=\"t" + numval + "\">" + inp3.substring(offset + token.beginPosition() + 1, offset + token.endPosition() + 1) + "</Event>" + inp3.substring(offset + token.endPosition() + 1);
                                            offsetwho = offsetwho + offsetEvent - 1 + (" tid=\"t" + numvalClose + "\"").length(); // por el ev -> who
                                        }

                                        inpWhat = inpWhat.substring(0, offsetev + beg) + "<Event argument=\"ev\"" + " tid=\"t" + numvalClose + "\" type=\"" + typ + "\">" + inpWhat.substring(offsetev + beg, offsetev + end) + "</Event>" + inpWhat.substring(offsetev + end);

                                        offsetev = offsetev + offsetEventEv + "\" type=\"".length() + typ.length() + (" tid=\"t" + numvalClose + "\"").length();
                                        numval++;
                                        flag = 1;

//                        flag = 1;
                                        break;
                                    } else {
                                        flag = 1;
                                        break;
                                    }
                                }

//            System.out.println("token: word=" + word + ",  \t lemma=" + lemma + ",  \t pos=" + pos + ",  \t ne=" + ne + ",  \t normalized=" + normalized);
                            }

                        }
                        if (flag == 1) {
                            break;
                        }

//                        Properties props = new Properties();
//    // set the list of annotators to run
//    props.setProperty("annotators", "tokenize,ssplit,pos");
//    // build pipeline
//    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                    }

                    /////
                    if (flag != 1) {
                        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                            String word = token.get(CoreAnnotations.TextAnnotation.class);
////                    System.out.println("w: " + word);
                            String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
//                            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                            for (String k : topicWords) {
                                // create a document object
//                            Annotation document = pipeline.process(k);

//                            String kpos = document.get(CoreAnnotations.TokensAnnotation.class).get(0).tag();
                                if (lemma.contains(k)) {// && ((pos.contains("V") && !pos.equalsIgnoreCase("VBG") && kpos.equalsIgnoreCase("v")))) {// || (pos.equalsIgnoreCase("NN") && va.pos.equalsIgnoreCase("n")))) {

                                    Event ev = checkEventKW(word, sentence.toString(), token.index());
//                                if (!lemma.contains("bear") && ev.who.arrayEl.contains("applicant")) {

                                    if (ev != null) {
                                        ArrayList<Integer> positions = searchPositionInSentence(sentence, ev);

                                        //TODO annotate sentence
                                        List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                                        int beg = toks.get(0).beginPosition();
                                        int end = toks.get(toks.size() - 1).endPosition();
                                        //CHOOSE TYPE
                                        String typ = "circumstance";
//                                    String typ = "circumstance";
//                                    if(frame.percProc > 0.5){
//                                        typ = "procedure";
//                                    }

                                        if (positions.isEmpty()) {
                                            positions.add(token.beginPosition() + 1);
                                            positions.add(token.endPosition() + 1);

                                        }
                                        // We check the closest timex
                                        if (flagTIMEX != 1) {
                                            float dist = 1000000;
                                            float corepos = (float) (positions.get(1) + positions.get(0)) / 2;
                                            for (Entry<Float, Integer> ent : positionsTIMEX.entrySet()) {
                                                float newdist = Math.abs(corepos - ent.getKey());
                                                if (newdist < dist) {
                                                    numvalClose = ent.getValue();
                                                    dist = newdist;
                                                }

                                            }
                                        }

                                        if (positions.get(0) != -1) {
                                            inpCore = inpCore.substring(0, offset + positions.get(0)) + "<Event argument=\"what\"" + " tid=\"t" + numvalClose + "\" type=\"" + typ + "\" prov=\"deepLSim" + "\">" + inpCore.substring(offset + positions.get(0), offset + positions.get(1)) + "</Event>" + inpCore.substring(offset + positions.get(1));
                                            offset = offset + offsetEvent + (" tid=\"t" + numvalClose + "\" type=\"" + typ + "\" prov=\"deepLSim" + "\"").length();
                                        }
                                        if (positions.size() == 4) {

                                            // ADD WHO TO INP4
                                            inpWho = inpWho.substring(0, offsetwho + positions.get(2)) + "<Event argument=\"who\"" + " tid=\"t" + numvalClose + "\">" + inpWho.substring(offsetwho + positions.get(2), offsetwho + positions.get(3)) + "</Event>" + inpWho.substring(offsetwho + positions.get(3));
//                               inp3 = inp3.substring(0, offset + token.beginPosition() + 1) + "<Event argument=\"what\"" + " tid=\"t" + numval + "\">" + inp3.substring(offset + token.beginPosition() + 1, offset + token.endPosition() + 1) + "</Event>" + inp3.substring(offset + token.endPosition() + 1);
                                            offsetwho = offsetwho + offsetEvent - 1 + (" tid=\"t" + numvalClose + "\"").length(); // por el ev -> who
                                        }

                                        inpWhat = inpWhat.substring(0, offsetev + beg) + "<Event argument=\"ev\"" + " tid=\"t" + numvalClose + "\" type=\"" + typ + "\">" + inpWhat.substring(offsetev + beg, offsetev + end) + "</Event>" + inpWhat.substring(offsetev + end);

                                        offsetev = offsetev + offsetEventEv + "\" type=\"".length() + typ.length() + (" tid=\"t" + numvalClose + "\"").length();
                                        numval++;
                                        flag = 1;

//                        flag = 1;
                                        break;
                                    }

//            System.out.println("token: word=" + word + ",  \t lemma=" + lemma + ",  \t pos=" + pos + ",  \t ne=" + ne + ",  \t normalized=" + normalized);
                                }
                            }
                        }
                        /////
                    }
                    // ELSE SI NO TIMEXFLAG, AUN ASI BUSCAMOS
                } else {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
////                    System.out.println("w: " + word);
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        for (String k : topicWords) {
                            // create a document object
//                            Annotation document = pipeline.process(k);

//                            String kpos = document.get(CoreAnnotations.TokensAnnotation.class).get(0).tag();
                            if (lemma.contains(k)) {// && ((pos.contains("V") && !pos.equalsIgnoreCase("VBG") && kpos.equalsIgnoreCase("v")))) {// || (pos.equalsIgnoreCase("NN") && va.pos.equalsIgnoreCase("n")))) {
                                Event ev = checkEventKW(word, sentence.toString(), token.index());
//                                if (!lemma.contains("bear") && ev.who.arrayEl.contains("applicant")) {
                                if (ev != null) {
                                    ArrayList<Integer> positions = searchPositionInSentence(sentence, ev);

                                    //TODO annotate sentence
                                    List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                                    int beg = toks.get(0).beginPosition();
                                    int end = toks.get(toks.size() - 1).endPosition();
                                    //CHOOSE TYPE
                                    String typ = "circumstance";
//                                    String typ = "circumstance";
//                                    if(frame.percProc > 0.5){
//                                        typ = "procedure";
//                                    }

                                    if (positions.isEmpty()) {
                                        inpCore = inpCore.substring(0, offset + token.beginPosition() + 1) + "<Event argument=\"what\"" + " tid=\"t" + numval + "\" type=\"" + typ + "\">" + inpCore.substring(offset + token.beginPosition() + 1, offset + token.endPosition() + 1) + "</Event>" + inpCore.substring(offset + token.endPosition() + 1);
                                        offset = offset + "\" prov=\"deepLSim".length();

                                    } else if (positions.size() >= 2 && positions.get(0) != -1) {
                                        inpCore = inpCore.substring(0, offset + positions.get(0)) + "<Event argument=\"what\"" + " tid=\"t" + numval + "\" type=\"" + typ + "\" prov=\"deepLSim" + "\">" + inpCore.substring(offset + positions.get(0), offset + positions.get(1)) + "</Event>" + inpCore.substring(offset + positions.get(1));
                                        offset = offset + offsetEvent + (" tid=\"t" + numval + "\" type=\"" + typ + "\" prov=\"deepLSim" + "\"").length();
                                    }
                                    if (positions.size() == 4) {

                                        // ADD WHO TO INP4
                                        inpWho = inpWho.substring(0, offsetwho + positions.get(2)) + "<Event argument=\"who\"" + " tid=\"t" + numval + "\">" + inpWho.substring(offsetwho + positions.get(2), offsetwho + positions.get(3)) + "</Event>" + inpWho.substring(offsetwho + positions.get(3));
//                               inp3 = inp3.substring(0, offset + token.beginPosition() + 1) + "<Event argument=\"what\"" + " tid=\"t" + numval + "\">" + inp3.substring(offset + token.beginPosition() + 1, offset + token.endPosition() + 1) + "</Event>" + inp3.substring(offset + token.endPosition() + 1);
                                        offsetwho = offsetwho + offsetEvent - 1 + (" tid=\"t" + numval + "\"").length(); // por el ev -> who
                                    }

                                    inpWhat = inpWhat.substring(0, offsetev + beg) + "<Event argument=\"ev\"" + " tid=\"t" + numval + "\" type=\"" + typ + "\">" + inpWhat.substring(offsetev + beg, offsetev + end) + "</Event>" + inpWhat.substring(offsetev + end);

                                    offsetev = offsetev + offsetEventEv + "\" type=\"".length() + typ.length() + (" tid=\"t" + numval + "\"").length();
                                    numval++;
                                    flag = 1;

//                        flag = 1;
                                    break;

                                }
//            System.out.println("token: word=" + word + ",  \t lemma=" + lemma + ",  \t pos=" + pos + ",  \t ne=" + ne + ",  \t normalized=" + normalized);
                            }
                        }
                        if (flag == 1) {
                            break;
                        }
                    }
                }

            }

            inpCore = inpCore.replaceAll("\\.</Event>", "</Event>\\.");
            inpCore = inpCore.replaceAll(",</Event>", "</Event>,");

            inpWho = inpWho.replaceAll("THE COURT, UNANIMOUSLY", "<Event argument=\"who\" tid=\"t" + numval + "\">THE COURT</Event>, UNANIMOUSLY");

            inpWhen = inpWhen.replaceAll("\\n", "\r\n");
            inpCore = inpCore.replaceAll("\\n", "\r\n");
            inpCore = inpCore.replaceAll("<Event", "<Event_what");
            inpCore = inpCore.replaceAll("</Event", "</Event_what");
            inpWho = inpWho.replaceAll("\\n", "\r\n");
            inpWho = inpWho.replaceAll("<Event", "<Event_who");
            inpWho = inpWho.replaceAll("</Event", "</Event_who");
            inpWhat = inpWhat.replaceAll("\\n", "\r\n");

            //TODO MERGE ALL XMLS
            XMLMerger xmlM = new XMLMerger();

            String res = xmlM.mergeXML3(inpWho, inpCore);
            res = xmlM.mergeXML3(res, inpWhen);
            res = xmlM.mergeXML3(res, inpWhat);
            res = res.replaceAll("(<\\/Event><TIMEX3[^>]+>)<Event argument=\"what\"[^>]+>([^<]+)<\\/Event>(<\\/TIMEX3><Event argument=\"what\"[^>]+>[^<]+<\\/Event>)", "$1$2$3");
            res = res.replaceAll("<\\/Event><Event argument=\"ev\" [^>]+>", "");

//            System.out.println("res1:\n" + res + "\n----------\n");
            res = res.replaceAll("<TIMEX3", "<Event_when");
            res = res.replaceAll("<\\/TIMEX3", "<\\/Event_when");
            res = res.replaceAll("(<Event_who argument=\"who\"[^>]+>)([^<]*)(<\\/Event_when>)", "$2$3$1");

            res = res.replaceAll("(<Event_when [^>]+>)(<Event_what [^>]+>)([^<]+)(<\\/Event_what>)(<\\/Event_when>)", "$1$3$5");

//            System.out.println("res2:\n" + res + "\n----------\n");
            res = res.replaceAll("(<Event_[^>]+>)(<Event [^>]+>)", "$2$1");
            res = res.replaceAll("(<\\/Event>)(<\\/Event_[^>]+>)", "$2$1");

//            System.out.println("res3:\n" + res + "\n----------\n");
            res = res.replaceAll("<\\/Event><Event argument=\"ev\" [^>]+>", "");

            //
            pAnchor = Pattern.compile("<Event_when tid=(\\\"t\\d+\\\") [^>]+>([^<]+)<\\/Event_when>");

            Matcher m = pAnchor.matcher(res);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {

                String id = m.group(1);
                String inside = m.group(2);

                if (!res.substring(0, m.start()).contains("<Event argument=\"ev\" tid=" + id)) {
                    m.appendReplacement(sb, inside);
                }

            }
            m.appendTail(sb);

            res = sb.toString();

//            System.out.println("res5:\n" + res + "\n----------\n");
            res = res.replaceAll("(<Event_when [^>]+>)(<Event_what [^>]+>)([^<]+)(<\\/Event_what>)(<\\/Event_when>)", "$1$3$5");
            res = res.replaceAll("(<\\/Event_what>)([^<]*)(<\\/Event_when>)", "$2$3$1");
            res = res.replaceAll("(<\\/Event_who>)([^<]*)(<\\/Event_when>)", "$2$3$1");
            res = res.replaceAll("(<\\/Event_what>)([^<]*)(<\\/Event_who>)", "$2$3$1");
             res = res.replaceAll("(<Event_what [^>]+>)([^<]*)(<\\/Event_who>)", "$3$1$2");

//            System.out.println("res6:\n" + res + "\n----------\n");
            // LAST EVENT
            int ini = res.indexOf("FOR THESE REASONS, <Event_who argument=\"who\" ");
            if (ini != -1) {
                int end = res.indexOf("\tPresident", ini);
                if (end == -1) {
                    end = res.length() - 1;
                }
                String ending = res.substring(ini, end);

                ending = ending.replaceAll("<Event [^>]+>", "");
                ending = ending.replaceAll("<\\/Event>", "");
                ending = ending.replaceAll("<Event_what [^>]+>", "");
                ending = ending.replaceAll("<\\/Event_what>", "");

                Pattern pText4 = Pattern.compile("(\\s)*\\d+\\.(\\s*)(.+)", Pattern.MULTILINE);

                if (ending.contains("\n")) {
                    res = res.replaceAll("(<Event_who [^>]+>)([Tt]he )", "$2$1");
                    res = res.replaceAll("(<Event_who [^>]+>)([Aa]n? )", "$2$1");
                    String[] split = ending.split("\n");
                    for (String s : split) {
                        Matcher mText4 = pText4.matcher(s);
                        if (mText4.find()) {
                            ending = ending.replaceFirst(mText4.group(3), "<\\/Event_what>\n" + mText4.group(2) + "<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\">" + mText4.group(3));
                        } else if (s.startsWith("Done in")) {
                            ending = ending.substring(0, ending.indexOf(s) - 1) + "</Event_what>" + ending.substring(ending.indexOf(s) - 1, ending.indexOf(s) + s.length() - 1) + "</Event>" + ending.substring(ending.indexOf(s) + s.length() - 1);
                        }
                    }
                    ending = ending.replaceAll("(\\\n)(\\d+\\.)(<\\/Event_what>)", "$3$1$2");

                    ending = ending.replaceAll("tid=\"t\\d+\"", "tid=\"t0\"");

                    ending = ending.replaceFirst("(<\\/Event_what>)", "");
                    ending = ending.replaceAll("(\\r)(<\\/Event_what>)(\\n)", "$2$1$3");
                    ending = ending.replaceAll("([\\r\\n])(<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\">)([^\\w]+)", "$3$2");
                } else {

//System.out.println("ENDING1 :\n" + ending + "\nççççççççççççç");
                    ending = ending.substring(0, ending.indexOf("UNANIMOUSLY") + 12) + "<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\">" + ending.substring(ending.indexOf("UNANIMOUSLY") + 12);

//System.out.println("ENDING2 :\n" + ending + "\nççççççççççççç");
                    ending = ending.substring(0, ending.lastIndexOf("Done in")) + "</Event_what>" + ending.substring(ending.lastIndexOf("Done in"), ending.indexOf(".", ending.lastIndexOf("Done in"))) + "</Event>" + ending.substring(ending.indexOf(".", ending.lastIndexOf("Done in")));

//System.out.println("ENDING3 :\n" + ending + "\nççççççççççççç");       
                    ending = ending.replaceAll("tid=\"t\\d+\"", "tid=\"t0\"");

//System.out.println("ENDING4 :\n" + ending + "\nççççççççççççç");
                    ending = ending.replaceAll("(<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\">)(\\s*)", "$2$1");

                }

                res = res.substring(0, ini) + "<Event argument=\"ev\" tid=\"t0\" type=\"procedure\"[^>]*>" + ending;
            }

            res = res.replaceAll("(<Event_who argument=\"who\"[^>]+>)([^<]*)(<Event_when[^>]+>)([^<]*)(<\\/Event_when>)([^<]*)(<\\/Event_who>)", "$1$2$7$3$4$5$6");

            res = res.replaceAll("(<Event[^>]+>)(\\s*)", "$2$1");
            res = res.replaceAll("(\\s*)(<\\/Event[^>]+>)", "$2$1");

            res = res.replaceAll("(<Event_when [^>]+>)([^<\\n]+)(<Event [^>]+>)", "$3$2$1");
            res = res.replaceAll("<\\/<Event_what argument=\"what\" tid=\"t0\" type=\"procedure\"[^>]*>Event_what>", "");

            //NEWADD
            res = res.replaceAll("(<Event_what [^>]+>)([^<]*)(<Event_who [^>]+>)([^<]*)(<\\/Event_who>)", "$1$2<\\/Event_what>$3$4$5$1");
           
            System.out.println("CONJ:\n" + res + "\n----------\n");

            return res;

        } catch (Exception ex) {
            Logger.getLogger(ExtractorEvFrDL.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ExtractorEvFrDL.class.getName()).log(Level.SEVERE, null, ex);
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
     *
     * @param text one sentence (if not, it will just return the result for the
     * first sentcene)
     * @return String with the dependency parsing
     */
    public ArrayList<String> dependencyParsingTopic(String text) {

        ArrayList<String> lista = new ArrayList<String>();
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> tagged = tagger.tagSentence(sentence);
            lista.add(tagged.toString());
            GrammaticalStructure gs = parser.predict(tagged);
            lista.add(gs.typedDependenciesEnhancedPlusPlus().toString());
            return lista;
        }

        return null;
    }

    /**
     * We check the arguments of the purchase frame for an event 'sell'
     *
     * @param deppar: dependency parsing string
     * @param word: the event word
     * @param frame
     * @param pos
     * @return the frame with new information (or not)
     */
    public Event checkEventF(String deppar, String word, FrameFrame frame, int pos) {

        Event ev = new Event();
        try {

            ev.addCore(word);

            ev.addPos(pos);
            /* We check if i is a passive */
            Pattern pText1 = Pattern.compile("nsubjpass\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
            Matcher mText1 = pText1.matcher(deppar);

//        System.out.println("PRE");
            if (mText1.find()) { // passive sentence

//        System.out.println("PASSIVE");
                String word2 = mText1.group(1);
                String word2pos = mText1.group(2);
                Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                Matcher mText2 = pText2.matcher(deppar);
                ev.who.arrayEl.add(word2);
                ev.who.positions.add(Integer.parseInt(word2pos));
                while (mText2.find()) { // passive sentence
                    ev.who.arrayEl.add(mText2.group(1));
                    ev.who.positions.add(Integer.parseInt(mText2.group(2)));
                }
//                    System.out.println("PASSIVE2");

            } /* We check the subject-seller */ else {
//                System.out.println("ACTIVE");
                pText1 = Pattern.compile("nsubj[^\\(]*\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                mText1 = pText1.matcher(deppar);
                while (mText1.find()) {
                    String word2 = mText1.group(1);
                    String word2pos = mText1.group(2);
                    Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                    Matcher mText2 = pText2.matcher(deppar);
                    ev.who.arrayEl.add(word2);
                    ev.who.positions.add(Integer.parseInt(word2pos));
                    while (mText2.find()) {
                        ev.who.arrayEl.add(mText2.group(1));
                        ev.who.positions.add(Integer.parseInt(mText2.group(2)));

                    }
                }

                pText1 = Pattern.compile("obj[^\\(]*\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                mText1 = pText1.matcher(deppar);
                while (mText1.find()) {
                    String word2 = mText1.group(1);
                    String word2pos = mText1.group(2);
                    Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                    Matcher mText2 = pText2.matcher(deppar);
                    ev.core.arrayEl.add(word2);
                    ev.core.positions.add(Integer.parseInt(word2pos));
                    while (mText2.find()) {
                        ev.core.arrayEl.add(mText2.group(1));
                        ev.core.positions.add(Integer.parseInt(mText2.group(2)));

                    }
                }

//                System.out.println("ACTIVE2");
                /* We check the item, that can be an nmod:of or a dobj */
//        pText1 = Pattern.compile("nmod:of\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
                /* We check the relations previously seen in the training set for this verb in active*/
            }

            // Anadimos of y otras cosas iterativamente al core
            List<String> constRels = new ArrayList<String>();
            constRels.addAll(Arrays.asList("comp", "aux", "nmod:of"));

            List<String> arr = new ArrayList<String>();
            arr.addAll(ev.core.arrayEl);

            List<Integer> arrpos = new ArrayList<Integer>();
            arrpos.addAll(ev.core.positions);

            while (!arr.isEmpty()) {
                for (String rel : constRels) {
                    String newword = arr.get(0);
                    Integer newpos = arrpos.get(0);
                    pText1 = Pattern.compile(rel + "\\(" + newword + "-" + newpos + ", ([^-]+)-(\\d+)\\)");
                    mText1 = pText1.matcher(deppar);
                    while (mText1.find()) {
                        String word2 = mText1.group(1);
                        int word2pos = Integer.parseInt(mText1.group(2));
                        if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(word2pos) && ev.who.positions.get(0) < word2pos)) {
                            if (!ev.core.positions.contains(word2pos)) {
                                ev.core.arrayEl.add(word2);
                                ev.core.positions.add(word2pos);
                                arr.add(word2);
                                arrpos.add(word2pos);
                            }
                        }
                    }
                }
                arr.remove(0);
                arrpos.remove(0);
            }

//                System.out.println("GENERAL");
//        System.out.println(ev.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return ev;

    }

    /**
     * We check the arguments of the purchase frame for an event 'sell'
     *
     * @param deppar: dependency parsing string
     * @param word: the event word
     * @param frame
     * @param pos
     * @return the frame with new information (or not)
     */
    public Event checkEvent(String deppar, String word, Frame frame, int pos) {

        Event ev = new Event();
        try {
//        System.out.println("CHECK");
//        
//        
//        System.out.println("HOLo");
            ArrayList<String> constRels = new ArrayList<String>();
            constRels.addAll(Arrays.asList("ccomp", "xcomp", "aux", "auxpass", "aux:pass"));

//        System.out.println("HOLI " + word);
//        System.out.println("HOLI " + pos);
//        System.out.println("size pos " + ev.core.arrayEl.size());
            ev.addCore(word);

//        System.out.println("wordone ");
//        System.out.println("size pos " + ev.core.toString());
            ev.addPos(pos);
//        System.out.println("size pos2 " + ev.core.positions.size());
//        
//
//        System.out.println("HOLI " + deppar);
            /* We check if i is a passive */
            Pattern pText1 = Pattern.compile("nsubjpass\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
            Matcher mText1 = pText1.matcher(deppar);

//        System.out.println("PRE");
            if (mText1.find()) { // passive sentence

//        System.out.println("PASSIVE");
                String word2 = mText1.group(1);
                String word2pos = mText1.group(2);
                Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                Matcher mText2 = pText2.matcher(deppar);
                ev.who.arrayEl.add(word2);
                ev.who.positions.add(Integer.parseInt(word2pos));
                while (mText2.find()) { // passive sentence
                    ev.who.arrayEl.add(mText2.group(1));
                    ev.who.positions.add(Integer.parseInt(mText2.group(2)));
                }
//                    System.out.println("PASSIVE2");
                /* We check the relations previously seen in the training set for this verb in active*/
                ArrayList<String> passrelstoCheck = frame.passRels;
                passrelstoCheck.add("obl");
                passrelstoCheck.add("oblagent");
                for (String rel : passrelstoCheck) {
                    pText1 = Pattern.compile(rel + "\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                    mText1 = pText1.matcher(deppar);
                    if (mText1.find()) {
                        word2 = mText1.group(1);
                        word2pos = mText1.group(2);
                        pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                        mText2 = pText2.matcher(deppar);
                        if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(word2pos)) && ev.who.positions.get(0) < Integer.parseInt(word2pos))) {

                            ev.core.arrayEl.add(word2);
                            ev.core.positions.add(Integer.parseInt(word2pos));
                            while (mText2.find()) {
                                if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(mText2.group(2))) && ev.who.positions.get(0) < Integer.parseInt(mText2.group(2)))) {
                                    ev.core.arrayEl.add(mText2.group(1));
                                    ev.core.positions.add(Integer.parseInt(mText2.group(2)));
                                }
                            }
                        }
                    }
                }

            } /* We check the subject-seller */ else {
//                System.out.println("ACTIVE");
                pText1 = Pattern.compile("nsubj[^\\(]*\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                mText1 = pText1.matcher(deppar);
                while (mText1.find()) {
                    String word2 = mText1.group(1);
                    String word2pos = mText1.group(2);
                    Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                    Matcher mText2 = pText2.matcher(deppar);
                    ev.who.arrayEl.add(word2);
                    ev.who.positions.add(Integer.parseInt(word2pos));
                    while (mText2.find()) {
                        ev.who.arrayEl.add(mText2.group(1));
                        ev.who.positions.add(Integer.parseInt(mText2.group(2)));

                    }
                }

//                System.out.println("ACTIVE2");
                /* We check the item, that can be an nmod:of or a dobj */
//        pText1 = Pattern.compile("nmod:of\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
                /* We check the relations previously seen in the training set for this verb in active*/
                ArrayList<String> actRels = frame.actRels;
                actRels.add("obj");
                for (String rel : actRels) {
                    pText1 = Pattern.compile(rel + "\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                    mText1 = pText1.matcher(deppar);
                    if (mText1.find()) {
                        String word2 = mText1.group(1);
                        String word2pos = mText1.group(2);
                        Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                        Matcher mText2 = pText2.matcher(deppar);
                        if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(word2pos)) && ev.who.positions.get(0) < Integer.parseInt(word2pos))) {

                            ev.core.arrayEl.add(word2);
                            ev.core.positions.add(Integer.parseInt(word2pos));
                            while (mText2.find()) {
                                if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(mText2.group(2))) && ev.who.positions.get(0) < Integer.parseInt(mText2.group(2)))) {
                                    ev.core.arrayEl.add(mText2.group(1));
                                    ev.core.positions.add(Integer.parseInt(mText2.group(2)));
                                }
                            }
                        }
                    }

                }
            }

//                System.out.println("GENERAL");
            for (String rel : constRels) {
                pText1 = Pattern.compile(rel + "\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                mText1 = pText1.matcher(deppar);
                if (mText1.find()) {
                    String word2 = mText1.group(1);
                    String word2pos = mText1.group(2);
                    Pattern pText2 = Pattern.compile("([^\\(]+)\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                    Matcher mText2 = pText2.matcher(deppar);
                    if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(word2pos)) && ev.who.positions.get(0) < Integer.parseInt(word2pos))) {
                        ev.core.arrayEl.add(word2);
                        ev.core.positions.add(Integer.parseInt(word2pos));
                        while (mText2.find()) {
                            if (mText2.group(1).contains("nsubj")) {

//                System.out.println("GEN SUJ");
                            } else {

//                System.out.println("GEN NO SUJ");
                                if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(mText2.group(3))) && ev.who.positions.get(0) < Integer.parseInt(mText2.group(3)))) {
                                    ev.core.arrayEl.add(mText2.group(2));
                                    ev.core.positions.add(Integer.parseInt(mText2.group(3)));
                                }
                            }
                        }

                    }
                }
            }

            // Anadimos of y otras cosas iterativamente al core
            constRels.clear();
            constRels.addAll(Arrays.asList("comp", "aux", "nmod:of"));

            List<String> arr = new ArrayList<String>();
            arr.addAll(ev.core.arrayEl);

            List<Integer> arrpos = new ArrayList<Integer>();
            arrpos.addAll(ev.core.positions);

            while (!arr.isEmpty()) {
                for (String rel : constRels) {
                    String newword = arr.get(0);
                    Integer newpos = arrpos.get(0);
                    pText1 = Pattern.compile(rel + "\\(" + newword + "-" + newpos + ", ([^-]+)-(\\d+)\\)");
                    mText1 = pText1.matcher(deppar);
                    while (mText1.find()) {
                        String word2 = mText1.group(1);
                        int word2pos = Integer.parseInt(mText1.group(2));
                        if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(word2pos) && ev.who.positions.get(0) < word2pos)) {
                            if (!ev.core.positions.contains(word2pos)) {
                                ev.core.arrayEl.add(word2);
                                ev.core.positions.add(word2pos);
                                arr.add(word2);
                                arrpos.add(word2pos);
                            }
                        }
                    }
                }
                arr.remove(0);
                arrpos.remove(0);
            }

//        System.out.println(ev.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return ev;

    }

    /**
     *
     * @param sentence with information about the tree
     * @param word
     * @return String with the parsing tree
     */
//    public String constituencyParsing(CoreMap sentence, String word) {
//
//        System.out.println("\n\n----------------\n");
//        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
//
//        Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());
//        for (Constituent constituent : treeConstituents) {
//            if (constituent.label() != null
//                    && (constituent.label().toString().equals("VP")) && tree.getLeaves().get(constituent.start()).toString().equalsIgnoreCase(word)) {
//                System.err.println("found constituent: " + constituent.toString());
//                System.err.println(tree.getLeaves().subList(constituent.start(), constituent.end() + 1));
//            }
//        }
//
//        System.out.println(tree);
//        System.out.println("\n\n----------------\n");
//        return tree.toString();
//    }
    private ArrayList<Integer> searchPositionInSentence(CoreMap sentence, Event ev) {
        ArrayList<Integer> pos = new ArrayList<Integer>();
        List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);

        // CORE
        List<Integer> whatpos = ev.core.positions;
        if (!whatpos.isEmpty()) {
            Collections.sort(whatpos);
            Integer coremin = whatpos.get(0) - 1;
            Integer coremax = whatpos.get(whatpos.size() - 1) - 1;

            pos.add(toks.get(coremin).beginPosition());
            pos.add(toks.get(coremax).endPosition());
        } else {
            pos.add(-1);
            pos.add(-1);
        }

        // WHO
        List<Integer> whopos = ev.who.positions;

        if (!whopos.isEmpty()) {
            Collections.sort(whopos);

            Integer whomin = whopos.get(0) - 1;
            Integer whomax = whopos.get(whopos.size() - 1) - 1;

            pos.add(toks.get(whomin).beginPosition());
            pos.add(toks.get(whomax).endPosition());
        }

        return pos;

    }

    public ArrayList<String> getTopic(Document doc) {
        ArrayList<String> topicParts = doc.getTopicParts();
        LinkedHashSet<String> words = new LinkedHashSet<>();

        for (String s : topicParts) {
            ArrayList<String> lista = dependencyParsingTopic(s);
            String nns = lista.get(0);
            String deppar = lista.get(1);

            if (deppar.contains("),")) {
//                final String regex = "(\\w+)\\/NN";
                final String regex = "(\\w+)\\/(\\w+)";
                final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                final Matcher matcher = pattern.matcher(nns.substring(1));

                if (matcher.find()) {
                    if (matcher.group(2).startsWith("N") || matcher.group(2).startsWith("V")) {
                        words.add(matcher.group(1).toLowerCase());
                    }
                }

                if (matcher.find()) {
                    if (matcher.group(2).startsWith("N") || matcher.group(2).startsWith("V")) {
                        words.add(matcher.group(1).toLowerCase());
                    }
                }

                final String regex2 = "compound\\(([^-]+)-\\d+, ([^-]+)-\\d+\\)";
                final Pattern pattern2 = Pattern.compile(regex2, Pattern.MULTILINE);
                final Matcher matcher2 = pattern2.matcher(deppar);

                while (matcher2.find()) {
                    words.add(matcher2.group(2).toLowerCase() + "_" + matcher2.group(1).toLowerCase());
                }
            }

        }

        return new ArrayList<String>(words);
    }

    private Event checkEventKW(String wordK, String sentence, int posK) {
        // Look for main event (verb?) and mark until keyword
        ArrayList<String> res = dependencyParsingTopic(sentence);
        String nns = res.get(0);
        String deppar = res.get(1);
        Event ev = new Event();
        String word = "";
        int pos = -1;
        ev.core.arrayEl.add(wordK);
        ev.core.positions.add(posK);

        // Find main verb (root)
        Pattern pTextX = Pattern.compile("root\\(ROOT-0, ([^-]+)-(\\d+)\\)");
        Matcher mTextX = pTextX.matcher(deppar);

        if (mTextX.find()) {
            word = mTextX.group(1);
            pos = Integer.parseInt(mTextX.group(2));

            final String regex = word + "\\/(\\w+)";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(nns.substring(1));

            if (matcher.find()) {
                if (!matcher.group(1).startsWith("V")) {
                    return null;
                }
            }

            if (pos == posK || deppar.contains("(" + word + "-" + pos + ", " + wordK + "-" + posK + ")")) { // the word is related to the main verb somehow, good

                try {

                    ArrayList<String> constRels = new ArrayList<String>();
                    constRels.addAll(Arrays.asList("ccomp", "xcomp", "aux", "auxpass", "aux:pass"));

                    ev.addCore(word);

                    ev.addPos(pos);

                    /* We check if i is a passive */
                    Pattern pText1 = Pattern.compile("nsubjpass\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                    Matcher mText1 = pText1.matcher(deppar);

                    if (mText1.find()) { // passive sentence

                        String word2 = mText1.group(1);
                        String word2pos = mText1.group(2);
                        Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                        Matcher mText2 = pText2.matcher(deppar);
                        ev.who.arrayEl.add(word2);
                        ev.who.positions.add(Integer.parseInt(word2pos));
                        while (mText2.find()) { // passive sentence
                            ev.who.arrayEl.add(mText2.group(1));
                            ev.who.positions.add(Integer.parseInt(mText2.group(2)));
                        }
//                    System.out.println("PASSIVE2");
                        /* We check the relations previously seen in the training set for this verb in active*/
                        ArrayList<String> passrelstoCheck = new ArrayList<String>();
                        passrelstoCheck.add("obl");
                        passrelstoCheck.add("oblagent");
                        for (String rel : passrelstoCheck) {
                            pText1 = Pattern.compile(rel + "\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                            mText1 = pText1.matcher(deppar);
                            if (mText1.find()) {
                                word2 = mText1.group(1);
                                word2pos = mText1.group(2);
                                pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                                mText2 = pText2.matcher(deppar);
                                if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(word2pos)) && ev.who.positions.get(0) < Integer.parseInt(word2pos))) {

                                    ev.core.arrayEl.add(word2);
                                    ev.core.positions.add(Integer.parseInt(word2pos));
                                    while (mText2.find()) {
                                        if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(mText2.group(2))) && ev.who.positions.get(0) < Integer.parseInt(mText2.group(2)))) {
                                            ev.core.arrayEl.add(mText2.group(1));
                                            ev.core.positions.add(Integer.parseInt(mText2.group(2)));
                                        }
                                    }
                                }
                            }
                        }

                    } /* We check the subject-seller */ else {
//                System.out.println("ACTIVE");
                        pText1 = Pattern.compile("nsubj[^\\(]*\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                        mText1 = pText1.matcher(deppar);
                        while (mText1.find()) {
                            String word2 = mText1.group(1);
                            String word2pos = mText1.group(2);
                            Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                            Matcher mText2 = pText2.matcher(deppar);
                            ev.who.arrayEl.add(word2);
                            ev.who.positions.add(Integer.parseInt(word2pos));
                            while (mText2.find()) {
                                ev.who.arrayEl.add(mText2.group(1));
                                ev.who.positions.add(Integer.parseInt(mText2.group(2)));

                            }
                        }

                        /* We check the relations previously seen in the training set for this verb in active*/
                        ArrayList<String> actRels = new ArrayList<String>();
                        actRels.add("obj");
                        for (String rel : actRels) {
                            pText1 = Pattern.compile(rel + "\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                            mText1 = pText1.matcher(deppar);
                            if (mText1.find()) {
                                String word2 = mText1.group(1);
                                String word2pos = mText1.group(2);
                                Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                                Matcher mText2 = pText2.matcher(deppar);
                                if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(word2pos)) && ev.who.positions.get(0) < Integer.parseInt(word2pos))) {

                                    ev.core.arrayEl.add(word2);
                                    ev.core.positions.add(Integer.parseInt(word2pos));
                                    while (mText2.find()) {
                                        if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(mText2.group(2))) && ev.who.positions.get(0) < Integer.parseInt(mText2.group(2)))) {
                                            ev.core.arrayEl.add(mText2.group(1));
                                            ev.core.positions.add(Integer.parseInt(mText2.group(2)));
                                        }
                                    }
                                }
                            }

                        }
                    }

//                System.out.println("GENERAL");
                    for (String rel : constRels) {
                        pText1 = Pattern.compile(rel + "\\(" + word + "-" + pos + ", ([^-]+)-(\\d+)\\)");
                        mText1 = pText1.matcher(deppar);
                        if (mText1.find()) {
                            String word2 = mText1.group(1);
                            String word2pos = mText1.group(2);
                            Pattern pText2 = Pattern.compile("([^\\(]+)\\(" + word2 + "-" + word2pos + ", ([^-]+)-(\\d+)\\)");
                            Matcher mText2 = pText2.matcher(deppar);
                            if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(word2pos)) && ev.who.positions.get(0) < Integer.parseInt(word2pos))) {
                                ev.core.arrayEl.add(word2);
                                ev.core.positions.add(Integer.parseInt(word2pos));
                                while (mText2.find()) {
                                    if (mText2.group(1).contains("nsubj")) {

//                System.out.println("GEN SUJ");
                                    } else {

//                System.out.println("GEN NO SUJ");
                                        if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(mText2.group(3))) && ev.who.positions.get(0) < Integer.parseInt(mText2.group(3)))) {
                                            ev.core.arrayEl.add(mText2.group(2));
                                            ev.core.positions.add(Integer.parseInt(mText2.group(3)));
                                        }
                                    }
                                }

                            }
                        }
                    }

                    // Anadimos of y otras cosas iterativamente al core
                    constRels.clear();
                    constRels.addAll(Arrays.asList("comp", "aux", "nmod:of"));

                    List<String> arr = new ArrayList<String>();
                    arr.addAll(ev.core.arrayEl);

                    List<Integer> arrpos = new ArrayList<Integer>();
                    arrpos.addAll(ev.core.positions);

                    while (!arr.isEmpty()) {
                        for (String rel : constRels) {
                            String newword = arr.get(0);
                            Integer newpos = arrpos.get(0);
                            pText1 = Pattern.compile(rel + "\\(" + newword + "-" + newpos + ", ([^-]+)-(\\d+)\\)");
                            mText1 = pText1.matcher(deppar);
                            while (mText1.find()) {
                                String word2 = mText1.group(1);
                                int word2pos = Integer.parseInt(mText1.group(2));
                                if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(word2pos) && ev.who.positions.get(0) < word2pos)) {
                                    if (!ev.core.positions.contains(word2pos)) {
                                        ev.core.arrayEl.add(word2);
                                        ev.core.positions.add(word2pos);
                                        arr.add(word2);
                                        arrpos.add(word2pos);
                                    }
                                }
                            }
                        }
                        arr.remove(0);
                        arrpos.remove(0);
                    }

//        System.out.println(ev.toString());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

            } else {
                return null;
            }

        } else {
            return null;
        }

        return ev;

    }

    public ArrayList<String> getSemanticField(ArrayList<String> topic) {
        Pattern pAnchor = Pattern.compile("\\('(\\w+)', (\\d\\.\\d+)\\)");
        final String urlDL = "https://terminoteca.linkeddata.es/nlp/similar?w=";
        ArrayList<String> terms = new ArrayList<String>();

        for (String wordT : topic) {
            terms.add(wordT);
            try {
                URL url = new URL(urlDL + wordT);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                InputStream stream = connection.getInputStream();
                String outm = IOUtils.toString(stream, "UTF-8");

                Matcher m = pAnchor.matcher(outm);
                while (m.find()) {
                    String term = m.group(1);
                    Float sim = Float.parseFloat(m.group(2));
                    if (sim > 0.8) {
                        terms.add(term);
                    } else {
                        break;
                    }

                }

            } catch (Exception ex) {
//                int code = connection.getResponseCode();
//                String msg = connection.getResponseMessage();

            }
        }

        return terms;
    }
}
