package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import static models.Hauptprogramm.*;

public class JavaFX extends Application {

    public void start(Stage stage) throws Exception {

        Börsendaten_1 a=new Börsendaten_1(typ, anzahlTage, host, database, user, passwort);

        final NumberAxis yAchse=new NumberAxis();
        final CategoryAxis xAchse=new CategoryAxis();

        yAchse.setAutoRanging(false);
        System.out.println(a.min);
        System.out.println(a.max);
        yAchse.setLowerBound(a.min - (a.min*0.1));
        yAchse.setUpperBound(a.max + (a.max*0.1));



        final AreaChart<String, Number> areaChart=new AreaChart<>(xAchse, yAchse);
        areaChart.setTitle("Börsendaten "+ typ);
        areaChart.setCreateSymbols(false);
        areaChart.setLegendVisible(false);
        xAchse.setLabel("Tage");
        yAchse.setLabel("Börsendaten");

        XYChart.Series series_aktie= new XYChart.Series();
        XYChart.Series series_durchschnitt= new XYChart.Series();
        series_durchschnitt.setName("200er Durchschnitt");
        ArrayList<Double> aktie=new ArrayList<>();
        ArrayList<Double> durchschnitt=new ArrayList<>();
        ArrayList<String> datum=new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String URL = "jdbc:mysql://localhost:3306/Börsendaten?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
            Connection con = DriverManager.getConnection(URL, "root", "Fussball0508+");
            Statement stat=con.createStatement();
            ResultSet reSe=stat.executeQuery("select * from Daten"+ typ);
            while(reSe.next()) {
                series_aktie.getData().add(new XYChart.Data(reSe.getString("Zeitpunkt"), Double.parseDouble(reSe.getString("TagesEndPreis"))));
                aktie.add(Double.parseDouble(reSe.getString("TagesEndPreis")));
                datum.add(reSe.getString("Zeitpunkt"));
            }

            reSe=stat.executeQuery("select * from Durchschnitt"+ typ);
            while(reSe.next()) {
                series_durchschnitt.getData().add(new XYChart.Data(reSe.getString("Zeitpunkt"), Double.parseDouble(reSe.getString("Durchschnitt"))));
                durchschnitt.add(Double.parseDouble(reSe.getString("Durchschnitt")));
            }
            con.close();
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("Verbinden fehlgeschlagen");
        }

        XYChart.Series series_gruen= new XYChart.Series();
        XYChart.Series series_rot= new XYChart.Series();
        series_gruen.setName("Börsendaten(Gewinn)");
        series_rot.setName("Börsendaten(Verlust)");

        for(int i=0; i<series_aktie.getData().size(); i++) {
            if(aktie.get(i)>durchschnitt.get(i)){
                series_gruen.getData().add(new XYChart.Data(datum.get(i), aktie.get(i)));
                series_rot.getData().add(new XYChart.Data(datum.get(i), 0.0));
            }else {
                if(aktie.get(i)<=durchschnitt.get(i)){
                    series_rot.getData().add(new XYChart.Data(datum.get(i), aktie.get(i)));
                    series_gruen.getData().add(new XYChart.Data(datum.get(i), 0.0));
                }
            }
        }
        Scene scene=new Scene(areaChart, 800, 600);
        //scene.getStylesheets().add("models/style.xsl");
        areaChart.getData().addAll(series_aktie, series_durchschnitt, series_gruen, series_rot);
        stage.setScene(scene);
        stage.show();
    }
}