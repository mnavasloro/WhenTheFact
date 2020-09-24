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
    
    
    public Who who = new Who();
    public When when =new When();
    public Core core = new Core();

    public String EventId;
    public String Link;
    
    @Override
    public String toString(){
        return "Who: " + who.toString() + "\nCore: " + core.toString() + "\nEventId: " + EventId +  "\nLink: " + Link +  "\n";
//        return "when: " + when.toString() + "\nWho: " + who.toString() + "\nCore: " + core.toString() + "\nEventId: " + EventId +  "\nLink: " + Link +  "\n";
    }

    public void addCore(String lemma) {
        core.arrayEl.add(lemma);
    }
    
    public void addPos(int lemma) {
        core.positions.add(lemma);
    }
    
}
