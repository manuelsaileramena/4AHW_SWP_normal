package models;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.json.JSONException;
import java.net.MalformedURLException;
import java.io.IOException;

public class Hauptprogramm extends Application {

    static Zaehler zaehler = new Zaehler();

    public static void main(String[] args) throws MalformedURLException, JSONException, IOException {

        Verknüpfung_mit_DB db = new Verknüpfung_mit_DB();

        db.output();
        launch(args);

        db.VerbindungDB("localhost:3306", "Feiertage", "root", "Fussball0508+");

    }

        @Override
        public void start (Stage stage) throws Exception {
            String montag = "Montag", dienstag = "Dienstag", mittwoch = "Mittwoch", donnerstag = "Donnerstag", freitag = "Freitag";

            final NumberAxis xAchse = new NumberAxis();
            final CategoryAxis yAchse = new CategoryAxis();

            final BarChart<Number, String> barChart = new BarChart<Number, String>(xAchse, yAchse);
            barChart.setTitle("Freie Wochentage");
            xAchse.setLabel("Wochentage");
            yAchse.setLabel("Tage");

            XYChart.Series series = new XYChart.Series();
            series.getData().add(new XYChart.Data(zaehler.montag, montag));
            series.getData().add(new XYChart.Data(zaehler.dienstag, dienstag));
            series.getData().add(new XYChart.Data(zaehler.mittwoch, mittwoch));
            series.getData().add(new XYChart.Data(zaehler.donnerstag, donnerstag));
            series.getData().add(new XYChart.Data(zaehler.freitag, freitag));


            Scene scene = new Scene(barChart, 640, 480);
            barChart.getData().addAll(series);
            stage.setScene(scene);
            stage.show();
        }

}