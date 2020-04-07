/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.timelineRepresentation;

import oeg.eventRepresentation.Event;

/**
 *
 * @author mnavas
 */
public class Box {
    
    
    public DateP date;
    public String content;
    public String link;
    
    public Box(Event ev){
        date.day = ev.When.day;
        date.month = ev.When.month;
        content =  String.join(" ", ev.Core.arrayEl);
        link = ev.Link;
    }
    
    
        @Override
    public String toString(){
        String res = "                            <div class=\"timeline__box\">\n" ;
        res = res + date.toString();
        res = res + "<div class=\"timeline__post\">\n" +
"                                    <div class=\"timeline__content\"><a href=\"#" + link + "\">\n" +
"                                        <p>" + content + "</p>\n" +
"                                    </a></div>\n" +
"                                </div>\n";
        return res + "                            </div>\n";
    }
    
}
