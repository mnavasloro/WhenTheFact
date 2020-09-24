/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventRepresentation;

/**
 *
 * @author Maria
 */
public class CoreElementF {
    
    
    public String anchor;
    public float Begin;
    public float End;
    
    @Override
    public String toString(){
        return "Anchor: " + anchor + "\nBegin: " + Begin +  "\nEnd: " + End + "\n";
    }
    
}
