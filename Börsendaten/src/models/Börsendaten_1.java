package models;

import com.mysql.cj.exceptions.ConnectionIsClosedException;
import com.mysql.cj.jdbc.exceptions.ConnectionFeatureNotAvailableException;
import com.mysql.cj.protocol.Resultset;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class Börsendaten_1 extends Durchschnitt{

    static String url;
    static double min, max;
    static ArrayList<LocalDate> datums = new ArrayList<>();
    static ArrayList<Double> closeValue = new ArrayList<>();
    static ArrayList<Double> average = new ArrayList<>();
    public void setType(String type) {
        this.type = type;
    }

    /*String type;*/
    static final String URL = "jdbc:mysql://localhost:3306/Börsendaten?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC";
    int tage;
    int zaehler=0;
    String host, database, user, passwort;

    public Börsendaten_1(String s, int t, String h, String d, String u, String p) {
        super(s, t);
        this.host=h;
        this.database=d;
        this.user=u;
        this.passwort=p;
    }
    public void closePreis() throws JSONException, MalformedURLException, IOException {
        url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + super.getType() + "&outputsize=full&apikey=c";
        JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
        json = json.getJSONObject("Time Series (Daily)");
        for (int i = 0; i < json.names().length(); i++) {
            datums.add(LocalDate.parse((CharSequence) json.names().get(i)));
            closeValue.add(json.getJSONObject(LocalDate.parse((CharSequence) json.names().get(i)).toString()).getDouble("4. close"));
        }
    }
    public static void connect()
    {
        Connection conn = null;
        try
        {
            conn = DriverManager.getConnection(URL, "root", "Fussball0508+");
            System.out.println("Connection to MySQL has been established");
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            try
            {
                if(conn != null)
                {
                    conn.close();
                }
            }

            catch(SQLException ex)
            {
                System.out.println(ex.getMessage());
            }
        }
    }
    public Connection connection()
    {
        Connection conn = null;
        try
        {
            conn = DriverManager.getConnection(URL,"root","Fussball0508+");
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    public void verbindungDB(){
        Scanner s=new Scanner(System.in);
        String sql = "Create table if not exists Daten"+super.getType()+" (Zeitpunkt Date,"+"TagesEndPreis double,"+"Primary Key(Zeitpunkt)";
        String sqlAvg = "Create table if not exists Durchschnitt"+super.getType()+" (Zeitpunkt Date," + "Durchschnitt double," + " Primary Key (Zeitpunkt);";
        String drop = "Drop table if exists Daten"+super.getType()+";";
        String dropAvg = "Drop table if exists Durchschnitt"+super.getType()+";";

        try {
            Connection con = this.connection();
            Statement stat=con.createStatement();
            System.out.println("DB-Tabelle erstellen?(j/n)");
            if(s.next().equals("j")) {
                stat.execute(drop);
                stat.execute(sql);
                stat.execute(dropAvg);
                stat.execute(sqlAvg);
            }
        }catch(SQLException ex){
            System.out.println(ex);
        }
    }

    public void DB_INSERT(){
        String sql = "Insert into Daten"+super.getType()+ " (Zeitpunkt,TagesEndPreis) values ('?',?);";
        try {
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for(int i = 1; i< closeValue.size(); i++)
            {
                sql= "Insert into Daten"+super.getType()+" (Zeitpunkt, TagesEndPreis) values (\'"+ datums.get(i).toString() +"\',"+closeValue.get(i)+");";
            }
        }catch(SQLException ex){
            System.out.println(ex.getMessage());
        }
    }
    public void DB_Average()
    {
        ResultSet rs = null;
        Connection conn = null;
        Statement stmt = null;
        try
        {
            conn = this.connection();
            stmt = conn.createStatement();
            String sql;
            for(LocalDate avg : datums)
            {
                sql = "select avg(TagesEndPreis) from Daten" + super.getType() + " where (Zeitpunkt < \'" + avg.toString() + "\') and (Zeitpunkt >=  \'" + avg.minusDays(200).toString() + "\') order by Zeitpunkt desc";
                rs = stmt.executeQuery(sql);
                while(rs.next())
                {
                    for(int i = 0; i<rs.getMetaData().getColumnCount();i++)
                    {
                        average.add(rs.getDouble(i+1));
                    }
                }
            }
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
    public void DB_INSERTAVG()
    {
        String sqlAvg="Insert into Durchschnitt"+super.getType()+" (Zeitpunkt, Durchschnitt) values ('?',?)";
        try
        {
            Connection conn = this.connection();
            PreparedStatement pstmt = conn.prepareStatement(sqlAvg);
            for(int i = 0; i<average.size();i++)
            {
                sqlAvg = "Insert into Durchschnitt"+super.getType()+ "(Zeitpunkt, Durchschnitt) values (\""+datums.get(i).toString() + "\',"+average.get(i)+");";
            }
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
    public void DB_SELECT(){
        try {
            Connection conn = this.connection();
            Statement stat= conn.createStatement();
            ResultSet reSe=stat.executeQuery("select * from Daten"+super.getType());
            System.out.println();
            System.out.println("TagesEndPreis:");
            System.out.println(" Zeitpunkt | TagesEndPreis");
            while(reSe.next()) {
                String zeitpunkt=reSe.getString("Zeitpunkt");
                String wert=reSe.getString("TagesEndPreis");
                System.out.println(zeitpunkt+" | "+wert);
            }
            reSe=stat.executeQuery("select * from Durchschnitt"+super.getType());
            System.out.println();
            System.out.println("200erDurchschnitt:");
            System.out.println(" Zeitpunkt | Durchschnitt");
            while(reSe.next()) {
                String zeitpunkt=reSe.getString("Zeitpunkt");
                String wert=reSe.getString("Durchschnitt");
                System.out.println(zeitpunkt+" | "+wert);
            }
        }catch(SQLException ex){
            System.out.println(ex.getMessage());
        }
    }

    public void MinMax(){
        String sqlMax = "select max(TagesEndPreis) from Daten" + super.getType() + ";";
        String sqlMin = "select min(TagesEndPreis) from Daten" + super.getType() + ";";
        try{
            Connection conn = this.connection();
            Statement stmt = conn.createStatement();
            ResultSet rsmax = stmt.executeQuery(sqlMax);
            while(rsmax.next())
            {
                max = rsmax.getDouble(1);
            }
            ResultSet rsmin = stmt.executeQuery(sqlMin);
            while(rsmin.next())
            {
                min = rsmin.getDouble(1);
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());

        }
    }

}
