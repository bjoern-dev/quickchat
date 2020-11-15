package de.bjoernstolper.quickchat.service;

import de.bjoernstolper.quickchat.model.Message;
import de.bjoernstolper.quickchat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

@Log4j2
@DataMongoTest
@Import(MessageService.class)
public class MessageServiceIT {

    private final MessageService service;
    private final MessageRepository repository;

    public MessageServiceIT(@Autowired MessageService service, @Autowired MessageRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @BeforeEach
    public void cleanDB() {
        repository.deleteAll().then().block();
    }

    @Test
    @Disabled("Some weird concurrency behaviour with test class running indefinately if more than one test is active")
    public void getAll() {
        Flux<Message> savedMessages = repository
                .saveAll(Flux.just( idlessMessage("Max", "Hallo zusammen"),
                        idlessMessage("Donald", "Hi everyone"),
                        idlessMessage("Pierre", "Salut")));
        Flux<Message> emittedMessages = service.getAll().thenMany(savedMessages);
        Predicate<Message> matchPredicate = message -> savedMessages.any(savedMessage -> savedMessage.equals(message)).block();

        StepVerifier
                .create(emittedMessages)
                .expectNextMatches(matchPredicate)
                .expectNextMatches(matchPredicate)
                .expectNextMatches(matchPredicate)
                .verifyComplete();
    }

    @Test
    public void createOneEntryAndCheckId() {
        Mono<Message> messageMono = service.create(idlessMessage("Tom", "Hi Jerry"));
        StepVerifier
                .create(messageMono)
                .expectNextMatches(savedMessage -> StringUtils.hasText(savedMessage.getId().toString()))
                .verifyComplete();
    }

    private Message idlessMessage(String username, String text) {
        return new Message(null, username, text);
    }
}
