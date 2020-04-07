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
public class Event {
    
    
    public When When;
    public Core Core;
    public String EventId;
    public String Link;
    
    @Override
    public String toString(){
        return "When: " + When.toString() + "\nCore: " + Core.toString() + "\nEventId: " + EventId +  "\nLink: " + Link +  "\n";
    }
    
}
