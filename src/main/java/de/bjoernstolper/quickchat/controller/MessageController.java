package de.bjoernstolper.quickchat.controller;

import de.bjoernstolper.quickchat.model.Message;
import de.bjoernstolper.quickchat.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public Mono<ResponseEntity<Message>> addMessage(@RequestBody Message message) {
        return messageService.create(message)
                .map(m -> ResponseEntity
                        .created(URI.create("/messages/" + m.getId()))
                        .body(m))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @GetMapping(path = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> getMessageStream() {
        return messageService.getAll();
    }

}
