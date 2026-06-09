package backend.academy.linktracker.bot.commands;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LinkStartTrackCommandHandlerTest {

    private ChatStateService chatStateService;
    private BotClient botClient;
    private LinkStartTrackCommandHandler handler;

    private final String LINK = "https://github.com/Tinkoff/career";

    @BeforeEach
    void setUp() {
        chatStateService = mock(ChatStateService.class);
        botClient = mock(BotClient.class);
        handler = new LinkStartTrackCommandHandler(chatStateService, botClient);
    }

    @Test
    void handle_shouldSendMainMessageAndSetWaitTagChatState() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, LINK);
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_TAG_TO_TRACK, LINK));
        verify(botClient).sendMessage(eq(chatId), eq(handler.MAIN_MESSAGE));
    }

    @Test
    void handle_shouldSendCancelMessageAndSetWaitCommandChatState_whenCommandIsCancel() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/cancel");
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(botClient).sendMessage(eq(chatId), eq(handler.CANCEL_MESSAGE));
    }
}
