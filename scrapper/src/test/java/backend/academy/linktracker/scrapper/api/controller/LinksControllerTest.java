package backend.academy.linktracker.scrapper.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import backend.academy.linktracker.scrapper.api.generated.controller.model.AddLinkRequest;
import backend.academy.linktracker.scrapper.api.generated.controller.model.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.service.ChatService;
import backend.academy.linktracker.scrapper.link.service.LinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class LinksControllerTest {

    private MockMvc mockMvc;
    private ChatService chatService;
    private LinkService linkService;
    private ObjectMapper objectMapper;
    private final Long tgChatId = 123L;

    @BeforeEach
    void setup() {
        chatService = mock(ChatService.class);
        linkService = mock(LinkService.class);
        objectMapper = new ObjectMapper();
        LinksController controller = new LinksController(chatService, linkService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        reset(chatService, linkService);
    }

    @Test
    void shouldReturnOkWithLinks_whenChatExists() throws Exception {
        when(chatService.isChatExist(tgChatId)).thenReturn(true);
        when(linkService.getListLinksByChatId(tgChatId, 1000, 0))
                .thenReturn(List.of(new Link(
                        tgChatId, "https://github.com/Tinkoff/career", Set.of("tag1"), Set.of(tgChatId), null, null)));
        when(linkService.getListLinksByChatId(tgChatId, 1000, 1000)).thenReturn(List.of());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", tgChatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value("https://github.com/Tinkoff/career"));
    }

    @Test
    void shouldReturnOkWithLinksByTag_whenChatExists() throws Exception {
        String tag = "tag1";
        when(chatService.isChatExist(tgChatId)).thenReturn(true);
        when(linkService.getListLinksByChatIdAndTag(tgChatId, tag, 1000, 0))
                .thenReturn(List.of(new Link(
                        tgChatId, "https://github.com/Tinkoff/career", Set.of("tag1"), Set.of(tgChatId), null, null)));

        mockMvc.perform(get("/links").header("Tg-Chat-Id", tgChatId).param("tag", tag))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value("https://github.com/Tinkoff/career"));
    }

    @Test
    void shouldReturnBadRequest_whenTgChatIdHeaderMissing() throws Exception {
        mockMvc.perform(get("/links")).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFound_whenChatDoesNotExist() throws Exception {
        when(chatService.isChatExist(tgChatId)).thenReturn(false);
        mockMvc.perform(get("/links").header("Tg-Chat-Id", tgChatId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldAddLinkSuccessfully() throws Exception {
        AddLinkRequest request =
                new AddLinkRequest(new URI("https://github.com/Tinkoff/career"), List.of("tag1"), null);
        when(chatService.isChatExist(tgChatId)).thenReturn(true);
        when(linkService.isLinkExist(request.link().toString())).thenReturn(false);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", tgChatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://github.com/Tinkoff/career"));

        verify(linkService).trackLink(any());
    }

    @Test
    void shouldReturnBadRequest_whenAddLinkRequestInvalid() throws Exception {
        AddLinkRequest request = new AddLinkRequest(null, List.of(), null);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", tgChatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(linkService);
    }

    @Test
    void shouldRemoveLinkSuccessfully() throws Exception {
        RemoveLinkRequest request = new RemoveLinkRequest(new URI("https://github.com/Tinkoff/career"));
        when(chatService.isChatExist(tgChatId)).thenReturn(true);
        when(linkService.isLinkExist(request.link().toString())).thenReturn(true);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", tgChatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(linkService).untrackLink(tgChatId, "https://github.com/Tinkoff/career");
    }

    @Test
    void shouldReturnNotFound_whenRemoveLinkDoesNotExist() throws Exception {
        RemoveLinkRequest request = new RemoveLinkRequest(new URI("https://github.com/Tinkoff/career"));
        when(chatService.isChatExist(tgChatId)).thenReturn(false);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", tgChatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequest_whenRemoveLinkHeaderMissing() throws Exception {
        RemoveLinkRequest request = new RemoveLinkRequest(new URI("https://github.com/Tinkoff/career"));

        mockMvc.perform(delete("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(linkService);
    }
}
