/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.auxiliary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oeg.eventRepresentation.Frame;
import oeg.eventRepresentation.FrameFrame;
import oeg.tagger.eventextractors.eventverbsextractor;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mnavas
 */
public class readFrames {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BufferedReader reader;
        ArrayList<String> types = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(
					".\\src\\main\\resources\\frames.txt"));
			String line = reader.readLine();
			while (line != null) {
//				System.out.println(line);
				types.add(line);
				line = reader.readLine();
			}
			reader.close();


            String input = FileUtils.readFileToString(new File(".\\src\\main\\resources\\frames.txt"), "UTF-8");

//                        
//                        eventverbsextractor eve = new eventverbsextractor();
//                        HashMap<String, Frame> output = eve.annotate(input, types);

HashMap<String, FrameFrame> output = frameReader(input);
                        
                        for(FrameFrame fr : output.values()){
                            System.out.println(fr.name + " : " + fr.core + " - " + fr.pos);
                        }
                        
         FileOutputStream fileOut =
         new FileOutputStream(".\\src\\main\\resources\\frames.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(output);
         out.close();
         fileOut.close();
         System.out.printf("Serialized data is saved in .\\src\\main\\resources\\frames.ser");

                        
                        
		} catch (Exception e) {
			e.printStackTrace();
		}
	
}

    private static HashMap<String, FrameFrame> frameReader(String input) {
        
        HashMap<String, FrameFrame> fr = new HashMap<String, FrameFrame>();
        
        
        Pattern p = Pattern.compile("\\'([^\\.]+)\\.([^\\']+)\\'");
        
        String[] lines = input.split(System.getProperty("line.separator"));
        for(String l : lines){
            String[] line = l.split(" | dict_keys\\(\\[");
            String framename = line[0];
            String words = l;
            
            Matcher m = p.matcher(words);

            while(m.find()) {
                FrameFrame ff = new FrameFrame();
                ff.core = m.group(1);
                ff.pos = m.group(2);
                ff.name = framename;                
                fr.put(m.group(1), ff);
            }           
            
            
        }
        
        return fr;
    }
    
}
