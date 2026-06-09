package backend.academy.linktracker.bot.chat_state.repository;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatStateRepositoryInMemoryImplTest {

    private ChatStateRepositoryInMemoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ChatStateRepositoryInMemoryImpl();
    }

    @Test
    void shouldReturnDefaultStateWhenChatIdNotFound() {
        Long chatId = 1L;

        ChatState result = repository.findStateByChatId(chatId);

        assertNotNull(result);
        assertEquals(ChatState.State.WAIT_FOR_COMMAND, result.getCurrentState());
    }

    @Test
    void shouldUpdateAndReturnState() {
        Long chatId = 2L;
        ChatState newState = new ChatState(ChatState.State.WAIT_FOR_COMMAND, "test");

        repository.update(chatId, newState);
        ChatState result = repository.findStateByChatId(chatId);

        assertEquals(newState, result);
    }

    @Test
    void shouldOverwriteExistingState() {
        Long chatId = 3L;

        ChatState firstState = new ChatState(ChatState.State.WAIT_FOR_COMMAND, "first");
        ChatState secondState = new ChatState(ChatState.State.WAIT_FOR_COMMAND, "second");

        repository.update(chatId, firstState);
        repository.update(chatId, secondState);

        ChatState result = repository.findStateByChatId(chatId);

        assertEquals(secondState, result);
    }
}
