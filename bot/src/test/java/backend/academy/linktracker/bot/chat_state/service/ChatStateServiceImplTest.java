package backend.academy.linktracker.bot.chat_state.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.repository.ChatStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ChatStateServiceImplTest {

    private ChatStateRepository chatStateRepository;
    private ChatStateServiceImpl chatStateService;

    @BeforeEach
    void setUp() {
        chatStateRepository = mock(ChatStateRepository.class);
        chatStateService = new ChatStateServiceImpl(chatStateRepository);
    }

    @Test
    void shouldReturnStateFromRepository() {
        Long chatId = 1L;
        ChatState expectedState = new ChatState(ChatState.State.WAIT_FOR_COMMAND, "data");

        when(chatStateRepository.findStateByChatId(chatId)).thenReturn(expectedState);

        ChatState result = chatStateService.getStateByChatId(chatId);

        assertEquals(expectedState, result);
        verify(chatStateRepository).findStateByChatId(chatId);
    }

    @Test
    void shouldCallRepositoryUpdate() {
        Long chatId = 2L;
        ChatState chatState = new ChatState(ChatState.State.WAIT_FOR_COMMAND, "test");

        chatStateService.update(chatId, chatState);

        verify(chatStateRepository).update(chatId, chatState);
    }

    @Test
    void shouldPassCorrectArgumentsToRepositoryOnUpdate() {
        Long chatId = 3L;
        ChatState chatState = new ChatState(ChatState.State.WAIT_FOR_COMMAND, "payload");

        chatStateService.update(chatId, chatState);

        ArgumentCaptor<Long> chatIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ChatState> stateCaptor = ArgumentCaptor.forClass(ChatState.class);

        verify(chatStateRepository).update(chatIdCaptor.capture(), stateCaptor.capture());

        assertEquals(chatId, chatIdCaptor.getValue());
        assertEquals(chatState, stateCaptor.getValue());
    }
}
