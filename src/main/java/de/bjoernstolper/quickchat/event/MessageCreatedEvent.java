package de.bjoernstolper.quickchat.event;

import de.bjoernstolper.quickchat.model.Message;
import org.springframework.context.ApplicationEvent;

public class MessageCreatedEvent extends ApplicationEvent {

    public MessageCreatedEvent(Message source) {
        super(source);
    }
}
