Börsendaten:

Ziel der Aufgabe:
Die Finanzen der Aktie über ein JavaFX Diagramm darzstellen, damit auf einem Blick klar ist ob man Gewinn oder Verlust mit der Aktie gemacht hat.

Benötigte Libaries für dieses Projekt: 
java-json.jar | commons-io-2.7.jar | javafx.base.jar | mysql-connector-java-8.0.jar
   
Ausführung:

Der Benutzer kann am Anfang auswählen welche Aktie er/sie haben will und wie viele Tage der Graph beinhalten soll.
Die Werte werden zu Beginn aus der URL gelesen und in den entsprechenden Arraylisten abgespeichert. Anschließend verbinde ich mich mit der dazugehörigen Datenbank in diesem Fall heißt sie api. Ich habe ebenfalls eine Klasse connection() geschrieben, damit ich nicht jedes mal diese Zeilen schreiben muss zum Verbinden. Danach habe ich einen neuen Table erstellt und diesen auch befüllt. Nach alledem habe ich den Durchschnitt der Werte für die Aktie berechnet und diesen in die Datenbank eingefügt. Mit der Methode MinUndMax() habe ich den größten Wert und den kleinsten Wert der Aktie heraus gelesen. Als vorletztes habe ich so viele Werte wie der Benutzer ausgewählt hat in der Datenbank ausgegeben. Und zum krönenden Abschluss habe ich die Werte in einen Graph mittels JavaFX gezeichnet, wobei man hier einstellen kann ob alle Werte ausgegeben werden sollen oder nur zehn wie man im Code erkennen kann.

Graph: 

