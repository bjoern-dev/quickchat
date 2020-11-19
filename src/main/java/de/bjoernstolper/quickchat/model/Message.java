package de.bjoernstolper.quickchat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Document
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    private ObjectId id;
    @NonNull
    private String username;
    @NonNull
    private String text;

    public Message(ObjectId id,
                   @JsonProperty(required = true) String username,
                   @JsonProperty(required = true) String text) {
        this.id = id;
        this.username = username;
        this.text = text;
    }
}
