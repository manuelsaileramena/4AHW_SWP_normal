package models;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.application.Application;
import java.io.File;
import javafx.scene.image.WritableImage;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.SwingWrapper;
import javax.imageio.ImageIO;

public class API_Data_Boerse /*extends Application*/{
    static Scanner reader = new Scanner(System.in);
    static ArrayList<Double> splitWerte = new ArrayList<>();
    static ArrayList<Double> splitCorrected = new ArrayList<>();
    static ArrayList<Double> closeWerte = new ArrayList<>();
    static ArrayList<Double> gleitenderDurchschnitt = new ArrayList<>();
    static ArrayList<LocalDate> daten = new ArrayList<>();
    static ArrayList<Date> datumChart = new ArrayList<>();

    static ArrayList<Double> avgDB = new ArrayList<>();
    static ArrayList<Double> closeDB = new ArrayList<>();
    static ArrayList<String> dateDB = new ArrayList<>();
    static ArrayList<String> aktien = new ArrayList<>();

    static String url, aktie;
    static int Tage;
    static double min, max;

    public static void main (String args[]) throws IOException, JSONException {
        try
        {API_Data_Boerse a = new API_Data_Boerse();
        a.readFile();
        for(int i =0;i<aktien.size();i++)
        {
            aktie = aktien.get(i);
            System.out.println(aktie);
            if(!check(aktie))
            {
                a.urlLesen();
                a.getWert(url);
                a.verbinden();
                a.neuenTableErstellen();
                a.einfügen();
                a.splitEinfügen();
                a.split();
                a.update();
                a.durchschnitt();
                a.durchschnittEinfügen();
                a.MinUndMax();
                a.ListNull();
                a.alleAuswählen();
                for(String dates : dateDB)
                {
                    datumChart.add(Date.valueOf(dates.toString()));
                }
                createFile(createChart(datumChart,closeDB,avgDB));
                new SwingWrapper<XYChart>(createChart(datumChart,closeDB,avgDB)).displayChart();
                //Application.launch(args);
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
    static void readFile() throws FileNotFoundException {
        Scanner reader = new Scanner(new File("C:\\4AHWII\\SWP\\SWP Kölle\\Projekte\\Börsendaten\\src\\models\\Aktien.txt"));
        while(reader.hasNextLine())
        {
            aktien.add(reader.nextLine());
        }

    }

    public static boolean check (String aktie)
    {
        File file = new File ("C:\\4AHWII\\SWP\\SWP Kölle\\Projekte\\Börsendaten\\Aktienbilder\\Chart_"+ aktie +"_"+LocalDate.now()+".jpg");
        return file.exists();
    }
    /*static void eingangNutzer() {
        System.out.println("Aktie (nur USA): ");
        aktie = reader.next();
        /*System.out.println("Wie viele Tage sollte der Graph beinhalten: ");
        Tage = reader.nextInt();
    }*/
    static void urlLesen() {
        url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="+ aktie + "&outputsize=full&apikey=AR87OJ64MUWOW1H1"; //alphavantage-schüssel einfügen
    }
    static void getWert(String URL) throws JSONException, IOException {
        JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
        json = json.getJSONObject("Time Series (Daily)");
        for(int i = 0; i < /*Tage*/ json.names().length(); i++) {
            daten.add(LocalDate.parse((CharSequence) json.names().get(i)));
            closeWerte.add(json.getJSONObject(LocalDate.parse((CharSequence) json.names().get(i)).toString()).getDouble("4. close"));
            splitWerte.add(json.getJSONObject(LocalDate.parse((CharSequence) json.names().get(i)).toString()).getDouble("8. split coefficient"));
        }

    }

    public static void verbinden() {
        Connection conn = null;
        try {
            String url = "jdbc:mysql://localhost:3306/api?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
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
    private Connection connection(){
        String url = "jdbc:mysql://localhost:3306/api?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"; //Pfad einfügen
        Connection conn = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            System.out.println(e.getException());
        }
        try {

            conn = DriverManager.getConnection(url,"root", "Fussball0508+");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    public static void neuenTableErstellen() {
        String url = "jdbc:mysql://localhost:3306/api?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"; //Pfad einfügen
        String drop = "Drop Table if exists " + aktie + ";";
        String sql = "CREATE table if not exists "+ aktie +" (\n"
                + "datum Date primary key unique," + "close double);";
        String dropavg = "drop table if exists " + aktie +"avg ;";
        String avgsql = "CREATE TABLE IF NOT EXISTS " + aktie + "avg (\n"
                + "datum Date primary key unique," + "gleitenderDurchschnitt double)";
        String dropsplit = "drop table if exists " + aktie +"split ;";
        String splitsql = "CREATE TABLE IF NOT EXISTS " + aktie + "split (\n"
                + "datum Date primary key unique," + "close double," + "splitCoefficient double)";
        try{
            Connection conn = DriverManager.getConnection(url, "root", "Fussball0508+");
            Statement stmt = conn.createStatement();
            stmt.execute(drop);
            stmt.execute(sql);
            stmt.execute(dropavg);
            stmt.execute(avgsql);
            stmt.execute(dropsplit);
            stmt.execute(splitsql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void einfügen()
    {
        String sql = "INSERT INTO " + aktie + "(datum, close) VALUES('?', ?);";
        try {
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 1; i < closeWerte.size(); i++) {
                sql = "INSERT INTO " + aktie + "(datum, close) VALUES(\""+daten.get(i).toString()+"\","+ closeWerte.get(i)+");";
                pstmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void splitEinfügen(){
        String sql = "INSERT INTO " + aktie + "split (datum, close, splitCoefficient) VALUES('?', ?, ?);";
        try{
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i=1; i<splitWerte.size(); i++){
                sql = "INSERT INTO " + aktie + "split (datum, close, splitCoefficient) VALUES(\""+daten.get(i).toString()+"\","+closeWerte.get(i)+","+splitWerte.get(i)+");";
                pstmt.execute(sql);
            }
        } catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void split(){
        String sql = "Select * from " + aktie + "split order by datum desc;";
        try
        {
            daten = new ArrayList<>();
            splitWerte= new ArrayList<>();
            closeWerte= new ArrayList<>();
            Connection conn = this.connection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next())
            {
                rs.getString("datum");
                rs.getDouble("close");
                rs.getDouble("splitCoefficient");
                daten.add(LocalDate.parse(rs.getString("datum")));
                closeWerte.add(rs.getDouble("close"));
                splitWerte.add(rs.getDouble("splitCoefficient"));
            }
            double dividende = 1;
            for (int i=0; i<splitWerte.size(); i++){
                splitCorrected.add(closeWerte.get(i)/dividende);
                dividende = dividende * splitWerte.get(i);
            }
            /*for(int i = 0 ; i<splitWerte.size();i++)
            {
                System.out.println(String.format("%10s", closeWerte.get(i).toString())+String.format("%10s", splitWerte.get(i).toString()) + String.format("%30s", splitCorrected.get(i).toString()));
            }*/
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }

    }
    public void update ()
    {
        String sql = "update " + aktie + " set close = ? where datum = \'?\';";
        try{
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for(int i = 0;i<closeWerte.size();i++)
            {
                sql = "update "+ aktie +" set close = " + splitCorrected.get(i) + " where datum = \""+ daten.get(i).toString()+ "\";";
                pstmt.execute(sql);
            }
            System.out.println(sql);
        }
        catch(SQLException e)
        {
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
                sql = "Select avg(close) from " + aktie + " where (datum < \'" + avg.toString() + "\') and (datum >= \'" + avg.minusDays(200).toString() + "\') order by datum desc;";
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
        String sqlAVG = "INSERT INTO "+ aktie +"avg (datum, gleitenderDurchschnitt) VALUES(?, ?)";
        try{
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sqlAVG);
            for (int i = 0; i < gleitenderDurchschnitt.size(); i++) {
                sqlAVG = "INSERT INTO "+ aktie +"avg (datum, gleitenderDurchschnitt) VALUES(\""+daten.get(i).toString()+"\","+ gleitenderDurchschnitt.get(i)+");";
                pstmt.execute(sqlAVG);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void MinUndMax()
    {
        String sqlmax = "select max(close) from "+ aktie + ";";
        String sqlmin = "select min(close) from "+ aktie + ";";
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
        String sql = "SELECT * FROM "+ aktie +" order by datum;";
        String sqlAVG = "SELECT * FROM "+ aktie +"AVG order by datum;";
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
    public static void ListNull()
    {
        dateDB = new ArrayList<String>();
        closeDB = new ArrayList<Double>();
        avgDB = new ArrayList<Double>();
        datumChart = new ArrayList<Date>();
    }
    public static XYChart createChart(List<Date> d, List<Double>... multipleYAxis)
    {
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
    public static boolean createFile(Object object) throws  IOException {
        if(object.getClass() != XYChart.class)
        {
            return false;
        }
        BitmapEncoder.saveBitmap((XYChart) object,"C:\\4AHWII\\SWP\\SWP Kölle\\Projekte\\Börsendaten\\Aktienbilder\\Chart_"+ aktie +"_"+ LocalDate.now(),BitmapEncoder.BitmapFormat.JPG);
        return true;
    }
   /* @Override
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
            for (int i = 0; i< closeWerte.size() - 1; i++)
            {
                tatsaechlich.getData().add(new XYChart.Data(dateDB.get(i), closeDB.get(i)));
            }
            XYChart.Series<String, Number> durchschnitt = new XYChart.Series();
            durchschnitt.setName("gleitender Durchschnitt");
            for (int i = (gleitenderDurchschnitt.size() == 10) ? gleitenderDurchschnitt.size()-10 : gleitenderDurchschnitt.size() - 11 ; i < gleitenderDurchschnitt.size()-1; i++) {
                durchschnitt.getData().add(new XYChart.Data(dateDB.get(i), avgDB.get(i)));
            }
            for (int i = 1; i< gleitenderDurchschnitt.size() - 1; i++)
            {
                durchschnitt.getData().add(new XYChart.Data(dateDB.get(i), avgDB.get(i)));
            }
            if(closeWerte.get(closeWerte.size()-1) > gleitenderDurchschnitt.get(gleitenderDurchschnitt.size()-1))
            {
                lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color:transparent;");
                lineChart.setStyle("-fx-background-color:#0ef898;");
            }
            else
            {
                lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color:transparent;");
                lineChart.setStyle("-fx-background-color:#fa6f50;");
            }
            Scene scene = new Scene(lineChart, 1000, 600);
            lineChart.getData().add(tatsaechlich);
            lineChart.getData().add(durchschnitt);

            WritableImage image = scene.snapshot(null);
            File file = new File ("C:\\4AHWII\\SWP\\SWP Kölle\\Projekte\\Börsendaten\\Aktienbilder\\Chart_"+ aktie +"_"+LocalDate.now()+".png");
            ImageIO.write(SwingFXUtils.fromFXImage(image,null),"PNG",file);

            lineChart.setCreateSymbols(false);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }*/
}
