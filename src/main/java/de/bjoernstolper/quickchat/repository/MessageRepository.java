package de.bjoernstolper.quickchat.repository;

import de.bjoernstolper.quickchat.model.Message;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MessageRepository extends ReactiveMongoRepository<Message, String> {
}
