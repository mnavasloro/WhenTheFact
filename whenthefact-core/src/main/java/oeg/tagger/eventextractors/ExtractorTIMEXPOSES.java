package oeg.tagger.eventextractors;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.ling.tokensregex.types.Value;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
import oeg.tagger.docHandler.Document;
import oeg.tagger.docHandler.DocumentPart;
import oeg.tagger.docHandler.StructureExtractorECHR;
import oeg.tagger.docHandler.XMLMerger;
import oeg.tagger.docHandler.XMLMerger2;
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
public class ExtractorTIMEXPOSES {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExtractorTIMEXPOSES.class);

    static ArrayList<String> NEvs = new ArrayList<>(Arrays. asList("pecado", "presidencial", "mejora", "fútbol", "partidismo", "coexistencia", "diputado", "guarda", "ventaja", "pauta", "campeonato", "retrato", "postura", "gesto", "sombra", "trasfondo", "fijación", "clave", "descripción", "gracioso", "consigna", "derribo", "régimen", "culminación", "premio", "jalón", "respuesta", "euforia", "nuevo", "motivo", "concurso", "réplica", "transacción", "belleza", "ansia", "rotura", "puntualización", "derecho", "fallo", "expresivo", "arbitraje", "mejor", "orden", "jugada", "inhabilitación", "característico", "interés", "molestia", "capacidad", "taller", "modernización", "equilibrio", "parodia", "reivindicación", "legalidad", "aviso", "relación", "decisión", "haber", "tortura", "perdedor", "consideración", "clamor", "certamen", "reunión", "solo", "oscuro", "optimismo", "colaboración", "levantamiento", "síndrome", "compra", "campeón", "cargo", "lacra", "exportación", "probable", "importante", "adversidad", "sydney", "estable", "mejora", "encuentro", "posición", "comisión", "preso", "cumbre", "derecho", "viabilidad", "sospecha", "ornamentación", "gasto", "autorización", "invencible", "licencia", "miedo", "octavo", "pago", "estrategia", "recorrido", "contrarreloj", "precio", "principio", "cumbre", "servicio", "tarea", "inyección", "silbido", "demanda", "jjoo", "financiación", "clima", "circunstancia", "liga", "preolímpico", "importancia", "caducidad", "dolor", "centro", "siniestralidad", "copa", "dificultad", "incumplimiento", "interferencia", "disparidad", "elogio", "mantenimiento", "reacción", "forma", "cierto", "pueril", "fácil", "duelo", "empate", "acuerdo", "sonrojo", "estudio", "dimensión", "paro", "inflexión", "cambio", "vicepresidencia", "previsión", "mecenazgo", "presidente", "edición", "subsecretario", "presidente", "reto", "fin", "paso", "orgulloso", "coincidencia", "migración", "constancia", "gestión", "cuarto", "determinación", "podio", "rigor", "recta", "fluctuación", "entredicho", "homenaje", "éxito", "cabezada", "consumo", "locura", "final", "confusión", "visión", "apoyo", "inmunidad", "equiparable", "duda", "conferencia", "prestigio", "pacto", "organización", "delito", "nacimiento", "precaución", "agravante", "subasta", "dinamismo", "seguro", "seguro", "balance", "insulto", "objetividad", "fuerza", "efecto", "mal", "libre", "responsabilidad", "operador", "récord", "ataque", "orquestación", "dimisión", "reforma", "fuerte", "rehén", "uso", "baja", "causante", "sondeo", "cimentación", "acompañante", "listo", "político", "convivencia", "promedio", "emparejamiento", "condicionante", "contraste", "extinto", "publicidad", "encuentro", "distribución", "conocimiento", "reclamación", "malo", "desilusión", "rectificación", "labor", "ministro", "excavación", "objeto", "medida", "actitud", "ampliación", "simultaneidad", "concesión", "zurdazo", "monólogo", "convencimiento", "respaldo", "ironía", "progreso", "conjetura", "revocación", "arreglo", "imperio", "triunfo", "oportunismo", "tolerancia", "europeo", "admiración", "ilusión", "frío", "avalancha", "dedicación", "delirio", "búsqueda", "rally", "loco", "renovación", "espectáculo", "impedimento", "haber", "relato", "negro", "avance", "movida", "relanzamiento", "discreto", "combate", "senda", "galardón", "entrada", "aplazamiento", "baza", "prestación", "amiguismo", "teoría", "procedencia", "embajador", "sueño", "peculiaridad", "pregunta", "querella", "alcance", "disposición", "afirmación", "imparcialidad", "grave", "calidad", "limitación", "autoritarismo", "rueda", "operación", "posible", "licitación", "vicepresidente", "solidaridad", "reorganización", "mayor", "asamblea", "dificultad", "vista", "mezcla", "orden", "derrota", "inconformismo", "batalla", "reinserción", "vocalidad", "apertura", "debate", "revelación", "prolongación", "práctica", "campaña", "vencedor", "explotación", "infracción", "golpe", "homologación", "fusión", "frenazo", "vocal", "halagüeño", "cuarentena", "sentimiento", "reparo", "deslegitimación", "creación", "valoración", "cuestionable", "moral", "estancia", "peculiaridad", "nombramiento", "cálido", "emoción", "mundiales", "premio", "cruce", "oleada", "notable", "alusión", "exigencia", "honor", "victoria", "vía", "desmentido", "línea", "manipulación", "lesión", "posibilidad", "urgencia", "inscripción", "sesión", "vigente", "actividad", "forma", "vuelta", "mandato", "caso", "rechace", "patriotismo", "dureza", "gol", "marcha", "ayuda", "centenario", "incontinencia", "acontecimiento", "haber", "rebaja", "ocasión", "naufragio", "presencia", "re-flotamiento", "objetivo", "disconformidad", "reparación", "islamismo", "remedio", "espera", "invitación", "esfuerzo", "duración", "tráfico", "privatización", "jornada", "cisma", "charla", "frialdad", "mensaje", "modernidad", "observación", "supercopa", "gira", "regreso", "giro", "disputa", "advertencia", "inicio", "prioridad", "tesorero", "temporada", "índice", "estabilidad", "planteamiento", "ocultación", "declaración", "plan", "prueba", "haber", "tradición", "superliga", "dictamen", "recogida", "vuelta", "situación", "operación", "proceso", "estado", "humilde", "presente", "petición", "carente", "indemnización", "hombre", "fuerza", "deducción", "candidato", "actuación", "colofón", "autogobierno", "sentencia", "profesional", "bolsa", "conveniente", "alusión", "ilusión", "fin", "evento", "tuberculosis", "semifinal", "detención", "líder", "congreso", "posibilidad", "proyecto", "demagogia", "mengua", "homenajeado", "exposición", "subida", "parón", "coincidencia", "contiguo", "salud", "actor", "acto", "refuerzo", "discurso", "intento", "insumiso", "prólogo", "terrorismo", "escándalo", "cameos", "contratación", "energía", "pareja", "fe", "pluralismo", "referencia", "revolución", "eliminatoria", "venta", "claro", "resultado", "supervisión", "presente", "conspiración", "nombramiento", "ajuste", "compañero", "oferta", "congreso", "difícil", "consecuencia", "meta", "contrato", "kemalismo", "promotor", "diversidad", "identidad", "voto", "consecuencia", "brillante", "violencia", "amenaza", "función", "placer", "preolímpico", "perspectiva", "contacto", "lleno", "jornada", "sequía", "negocio", "triunfo", "retraso", "necesidad", "beneficiario", "burla", "protagonismo", "preparación", "descanso", "fiesta", "escándalo", "sesión", "sorpresa", "fracaso", "encuesta", "iniciativa", "producción", "ganancia", "descalificación", "cota", "independiente", "inevitable", "escenario", "interés", "poder", "político", "gratificante", "payasada", "peligro", "secretario", "empate", "pugna", "exportación", "lectura", "condición", "diferente", "saldo", "regidor", "retorno", "espíritu", "continuidad", "revancha", "interpelación", "acusación", "puesta", "retroceso", "denuncia", "resolución", "enfrentamiento", "evidencia", "convicción", "gasto", "prosperidad", "participante", "pánico", "oro", "acuerdo", "amigo", "superior", "conversación", "hostil", "entrenamiento", "suficiente", "justo", "frente", "liga", "comida", "psicosis", "raro", "mundial", "pleno", "relajación", "penalti", "rumor", "ambiente", "tanto", "ceremonia", "ayuda", "ingreso", "bronce", "preciso", "aquí", "integración", "encanto", "luxación", "oportunidad", "copa", "dividendo", "preocupación", "viaje", "herido", "construcción", "pronunciamiento", "reafirmación", "prebenda", "digno", "cobro", "coste", "turismo", "monopolio", "felicitación", "concierto", "bienvenida", "salida", "inclusión", "competitividad", "satisfacción", "populismo", "coalición", "aventura", "juicio", "confección", "moderno", "delegación", "crítico", "incorporación", "precario", "fundamentalismo", "anécdota", "deterioro", "corrupción", "histórico", "neohippismo", "indulto", "rápido", "ronda", "rebote", "carrera", "convencionalidad", "legislatura", "gol", "objetivo", "imposible", "paz", "caída", "venta", "sexualidad", "diferencia", "eficacia", "ruptura", "ensayo", "derrota", "juegos", "ario", "escena", "contienda", "razón", "indulto", "relación", "monopolio", "confesión", "aplauso", "irregularidad", "mini-transat", "órden", "insistencia", "revuelto", "tarea", "reducción", "misión", "causa", "vanidad", "queja", "mitin", "voluntad", "solicitud", "recuerdo", "favorable", "sueldo", "aumento", "admisión", "mejorable", "bueno", "defensa", "acoso", "juego", "huelga", "solidez", "pena", "seguridad", "tesitura", "marcaje", "inexactitud", "mensaje", "muestra", "mentira", "confrontación", "haber", "crecimiento", "otorgamiento", "desertor", "tránsito", "tensión", "afán", "voto", "medida", "titularidad", "discriminación", "imposibilidad", "bueno", "embargo", "rechazo", "trampa", "amortización", "resultado", "lista", "desconfianza", "peso", "dueño", "esbozo", "visita", "acusación", "decisivo", "pérdida", "beneficio", "anfitrión", "obra", "agonía", "egocentrismo", "tiro", "aniquilación", "obsesión", "campaña", "facilidad", "cuenta", "rally", "circulación", "semifinal", "ocasión", "trabajador", "incidente", "obra", "conductor", "cálculo", "maleficio", "gloria", "razón", "indiferencia", "manifiesto", "consciente", "pretemporada", "diana", "cohesión", "detención", "trasvase", "desenlace", "soborno", "presunto", "ataque", "subasta", "gana", "agujero", "proceso", "escasez", "fuera", "fuego", "ileso", "papelito", "efectividad", "golpe", "período", "marca", "grabación", "descalabro", "cita", "festival", "afín", "provecho", "firma", "situación", "saludable", "calma", "escrutinio", "elogio", "marco", "partido", "facturación", "discurso", "entrega", "prohibición", "plan", "serenidad", "plantilla", "haber", "rareza", "división", "trabajo", "colapso", "fortalecimiento", "juego", "quiebra", "diploma", "minoría", "narración", "intención", "temor", "división", "represión", "victoria", "tipo", "periodo", "gloria", "tasa", "amonestación", "turco", "existencia", "vistazo", "experiencia", "cocotazo", "unidad", "revés", "mirada", "solidaridad", "inversión", "llamada", "beneficio", "cantante", "destitución", "derbi", "problema", "formación", "interrogante", "reparto", "fuerte", "manía", "congreso", "mesa", "realidad", "atención", "cárcel", "beso", "contradicción", "fraude", "seleccionador", "programa", "acuerdo", "práctica", "sentido", "temporal", "progresivo", "previsión", "inteligencia", "capítulo", "impulso", "bombardeo", "tregua", "prueba", "exceso", "contencioso", "convergencia", "expresión", "violación", "insistente", "maravilla", "elección", "valor", "racha", "habitual", "positivo", "europeísmo", "ida", "potencial", "evidente", "cotarro", "predominio", "sorpresa", "comida", "empleo", "trabajador", "conquista", "condena", "tropiezo", "patrón", "irregularidad", "olimpismo", "inquietud", "pacto", "haber", "orgullo", "sistema", "competitivo", "favorito", "inicio", "2", "opción", "participación", "gracia", "elección", "alegación", "estallido", "celebración", "candidato", "excursión", "tiempo", "condición", "intercambio", "crédito", "opinión", "negociación", "fama", "actor", "desarrollo", "error", "momento", "alianza", "solvencia", "jubilación", "cuenta", "papel", "esfuerzo", "genocidio", "opinión", "clasificación", "popularidad", "motivación", "vía", "comicio", "estreno", "funcionamiento", "denuncia", "versión", "entrevista", "exigencia", "titular", "criterio", "comercialización", "paso", "fase", "eliminatoria", "verdad", "flexibilidad", "referencia", "democracia", "caballo", "cambio", "torneo", "concesión", "acción", "voracidad", "interferencia", "duda", "prisión", "desafío", "noche", "disturbio", "saneamiento", "título", "jefatura", "codicia", "copa", "entrevista", "negociación", "volumen", "votación", "conformidad", "separación", "apretura", "gesto", "renovación", "asomo", "partido", "pedalada", "propuesta", "partida", "futuro", "cita", "interpretación", "representación", "duelo", "infierno", "vuelo", "boda", "desobediencia", "panorama", "alternativa", "idéntico", "competición", "cierre", "innegable", "obtención", "comentario", "malestar", "reacción", "diferencia", "bloqueo", "déficit", "respetabilidad", "liquidez", "lógico", "verdad", "capaz", "semifinalista", "acto", "grito", "nivel", "prevaricación", "compra", "vida", "novedad", "asistencia", "consideración", "4", "público", "sacrificio", "trascendencia", "democratización", "severo", "compromiso", "haber", "protesta", "inflación", "pase", "lluvia", "debate", "atentado", "posesión", "liberalización", "evolución", "negocio", "duro", "necesidad", "respeto", "subida", "limpio", "margen", "show", "silencio", "acierto", "presión", "conclusión", "empleo", "comparación", "sustitución", "dinámica", "peligro", "3", "significativo", "envío", "globalización", "apelación", "caso", "vertido", "reivindicación", "imagen", "harto", "travesía", "candidatura", "enfermedad", "procesamiento", "incompetencia", "conservación", "error", "ahorro", "cooperación", "mayoría", "cuestión", "torneo", "razonable", "portavoz", "difícil", "suerte", "crisis", "establecimiento", "raíz", "ascenso", "amenaza", "dirección", "comercio", "prioritario", "fórmula", "solución", "oferta", "inversión", "alto", "alteración", "requisito", "motivo", "ruido", "dependencia", "desastre", "consulta", "cumplimiento", "haber", "circunstancia", "partidazo", "atentado", "megaelección", "actividad", "festividad", "llegada", "intención", "aparición", "sensación", "ejecución", "crítico", "auge", "proyecto", "distanciamiento", "víctima", "competencia", "violación", "lucha", "manifestación", "promesa", "cosquilleo", "net-99", "parte", "resumen", "humano", "habilitación", "encargo", "vivo", "procedente", "goleada", "reducción", "montaje", "lesión", "abastecimiento", "signo", "administración", "historia", "inquietud", "confirmación", "falta", "sanción", "frecuencia", "laicismo", "moderación", "concurso", "tertulia", "cerco", "colaborador", "compraventa", "muerte", "director", "persecución", "kurdo", "presentación", "optimista", "plata", "pena", "propietario", "contador", "tónica", "permanencia", "concentración", "falta", "novio", "haber", "crédito", "plazo", "trabajo", "comportamiento", "escena", "peor", "expansión", "insumisión", "movimiento", "excepción", "compostaje", "propuesta", "espontaneidad", "total", "tiro", "opción", "intervención", "demanda", "informe", "control", "audiencia", "carrera", "complicidad", "compromiso", "haber", "favorito", "suspensión", "delegación", "servicio", "tentación", "unanimidad", "riesgo", "acceso", "corresponsable", "vergüenza", "tour", "decisión", "método", "castigo", "feliz", "necesario", "problema", "descenso", "alegría", "aplicación", "engaño", "delito", "cantidad", "idea", "etapa", "grec", "crítico", "reestructuración", "significativo", "conflicto", "figura", "rodaje", "flojo", "daño", "regulación", "dragado", "reunión", "extradición", "laico", "palabra", "investigación", "independencia", "reconocimiento", "autonomía", "confianza", "exhibición", "función", "argumento", "intentona", "ausencia", "sobresalto", "extraño", "atractivo", "conferencia", "vinculación", "drama", "liberación", "retirada", "designación", "eliminación", "representación", "guerra", "revisión", "atlanta-96", "comparecencia"));
//    PrintWriter out;
    String rules;
    Properties properties = new Properties();
    String posModel;
    String lemmaModel;
    StanfordCoreNLP pipeline;

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
    public ExtractorTIMEXPOSES() {
        init();
    }

    public ExtractorTIMEXPOSES(String language) {
        lang = language;
        init();
    }

    public ExtractorTIMEXPOSES(String pos, String lemma, String rul, String language) {
        posModel = pos;
        lemmaModel = lemma;
        rules = rul;
        lang = language;
        init();
    }

    public ExtractorTIMEXPOSES(String rul, String language) {
        rules = rul;
        lang = language;
        init();
    }

    public void init() {

            if (rules == null) {
                rules = "./src/main/resources/rules/rulesES.txt";
            }

//        out = new PrintWriter(System.out);
            properties = StringUtils.argsToProperties(new String[]{"-props", "StanfordCoreNLP-spanish.properties"});

            if (posModel == null) {
                posModel = "./src/main/resources/ixa-pipes/morph-models-1.5.0/es/es-pos-perceptron-autodict01-ancora-2.0.bin";
            }
            if (lemmaModel == null) {
                lemmaModel = "./src/main/resources/ixa-pipes/morph-models-1.5.0/es/es-lemma-perceptron-ancora-2.0.bin";
            }

            properties.setProperty("annotators", "tokenize,ssplit,spanish,readability,ner,tokensregexdemo");
//    properties.setProperty("ner.useSUTime", "false");
            properties.setProperty("spanish.posModel", posModel);
            properties.setProperty("spanish.lemmaModel", lemmaModel);
            properties.setProperty("readability.language", "es");

            properties.setProperty("customAnnotatorClass.spanish", "oeg.tagger.core.time.aidCoreNLP.BasicAnnotator");
            properties.setProperty("customAnnotatorClass.readability", "eu.fbk.dh.tint.readability.ReadabilityAnnotator");

            properties.setProperty("customAnnotatorClass.tokensregexdemo", "edu.stanford.nlp.pipeline.TokensRegexAnnotator");
            properties.setProperty("tokensregexdemo.rules", rules);
            properties.setProperty("tokenize.verbose", "false");
            properties.setProperty("TokensRegexNERAnnotator.verbose", "false");

        try {
            pipeline = new StanfordCoreNLP(properties);
        } catch (Exception ex) {
            System.out.println("Error: " + ex.toString());
        }

    }

    /**
     * Returns the next month monthS given a date dt and the rank of a month
     * monthS
     *
     * @param dt DateTime of reference
     * @param monthS int with the rank of a month
     *
     * @return DateTime with the new date
     */
    public DateTime getNextMonth(DateTime dt, int monthS) {
        int current = dt.getMonthOfYear();
        if (monthS <= current) {
            monthS += 12;
        }
        DateTime next = dt.plusMonths(monthS - current);
        return next;
    }

    /**
     * Returns the last month monthS given a date dt and the rank of a month
     * monthS
     *
     * @param dt DateTime of reference
     * @param monthS int with the rank of a month
     *
     * @return DateTime with the new date
     */
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

    /**
     * Returns the next day of the week dayW given a date dt and the rank of a
     * day of the week dayW
     *
     * @param dt DateTime of reference
     * @param dayW int with the rank of a day of the week
     *
     * @return DateTime with the new date
     */
    public DateTime getNextDayWeek(DateTime dt, int dayW) {
        int current = dt.getDayOfWeek();
        if (dayW <= current) {
            dayW += 7;
        }
        DateTime next = dt.plusDays(dayW - current);
        return next;
    }

    /**
     * Returns the last day of the week dayW given a date dt and the rank of a
     * day of the week dayW
     *
     * @param dt DateTime of reference
     * @param dayW int with the rank of a day of the week
     *
     * @return DateTime with the new date
     */
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

    /**
     * Returns the next month given a date dt and the name of a month
     *
     * @param dt DateTime of reference
     * @param monthSS int with the name of a month
     *
     * @return String with the new date
     */
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

    /**
     * Returns the last month given a date dt and the name of a month
     *
     * @param dt DateTime of reference
     * @param monthSS int with the name of a month
     *
     * @return String with the new date
     */
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

    /**
     * Returns the next date given a referece date dt and a relative date refD
     *
     * @param dt DateTime of reference
     * @param refD String with the relative date
     *
     * @return String with the new date
     */
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

    /**
     * Returns the last date given a referece date dt and a relative date refD
     *
     * @param dt DateTime of reference
     * @param refD String with the relative date
     *
     * @return String with the new date
     */
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

    /**
     * Returns the next season given a referece date dt and a season
     *
     * @param dt DateTime of reference
     * @param refD String with the season
     *
     * @return String with the new date
     */
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

    /**
     * Returns the last season given a referece date dt and a season
     *
     * @param dt DateTime of reference
     * @param refD String with the season
     *
     * @return String with the new date
     */
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

    /**
     * Function that parses a String with several durations
     *
     * @param input String with concatenated durations
     *
     * @return Map with durations (granularity,amount)
     */
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

    /**
     *
     * @param input String text to annotate
     * @param anchorDate String date of reference
     *
     * @return String text tagged
     */
    public String annotate(String input, String anchorDate) {
        int i = 1;
        ArrayList<String> keywords = new ArrayList<String>();
       
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
            String inp2 = input;
            String inp3 = input;
            int flagRN = 0;

            inp2 = inp2.replaceAll("\\r\\n", "\n");

            int offsetdelay = 0;
            int numval = 0;
            Annotation annotation = new Annotation(inp2);

            pipeline.annotate(annotation);

            int offset = 0;
            
//            StructureExtractorECHR seECHR = new StructureExtractorECHR();
//            Document doc = seECHR.extractFromDocument(wordfile);
//            List<DocumentPart> eventRelevantParts = doc.getEventRelevantParts();
//
            List<CoreMap> sentencesAll = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//            List<CoreMap> sentences = new ArrayList<CoreMap>();
//            if (eventRelevantParts.isEmpty()) {
//                sentences = sentencesAll;
//            } else {
//                for (CoreMap sentence : sentencesAll) {
//                    List<CoreLabel> toks = sentence.get(CoreAnnotations.TokensAnnotation.class);
//                    Integer beginSentence = toks.get(0).beginPosition();
//                    for (DocumentPart erp : eventRelevantParts) {
//                        if (beginSentence >= erp.offset_ini && beginSentence < erp.offset_end) {
//                            sentences.add(sentence);
//                            break;
//                        }
//                    }
//                }
//            }

            lastfullDATE = backupAnchor;
            for (CoreMap sentence : sentencesAll) {
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

                    String mod = (String) a.get(3).get();
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
                        if (val.startsWith("Danchor(+,") && lastfullDATE != null) {
                            String refDate = val.substring(10, val.length() - 1);
                            val = getNextDate(lastfullDATE, refDate);
                        } else if (val.startsWith("Danchor(-,") && lastfullDATE != null) {
                            String refDate = val.substring(10, val.length() - 1);
                            val = getLastDate(lastfullDATE, refDate);
                        } else if (val.startsWith("Sanchor(+,") && lastfullDATE != null) {
                            String refDate = val.substring(10, val.length() - 1);
                            val = getNextSeason(lastfullDATE, refDate);
                        } else if (val.startsWith("Sanchor(-,") && lastfullDATE != null) {
                            String refDate = val.substring(10, val.length() - 1);
                            val = getLastSeason(lastfullDATE, refDate);
                        } else if (val.startsWith("Ranchor(+,") && lastfullDATE != null) {
                            String gran = val.substring(10, val.length() - 1);
                            DateTime dat = new DateTime(lastfullDATE);
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
                        } else if (val.startsWith("Ranchor(-,") && lastfullDATE != null) {
																																								  
                            String gran = val.substring(10, val.length() - 1);
                            DateTime dat = new DateTime(lastfullDATE);
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
						 
                        } else if (val.startsWith("DWanchor(+,") && lastfullDATE != null) {
                            String refDate = val.substring(11, val.length() - 1);
                            val = getNextMonthS(new DateTime(lastfullDATE), refDate);
                        } else if (val.startsWith("DWanchor(-,") && lastfullDATE != null) {
                            String refDate = val.substring(11, val.length() - 1);
                            val = getLastMonthS(new DateTime(lastfullDATE), refDate);
                        } else if (val.startsWith("anchor") && lastfullDATE != null) {
                            DateTime dt = new DateTime(lastfullDATE);

																									 
								 
												
												 
													 

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
                                // To do: BD should use real work calendar, not just a simple day, but...
																 
															  
																		
															   
										
																	
																 
								 
																	
																 
															 
																		
															  
										
																	
															  
								 
                                if (gran.equalsIgnoreCase("BD")) {
                                    if (plus.equalsIgnoreCase("+")) {
                                        dt = dt.plusDays(plusI);
                                    } else if (plus.equalsIgnoreCase("-")) {
                                        dt = dt.minusDays(plusI);
										
																			   
															
																  
																   
																		
                                    } else {
                                        dt = new DateTime(lastfullDATE);
                                        val = dt.toString("YYYY-MM-dd") + val.substring(val.lastIndexOf(")") + 1);

                                    }
								 
                                } else if (gran.equalsIgnoreCase("D")) {
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
					 

                        if ((typ.equalsIgnoreCase("DURATION") || typ.equalsIgnoreCase("SET")) && !rul.equalsIgnoreCase("Rule$losWEEKL")) {
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

                        if (typ.equalsIgnoreCase("TIME") && val.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d.*")) {
                            lastfullDATE = val.substring(0, 10);
                        }

                        if (typ.equalsIgnoreCase("DATE")) {
                            lastDATE = val;
                        }

																								 
									   
					 
                        String addini = "<TIMEX3 tid=\"t" + numval + "\" type=\"" + typ + "\" value=\"" + val;
                        if (!freq.isEmpty()) {
                            addini = addini + "\" freq=\"" + freq;
                        }
                        if (!mod.isEmpty()) {
                            addini = addini + "\" mod=\"" + mod;
                        }
                        addini = addini + "\">";

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

                        inp2 = inp2.substring(0, ini + offsetdelay) + toAdd + inp2.substring(ini + text.length() + offsetdelay);
																																

                        offsetdelay = offsetdelay + toAdd.length() - text.length();
                    }
//                if (flagTIMEX == 1) { // Otra opcion es sumar uno por cada TIMEX, pero tampoco es una relacion uno a uno...
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        /* We collect the different tags of each token */
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                        String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

                        /* Verb Event */
                        if (pos.startsWith("VM") || pos.startsWith("VS") || pos.matches("AQ.+P") ){// && !pos.equalsIgnoreCase("VBG")) {
                            if(!word.equalsIgnoreCase("amp")){    
                            inp3 = inp3.substring(0, offset + token.beginPosition()) + "<EVENT eid=\"e" + i + "\">" + inp3.substring(offset + token.beginPosition(), offset + token.endPosition()) + "</EVENT>" + inp3.substring(offset + token.endPosition());
                                offset = offset + ("<EVENT eid=\"e" + i + "\">" + "</EVENT>" ).length();
                                i++;
                            }
                        } else if(pos.startsWith("NC") || pos.startsWith("SP")){
                            if(NEvs.contains(lemma)){
                                 inp3 = inp3.substring(0, offset + token.beginPosition()) + "<EVENT eid=\"e" + i + "\">" + inp3.substring(offset + token.beginPosition(), offset + token.endPosition()) + "</EVENT>" + inp3.substring(offset + token.endPosition());
                                offset = offset + ("<EVENT eid=\"e" + i + "\">" + "</EVENT>" ).length();
                                i++;
                            }
                        }
                }
            }

//            inp2 = inp2.replaceAll(" , ", ", ");
//            inp3 = inp3.replaceAll(" , ", ", ");
//            inp3 = inp3.replaceAll("THE COURT, UNANIMOUSLY", "<Event argument=\"core\">THE COURT, UNANIMOUSLY</Event>");

            inp2 = inp2.replaceAll("\\n", "\r\n");
            inp3 = inp3.replaceAll("\\n", "\r\n");

            XMLMerger2 xmlM2 = new XMLMerger2();
            System.out.println("TIMEX:\n" + inp2 + "\n----------\n");
            System.out.println("EVS:\n" + inp3 + "\n----------\n");
            String res = xmlM2.mergeXML(inp3, inp2);
            System.out.println("CONJ:\n" + res + "\n----------\n");

            return res;

        } catch (Exception ex) {
            Logger.getLogger(ExtractorTIMEXPOS.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ExtractorTIMEXPOS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
