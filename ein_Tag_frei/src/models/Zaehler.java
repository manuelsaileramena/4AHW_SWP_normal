package models;

import org.json.JSONException;

import java.io.IOException;

public class Zaehler extends Feiertage{

    static int montag = 0;
    static int dienstag = 0;
    static int mittwoch = 0;
    static int donnerstag = 0;
    static int freitag = 0;


    public void dayOfWeek() throws JSONException, IOException {
        Date();
        for(int i = 0; i < feiertage.size(); i++){
            switch(feiertage.get(i).getDayOfWeek()){
                case MONDAY: montag++;break;
                case TUESDAY: dienstag++;break;
                case WEDNESDAY: mittwoch++;break;
                case THURSDAY: donnerstag++;break;
                case FRIDAY: freitag++;break;
                default: break;
            }
        }

    }

    public void output() throws JSONException, IOException {
        dayOfWeek();

        System.out.println("Montag: " + montag);
        System.out.println("Dienstag: " + dienstag);
        System.out.println("Mittwoch: " + mittwoch);
        System.out.println("Donnerstag: " + donnerstag);
        System.out.println("Freitag: " + freitag);
    }
}
