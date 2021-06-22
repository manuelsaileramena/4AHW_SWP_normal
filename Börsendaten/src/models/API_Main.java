package models;

import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static models.API_Data_Boerse.depot;

public class API_Main {

    static ArrayList<String> aktien = new ArrayList<>();
    static ArrayList<Double> splitWerte = new ArrayList<>();
    static ArrayList<Double> splitVerbessert = new ArrayList<>();
    static ArrayList<Double> closeWerte = new ArrayList<>();
    static ArrayList<Double> gleitenderDurchschnitt = new ArrayList<>();
    static ArrayList<LocalDate> daten = new ArrayList<>();
    static ArrayList<Date> datumChart = new ArrayList<>();

    static ArrayList<Double> durchschnittDB = new ArrayList<>();
    static ArrayList<Double> closeDB = new ArrayList<>();
    static ArrayList<String> datumDB = new ArrayList<>();
    static String aktie, url;

    public static void main(String[] args) {

        try
        {
            API_Data_Boerse a = new API_Data_Boerse();
            a.readFile(aktien);
            for(int i =0;i<aktien.size();i++)
            {
                aktie = aktien.get(i);
                System.out.println(aktie);
                if(!a.check(aktie))
                {
                    a.urlLesen(aktie);
                    a.getWert(url,daten,closeWerte,splitWerte);
                    a.verbinden();
                    a.neuenTableErstellen(aktie);
                    a.einfügen(aktie,daten,closeWerte);
                    a.splitEinfügen(aktie,daten,closeWerte,splitWerte);
                    a.summeAllerStrategienEinfügen(aktie);
                    a.split(aktie,daten,closeWerte,splitWerte,splitVerbessert);
                    a.update(aktie,daten,splitVerbessert);
                    a.durchschnitt(aktie, daten, gleitenderDurchschnitt);
                    a.durchschnittEinfügen(aktie, daten, gleitenderDurchschnitt);
                    a.ListNull(datumDB,closeDB,durchschnittDB,datumChart);
                    a.alleAuswählen(aktie);
                    for(String dates : datumDB)
                    {
                        datumChart.add(Date.valueOf(dates.toString()));
                    }
                    createFile(createChart(datumChart,closeDB,durchschnittDB));
                    new SwingWrapper<XYChart>(createChart(datumChart,closeDB,durchschnittDB)).displayChart();
                    System.exit(0);
                }
            }
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            Scanner scan = new Scanner(System.in);
            scan.next();
        }

    }
    public static XYChart createChart(List<Date> d, List<Double>... multipleYAxis) {
        XYChart chart = new XYChartBuilder().title(aktie).width(1000).height(600).build();
        chart.setYAxisTitle("Close_Values");
        chart.setXAxisTitle("Dates");
        List<String> seriesName = new ArrayList<String>();
        seriesName.add("Close_Value");
        seriesName.add("Average_Value");
        //chart.getStyler().setZoomEnabled(true);
        for(int i = 0; i<seriesName.size();i++)
        {
            XYSeries seriesStock = chart.addSeries(seriesName.get(i), datumChart,multipleYAxis[i]);
            seriesStock.setMarker(null);
            seriesStock.setMarker(SeriesMarkers.NONE);
        }

        return chart;

    }

    public static boolean createFile(Object object) throws IOException {
        if(object.getClass() != XYChart.class)
        {
            return false;
        }
        BitmapEncoder.saveBitmap((XYChart) object,"C:\\4AHWII\\SWP\\SWP Kölle\\Projekte\\Börsendaten\\Aktienbilder\\Chart_"+ aktie +"_"+ LocalDate.now(),BitmapEncoder.BitmapFormat.JPG);
        return true;
    }

}
