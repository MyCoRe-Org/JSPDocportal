SQLTree Plug-in for Eclipse
===========================

Leistungsumfang
---------------
Das SQLTree-Plugin dient zur Demonstration von Algorithmen, die f�r die Realisierung der Speicherung von B�umen in einer Tabellenstruktur ben�tigt werden. Das Konzept wurde aus Artikeln "SQL for Smarties" von Joe Celko aus DBMSonline �bernommen.
(siehe http://www.dbmsmag.com/9603d06.html, http://www.dbmsmag.com/9604d06.html, http://www.dbmsmag.com/9605d06.html http://www.dbmsmag.com/9606d06.html)
Implementiert wurden, das Erzeugen von B�umen, das Einf�gen von Knoten, das Verschieben von Teilb�umen und das L�schen von Teilb�umen.
Die B�ume werden in einer HSQL-DB im Speicher abgelegt, die beim Beenden des Programms nicht gesichert wird.

Installation
------------
Das Tool wird als Plug-in in Eclipse integriert. Zur Installation ist lediglich der Inhalt der Zip-Datei in das ../eclipse/plugins Verzeichnis zu entpacken.

Start
-----
Das Tool wurde als View in die Eclipse-Oberfl�che integriert.
Er wird �ber das Men�: Window -> Show View -> Other... ge�ffnet.
Im sich �ffnenden Dialog kann er unter SQLTree -> SQLTree View gefunden werden.

Eignabe-Syntax f�r B�ume
------------------------
F�r die effiziente Eingabe wurde die LISP-Notation gew�hlt
tree = subtree
subtree = (node (subtree)...(subtree)) | (node)
node = <string>

z.B: "(World(Europe(Germany)(UK))(Asia))" ergibt:
              World
         +------+---------+  	
       Europe            Asia
   +------+---+
Germany       UK		


Algorithmen
-----------
Die Implementation der Algorithmen erfolgt in der Klasse sqltree.SQLTreeManager


