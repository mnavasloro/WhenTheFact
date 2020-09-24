/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.timelineRepresentation;

import oeg.eventRepresentation.EventF;

/**
 *
 * @author mnavas
 */
public class Box {
    
    
    public DateP date = new DateP();
    public String content;
    public String who;
    public String link;
    public String color ="";
    
    public Box(EventF ev){
        date.day = ev.when.day;
        date.month = ev.when.month;
        if(ev.who != null){
            who = String.join(" ", ev.who.arrayEl);
        }
        content =  String.join(" ", ev.core.arrayEl);
        if(content.length() >= 60){
            int indaux = content.indexOf(" ", 60);
            if(indaux!=-1){
                content = content.substring(0, indaux) + " (...)";
            }
        }
        link = ev.link;
        
        if(ev.core.type!=null && ev.core.type.startsWith("circumstance")){
            color= " style=\"color:green\"";
        }
       
    }
    
    
        @Override
    public String toString(){
        String res = "                            <div class=\"timeline__box\">\n" ;
        res = res + date.toString();
        res = res + "<div class=\"timeline__post\">\n" +
"                                    <div class=\"timeline__content\"><a href=\"#" + link + "\">\n" +
"                                        <p" + color + ">" + content + "</p>\n" +
"                                    </a></div>\n" +
"                                </div>\n";
        
        if(who!=null){
            res = "                            <div class=\"timeline__box\">\n" ;
        res = res + date.toString();
        res = res + "<div class=\"timeline__post\">\n" +
"                                    <div class=\"timeline__content\"><a href=\"#" + link + "\">\n" +
 "                                        <p" + color + "><b>" + who + "</b></p>\n" +               
"                                        <p" + color + ">" + content + "</p>\n" +
"                                    </a></div>\n" +
"                                </div>\n";
        }
        
        return res + "                            </div>\n";
    }
    
}
