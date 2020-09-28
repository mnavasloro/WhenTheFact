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
        StringBuilder sb = new StringBuilder("                            <div class=\"timeline__box\">\n");
        sb.append(date.toString());
        sb.append("<div class=\"timeline__post\">\n                                    <div class=\"timeline__content\"><a href=\"#").append(link).append("\">\n                                        <p").append(color).append(">").append(content).append("</p>\n                                    </a></div>\n                                </div>\n");
        
        if(who!=null){
            sb = new StringBuilder("                            <div class=\"timeline__box\">\n") ;
        sb.append(date.toString());
        sb.append("<div class=\"timeline__post\">\n                                    <div class=\"timeline__content\"><a href=\"#").append(link).append("\">\n                                        <p").append(color).append("><b>").append(who).append("</b></p>\n                                        <p").append(color).append(">").append(content).append("</p>\n                                    </a></div>\n                                </div>\n");
        }
        
        sb.append("                            </div>\n");
        return sb.toString() ;
    }
    
}
