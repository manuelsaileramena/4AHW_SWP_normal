package models;

import javafx.application.Application;

import java.util.Scanner;

public abstract class Hauptprogramm {

    public static String host="localhost:3306", database="Börsendaten", user="Manuel Sailer", passwort="Fussball0508+";
    public static String typ;
    public static int anzahlTage=500;

    public static void main(String[] args) throws Exception {
        angaben();

        Börsendaten_1 a=new Börsendaten_1(typ, anzahlTage, host, database, user, passwort);
        a.closePreis();
        a.connect();
        a.verbindungDB();
        a.DB_INSERT();
        a.DB_Average();
        a.DB_INSERTAVG();
        a.MinMax();
        a.DB_SELECT();
        Application.launch(JavaFX.class, args);

    }

    public static void angaben() {
        Scanner a=new Scanner(System.in);
        System.out.println("Firma angeben: ");
        typ=a.nextLine();
    }

}
