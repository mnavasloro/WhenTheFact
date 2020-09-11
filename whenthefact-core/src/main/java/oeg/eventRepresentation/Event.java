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
    
    
    public Who Who = new Who();
    public When When = new When();
    public Core Core = new Core();
    public String EventId;
    public String Link;
    
    @Override
    public String toString(){
        return "Who: " + Who.toString() + "\nCore: " + Core.toString() + "\nEventId: " + EventId +  "\nLink: " + Link +  "\n";
//        return "When: " + When.toString() + "\nWho: " + Who.toString() + "\nCore: " + Core.toString() + "\nEventId: " + EventId +  "\nLink: " + Link +  "\n";
    }

    public void addCore(String lemma) {
        Core.arrayEl.add(lemma);
    }
    
}
