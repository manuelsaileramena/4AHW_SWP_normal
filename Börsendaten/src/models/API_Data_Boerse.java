package models;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.SwingWrapper;

public class API_Data_Boerse /*extends Application*/{
    static Scanner reader = new Scanner(System.in);
    static ArrayList<Double> splitWerte = new ArrayList<>();
    static ArrayList<Double> splitVerbessert = new ArrayList<>();
    static ArrayList<Double> closeWerte = new ArrayList<>();
    static ArrayList<Double> gleitenderDurchschnitt = new ArrayList<>();
    static ArrayList<LocalDate> daten = new ArrayList<>();
    static ArrayList<Date> datumChart = new ArrayList<>();

    static ArrayList<Double> durchschnittDB = new ArrayList<>();
    static ArrayList<Double> closeDB = new ArrayList<>();
    static ArrayList<String> datumDB = new ArrayList<>();
    static ArrayList<String> aktien = new ArrayList<>();

    static ArrayList<LocalDate> datumHandelsliste = new ArrayList<>();
    static ArrayList<Double> closeHandelsliste = new ArrayList<>();
    static ArrayList<Double> durchschnittHandelsliste = new ArrayList<>();

    static LocalDate derzeitig = LocalDate.now();
    static String url, aktie;
    static double min, max;
    static double startkapital = 100000;
    static LocalDate startdatum = LocalDate.parse("2010-01-01");

    public static void main (String args[]) throws IOException, JSONException {
        try
        {
            API_Data_Boerse a = new API_Data_Boerse();
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
                    a.datumHandelslisteEinfügen();
                    a.handeln200();
                    a.kaufenUndHalten();
                    a.handeln200mit3();
                    //a.tradingEinfügen();
                    //a.trading200();
                    a.MinUndMax();
                    a.ListNull();
                    a.alleAuswählen();
                    for(String dates : datumDB)
                    {
                        datumChart.add(Date.valueOf(dates.toString()));
                    }
                    createFile(createChart(datumChart,closeDB,durchschnittDB));
                    new SwingWrapper<XYChart>(createChart(datumChart,closeDB,durchschnittDB)).displayChart();
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

    public static boolean check (String aktie) {
        File file = new File ("C:\\4AHWII\\SWP\\SWP Kölle\\Projekte\\Börsendaten\\Aktienbilder\\Chart_"+ aktie +"_"+LocalDate.now()+".jpg");
        return file.exists();
    }

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
                + "datum Date primary key unique," + "gleitenderDurchschnitt double);";
        String dropsplit = "drop table if exists " + aktie +"split ;";
        String splitsql = "CREATE TABLE IF NOT EXISTS " + aktie + "split (\n"
                + "datum Date primary key unique," + "close double," + "splitCoefficient double);";
        String dropbuyandhold = "drop table if exists " + aktie +"buyandhold ;";
        String buyandhold = "CREATE TABLE IF NOT EXISTS " + aktie + "buyandhold (\n"
                + "datum Date primary key unique, " + "ticker varchar(10), " + "flag varchar(1), " + "stücke int, " + "depot double);";
        String droptrading = "drop table if exists " + aktie +"trade ;";
        String trading = "CREATE TABLE IF NOT EXISTS " + aktie + "trade (\n"
                + "datum Date primary key unique, " + "ticker varchar(10), " + "flag varchar(1), " + "stücke int, " + "depot double);";
        String droptrading3 = "drop table if exists " + aktie +"trade3 ;";
        String trading3 = "CREATE TABLE IF NOT EXISTS " + aktie + "trade3 (\n"
                + "datum Date primary key unique, " + "ticker varchar(10), " + "flag varchar(1), " + "stücke int, " + "depot double);";

        try{
            Connection conn = DriverManager.getConnection(url, "root", "Fussball0508+");
            Statement stmt = conn.createStatement();
            stmt.execute(drop);
            stmt.execute(sql);
            stmt.execute(dropavg);
            stmt.execute(avgsql);
            stmt.execute(dropsplit);
            stmt.execute(splitsql);
            stmt.execute(dropbuyandhold);
            stmt.execute(buyandhold);
            stmt.execute(droptrading);
            stmt.execute(trading);
            stmt.execute(droptrading3);
            stmt.execute(trading3);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void einfügen() {
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
                splitVerbessert.add(closeWerte.get(i)/dividende);
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

    public void update () {
        String sql = "update " + aktie + " set close = ? where datum = \'?\';";
        try{
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for(int i = 0;i<closeWerte.size();i++)
            {
                sql = "update "+ aktie +" set close = " + splitVerbessert.get(i) + " where datum = \""+ daten.get(i).toString()+ "\";";
                pstmt.execute(sql);
            }
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

    public void MinUndMax() {
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
                datumDB.add(rsAVG.getString("datum"));
                closeDB.add(rs.getDouble("close"));
                durchschnittDB.add(avgTemp == 0 ? null : avgTemp);
            }
            datumDB.sort(null);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void ListNull() {
        datumDB = new ArrayList<String>();
        closeDB = new ArrayList<Double>();
        durchschnittDB = new ArrayList<Double>();
        datumChart = new ArrayList<Date>();
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

    public static boolean createFile(Object object) throws  IOException {
        if(object.getClass() != XYChart.class)
        {
            return false;
        }
        BitmapEncoder.saveBitmap((XYChart) object,"C:\\4AHWII\\SWP\\SWP Kölle\\Projekte\\Börsendaten\\Aktienbilder\\Chart_"+ aktie +"_"+ LocalDate.now(),BitmapEncoder.BitmapFormat.JPG);
        return true;
    }

    // Trading 200er Strategy
    public void insertStartTrade(String endung) {
        String sql = "insert into " + aktie + endung +" (datum, ticker, flag, stücke, depot) values ('?',?,?,?,?);";
        try {
            Connection conn = this.connection();
            PreparedStatement ptsmt = conn.prepareStatement(sql);
            sql = "insert into " + aktie + endung + " (datum, ticker, flag, stücke, depot) values " +
                    "(\'" + startdatum.minusDays(1) + "\','" + aktie + "','s',0," + startkapital + ");";
            ptsmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void datumHandelslisteEinfügen() {
        datumHandelsliste = new ArrayList<LocalDate>();
        closeHandelsliste = new ArrayList<Double>();
        durchschnittHandelsliste = new ArrayList<Double>();
        String sql = "select datum,close from " + aktie + " where datum between \'" + startdatum + "\' AND \'" + derzeitig.minusDays(1) + "\' ;";
        String sqlAvg = "select gleitenderDurchschnitt from " + aktie + "avg where datum between \'" + startdatum + "\' " +
                "AND \'" + derzeitig.minusDays(1) + "\';";
        try {
            Connection conn = this.connection();
            Statement smt = conn.createStatement();
            Statement stmtAvg = conn.createStatement();
            ResultSet rs = smt.executeQuery(sql);
            ResultSet rsA = stmtAvg.executeQuery(sqlAvg);
            while (rs.next() && rsA.next()) {
                rs.getString("datum");
                rs.getDouble("close");
                rsA.getDouble("gleitenderDurchschnitt");
                datumHandelsliste.add(LocalDate.parse(rs.getString("datum")));
                closeHandelsliste.add(rs.getDouble("close"));
                durchschnittHandelsliste.add(rsA.getDouble("gleitenderDurchschnitt"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void handeln200() throws SQLException {
        String flag = null;
        int stücke = 0;
        int depot = 0;
        double count = 0;
        String endung = "trade";
        insertStartTrade(endung);
        System.out.println("Handeln mit 200");
        for (int i = 0; i < datumHandelsliste.size(); i++) {
            int rest = 0;
            flag = null;
            stücke = 0;
            depot = 0;
            String sqlFlag = "select * from " + aktie + "trade order by datum desc limit 1";
            Connection conn = null;
            try {
                conn = this.connection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlFlag);
                while (rs.next()) {
                    flag = rs.getString("flag");
                    stücke = rs.getInt("stücke");
                    depot = rs.getInt("depot");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
            finally {
                conn.close();
            }
            if (flag.equals("s")) {
                if (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        || (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                    if (closeHandelsliste.get(i) > durchschnittHandelsliste.get(i)) {
                        count = 1;
                        if(splitWerte.get(i) > 1.0)
                        {
                            count = count * splitWerte.get(i) ;
                        }
                        stücke = (int) (depot / (closeHandelsliste.get(i) * count));
                        rest = (int) (stücke * closeHandelsliste.get(i));
                        depot = (depot - rest);
                        flag = "b";

                        insertTradeIntoDB((LocalDate) datumHandelsliste.get(i), aktie, endung, flag, stücke, depot);
                        System.out.println("gekauft");
                        System.out.println(stücke + " Stücke von Aktien");
                    }
                }
            } else if (flag.equals("b")) {
                if (!datumHandelsliste.get(i).getDayOfWeek().equals("SAMSTAG")
                        || (!datumHandelsliste.get(i).getDayOfWeek().equals("SONNTAG"))) {
                    if (closeHandelsliste.get(i) < durchschnittHandelsliste.get(i)) {
                        count = 1;
                        if(splitWerte.get(i) > 1.0)
                        {
                            count = count * splitWerte.get(i);
                        }
                        depot = (int) ((stücke * closeHandelsliste.get(i)*count) + depot);
                        flag = "s";
                        stücke = 0;
                        insertTradeIntoDB((LocalDate) datumHandelsliste.get(i),aktie, endung, flag, stücke, depot);
                        System.out.println("verkauft");
                        System.out.println(depot + " Geld im Depot");
                    }
                }
            }
            else
            {
                System.out.println("Datenbankfehler");
            }
        }
        System.out.println(aktie);
        depot = (int) (depot - startkapital);
        System.out.println(depot + " Geld im Depot");
        System.out.println(((depot/startkapital)*100.00) + " prozentueller Gewinn");
    }
    public void insertTradeIntoDB (LocalDate dateTrading, String ticker, String end, String flag, int stücke, int depot) throws SQLException
    {
        String insertFlag = "insert into " + aktie + end +" (datum, ticker, flag, stücke, depot) values ('?',?,?,?,?);";
        try {
            Connection conn = this.connection();
            PreparedStatement ptsmt = conn.prepareStatement(insertFlag);
            insertFlag = "insert into " + aktie + end +" (datum, ticker, flag, stücke, depot) values " +
                    "(\'" + dateTrading + "\','" + ticker + "','" + flag + "'," + stücke + "," + depot + ");";
            ptsmt.execute(insertFlag);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void kaufenUndHalten () throws SQLException {
        String flag = null;
        int stücke = 0;
        int depot = 0;
        double count = 0;
        String endung = "buyandhold";
        insertStartTrade(endung);
        System.out.println("Kaufen und Halten");
        for ( int i = 0; i<daten.size(); i++) {
            int rest = 0;
            flag = null;
            stücke = 0;
            depot = 0;
            String sqlFlag = "select * from " + aktie + "buyandhold order by datum desc limit 1";
            Connection conn = null;
            try {
                conn = this.connection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlFlag);
                while (rs.next()) {
                    flag = rs.getString("flag");
                    stücke = rs.getInt("stücke");
                    depot = rs.getInt("depot");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            } finally {
                conn.close();
            }
            if(daten.get(i) == datumHandelsliste.get(0))
            {
                count = 1;
                if(splitWerte.get(i) > 1.0)
                {
                    count = count * splitWerte.get(i);
                }
                stücke = (int) (depot / (closeHandelsliste.get(i) * count));
                rest = (int) (stücke * closeHandelsliste.get(i));
                depot = (depot - rest);
                flag = "b";
                insertTradeIntoDB((LocalDate) daten.get(i), aktie, endung, flag, stücke, depot);
                System.out.println("gekauft");
                System.out.println(stücke + " Stücke von Aktien");
            }
            else if(daten.get(i) == datumHandelsliste.get(datumHandelsliste.size()-1))
            {
                count = 1;
                if(splitWerte.get(i) > 1.0)
                {
                    count = count * splitWerte.get(i);
                }
                depot = (int) ((stücke * closeHandelsliste.get(i)*count) + depot);
                flag = "s";
                stücke = 0;
                insertTradeIntoDB((LocalDate) daten.get(i),aktie,endung,flag,stücke,depot);
                System.out.println("verkauft");
                System.out.println(depot + " Geld im Depot");
            }
        }
        System.out.println(aktie);
        depot = (int) (depot - startkapital);
        System.out.println(depot + " Geld im Depot");
        System.out.println(((depot/startkapital)*100.00) + " prozentueller Gewinn");
    }
    public void handeln200mit3() throws SQLException {
        String flag = null;
        int stücke = 0;
        int depot = 0;
        double count = 0;
        String endung = "trade3";
        insertStartTrade(endung);
        System.out.println("Handeln mit 200 und 3%");
        for (int i = 0; i < datumHandelsliste.size(); i++) {
            int rest = 0;
            flag = null;
            stücke = 0;
            depot = 0;
            String sqlFlag = "select * from " + aktie + "trade3 order by datum desc limit 1";
            Connection conn = null;
            try {
                conn = this.connection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlFlag);
                while (rs.next()) {
                    flag = rs.getString("flag");
                    stücke = rs.getInt("stücke");
                    depot = rs.getInt("depot");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
            finally {
                conn.close();
            }
            if (flag.equals("s")) {
                if (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        || (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                    if ((closeHandelsliste.get(i)*1.03) > durchschnittHandelsliste.get(i)) {
                        count = 1;
                        if(splitWerte.get(i) > 1.0)
                        {
                            count = count * splitWerte.get(i) ;
                        }
                        stücke = (int) (depot / ((closeHandelsliste.get(i)*1.03) * count));
                        rest = (int) (stücke * (closeHandelsliste.get(i)*1.03));
                        depot = (depot - rest);
                        flag = "b";
                        insertTradeIntoDB((LocalDate) datumHandelsliste.get(i), aktie, endung, flag, stücke, depot);
                        System.out.println("gekauft");
                        System.out.println(stücke + " Stücke von Aktien");
                    }
                }
            } else if (flag.equals("b")) {
                if (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        || (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                    if ((closeHandelsliste.get(i)*1.03) < durchschnittHandelsliste.get(i)) {
                        count = 1;
                        if(splitWerte.get(i) > 1.0)
                        {
                            count = count * splitWerte.get(i);
                        }
                        depot = (int) ((stücke * (closeHandelsliste.get(i)*1.03)*count) + depot);
                        flag = "s";
                        stücke = 0;
                        insertTradeIntoDB((LocalDate) datumHandelsliste.get(i),aktie, endung, flag, stücke, depot);
                        System.out.println("verkauft");
                        System.out.println(depot + " Geld im Depot");
                    }
                }
            }
            else
            {
                System.out.println("Datenbankfehler");
            }
        }
        System.out.println(aktie);
        depot = (int) (depot - startkapital);
        System.out.println(depot + " Geld im Depot");
        System.out.println(((depot/startkapital)*100.00) + " prozentueller Gewinn");
    }
}
