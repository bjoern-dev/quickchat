# Quickchat

Ein Beispielprojekt für die Nutzung von Reactive Hot Streams mit einem reactive Stack (Spring WebFlux, Spring Data Reactive, Server Sent Events (SSE), JavaScript EventSource).

Beispielhaft ist ein Endpoint mit einem Cold Stream (Flux) implementiert, sowie ein weiterer Endpoint als Hot Stream, der neue Datenbankeinträge direkt als SSE an verbundene Clients ausgibt. Dies ist anhand einer Capped Collection in MongoDB implementiert (@Tailable Annotation).

Ein weiterer Beispiel-Endpoint mixt Heartbeat-Events (als Kommentare) in den Cold Stream, um die standardmäßigen Disconnects (~ alle 3 Sekunden) des Browsers bei Coldstreams zu vermeiden.

Hat der Browser einen Disconnect, findet das weitere Streaming anhand der letzten bekannten ID im Request-Header ab dem Punkt statt, wo abgebrochen wurde - gemäß der Server Sent Events Specs (https://html.spec.whatwg.org/multipage/server-sent-events.html#the-eventsource-interface).
