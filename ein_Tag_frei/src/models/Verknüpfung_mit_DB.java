package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class Verknüpfung_mit_DB extends Zaehler{


    public void VerbindungDB(String host, String database, String user, String passwort) {
        Scanner scan = new Scanner(System.in);
        try {

            //Load
            Class.forName("com.mysql.cj.jdbc.Driver");
            //Connect
            Connection connection = DriverManager.getConnection("jdbc:mysql://"+host+"/"+database+"?user="+user+"&password="+passwort+"&serverTimezone=UTC");
            //Statement
            Statement myStat = connection.createStatement();

            myStat.execute("drop database Feiertage");
            myStat.execute("create database if not exists Feiertage");
            myStat.execute("use Feiertage");
            myStat.execute("create Table DatenAuslesen(startjahr int, endjahr int, Montag int, Dienstag int, Mittwoch int, Donerstag int, Freitag int, Primary Key(endjahr))");
            myStat.execute("Insert Into Feiertage(startjahr, endjahr, Montag, Dienstag, Mittwoch, Donnerstag, Freitag) values("+2020+" , "+endjahr+", "+montag+" "+dienstag+", "+mittwoch+", "+donnerstag+", "+freitag+")");

            //Execute SQL Query
            ResultSet reSe = myStat.executeQuery("select * from Feiertage");

            //Process the result set
            while (reSe.next()) {
                System.out.println("JahrAnfang:"+2020+", JahrEnde:"+reSe.getString("endjahr")+", Montag:"+reSe.getString("Montag")+
                        ", Dienstag:"+reSe.getString("Dienstag")+", Mittwoch:"+reSe.getString("Mittwoch")+
                        ", Donnerstag:"+reSe.getString("Donnerstag")+", Freitag:"+reSe.getString("Freitag"));
            }

            scan.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Verbindung fehlgeschlagen! ");
        }
    }
}
