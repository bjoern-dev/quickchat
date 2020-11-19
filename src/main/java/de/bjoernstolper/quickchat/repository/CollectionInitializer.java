package de.bjoernstolper.quickchat.repository;

import de.bjoernstolper.quickchat.model.Message;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Log4j2
@Component
public class CollectionInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final MessageRepository messageRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CollectionInitializer(MessageRepository messageRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.messageRepository = messageRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        reactiveMongoTemplate.dropCollection(Message.class).then(reactiveMongoTemplate.createCollection(
                Message.class, CollectionOptions.empty().capped().size(2048))).block();

        messageRepository
                .saveAll(
                    Flux
                        .just("Max", "Tom", "John", "Peter")
                        .map(name -> new Message(name, "Hallo ich bin " + name))
                )
                .thenMany(messageRepository.findAll())
                .subscribe(message -> log.info("saving " + message.toString()));
    }
}
