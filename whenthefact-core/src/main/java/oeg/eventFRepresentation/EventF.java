/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventFRepresentation;

/**
 *
 * @author Maria
 */
public class EventF {
    
    
    public WhenF when;
    public CoreF core;
    public CoreF who;
    public CoreF what;
    public String eventId;
    public String link;
    public String type;
    
    @Override
    public String toString(){
        return "When: " + when.toString() + "\nCore: " + core.toString() + "\nEventId: " + eventId +  "\nLink: " + link +  "\n";
    }
    
}
