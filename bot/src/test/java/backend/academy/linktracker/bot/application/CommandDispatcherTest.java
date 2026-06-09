package backend.academy.linktracker.bot.application;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.commands.CommandHandler;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandDispatcherTest {

    private CommandHandler startHandler;
    private CommandHandler helpHandler;
    private CommandHandler unknownHandler;

    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        startHandler = mock(CommandHandler.class);
        helpHandler = mock(CommandHandler.class);
        unknownHandler = mock(CommandHandler.class);
        ChatStateService chatStateService = mock(ChatStateService.class);
        when(chatStateService.getStateByChatId(1L)).thenReturn(new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));

        dispatcher = new CommandDispatcher(
                Map.of(
                        "startCommandHandler",
                        startHandler,
                        "helpCommandHandler",
                        helpHandler,
                        "unknownCommandHandler",
                        unknownHandler),
                chatStateService);
    }

    private Update mockUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        return update;
    }

    // =========================
    // Проверка успешного выбора handler
    // =========================
    @Test
    void dispatch_shouldCallMatchingHandler() {
        Update update = mockUpdate("/start");

        when(startHandler.supports("/start")).thenReturn(true);
        when(helpHandler.supports("/start")).thenReturn(false);

        dispatcher.dispatch(update);

        verify(startHandler).handle(update);
        verify(helpHandler, never()).handle(any());
        verify(unknownHandler, never()).handle(any());
    }

    // =========================
    // Проверка fallback на unknownHandler
    // =========================
    @Test
    void dispatch_shouldCallUnknownHandler_whenNoMatch() {
        Update update = mockUpdate("/unknown");

        when(startHandler.supports(anyString())).thenReturn(false);
        when(helpHandler.supports(anyString())).thenReturn(false);

        dispatcher.dispatch(update);

        verify(unknownHandler).handle(update);
    }
}
