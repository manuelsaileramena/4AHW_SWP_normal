create database Feiertage;
use Feiertage;

create table DatenAuslesen 
(
	startjahr int,
	endjahr int,
    Montag int,
    Dienstag int,
    Mittwoch int,
    Donnerstag int,
    Freitag int,
    Primary Key(endjahr)
);