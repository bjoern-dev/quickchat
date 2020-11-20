package de.bjoernstolper.quickchat.controller;

import de.bjoernstolper.quickchat.model.ClientSentMessage;
import de.bjoernstolper.quickchat.model.Message;
import de.bjoernstolper.quickchat.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/")
    public Mono<ResponseEntity<Message>> addMessage(@RequestBody ClientSentMessage clientSentMessage) {
        return messageService.create(clientSentMessage.toMessage())
                .map(m -> ResponseEntity
                        .created(URI.create("/messages/" + m.getId()))
                        .body(m))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(path="/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> getMessageColdStream() {
        return messageService.getAll();
    }

    @GetMapping("/sse-endpoint/")
    public Flux<ServerSentEvent<Message>> getMessageHotStream(@RequestHeader(name = "Last-Event-ID", required = false) String lastEventId) {
        Flux<Message> allMessages = messageService.getAllAsHotStream();

        //in case client had disconnect, continue at last given ID
        if (lastEventId != null) {
            allMessages = allMessages.skipUntil(message -> String.valueOf(message.hashCode()).equals(lastEventId)).skip(1);
        }

        return allMessages
                .map(sequence -> ServerSentEvent.<Message>builder()
                        .id(String.valueOf(sequence.hashCode()))
                        .data(sequence)
                        .retry(Duration.ofMillis(10000))
                        .build());
    }

    @GetMapping("/sse-endpoint/with-heartbeat/")
    public Flux<ServerSentEvent<Message>> getMessageStreamWithHeartbeat() {
        return Flux.merge(heartbeatStream(), getMessageHotStream(null));
    }

    private Flux<ServerSentEvent<Message>> heartbeatStream() {
        return Flux.interval(Duration.ofSeconds(2))
                .map(sequence -> ServerSentEvent.<Message>builder()
                        .event("heartbeat")
                        .comment("heartbeat")
                        .data(null)
                        .build());
    }

}
