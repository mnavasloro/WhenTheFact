//package oeg.eventExtractor;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import oeg.tagger.core.time.tictag.Annotador;
//
///**
// * Test of the functionality of the servlets
// *
// * @author mnavas
// */
//public class Main {
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) throws Exception {
//
//        try {
//
//            String txt = "En lo que va de año."; // Our test sentence
//            System.out.println(txt);
//            System.out.println("---------------------------------------------------------------");
//            String parseLegalRef = parseAndTag(txt, "2019-02-19");  // We tag it
//            System.out.println(parseLegalRef);  // We print the output of the tagger
//
//        } catch (Exception ex) {
//            System.out.print(ex.toString());
//        }
//
//    }
//
//    public static String parseAndTag(String txt, String date) {
//
//        Date dct = null;
//        
//        
//        try {
//            if (date == null || date.isEmpty() || !date.matches("\\d\\d\\d\\d-(1[012]|0\\d)-(3[01]|[012]\\d)")) {
//                dct = Calendar.getInstance().getTime();
//                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//                date = df.format(dct);
//            }
//            Annotador annotador = new Annotador("ES");   // We innitialize the tagger in Spanish
//
//            String output = annotador.annotate(txt, date); // We annotate in BRAT format
//            return createHighlights(output); // We return the javascript with the values to evaluate
//        } catch (Exception ex) {
//            System.err.print(ex.toString());
//        }
//        return "";
//    }
//    
//    static public String createHighlights(String input2) {
////        input2 = input2.replaceFirst(Pattern.quote("<?xml version=\"1.0\"?>\n" + "<!DOCTYPE TimeML SYSTEM \"TimeML.dtd\">\n" + "<TimeML>"), "");
//        input2 = input2.replaceFirst(Pattern.quote("</TimeML>"), "");
//        input2 = input2.replaceAll("</TIMEX3>", "</span>");
//
//        String pattern = "(<TIMEX3 ([^>]*)>)";
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(input2);
//        StringBuffer sb = new StringBuffer();
//        while (m.find()) {
//            String color = "rgba(255, 165, 0, 0.5)";//"Orange";
//            String contetRegex = m.group(2);
//            contetRegex = contetRegex.replaceAll("\"", "");
//            contetRegex = contetRegex.replaceAll(" ", "\n");
//            if (contetRegex.contains("SET")) {
//                color = "rgba(135, 206, 235, 0.5)";//DodgerBlue";
//            } else if (contetRegex.contains("DURATION")) {
//                color = "hsla(9, 100%, 64%, 0.5)"; //Tomato
//            } else if (contetRegex.contains("TIME")) {
//                color = "rgba(102, 205, 170, 0.5)";//"MediumSeaGreen";
//            }
//
//            String aux2 = m.group(0);
//            aux2 = aux2.replace(">", "");
//
//            m.appendReplacement(sb, aux2.replaceFirst(Pattern.quote(aux2), "<span style=\"background-color:"
//                    + color + "\" title=\"" + contetRegex + "\">"));
//        }
//        m.appendTail(sb); // append the rest of the contents
//
//        return sb.toString();
//    }
//
//}
