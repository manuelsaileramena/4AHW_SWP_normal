Börsendaten:

Ziel der Aufgabe:
Bei diesen 5 Aktien: TSLA, IBM, ATVI, PTC, AMZN 
sind alle 3 Strategien: 200er, 200er mit 3%, buy and hold
anzuwenden.
Bei der 200er-Strategie soll erst dann gekauft/verkauft werden wenn der Kurs über/unter dem Schnittpunkt mit der 200er-Linie liegt. Bei der 200er-Strategie mit 3% soll erst dann gekauft/verkauft werden wenn der Kurs 3% über/unter dem Schnittpunkt mit der 200er-Linie liegt. Und bei der buy and hold Strategie soll am Startdatum eingekauft werden und erst heute verkauft werden. 
In der Console soll erkendlich sein welche diser 3 Strategien sich am besten zum Investieren eignet.
Diese Ivestition fängt nämlich an einem Zeitpunkt in der Vergangenheit an und hört am gestrigen Tag auf, da für heute noch keine close Werte erstellt wurden. 
Es wird ebenfalls mit einem bestimmten Startkapital investiert um zu sehen ob man Gewinn oder Verlust gemacht hätte. 

Eventuell benötigende Informationen:
Die gesamten Aktiendaten werden der Seite https://www.alphavantage.co/ entnommen.
Mithilfe dieser URL: "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="+ aktie + "&outputsize=full&apikey=AR87OJ64MUWOW1H1" erhalte ich alle für mich relevanten Daten. Mit denen ich anschließend auch die oben erwähnten Strategien programmiere. 
Bei den oben angesprochenen Strategien werden nicht die Rohwerte aus der Datenbank verwendet, sondern Werte, welche durch eine Splitcorrection korrigiert wurden. Welche von mir selbst programmiert wurde. Ebenfalls wurde vor den 3 Strategien ein Durschnitt berechnet auf welchen die 200er-Strategie und die 200er-Strategie mit 3% aufgebaut sind. 
