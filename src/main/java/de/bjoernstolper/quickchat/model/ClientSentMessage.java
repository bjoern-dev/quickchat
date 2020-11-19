package de.bjoernstolper.quickchat.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ClientSentMessage {

    @NonNull
    private final String username;
    @NonNull
    private final String text;

    public Message toMessage() {
        return new Message(username, text);
    }
}
