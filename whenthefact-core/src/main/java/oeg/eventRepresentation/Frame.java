/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventRepresentation;

import java.util.ArrayList;

/**
 *
 * @author mnavas
 */
public class Frame implements java.io.Serializable {
    
    public String core;
    public ArrayList<String> obj = new ArrayList<String>();
    public ArrayList<String> passRels = new ArrayList<String>();
    public ArrayList<String> actRels = new ArrayList<String>();
    public ArrayList<String> typeEvent = new ArrayList<String>();
    public ArrayList<String> subj = new ArrayList<String>();
    public double percCirc = 0;
    public double percProc = 0;

    @Override
    public String toString() {
        return "Frame{" + "core=" + core + ", obj=" + obj + ", subj=" + subj + ", passRels=" + passRels + ", actRels=" + actRels + ", typeEvent=" + typeEvent + ", percCirc=" + percCirc + ", percProc=" + percProc + '}' + '\n';
    }


    public void updatePer(){
        double total = typeEvent.size();
        if(total == 0){
            return;
        }
        double p = 0;
        double c = 0;
        for(String t : typeEvent){
            if(t.equalsIgnoreCase("procedure")){
                p++;
            } else{
                c++;
            }
        }
        percCirc = c/total;
        percProc = p/total;
    }
    
}
