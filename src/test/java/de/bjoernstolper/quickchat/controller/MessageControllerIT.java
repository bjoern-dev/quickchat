package de.bjoernstolper.quickchat.controller;

import de.bjoernstolper.quickchat.model.Message;
import de.bjoernstolper.quickchat.service.MessageService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebFluxTest
@Log4j2
public class MessageControllerIT {

    private final WebTestClient client;

    @MockBean
    private MessageService messageService;

    public MessageControllerIT(@Autowired WebTestClient client) {
        this.client = client;
    }

    @Test
    public void successfullSaveInRepositoryShouldYieldsStatus201AndCreatedHeader() {
        //given
        Message messageToBeSaved = new Message(new ObjectId(), "Max", "Hallo Welt");
        given(messageService.create(any(Message.class))).willReturn(Mono.just(messageToBeSaved));

        client
            .post()
            .uri("/messages/")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(messageToBeSaved), Message.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectHeader().exists("Location");
    }

    @Test
    public void failedSaveInRepositoryShouldYieldStatus409() {
        //given
        Message messageToBeSaved = new Message(new ObjectId(), "Max", "Hallo Welt");
        given(messageService.create(any(Message.class))).willReturn(Mono.empty());

        client
            .post()
            .uri("/messages/")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(messageToBeSaved), Message.class)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody().isEmpty();
    }

    @Test
    public void messageIdProvidedByClientShouldBeStripped() {
        //given
        Message messageToBeSaved = new Message(new ObjectId("507f191e810c19729de860ea"), "Max", "Hallo Welt");
        given(messageService.create(any(Message.class))).willReturn(Mono.just(messageToBeSaved));

        client
                .post()
                .uri("/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(messageToBeSaved), Message.class)
                .exchange()
                .expectStatus().isCreated();

        verify(messageService).create(argThat((Message messageParam) -> messageParam.getId() == null));
    }

    @Test
    public void shouldStreamAllEntries() {
        //given
        Message messageA = new Message(new ObjectId(), "Max", "Max sagt hi");
        Message messageB = new Message(new ObjectId(), "Tom", "Tom sagt auch hi");
        given(messageService.getAllAsHotStream()).willReturn(Flux.just(messageA, messageB));

        List<Message> messages = client.get().uri("/messages/sse-endpoint/")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(Message.class)
                .getResponseBody()
                .take(2)
                .collectList()
                .block();
        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals(messageA.getUsername(), messages.get(0).getUsername());
        assertEquals(messageB.getUsername(), messages.get(1).getUsername());
    }

}
