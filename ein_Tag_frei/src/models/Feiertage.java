package models;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Feiertage {

    public List<LocalDate> feiertage = new ArrayList<>();
    private int startjahr = 2020;
    public int endjahr = 2022;

    public void Date() throws JSONException, IOException {

        while(startjahr < endjahr) {
            String URL = "https://feiertage-api.de/api/?jahr=" + startjahr + "&nur_land=BY";
            JSONObject json = new JSONObject(IOUtils.toString(new URL(URL), Charset.forName("UTF-8")));


            feiertage.add(getWert(json, "Christi Himmelfahrt"));
            feiertage.add(getWert(json, "Ostermontag"));
            feiertage.add(getWert(json, "Pfingstmontag"));
            feiertage.add(getWert(json, "Fronleichnam"));
            feiertage.add(getWert(json, "Neujahrstag"));
            feiertage.add(getWert(json, "Heilige Drei Könige"));
            feiertage.add(getWert(json, "Allerheiligen"));
            feiertage.add(getWert(json, "1. Weihnachtstag"));
            feiertage.add(getWert(json, "2. Weihnachtstag"));
            feiertage.add(getWert(json, "Tag der Arbeit"));
            feiertage.add(getWert(json, "Mariä Himmelfahrt"));
            feiertage.add(LocalDate.of(startjahr, 10, 26));
            feiertage.add(LocalDate.of(startjahr, 12, 8));
            startjahr++;
        }



    }

    public static LocalDate getWert(JSONObject json, String key) throws JSONException {
        JSONObject jahr = (JSONObject) json.get(key);
        String tag = jahr.getString("datum");
        LocalDate a = LocalDate.parse(tag);
        return a;
    }
}
