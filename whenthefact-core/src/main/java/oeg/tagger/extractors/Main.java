//package oeg.tagger.extractors;
//
//import edu.stanford.nlp.util.logging.RedwoodConfiguration;
//import java.io.FileReader;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.List;
//import org.apache.commons.cli.BasicParser;
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.CommandLineParser;
//import org.apache.commons.cli.HelpFormatter;
//import org.apache.commons.cli.Options;
//import org.apache.log4j.ConsoleAppender;
//import org.apache.log4j.FileAppender;
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PatternLayout;
//import org.apache.maven.model.Model;
//import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
//
///**
// * Main class of the Contract Frames demo application.
// *
// * @author vroddon
// */
//public class Main {
//
//    /// El logger se inicializa más tarde porque a estas alturas todavía no sabemos el nombre del archivo de logs.
//    static Logger logger = null;
//    static boolean logs = false;
//    static String format = "proleg";
//
//    public static void main(String[] args) {
//
//        init(args);
//
//        if (args.length != 0) {
//            String res = parsear(args);
//            if (!res.isEmpty()) {
//                System.out.println(res);
//            }
//        }
//    }
//
//    public static void init(String[] args) {
//        logs = Arrays.asList(args).contains("-logs");
//        initLogger(logs);
//
//        //Welcome message
//        try {
//            MavenXpp3Reader reader = new MavenXpp3Reader();
//            Model model = reader.read(new FileReader("pom.xml"));
//            String welcome = model.getArtifactId() + " " + model.getVersion() + "\n-----------------------------------------------------\n";
//            logger.info(welcome);
//        } catch (Exception e) {
//        }
//
//    }
//
//    public static String parsear(String[] args) {
//        ///Respuesta
//        StringBuilder res = new StringBuilder();
//        CommandLineParser parser = null;
//        CommandLine cmd = null;
//        try {
//
//            Options options = new Options();
//            options.addOption("nologs", false, "OPTION to disables logs");
//            options.addOption("logs", false, "OPTION to enable logs");
//            options.addOption("format", true, "OPTION to choose the format. (proleg), rdf");
//            options.addOption("parse", true, "COMMAND to parse a file ");
//            options.addOption("help", false, "COMMAND to show help (Help)");
//            parser = new BasicParser();
//            cmd = parser.parse(options, args);
//
//            if (cmd.hasOption("help") || args.length == 0) {
//                new HelpFormatter().printHelp(Main.class.getCanonicalName(), options);
//            }
//            if (cmd.hasOption("format")) {
//                Main.format = cmd.getOptionValue("format");
//                
//            }
//            if (cmd.hasOption("parse")) {
//                String filename = cmd.getOptionValue("parse");
//                logger.info("Trying to parse the file " + filename);
//                parse(filename);
//            }
//
//        } catch (Exception e) {
//
//        }
//
//        return res.toString();
//    }
//
//    public static void parse(String filename) {
//       String res = "";
//        String txt = "";
//        try {
//            txt = new String(Files.readAllBytes(Paths.get(filename)));
//        } catch (Exception e) {
//            logger.error("error opening file");
//            return;
//        }
//
//        ExtractorKeywordBased cf = new ExtractorKeywordBased();
////        String output = cf.annotate(txt, Main.format);
////        logger.info("--------------------\nOUTPUT\n--------------------\n\n");
////        res+= output;
//    }
//
//    public static void initLogger(boolean logs) {
//        if (logs) {
//            initLoggerDebug();
//        } else {
//            initLoggerDisabled();
//        }
//
//    }
//
//    /**
//     * Silencia todos los loggers. Una vez invocada esta función, la función que
//     * arranca los logs normalmente queda anulada. Detiene también los logs
//     * ajenos (de terceras librerías etc.)
//     */
//    private static void initLoggerDisabled() {
//        logger = Logger.getLogger(Main.class);
//        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
//        loggers.add(LogManager.getRootLogger());
//        for (Logger log : loggers) {
//            log.setLevel(Level.OFF);
//        }
//        //Disable stanford logs
//        RedwoodConfiguration.current().clear().apply();
//        Logger root = Logger.getRootLogger();
//        logger.setLevel(Level.ERROR);
//
//    }
//
//    /**
//     * Si se desean logs, lo que se hace es: - INFO en consola - DEBUG en
//     * archivo de logs logs.txt
//     * http://stackoverflow.com/questions/8965946/configuring-log4j-loggers-programmatically
//     */
//    private static void initLoggerDebug() {
//       
//        Enumeration currentLoggers = LogManager.getCurrentLoggers();
//        List<Logger> loggers = Collections.<Logger>list(currentLoggers);
//        loggers.add(LogManager.getRootLogger());
//        for (Logger log : loggers) {
//            log.setLevel(Level.OFF);
//        }
//
//        Logger root = Logger.getRootLogger();
//        root.setLevel((Level) Level.DEBUG);
//
//        //APPENDER DE CONSOLA (INFO)%d{ABSOLUTE} 
//        PatternLayout layout = new PatternLayout("%d{HH:mm:ss} [%5p] %13.13C{1}:%-4L %m%n");
//        ConsoleAppender appenderconsole = new ConsoleAppender(); //create appender
//        appenderconsole.setLayout(layout);
//        appenderconsole.setThreshold(Level.INFO);
//        appenderconsole.activateOptions();
//        appenderconsole.setName("console");
//        root.addAppender(appenderconsole);
//
//        //APPENDER DE ARCHIVO (DEBUG)
//        PatternLayout layout2 = new PatternLayout("%d{ISO8601} [%5p] %13.13C{1}:%-4L %m%n");
//        FileAppender appenderfile = null;
//        String filename = "./logs/logs.txt";
//        try {
//            MavenXpp3Reader reader = new MavenXpp3Reader();
//            Model model = reader.read(new FileReader("pom.xml"));
//            filename = "./logs/" + model.getArtifactId() + ".txt";
//        } catch (Exception e) {
//        }
//        try {
//            appenderfile = new FileAppender(layout2, filename, false);
//            appenderfile.setName("file");
//            appenderfile.setThreshold(Level.DEBUG);
//            appenderfile.activateOptions();
//        } catch (Exception e) {
//        }
//
//        root.addAppender(appenderfile);
//
//
//        logger = Logger.getLogger(Main.class);
//    }
//
//}
