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
import java.util.logging.Level;
import java.util.logging.Logger;
import oeg.eventRepresentation.Frame;
import oeg.tagger.eventextractors.eventverbsextractor;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mnavas
 */
public class readCoresTrain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BufferedReader reader;
        ArrayList<String> types = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(
					".\\src\\main\\resources\\train-type.txt"));
			String line = reader.readLine();
			while (line != null) {
//				System.out.println(line);
				types.add(line);
				line = reader.readLine();
			}
			reader.close();


            String input = FileUtils.readFileToString(new File(".\\src\\main\\resources\\train-core.txt"), "UTF-8");

                        
                        eventverbsextractor eve = new eventverbsextractor();
                        HashMap<String, Frame> output = eve.annotate(input, types);
                        
         FileOutputStream fileOut =
         new FileOutputStream(".\\src\\main\\resources\\events.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(output);
         out.close();
         fileOut.close();
         System.out.printf("Serialized data is saved in .\\src\\main\\resources\\events.ser");

                        
                        
		} catch (Exception e) {
			e.printStackTrace();
		}
	
}
    
}
