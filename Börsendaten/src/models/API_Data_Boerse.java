package models;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.application.Application;

public class API_Data_Boerse extends Application{
    static Scanner reader = new Scanner(System.in);
    static ArrayList<Double> closeWerte = new ArrayList<>();
    static ArrayList<Double> gleitenderDurchschnitt = new ArrayList<>();
    static ArrayList<LocalDate> daten = new ArrayList<>();

    static ArrayList<Double> avgDB = new ArrayList<>();
    static ArrayList<Double> closeDB = new ArrayList<>();
    static ArrayList<String> dateDB = new ArrayList<>();

    static String url, Aktien;
    static int Tage;
    static double min, max;

    public static void main (String args[]) throws IOException, JSONException {
        API_Data_Boerse a = new API_Data_Boerse();
        a.eingangNutzer();
        a.urlLesen();
        a.getWert(url);
        a.verbinden();
        a.neuenTableErstellen();
        a.einfügen();
        a.durchschnitt();
        a.durchschnittEinfügen();
        a.MinUndMax();
        a.alleAuswählen();
        Application.launch(args);
    }
    static void eingangNutzer() {
        System.out.println("Aktie (nur USA): ");
        Aktien = reader.next();
        System.out.println("Wie viele Tage sollte der Graph beinhalten: ");
        Tage = reader.nextInt();
    }
    static void urlLesen() {
        url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="+ Aktien + "&outputsize=full&apikey=AR87OJ64MUWOW1H1"; //alphavantage-schüssel einfügen
    }
    static void getWert(String URL) throws JSONException, IOException {
        JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
        json = json.getJSONObject("Time Series (Daily)");
        for(int i = 0; i < Tage /*json.names().length()*/; i++) {
            daten.add(LocalDate.parse((CharSequence) json.names().get(i)));
            closeWerte.add(json.getJSONObject(LocalDate.parse((CharSequence) json.names().get(i)).toString()).getDouble("4. close"));
        }

    }

    public static void verbinden() {
        Connection conn = null;
        try {
            String url = "jdbc:mysql://localhost:3306/api?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(url, "root", "Fussball0508+");
            System.out.println("Connection to MySQL has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    private Connection connection() {
        String url = "jdbc:mysql://localhost:3306/api?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"; //Pfad einfügen
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url,"root", "Fussball0508+");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    public static void neuenTableErstellen() {
        String url = "jdbc:mysql://localhost:3306/api?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"; //Pfad einfügen
        String drop = "Drop Table if exists " + Aktien + ";";
        String sql = "CREATE table if not exists "+ Aktien +" (\n"
                + "datum Date primary key unique," + "close double);";
        String dropavg = "drop table if exists " + Aktien +"avg ;";
        String avgsql = "CREATE TABLE IF NOT EXISTS " + Aktien + "avg (\n"
                + "datum Date primary key unique," + "gleitenderDurchschnitt double)";
        try{
            Connection conn = DriverManager.getConnection(url, "root", "Fussball0508+");
            Statement stmt = conn.createStatement();
            stmt.execute(drop);
            stmt.execute(sql);
            stmt.execute(dropavg);
            stmt.execute(avgsql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void einfügen()
    {
        String sql = "INSERT INTO " + Aktien + "(datum, close) VALUES('?', ?);";
        try {
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 1; i < closeWerte.size(); i++) {
                sql = "INSERT INTO " + Aktien + "(datum, close) VALUES(\""+daten.get(i).toString()+"\","+ closeWerte.get(i)+");";
                pstmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void durchschnitt() {
        ResultSet rs = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:mysql://localhost:3306/api?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(url, "root", "Fussball0508+");
            stmt = conn.createStatement();
            String sql;
            for(LocalDate avg : daten) {
                sql = "Select avg(close) from " + Aktien + " where (datum < \'" + avg.toString() + "\') and (datum >= \'" + avg.minusDays(200).toString() + "\') order by datum desc;";
                rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    for(int i = 0; i<rs.getMetaData().getColumnCount();i++)
                    {
                        gleitenderDurchschnitt.add(rs.getDouble(i+1));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void durchschnittEinfügen() {
        String sqlAVG = "INSERT INTO "+ Aktien +"avg (datum, gleitenderDurchschnitt) VALUES(?, ?)";
        try{
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sqlAVG);
            for (int i = 0; i < gleitenderDurchschnitt.size(); i++) {
                sqlAVG = "INSERT INTO "+ Aktien +"avg (datum, gleitenderDurchschnitt) VALUES(\""+daten.get(i).toString()+"\","+ gleitenderDurchschnitt.get(i)+");";
                pstmt.execute(sqlAVG);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void MinUndMax()
    {
        String sqlmax = "select max(close) from "+ Aktien + ";";
        String sqlmin = "select min(close) from "+ Aktien + ";";
        try
        {
            Connection conn = this.connection();
            Statement stmt = conn.createStatement();
            ResultSet rsmax = stmt.executeQuery(sqlmax);
            while(rsmax.next())
            {
                max = rsmax.getDouble(1);
            }
            ResultSet rsmin = stmt.executeQuery(sqlmin);
            while (rsmin.next())
            {
                min = rsmin.getDouble(1);
            }

        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
    public void alleAuswählen() {
        String sql = "SELECT * FROM "+ Aktien +" order by datum;";
        String sqlAVG = "SELECT * FROM "+ Aktien +"AVG order by datum;";
        try {
            Connection conn = this.connection();
            Statement stmt = conn.createStatement();
            Statement stmtAVG  = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSet rsAVG = stmtAVG.executeQuery(sqlAVG);

            System.out.println("Datum               Close Werte             Durchschnitt");
            while (rs.next() && rsAVG.next()) {
                System.out.println(
                        rs.getString("datum")  + "\t \t \t \t" +
                                rs.getDouble("close") + "\t \t \t \t" +
                                rsAVG.getDouble("gleitenderDurchschnitt")
                );
                Double avgTemp = rsAVG.getDouble("gleitenderDurchschnitt");
                dateDB.add(rsAVG.getString("datum"));
                closeDB.add(rs.getDouble("close"));
                avgDB.add(avgTemp == 0 ? null : avgTemp);
            }
            dateDB.sort(null);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public void start(Stage primaryStage) {
        try {
            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            yAxis.setAutoRanging(false);

            yAxis.setLowerBound(min - (min * 0.1));         // Minimum Value
            yAxis.setUpperBound(max + (max * 0.1));         // Maximum Value

            xAxis.setLabel("Datum");
            yAxis.setLabel("Close-Wert");
            final LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
            lineChart.setTitle("Aktienkurs");
            XYChart.Series<String, Number> tatsaechlich = new XYChart.Series();
            tatsaechlich.setName("Close-Werte");
            for (int i = (closeWerte.size() == 10) ? closeWerte.size()-10 : closeWerte.size() - 11; i < closeWerte.size() -1; i++) {
                tatsaechlich.getData().add(new XYChart.Data(dateDB.get(i), closeDB.get(i)));
            }
            /*for (int i = 0; i< closeWerte.size() - 1; i++)
            {
                tatsaechlich.getData().add(new XYChart.Data(dateDB.get(i), closeDB.get(i)));
            }*/
            XYChart.Series<String, Number> durchschnitt = new XYChart.Series();
            durchschnitt.setName("gleitender Durchschnitt");
            for (int i = (gleitenderDurchschnitt.size() == 10) ? gleitenderDurchschnitt.size()-10 : gleitenderDurchschnitt.size() - 11 ; i < gleitenderDurchschnitt.size()-1; i++) {
                durchschnitt.getData().add(new XYChart.Data(dateDB.get(i), avgDB.get(i)));
            }
            /*for (int i = 1; i< gleitenderDurchschnitt.size() - 1; i++)
            {
                durchschnitt.getData().add(new XYChart.Data(dateDB.get(i), avgDB.get(i)));
            }*/
            Scene scene = new Scene(lineChart, 1000, 600);
            lineChart.getData().add(tatsaechlich);
            lineChart.getData().add(durchschnitt);

            lineChart.setCreateSymbols(false);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
