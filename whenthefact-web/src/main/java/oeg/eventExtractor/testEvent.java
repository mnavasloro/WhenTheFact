/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static oeg.eventExtractor.timelineGeneration.generateTimeline;
import oeg.eventRepresentation.Event;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author mnavas
 */
public class testEvent {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String inputTIMEX = IOUtils.toString(new FileInputStream(new File("C:\\apache-tomcat-9.0.14\\bin\\bh2.txt")), "UTF-8");
            Annotation2JSON t2j = new Annotation2JSON();
            ArrayList<Event> events = t2j.getEvents(inputTIMEX);
            String finaltimeline = generateTimeline(events);
            System.out.print(finaltimeline);
        } catch (Exception ex) {
            Logger.getLogger(testEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
