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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import oeg.eventRepresentation.Event;
import oeg.eventRepresentation.Frame;
import oeg.tagger.docHandler.Document;
import oeg.tagger.docHandler.DocumentPart;
import oeg.tagger.docHandler.StructureExtractorECHR;
import oeg.tagger.docHandler.XMLMerger;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;

/**
 * ExtractorTIMEXKeywordBased core class, where the rules are applied and the
 * normalization algorithm is.
 *
 * @author mnavas
 */
public class ExtractorTIMEXKeywordBasedNEnoTIMEXflag {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExtractorTIMEXKeywordBasedNEnoTIMEXflag.class);

//    PrintWriter out;
    String eventFile;
    String rules;
    Properties properties = new Properties();
    String posModel;
    String lemmaModel;
    StanfordCoreNLP pipeline;

    HashMap<String, Frame> eventFrames;

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
    public ExtractorTIMEXKeywordBasedNEnoTIMEXflag() {
        init();
    }

    public ExtractorTIMEXKeywordBasedNEnoTIMEXflag(String language) {
        lang = language;
        init();
    }

    public ExtractorTIMEXKeywordBasedNEnoTIMEXflag(String pos, String lemma, String rul, String language, String events) {
        posModel = pos;
        lemmaModel = lemma;
        rules = rul;
        lang = language;
        eventFile = events;
        init();
    }

    public ExtractorTIMEXKeywordBasedNEnoTIMEXflag(String rul, String language) {
        rules = rul;
        lang = language;
        init();
    }

    public void init() {

        FileInputStream fileIn = null;

        if (eventFile == null) {
            eventFile = ".\\src\\main\\resources\\events.ser";
        }

        try {
            fileIn = new FileInputStream(eventFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            eventFrames = (HashMap<String, Frame>) in.readObject();
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
            Logger.getLogger(ExtractorTIMEXKeywordBasedNEnoTIMEXflag.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileIn.close();
            } catch (IOException ex) {
                Logger.getLogger(ExtractorTIMEXKeywordBasedNEnoTIMEXflag.class.getName()).log(Level.SEVERE, null, ex);
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

    public String annotate(String input, String anchorDate, File wordfile, String filename) {

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
            String inpCore = input;  //CORE
            String inpWho = input;  //WHO
            String inpWhat = input;  //WHAT
            int flagRN = 0;

            inpWhen = inpWhen.replaceAll("\\r\\n", "\n");

            int offsetdelay = 0;
            int numval = 2;
            Annotation annotation = new Annotation(inpWhen);

            pipeline.annotate(annotation);

            int offset = 0;
            int offsetwho = 0;
            int offsetwhat = 0;
            int offsetEvent = "<Event argument=\"core\">".length() + "</Event>".length();

            StructureExtractorECHR seECHR = new StructureExtractorECHR();
            Document doc = seECHR.extractFromDocument(wordfile);
            List<DocumentPart> eventRelevantParts = doc.getEventRelevantParts();

            List<CoreMap> sentencesAll = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            List<CoreMap> sentences = new ArrayList<CoreMap>();
            if (eventRelevantParts.isEmpty()) {
                sentences = sentencesAll;
            } else {
                DocumentPart p = doc.getProcedure();
                for (CoreMap sentence : sentencesAll) {
                    List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                    Integer beginSentence = toks.get(0).beginPosition();
                    if(sentence.toString().startsWith("The case originated in ") && beginSentence >= p.offset_ini && beginSentence < p.offset_end){
                                                    sentences.add(sentence);

                    } else{
                    
                    for (DocumentPart erp : eventRelevantParts) {
                        if (beginSentence >= erp.offset_ini && beginSentence < erp.offset_end) {
                            sentences.add(sentence);
                            break;
                        }
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

                        Set<String> durString = durations.keySet();

                        for (String gran : durString) {

                            int plusI = Integer.valueOf(durations.get(gran));

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
                        Set<String> durString = auxVal.keySet();
                        for (String gran : durString) {
                            if ((gran.equalsIgnoreCase("AF") || gran.equalsIgnoreCase("MO") || gran.equalsIgnoreCase("MI") || gran.equalsIgnoreCase("EV") || gran.equalsIgnoreCase("NI")) && flagT == 0) {
                                flagT = 1;
                                auxfin = auxfin + "T" + auxVal.get(gran).replaceFirst("\\.0", "") + gran;
                            } else if (gran.equalsIgnoreCase("H") && flagT == 0) {
                                flagT = 1;
                                auxfin = auxfin + "T" + auxVal.get(gran).replaceFirst("\\.0", "") + gran;
                            } else if (gran.equalsIgnoreCase("MIN") && flagT == 0) {
                                flagT = 1;
                                auxfin = auxfin + "T" + auxVal.get(gran).replaceFirst("\\.0", "") + "M";
                            } else if (gran.equalsIgnoreCase("HALF")) {
                                flagT = 1;
                                auxfin = auxfin + auxVal.get(gran).replaceFirst("\\.0", "") + "H";
                            } else if (gran.equalsIgnoreCase("S") && flagT == 0) {
                                flagT = 1;
                                auxfin = auxfin + "T" + auxVal.get(gran).replaceFirst("\\.0", "") + gran;
                            } else {
                                auxfin = auxfin + auxVal.get(gran).replaceFirst("\\.0", "") + gran;
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

                    if (typ.equalsIgnoreCase("DATE") && !inpWhen.substring(ini + offsetdelay - "decision of ".length(), ini + offsetdelay).equalsIgnoreCase("decision of ") && !inpWhen.substring(ini + offsetdelay - "judgment of ".length(), ini + offsetdelay).equalsIgnoreCase("judgment of ")) {
//                    if (typ.equalsIgnoreCase("DATE") && !inp2.substring(ini + offsetdelay - " of ".length(), ini + offsetdelay).equalsIgnoreCase(" of ")) {
                        flagTIMEX = 1;
                        inpWhen = inpWhen.substring(0, ini + offsetdelay) + toAdd + inpWhen.substring(ini + text.length() + offsetdelay);
                        offsetdelay = offsetdelay + toAdd.length() - text.length();

                    }

                }
                int flag = 0;
//                if (flagTIMEX == 1) { // Otra opcion es sumar uno por cada TIMEX, pero tampoco es una relacion uno a uno...
                    if(sentence.toString().startsWith("The case originated in")){ // FIRST EVENT
                        int flagapp = 0;
                         for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                             String word = token.get(CoreAnnotations.TextAnnotation.class);
                             String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                             
                             if(word.equalsIgnoreCase("lodged")){
                                 inpCore = inpCore.substring(0, offset + token.beginPosition()) + "<Event argument=\"core\"" + " tid=\"t" + "1" + "\">" + inpCore.substring(offset + token.beginPosition(), offset + token.endPosition()) + "</Event>" + inpCore.substring(offset + token.endPosition());
                                 offset = offset + offsetEvent + (" tid=\"t" + "1" + "\"").length();

                             } else if(lemma.equalsIgnoreCase("application") && flagapp == 0){
                                 inpWho = inpWho.substring(0, offsetwho + token.beginPosition()) + "<Event argument=\"who\"" + " tid=\"t" + "1" + "\">" + inpWho.substring(offsetwho + token.beginPosition(), offsetwho + token.endPosition()) + "</Event>" + inpWho.substring(offsetwho + token.endPosition());                                 
                                 offsetwho = offsetwho + offsetEvent - 1 + (" tid=\"t" + "1" + "\"").length(); // por el what -> who
                                 flagapp = 1;
                             }
                         }
                         
                         List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                                    int beg = toks.get(0).beginPosition();
                                    int end = toks.get(toks.size() - 1).endPosition();
                                    String typ = "procedure";
                                    inpWhat = inpWhat.substring(0, offsetwhat + beg) + "<Event argument=\"what\"" + " tid=\"t" + "1" + "\" type=\"" + typ + "\">" + inpWhat.substring(offsetwhat + beg, offsetwhat + end) + "</Event>" + inpWhat.substring(offsetwhat + end);

                                    // HOMOG. WHENS
                                    if(offsetwhat == 0){
                                        int oldlength = inpWhen.length();
                                        inpWhen = inpWhen.replaceAll("id=\"t(\\d+)\"", "id=\"t1\"");
                                        offsetdelay = offsetdelay - (oldlength - inpWhen.length());
                                    }
                                    offsetwhat = offsetwhat + offsetEvent + "\" type=\"".length() + typ.length() + (" tid=\"t" + "1" + "\"").length();
                         
                         

                    } else{
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    /* We collect the different tags of each token */
                    String word = token.get(CoreAnnotations.TextAnnotation.class);
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                    String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

                    /* Verb Event */
                    flag = 0;

                    for (String k : eventFrames.keySet()) {
                        if (lemma.contains(k) && pos.contains("V") && !pos.equalsIgnoreCase("VBG")) {
                            String deppar = dependencyParsing(sentence.toString());
                            int wordpos = token.index();
                            Pattern pText1 = Pattern.compile("[^\\)]*aux[^\\(]*\\(([^-]+)-(\\d+), " + word + "-" + wordpos + "\\)");
                            Matcher mText1 = pText1.matcher(deppar);
                            if (mText1.find()) {
                                // It is an auxiliary verb, continue parsing!
                            } else {
//                                String contspar = constituencyParsing(sentence,word);
                                Frame frame = eventFrames.get(k);
                                Event ev = checkEvent(deppar, word, frame, token.index());
//                                if (!lemma.contains("bear") && ev.who.arrayEl.contains("applicant")) {
                                if (!(lemma.equalsIgnoreCase("bear") && (ev.who.arrayEl.contains("applicant") || ev.who.arrayEl.contains("applicants")))) {
                                    // ADD CORE TO INP3
                                    // coger sentence, buscar los tokens min y max en positions, y sus begin position y end position, y hacer lo de abajo
                                    ArrayList<Integer> positions = searchPositionInSentence(sentence, ev);
                                    
                                    if (positions.isEmpty()) {
                                        inpCore = inpCore.substring(0, offset + token.beginPosition() + 1) + "<Event argument=\"core\"" + " tid=\"t" + numval + "\">" + inpCore.substring(offset + token.beginPosition() + 1, offset + token.endPosition() + 1) + "</Event>" + inpCore.substring(offset + token.endPosition() + 1);

                                    } else if (positions.size() >= 2 && positions.get(0) != -1) {
                                        inpCore = inpCore.substring(0, offset + positions.get(0)) + "<Event argument=\"core\"" + " tid=\"t" + numval + "\">" + inpCore.substring(offset + positions.get(0), offset + positions.get(1)) + "</Event>" + inpCore.substring(offset + positions.get(1));
                                        offset = offset + offsetEvent + (" tid=\"t" + numval + "\"").length();
                                    }
                                    if (positions.size() == 4) {

                                        // ADD WHO TO INP4
                                        inpWho = inpWho.substring(0, offsetwho + positions.get(2)) + "<Event argument=\"who\"" + " tid=\"t" + numval + "\">" + inpWho.substring(offsetwho + positions.get(2), offsetwho + positions.get(3)) + "</Event>" + inpWho.substring(offsetwho + positions.get(3));
//                               inp3 = inp3.substring(0, offset + token.beginPosition() + 1) + "<Event argument=\"core\"" + " tid=\"t" + numval + "\">" + inp3.substring(offset + token.beginPosition() + 1, offset + token.endPosition() + 1) + "</Event>" + inp3.substring(offset + token.endPosition() + 1);
                                        offsetwho = offsetwho + offsetEvent - 1 + (" tid=\"t" + numval + "\"").length(); // por el what -> who
                                    }

                                    //TODO annotate sentence
                                    List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
                                    int beg = toks.get(0).beginPosition();
                                    int end = toks.get(toks.size() - 1).endPosition();
                                    //CHOOSE TYPE
                                    String typ = "circumstance";
                                    if(frame.percProc > 0.5){
                                        typ = "procedure";
                                    }
                                    inpWhat = inpWhat.substring(0, offsetwhat + beg) + "<Event argument=\"what\"" + " tid=\"t" + numval + "\" type=\"" + typ + "\">" + inpWhat.substring(offsetwhat + beg, offsetwhat + end) + "</Event>" + inpWhat.substring(offsetwhat + end);

                                    offsetwhat = offsetwhat + offsetEvent + "\" type=\"".length() + typ.length() + (" tid=\"t" + numval + "\"").length();
                                    numval++;
                                    flag = 1;

//                        flag = 1;
                                    break;
                                } else{
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
                }
                    }

//            }
            }

//            inpWhen = inpWhen.replaceAll(" , ", ", ");
//            inpCore = inpCore.replaceAll(" , ", ", ");
            inpCore = inpCore.replaceAll("\\.</Event>", "</Event>\\.");
            inpCore = inpCore.replaceAll(",</Event>", "</Event>,");
//            inpWho = inpWho.replaceAll(" , ", ", ");
//            inpWhat = inpWhat.replaceAll(" , ", ", ");

            inpWho = inpWho.replaceAll("THE COURT, UNANIMOUSLY", "<Event argument=\"who\" id=\"t" + numval + "\">THE COURT</Event>, UNANIMOUSLY");
//TODO lodge application y resto de arguments de unanimously

            inpWhen = inpWhen.replaceAll("\\n", "\r\n");
            inpCore = inpCore.replaceAll("\\n", "\r\n");
            inpCore = inpCore.replaceAll("<Event", "<Event_core");
            inpCore = inpCore.replaceAll("</Event", "</Event_core");
            inpWho = inpWho.replaceAll("\\n", "\r\n");
            inpWho = inpWho.replaceAll("<Event", "<Event_who");
            inpWho = inpWho.replaceAll("</Event", "</Event_who");
            inpWhat = inpWhat.replaceAll("\\n", "\r\n");

            //TODO MERGE ALL XMLS
            XMLMerger xmlM = new XMLMerger();
//            System.out.println("TIMEX:\n" + inpWhen + "\n----------\n");
//            System.out.println("EVS:\n" + inpCore + "\n----------\n");
//            System.out.println("WHO:\n" + inpWho + "\n----------\n");
//            System.out.println("WHAT:\n" + inpWhat + "\n----------\n");
//            String res = xmlM.mergeXML(inpCore, inpWhen);
//            String res = xmlM.mergeXML(inpWho, inpWhen);
//            res = xmlM.mergeXML3(res, inpCore);
//            res = xmlM.mergeXML3(res, inpWhat);

            String res = xmlM.mergeXML3(inpWho, inpCore);
            res = xmlM.mergeXML3(res, inpWhen);
            res = xmlM.mergeXML3(res, inpWhat);
            res = res.replaceAll("(<\\/Event><TIMEX3[^>]+>)<Event argument=\"core\"[^>]+>([^<]+)<\\/Event>(<\\/TIMEX3><Event argument=\"core\"[^>]+>[^<]+<\\/Event>)", "$1$2$3");
            res = res.replaceAll("<\\/Event><Event argument=\"what\" [^>]+>", "");
            
//            System.out.println("res1:\n" + res + "\n----------\n");

            res = res.replaceAll("<TIMEX3", "<Event_when");
            res = res.replaceAll("<\\/TIMEX3", "<\\/Event_when");
            res = res.replaceAll("(<Event_who argument=\"who\"[^>]+>)([^<]*)(<\\/Event_when>)", "$2$3$1");
            res = res.replaceAll("(<Event_when [^>]+>)(<Event_core [^>]+>)([^<]+)(<\\/Event_core>)(<\\/Event_when>)", "$1$3$5");
            
//            System.out.println("res2:\n" + res + "\n----------\n");
            res = res.replaceAll("(<Event_[^>]+>)(<Event [^>]+>)", "$2$1");
            res = res.replaceAll("(<\\/Event>)(<\\/Event_[^>]+>)", "$2$1");
            
//            System.out.println("res3:\n" + res + "\n----------\n");


            res = res.replaceAll("<\\/Event><Event argument=\"what\" [^>]+>", "");

            //
            pAnchor = Pattern.compile("<Event_when tid=(\\\"t\\d+\\\") [^>]+>([^<]+)<\\/Event_when>");
            
            
//            System.out.println("res4:\n" + res + "\n----------\n");


            Matcher m = pAnchor.matcher(res);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {

                String id = m.group(1);
                String inside = m.group(2);

                if (!res.substring(0,m.start()).contains("<Event argument=\"what\" tid=" + id)) {
                    m.appendReplacement(sb, inside);
                }

            }
            m.appendTail(sb);

            res = sb.toString();
            
            
//            System.out.println("res5:\n" + res + "\n----------\n");

            
            res = res.replaceAll("(<Event_when [^>]+>)(<Event_core [^>]+>)([^<]+)(<\\/Event_core>)(<\\/Event_when>)", "$1$3$5");
            res = res.replaceAll("(<\\/Event_core>)([^<]*)(<\\/Event_when>)", "$2$3$1");
            res = res.replaceAll("(<\\/Event_who>)([^<]*)(<\\/Event_when>)", "$2$3$1");
            res = res.replaceAll("(<\\/Event_core>)([^<]*)(<\\/Event_who>)", "$2$3$1");
            res = res.replaceAll("(<Event_core [^>]+>)([^<]+)(<\\/Event_who>)", "$3$1$2");
            
            res = res.replaceAll("(<Event_who [^>]+>)([Tt]he )", "$2$1");
 
            
//            System.out.println("res6:\n" + res + "\n----------\n");


             
            // LAST EVENT
            int ini = res.indexOf("FOR THESE REASONS, <Event_who argument=\"who\" ");
            if(ini != -1){
                int end = res.indexOf("\tPresident", ini);
                if(end==-1){
                    end=res.length()-1;
                }
            String ending = res.substring(ini, end);
            
            ending = ending.replaceAll("<Event [^>]+>", "");
            ending = ending.replaceAll("<\\/Event>", "");
            ending = ending.replaceAll("<Event_core [^>]+>", "");
            ending = ending.replaceAll("<\\/Event_core>", "");
            
            Pattern pText4 = Pattern.compile("(\\s)*\\d+\\.(\\s*)(.+)", Pattern.MULTILINE);
        
            String[] split = ending.split("\n");
            for(String s : split){
                Matcher mText4 = pText4.matcher(s);
                if(mText4.find()){
                    ending = ending.replaceFirst(mText4.group(3), "<\\/Event_core>\n" + mText4.group(2) + "<Event_core argument=\"core\" tid=\"t0\">" + mText4.group(3));
                } else if(s.startsWith("Done in")){
                    ending = ending.substring(0, ending.indexOf(s)-1) + "</Event_core>" + ending.substring(ending.indexOf(s)-1, ending.indexOf(s) + s.length() -1) + "</Event>" + ending.substring(ending.indexOf(s) + s.length()-1);
                }
            }
            ending = ending.replaceAll("(\\\n)(\\d+\\.)(<\\/Event_core>)", "$3$1$2");
//            ending = ending.replaceAll("(\\r?\\n\\r?)(<\\/Event_core>)Done", "$2$1Done");
            ending = ending.replaceAll("id=\"t(\\d+)\"", "id=\"t0\"");
//            ending = ending.replaceAll("(\\s)(<Event_core argument=\"core\" tid=\"t0\">)([ ]+)(.)", "$3$2$4");
            ending = ending.replaceFirst("(<\\/Event_core>)", "");
            ending = ending.replaceAll("(\\r)(<\\/Event_core>)(\\n)", "$2$1$3");
            ending = ending.replaceAll("([\\r\\n])(<Event_core argument=\"core\" tid=\"t0\">)([^\\w]+)", "$3$2");
            
            
//            System.out.println("res7:\n" + res + "\n----------\n");

//            // FIRST EVENT
//            CoreMap sent = null;
//            DocumentPart erp = doc.getProcedure();
//            for (CoreMap sentence : sentencesAll) {
//                    List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
//                    Integer beginSentence = toks.get(0).beginPosition();
//                        if (beginSentence >= erp.offset_ini && beginSentence < erp.offset_end && sentence.toString().startsWith("The case originated in")) {
//                            sent = sentence;
//                            break;
//                        }
//                    
//                }
//            
//            if(sent!=null){
//                List<CoreLabel> toks = sent.get(CoreAnnotations.TokensAnnotation.class);
//
////                dependencyParsing()
//            }
            
            
            
            
            
            res = res.substring(0, ini) + "<Event argument=\"what\" tid=\"t0\" type=\"procedure\">" + ending;
        }
            res = res.replace("</<Event_core argument=\"core\" tid=\"t0\">Event_core>", "");
            System.out.println("CONJ:\n" + res + "\n----------\n");
            
            

            return res;

        } catch (Exception ex) {
            Logger.getLogger(ExtractorTIMEXKeywordBasedNEnoTIMEXflag.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ExtractorTIMEXKeywordBasedNEnoTIMEXflag.class.getName()).log(Level.SEVERE, null, ex);
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
     * @param pos
     * @return the frame with new information (or not)
     */
    public Event checkEvent(String deppar, String word, Frame frame, int pos) {

        Event ev = new Event();
        ArrayList<String> constRels = new ArrayList<String>();
        constRels.addAll(Arrays.asList("ccomp", "xcomp", "aux", "auxpass", "aux:pass"));

        ev.core.arrayEl.add(word);
        ev.core.positions.add(pos);

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

//            pText1 = Pattern.compile("nmod:agent\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//            mText1 = pText1.matcher(deppar);
//            if (mText1.find()) {
//                // Passive sentence
//                if (mText1.find()) { // passive sentence
//                    word2 = mText1.group(1);
//                    pText2 = Pattern.compile("[^\\(]+\\(" + word2 + "-\\d+, ([^-]+)-\\d+\\)");
//                    mText2 = pText2.matcher(deppar);
//                    frame.core.arrayEl.add(word2);
//                    while (mText2.find()) { // passive sentence
//                        frame.core.arrayEl.add(mText2.group(1));
//                    }
//                }
//            }
            /* We check the relations previously seen in the training set for this verb in active*/
            for (String rel : frame.passRels) {
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

            /* We check the item, that can be an nmod:of or a dobj */
//        pText1 = Pattern.compile("nmod:of\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
            /* We check the relations previously seen in the training set for this verb in active*/
            for (String rel : frame.actRels) {
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

//        pText1 = Pattern.compile("dobj\\(" + word + "-\\d+, ([^-]+)-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
//        if (mText1.find()) {
//            String word2 = mText1.group(1);
//            Pattern pText2 = Pattern.compile("[^\\(]+\\(" + word2 +  "-\\d+, ([^-]+)-\\d+\\)");
//            Matcher mText2 = pText2.matcher(deppar);
//            while (mText2.find()){ // passive sentence
//               x = frame.who.arrayEl;
//               x.add(word2);
//               frame.who.arrayEl = x;
//            }
//        }
            }
//        if(frame.who.arrayEl.isEmpty()){
//            
//            pText1 = Pattern.compile("\\(([^-]+)-\\d+, " + word + "-\\d+\\)");
//        mText1 = pText1.matcher(deppar);
//        if (mText1.find()) {
//            String word2 = mText1.group(1);
//            if(!word2.equalsIgnoreCase("root")){
//                
//            
//            frame = checkEvent(deppar,word2);
//        }
//            }
        }
        
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
                            if(mText2.group(1).contains("nsubj")){
                                
//                                String word3 = mText2.group(2);
//                                String word3pos = mText2.group(3);
//                                ev.who.arrayEl.add(word3);
//                                ev.who.positions.add(Integer.parseInt(word3pos));
////                                Pattern pText3 = Pattern.compile("[^\\(]+\\(" + word3 + "-" + word3pos + ", ([^-]+)-(\\d+)\\)");
////                                Matcher mText3 = pText3.matcher(deppar);
////                                while (mText3.find()) {
////                                    ev.who.arrayEl.add(mText3.group(1));
////                                    ev.who.positions.add(Integer.parseInt(mText3.group(2)));
////
////                                }
//                                if (!ev.who.positions.contains(Integer.parseInt(word2pos)) && ev.who.positions.get(0) < Integer.parseInt(word2pos)) {
                                    
//                            }
                            
                            }
                            else{
                            if (ev.who.positions.isEmpty() || (!ev.who.positions.isEmpty() && !ev.who.positions.contains(Integer.parseInt(mText2.group(3))) && ev.who.positions.get(0) < Integer.parseInt(mText2.group(3)))) {
                                ev.core.arrayEl.add(mText2.group(2));
                                ev.core.positions.add(Integer.parseInt(mText2.group(3)));
                            }
                            }
                        }
                        
                    }
                }
        }

        return ev;

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
            if (constituent.label() != null
                    && (constituent.label().toString().equals("VP")) && tree.getLeaves().get(constituent.start()).toString().equalsIgnoreCase(word)) {
                System.err.println("found constituent: " + constituent.toString());
                System.err.println(tree.getLeaves().subList(constituent.start(), constituent.end() + 1));
            }
        }

        System.out.println(tree);
        System.out.println("\n\n----------------\n");
        return tree.toString();
    }

    private ArrayList<Integer> searchPositionInSentence(CoreMap sentence, Event ev) {
        ArrayList<Integer> pos = new ArrayList<Integer>();
        List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);

        // CORE
        List<Integer> corepos = ev.core.positions;
        if (!corepos.isEmpty()) {
            Collections.sort(corepos);
            Integer coremin = corepos.get(0) - 1;
            Integer coremax = corepos.get(corepos.size() - 1) - 1;

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
}
