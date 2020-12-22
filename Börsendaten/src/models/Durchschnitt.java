package models;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;

import org.json.JSONException;
import org.json.JSONObject;

public class Durchschnitt {

    static double summe=0;
    String type;
    int tage;


    public Durchschnitt(String s, int t) {
        this.type=s;
        this.tage=t;
    }

    public double gleitenderDurchschnitt(String datum, int zaehler2, JSONObject json) throws MalformedURLException, JSONException, IOException {
        LocalDate d=LocalDate.parse(datum);
        summe=0;
        int durchschnitt=200;
        int zaehler=0;

        zaehler=zaehler2;
        durchschnitt=durchschnitt+zaehler2;
        do {
            durchschnitt = durchschnitt + durchschnittBerechnen(json, "" + d.minusDays(zaehler) + "");
            zaehler++;
        } while (zaehler < durchschnitt);

        return summe/200;
    }

    public int durchschnittBerechnen(JSONObject json, String key) throws JSONException {
        JSONObject bestaetigt = null;
        try {
            bestaetigt = (JSONObject) json.get(key);
        } catch (Exception e) {
            return 1;
        }
        String preis = bestaetigt.getString("4. close");
        summe=summe+Double.parseDouble(preis);
        return 0;
    }
}
