package backend.academy.linktracker.scrapper.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.link.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TgChatControllerTest {

    private MockMvc mockMvc;
    private ChatService chatService;
    private final Long validId = 123L;
    private final Long invalidId = -1L;

    @BeforeEach
    void setup() {
        chatService = mock(ChatService.class);
        TgChatController controller = new TgChatController(chatService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        reset(chatService);
    }

    @Test
    void shouldDeleteChatSuccessfully() throws Exception {
        when(chatService.isChatExist(validId)).thenReturn(true);

        mockMvc.perform(delete("/tg-chat/{id}", validId)).andExpect(status().isOk());

        verify(chatService).deleteChat(validId);
    }

    @Test
    void shouldReturnBadRequest_whenDeleteChatIdInvalid() throws Exception {
        mockMvc.perform(delete("/tg-chat/{id}", invalidId)).andExpect(status().isBadRequest());

        verifyNoInteractions(chatService);
    }

    @Test
    void shouldReturnNotFound_whenDeleteChatDoesNotExist() throws Exception {
        when(chatService.isChatExist(validId)).thenReturn(false);

        mockMvc.perform(delete("/tg-chat/{id}", validId)).andExpect(status().isNotFound());

        verify(chatService).isChatExist(validId);
        verify(chatService, never()).deleteChat(any());
    }

    @Test
    void shouldRegisterChatSuccessfully() throws Exception {
        when(chatService.isChatExist(validId)).thenReturn(false);

        mockMvc.perform(post("/tg-chat/{id}", validId)).andExpect(status().isOk());

        verify(chatService).registerChat(validId);
    }

    @Test
    void shouldReturnBadRequest_whenRegisterChatIdInvalid() throws Exception {
        mockMvc.perform(post("/tg-chat/{id}", invalidId)).andExpect(status().isBadRequest());

        verifyNoInteractions(chatService);
    }

    @Test
    void shouldReturnConflict_whenRegisterAlreadyExistingChat() throws Exception {
        when(chatService.isChatExist(validId)).thenReturn(true);

        mockMvc.perform(post("/tg-chat/{id}", validId)).andExpect(status().isConflict());

        verify(chatService).isChatExist(validId);
        verify(chatService, never()).registerChat(any());
    }
}
