package models;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;

public class Hauptprogramm {

    static Börsendaten_1 constant =new Börsendaten_1();

    public static void main(String[] args) throws MalformedURLException, JSONException, IOException {

        constant.verbindungDB();
        constant.closePreis();
        constant.DB_SELECT();

    }

}
