package models;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

import static models.API_Data_Boerse.*;

public class Boersen_Main {

    static ArrayList<LocalDate> datumHandelsliste = new ArrayList<>();
    static ArrayList<Double> closeHandelsliste = new ArrayList<>();
    static ArrayList<Double> durchschnittHandelsliste = new ArrayList<>();
    static ArrayList<LocalDate> daten = new ArrayList<>();

    static ArrayList<String> aktien = new ArrayList<>();
    static String aktie;

    static LocalDate derzeitig = LocalDate.now();
    static double startkapital = 100000 / 5;
    static LocalDate startdatum = LocalDate.parse("2010-01-01");

    public static void main(String[] args) {

        try
        {
            API_Data_Boerse a = new API_Data_Boerse();
            a.readFile(aktien);
            for(int i =0;i<aktien.size();i++)
            {
                aktie = aktien.get(i);
                System.out.println();
                System.out.println(aktie);
                a.neuenTableErstellen(aktie);
                a.datumHandelslisteEinfügen(startkapital, derzeitig, startdatum, aktie, datumHandelsliste, closeHandelsliste, durchschnittHandelsliste, daten);
                System.out.println();
                System.out.println("Summe aller Strategien von "+ aktie +":");
                a.summeAllerStrategien();
            }
            System.out.println();
            System.out.println("Summe von allen 200er Strategien: " + depotAller200);
            System.out.println("Summe von allen 200er Strategien mit 3%: " + depotAller200mit3);
            System.out.println("Summe von allen buy and hold Strategien: " + depotAllerKaufenUndHalten);
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            Scanner scan = new Scanner(System.in);
            scan.next();
        }
    }

}
