/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.timelineRepresentation;

/**
 *
 * @author mnavas
 */
public class DateP {
    
    public int day;
    public int month;
    
    @Override
    public String toString(){
        String dayS = "X";
        if(day!=0){
           dayS = Integer.toString(day); 
        }
        String res = "<div class=\"timeline__date\">\n" +
"                                    <span class=\"timeline__day\">" + dayS + "</span>\n" +
"                                    <span class=\"timeline__month\">" + getmonthS(month) + "</span>\n" +
"                                </div>\n";
        
        return res;
    }
    
    public String getmonthS(int input){
        switch(input){
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "?";
        }
    }
}
