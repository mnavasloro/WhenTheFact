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
    
    public String day;
    public String month;
    
    @Override
    public String toString(){
        String res = "";
        res = "<div class=\"timeline__date\">\n" +
"                                    <span class=\"timeline__day\">" + day + "</span>\n" +
"                                    <span class=\"timeline__month\">" + getmonthS(month) + "</span>\n" +
"                                </div>\n";
        return res;
    }
    
    public String getmonthS(String input){
        switch(input){
            case "01":
                return "Jan";
            case "02":
                return "Feb";
            case "03":
                return "Mar";
            case "04":
                return "Apr";
            case "05":
                return "May";
            case "06":
                return "Jun";
            case "07":
                return "Jul";
            case "08":
                return "Aug";
            case "09":
                return "Sep";
            case "10":
                return "Oct";
            case "11":
                return "Nov";
            case "12":
                return "Dec";
            default:
                return "X";
        }
    }
}
