package de.bjoernstolper.quickchat.service;

import de.bjoernstolper.quickchat.model.Message;
import de.bjoernstolper.quickchat.repository.MessageRepository;
import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class MessageService {

    private final MessageRepository messageRepository;

    MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Flux<Message> getAllAsHotStream() {
        return this.messageRepository.findWithTailableCursorBy();
    }

    public Flux<Message> getAll() {
        return this.messageRepository.findAll();
    }

    public Mono<Message> create(Message message) {
        return this.messageRepository
                .save(message);
    }
}
