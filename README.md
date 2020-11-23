# Quickchat

#### Demo
Chat ist deployt unter https://quickchat-reactive.herokuapp.com/ 

Das System ist bei Inaktivität im Sleep-Modus, es dauert dann beim ersten Aufruf ca. 20 Sekunden, bis es reagiert

___

#### Aufbaubeschreibung
Ein Beispielprojekt für die Nutzung von Reactive Hot Streams mit einem reactive Stack (Spring WebFlux, Spring Data Reactive, Server Sent Events (SSE), JavaScript EventSource).

Beispielhaft ist ein Endpoint mit einem Cold Stream (Flux) implementiert, sowie ein weiterer Endpoint als Hot Stream, der neue Datenbankeinträge direkt als SSE an verbundene Clients ausgibt. Dies ist anhand einer Capped Collection in MongoDB implementiert (_@Tailable_ Annotation).

Ein weiterer Beispiel-Endpoint mixt Heartbeat-Events (als Kommentare) in den Cold Stream, um die standardmäßigen Disconnects (~ alle 3 Sekunden) des Browsers bei Coldstreams zu vermeiden.

Hat der Browser einen Disconnect, findet das weitere Streaming anhand der letzten bekannten ID im Request-Header ab dem Punkt statt, wo abgebrochen wurde - gemäß der Server Sent Events Specs (https://html.spec.whatwg.org/multipage/server-sent-events.html#the-eventsource-interface).

___

#### Tests
Getestet wird anhand von Sliced Integration Tests. 

Mit _@WebFluxTest_ wird die Controller-Schicht in einem Slice hochgefahren und mit einem _WebTestClient_ getestet.
 
Mit _@DataMongoTest_ wird die Datenbankschicht als Slice hochgefahren und das Zusammenspiel von Application-Service und Repository in einer embedded MongoDB getestet.

___

#### Frontend
Das "Frontend" besteht aus einer einzigen statischen HTML-Seite und hinzugeladenem CSS und JS (vanilla ES6).

Zum Verbinden mit den Streaming-Endpoints wird die EventSource-API benutzt. Diese hält im Falle der Hot-Streams die Verbindung zum REST-Server offen und erhält direkt neue Chat-Nachrichten sobald diese eintreffen.
Da SSE im Gegensatz zu Websockets nur unidirektional sind (dafür um einiges schlanker), wird für das Versenden von Chat-Nachrichten ein normaler POST-Request über die fetch-API verwendet. 

Ebenfalls per fetch ist eine Random-username API angebunden.  
