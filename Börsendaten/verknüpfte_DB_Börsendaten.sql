Drop database Börsendaten;
Create database if not exists Börsendaten;
use Börsendaten;

Create table Daten
(
	Zeitpunkt varchar(50),
    TagesEndPreis double,
    primary key(Zeitpunkt)
);
