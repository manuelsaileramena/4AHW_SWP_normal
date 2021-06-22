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

public class API_Data_Boerse /*extends Application*/{

    static String url;
    static String urlDatabase = "jdbc:mysql://localhost:3306/api?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
    static int depotHandeln = 0;
    static int depotKaufenUndHalten = 0;
    static int depotHandeln3 = 0;
    static int depot;
    static int depotAller200 = 0;
    static int depotAller200mit3 = 0;
    static int depotAllerKaufenUndHalten = 0;

    static void readFile(List<String> aktien) throws FileNotFoundException {
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

    static void urlLesen(String aktie) {
        url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="+ aktie + "&outputsize=full&apikey=AR87OJ64MUWOW1H1"; //alphavantage-schüssel einfügen
    }

    static void getWert(String URL, List<LocalDate> daten, List<Double> closeWerte, List<Double> splitWerte) throws JSONException, IOException {
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
            conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
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

    public static boolean disconnect(Connection connection) throws SQLException{
        if(connection == null || connection.isClosed())
        {
            return false;
        }
        else
        {
            connection.close();
            return connection.isClosed();
        }
    }

    public static void neuenTableErstellen(String aktie) {
        String url = "jdbc:mysql://localhost:3306/api?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"; //Pfad einfügen
        String sql = "CREATE table if not exists "+ aktie +" (\n"
                + "datum Date primary key unique," + "close double);";
        String avgsql = "CREATE TABLE IF NOT EXISTS " + aktie + "avg (\n"
                + "datum Date primary key unique," + "gleitenderDurchschnitt double);";
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
        String summeallerstrategien = "CREATE TABLE IF NOT EXISTS " + aktie + "summe (\n"
                + "datum Date primary key unique, " + "buyandhold double, " + "trading double, " + "trading3 double);";

        try{
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.execute(avgsql);
            stmt.execute(splitsql);
            stmt.execute(dropbuyandhold);
            stmt.execute(buyandhold);
            stmt.execute(droptrading);
            stmt.execute(trading);
            stmt.execute(droptrading3);
            stmt.execute(trading3);
            stmt.execute(summeallerstrategien);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void einfügen(String aktie, List<LocalDate> daten, List<Double> closeWerte) {
        String sql = "INSERT IGNORE INTO " + aktie + "(datum, close) VALUES('?', ?);";
        try {
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 1; i < closeWerte.size(); i++) {
                sql = "INSERT IGNORE INTO " + aktie + "(datum, close) VALUES(\""+daten.get(i).toString()+"\","+ closeWerte.get(i)+");";
                pstmt.execute(sql);
            }
            disconnect(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void splitEinfügen(String aktie, List<LocalDate> daten, List<Double> closeWerte, List<Double> splitWerte){
        String sql = "INSERT IGNORE INTO " + aktie + "split (datum, close, splitCoefficient) VALUES('?', ?, ?);";
        try{
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i=1; i<splitWerte.size(); i++){
                sql = "INSERT IGNORE INTO " + aktie + "split (datum, close, splitCoefficient) VALUES(\""+daten.get(i).toString()+"\","+closeWerte.get(i)+","+splitWerte.get(i)+");";
                pstmt.execute(sql);
            }
            disconnect(conn);
        } catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void split(String aktie, List<LocalDate> daten, List<Double> closeWerte, List<Double> splitWerte, List<Double> splitVerbessert){
        String sql = "Select * from " + aktie + "split order by datum desc;";
        try
        {
            daten = new ArrayList<>();
            splitWerte= new ArrayList<>();
            closeWerte= new ArrayList<>();
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
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
            disconnect(conn);
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }

    }

    public void update (String aktie,List<LocalDate> daten, ArrayList<Double> splitVerbessert) {
        String sql = "update " + aktie + " set close = ? where datum = \'?\';";
        try{
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for(int i = 0;i<daten.size();i++)
            {
                sql = "update "+ aktie +" set close = " + splitVerbessert.get(i) + " where datum = \""+ daten.get(i).toString()+ "\";";
                pstmt.execute(sql);
            }
            disconnect(conn);
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void durchschnitt(String aktie, List<LocalDate> daten, List<Double> gleitenderDurchschnitt) {
        ResultSet rs = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            String url = "jdbc:mysql://localhost:3306/api?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
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
            disconnect(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void durchschnittEinfügen(String aktie, List<LocalDate> daten, List<Double> gleitenderDurchschnitt) {
        String sqlAVG = "INSERT IGNORE INTO "+ aktie +"avg (datum, gleitenderDurchschnitt) VALUES(?, ?)";
        try{
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            PreparedStatement pstmt = conn.prepareStatement(sqlAVG);
            for (int i = 0; i < gleitenderDurchschnitt.size(); i++) {
                sqlAVG = "INSERT IGNORE INTO "+ aktie +"avg (datum, gleitenderDurchschnitt) VALUES(\""+daten.get(i).toString()+"\","+ gleitenderDurchschnitt.get(i)+");";
                pstmt.execute(sqlAVG);
            }
            disconnect(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void summeAllerStrategienEinfügen(String aktie){
        String summe = "INSERT IGNORE INTO " + aktie + "summe (buyandhold, trading, trading3) VALUES(?, ?, ?);";
        try {
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            PreparedStatement pstmt = conn.prepareStatement(summe);
            summe = "INSERT IGNORE INTO " + aktie + "(buyandhold, trading, trading3) VALUES(\""+ depotKaufenUndHalten +"\","+ depotHandeln +"\","+ depotHandeln3 +");";
            pstmt.execute(summe);
            disconnect(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void summeAllerStrategien(){
        depot = depotHandeln + depotKaufenUndHalten + depotHandeln3;
        System.out.println(depot);
    }

    public void alleAuswählen(String aktie) {
        String handeln = "SELECT depot FROM "+ aktie +"trade order by datum desc limit 1;";
        String haltenkaufen = "SELECT depot FROM "+ aktie +"bh order by datum des limit 1;";
        String handelnmit3 = "SELECT depot FROM "+ aktie +"trade3 order by datum desc limit 1";

        try {
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            Statement stmt = conn.createStatement();
            Statement stmtAVG  = conn.createStatement();
            ResultSet rsh = stmt.executeQuery(handeln);
            ResultSet rshk = stmtAVG.executeQuery(haltenkaufen);
            ResultSet rsh3 = stmtAVG.executeQuery(handelnmit3);

            while (rsh.next() && rsh3.next()) {
                rsh.getDouble("handeln");
                rshk.getDouble("halten und kaufen");
                rsh3.getDouble("handeln mit 3%");
            }
            disconnect(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void ListNull(List<String> datumDB, List<Double> closeDB, List<Double> durchschnittDB, List<Date> datumChart) {
        datumDB = new ArrayList<String>();
        closeDB = new ArrayList<Double>();
        durchschnittDB = new ArrayList<Double>();
        datumChart = new ArrayList<Date>();
    }

    // Trading 200er Strategy
    public void insertStartTrade(String aktie,String endung, LocalDate startdatum, double startkapital) {
        String sql = "insert into " + aktie + endung +" (datum, ticker, flag, stücke, depot) values ('?',?,?,?,?);";
        try {
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            PreparedStatement ptsmt = conn.prepareStatement(sql);
            sql = "insert into " + aktie + endung + " (datum, ticker, flag, stücke, depot) values " +
                    "(\'" + startdatum.minusDays(1) + "\','" + aktie + "','s',0," + startkapital + ");";
            ptsmt.execute(sql);
            disconnect(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void datumHandelslisteEinfügen(double startkapital, LocalDate derzeitig, LocalDate startdatum, String aktie, List<LocalDate> datumHandelsliste, List<Double> closeHandelsliste, List<Double> durchschnittHandelsliste, List<LocalDate> daten) {
        datumHandelsliste = new ArrayList<LocalDate>();
        closeHandelsliste = new ArrayList<Double>();
        durchschnittHandelsliste = new ArrayList<Double>();
        String sql = "select datum,close from " + aktie + " where datum between \'" + startdatum + "\' AND \'" + derzeitig.minusDays(1) + "\' ;";
        String sqlAvg = "select gleitenderDurchschnitt from " + aktie + "avg where datum between \'" + startdatum + "\' " +
                "AND \'" + derzeitig.minusDays(1) + "\';";
        try {
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
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
            handeln200(startkapital, aktie, datumHandelsliste, closeHandelsliste, durchschnittHandelsliste, startdatum);
            System.out.println();
            kaufenUndHalten(startkapital, aktie, datumHandelsliste, closeHandelsliste, daten, startdatum);
            System.out.println();
            handeln200mit3(startkapital, aktie, datumHandelsliste, closeHandelsliste, durchschnittHandelsliste, startdatum);
            disconnect(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void handeln200(double startkapital, String aktie, List<LocalDate> datumHandelsliste, List<Double> closeHandelsliste, List<Double> durchschnittHandelsliste, LocalDate startdatum) throws SQLException {
        String flag = null;
        int stücke = 0;
        String endung = "trade";
        insertStartTrade(aktie,endung, startdatum, startkapital);
        System.out.println("Handeln mit 200");
        Connection conn = null;
        conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
        for (int i = 0; i < datumHandelsliste.size(); i++) {
            int rest = 0;
            String sqlFlag = "select * from " + aktie + "trade order by datum desc limit 1";
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlFlag);
                while (rs.next()) {
                    flag = rs.getString("flag");
                    stücke = rs.getInt("stücke");
                    depotHandeln = rs.getInt("depot");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
            if (flag.equals("s")) {
                if (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        || (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                    if (closeHandelsliste.get(i) > durchschnittHandelsliste.get(i)) {
                        stücke = (int) (depotHandeln / (closeHandelsliste.get(i)));
                        rest = (int) (stücke * closeHandelsliste.get(i));
                        depotHandeln = (depotHandeln - rest);
                        flag = "b";

                        insertTradeIntoDB(aktie,(LocalDate) datumHandelsliste.get(i), aktie, endung, flag, stücke, depotHandeln);
                    }
                }
            } else if (flag.equals("b")) {
                if (!datumHandelsliste.get(i).getDayOfWeek().equals("SAMSTAG")
                        || (!datumHandelsliste.get(i).getDayOfWeek().equals("SONNTAG"))) {
                    if (closeHandelsliste.get(i) < durchschnittHandelsliste.get(i)) {
                        depotHandeln = (int) ((stücke * closeHandelsliste.get(i)) + depotHandeln);
                        flag = "s";
                        stücke = 0;
                        insertTradeIntoDB(aktie,(LocalDate) datumHandelsliste.get(i),aktie, endung, flag, stücke, depotHandeln);
                    }
                }
                if(datumHandelsliste.get(i) == datumHandelsliste.get(datumHandelsliste.size()-1))
                {
                    double tempClose = closeHandelsliste.get(datumHandelsliste.size()-1);
                    if(flag.equals("b"))
                    {
                        depotHandeln = (int) ((stücke*tempClose) + depotHandeln);
                        flag = "s";
                        stücke = 0;
                        insertTradeIntoDB(aktie,(LocalDate) datumHandelsliste.get(i),aktie, endung, flag, stücke, depotHandeln);
                    }
                }
            }
            else
            {
                System.out.println("Datenbankfehler");
            }
        }
        disconnect(conn);
        depotHandeln = (int) (depotHandeln - startkapital);
        System.out.println(depotHandeln + " Geld im Depot");
        depotAller200 += depotHandeln;
        System.out.println(((depotHandeln/startkapital)*100.00) + " prozentuelle Änderung");
    }

    public void insertTradeIntoDB (String aktie,LocalDate dateTrading, String ticker, String end, String flag, int stücke, int depot) throws SQLException
    {
        String insertFlag = "insert into " + aktie + end +" (datum, ticker, flag, stücke, depot) values ('?',?,?,?,?);";
        try {
            Connection conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
            PreparedStatement ptsmt = conn.prepareStatement(insertFlag);
            insertFlag = "insert into " + aktie + end +" (datum, ticker, flag, stücke, depot) values " +
                    "(\'" + dateTrading + "\','" + ticker + "','" + flag + "'," + stücke + "," + depot + ");";
            ptsmt.execute(insertFlag);
            disconnect(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void kaufenUndHalten (double startkapital, String aktie, List<LocalDate> datumHandelsliste, List<Double> closeHandelsliste, List<LocalDate> daten, LocalDate startdatum) throws SQLException {
        String flag = null;
        int stücke = 0;
        String endung = "buyandhold";
        insertStartTrade(aktie, endung, startdatum, startkapital);
        System.out.println("Kaufen und Halten");
        Connection conn = null;
        conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");
        for ( int i = 0; i<datumHandelsliste.size(); i++) {
            int rest = 0;
            String sqlFlag = "select * from " + aktie + "buyandhold order by datum desc limit 1";
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlFlag);
                while (rs.next()) {
                    flag = rs.getString("flag");
                    stücke = rs.getInt("stücke");
                    depotKaufenUndHalten = rs.getInt("depot");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
            if(datumHandelsliste.get(i) == datumHandelsliste.get(0))
            {
                stücke = (int) (depotKaufenUndHalten / (closeHandelsliste.get(i)));
                rest = (int) (stücke * closeHandelsliste.get(i));
                depotKaufenUndHalten = (depotKaufenUndHalten - rest);
                flag = "b";
                insertTradeIntoDB(aktie,(LocalDate) datumHandelsliste.get(i), aktie, endung, flag, stücke, depotKaufenUndHalten);
            }
            else if(datumHandelsliste.get(i) == datumHandelsliste.get(datumHandelsliste.size()-1))
            {
                depotKaufenUndHalten = (int) ((stücke * closeHandelsliste.get(i)) + depotKaufenUndHalten);
                flag = "s";
                stücke = 0;
                insertTradeIntoDB(aktie,(LocalDate) datumHandelsliste.get(i),aktie,endung,flag,stücke,depotKaufenUndHalten);
            }
        }
        disconnect(conn);
        depotKaufenUndHalten = (int) (depotKaufenUndHalten - startkapital);
        System.out.println(depotKaufenUndHalten + " Geld im Depot");
        depotAllerKaufenUndHalten += depotKaufenUndHalten;
        System.out.println(((depotKaufenUndHalten/startkapital)*100.00) + " prozentuelle Änderung");
    }

    public void handeln200mit3(double startkapital, String aktie, List<LocalDate> datumHandelsliste, List<Double> closeHandelsliste, List<Double> durchschnittHandelsliste, LocalDate startdatum) throws SQLException {
        String flag = null;
        int stücke = 0;
        String endung = "trade3";
        insertStartTrade(aktie, endung, startdatum, startkapital);
        System.out.println("Handeln mit 200 und 3%");
        Connection conn = null;
        conn = DriverManager.getConnection(urlDatabase, "root", "Fussball0508+");;
        for (int i = 0; i < datumHandelsliste.size(); i++) {
            int rest = 0;
            String sqlFlag = "select * from " + aktie + "trade3 order by datum desc limit 1";
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlFlag);
                while (rs.next()) {
                    flag = rs.getString("flag");
                    stücke = rs.getInt("stücke");
                    depotHandeln3 = rs.getInt("depot");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
            if (flag.equals("s")) {
                if (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        || (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                    if ((closeHandelsliste.get(i)*1.03) > durchschnittHandelsliste.get(i)) {
                        stücke = (int) (depotHandeln3 / ((closeHandelsliste.get(i)*1.03)));
                        rest = (int) (stücke * (closeHandelsliste.get(i)*1.03));
                        depotHandeln3 = (depotHandeln3 - rest);
                        flag = "b";
                        insertTradeIntoDB(aktie,(LocalDate) datumHandelsliste.get(i), aktie, endung, flag, stücke, depotHandeln3);
                    }
                }
            } else if (flag.equals("b")) {
                if (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        || (!datumHandelsliste.get(i).getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                    if ((closeHandelsliste.get(i)*1.03) < durchschnittHandelsliste.get(i)) {
                        depotHandeln3 = (int) ((stücke * (closeHandelsliste.get(i)*1.03)) + depotHandeln3);
                        flag = "s";
                        stücke = 0;
                        insertTradeIntoDB(aktie,(LocalDate) datumHandelsliste.get(i),aktie, endung, flag, stücke, depotHandeln3);
                    }
                }
                if(datumHandelsliste.get(i) == datumHandelsliste.get(datumHandelsliste.size()-1))
                {
                    double tempClose = closeHandelsliste.get(datumHandelsliste.size()-1);
                    if(flag.equals("b"))
                    {
                        depotHandeln3 = (int) ((stücke*tempClose) + depotHandeln3);
                        flag = "s";
                        stücke = 0;
                        insertTradeIntoDB(aktie,(LocalDate) datumHandelsliste.get(i),aktie, endung, flag, stücke, depotHandeln3);
                    }
                }
            }
            else
            {
                System.out.println("Datenbankfehler");
            }
        }
        disconnect(conn);
        depotHandeln3 = (int) (depotHandeln3 - startkapital);
        System.out.println(depotHandeln3 + " Geld im Depot");
        depotAller200mit3 += depotHandeln3;
        System.out.println(((depotHandeln3/startkapital)*100.00) + " prozentuelle Änderung");
    }
}
