package de.bjoernstolper.quickchat.controller;

import de.bjoernstolper.quickchat.model.Message;
import de.bjoernstolper.quickchat.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/")
    public Mono<ResponseEntity<Message>> addMessage(@RequestBody Message message) {
        message.setId(null);    //prevent IDs from outside. TODO: Refactor to not use Message here. For Simplicity we use it for retrieval of username
        return messageService.create(message)
                .map(m -> ResponseEntity
                        .created(URI.create("/messages/" + m.getId()))
                        .body(m))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping("/sse-endpoint/")
    public Flux<ServerSentEvent<Message>> getMessageStream() {
        return messageService.getAll()
                .map(sequence -> ServerSentEvent.<Message>builder()
                        .id(String.valueOf(sequence.hashCode()))
                        .event("message")
                        .data(sequence)
                        .retry(Duration.ofMillis(10000))
                        .build());
    }

}
