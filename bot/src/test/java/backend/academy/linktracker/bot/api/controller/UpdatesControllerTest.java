package backend.academy.linktracker.bot.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import backend.academy.linktracker.bot.api.generated.controller.model.LinkUpdate;
import backend.academy.linktracker.bot.client.BotClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UpdatesControllerTest {

    private MockMvc mockMvc;
    private BotClient botClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        botClient = mock(BotClient.class);
        objectMapper = new ObjectMapper();
        UpdatesController controller = new UpdatesController(botClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnOkAndSendMessages_whenValidRequest() throws Exception {
        LinkUpdate request1 =
                new LinkUpdate(1L, URI.create("https://github.com/Tinkoff/career"), "Some update", List.of(123L, 456L));
        LinkUpdate request2 = new LinkUpdate(
                2L, URI.create("https://stackoverflow.com/questions/1"), "Another update", List.of(789L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request1, request2))))
                .andExpect(status().isOk());

        verify(botClient, times(1)).sendMessage(123L, "Some update");
        verify(botClient, times(1)).sendMessage(456L, "Some update");
        verify(botClient, times(1)).sendMessage(789L, "Another update");
    }

    @Test
    void shouldReturnBadRequest_whenRequiredParameterIsNull() throws Exception {
        LinkUpdate request = new LinkUpdate(1L, URI.create("https://github.com/Tinkoff/career"), null, List.of(123L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(botClient);
    }
}
