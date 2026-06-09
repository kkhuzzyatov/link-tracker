package backend.academy.linktracker.bot.commands;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.api.client.LinksClient;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LinkStopTrackCommandHandlerTest {

    private ChatStateService chatStateService;
    private LinksClient linksClient;
    private BotClient botClient;

    private LinkStopTrackCommandHandler handler;

    private final String LINK = "https://github.com/Tinkoff/career";

    @BeforeEach
    void setUp() {
        chatStateService = mock(ChatStateService.class);
        linksClient = mock(LinksClient.class);
        botClient = mock(BotClient.class);
        handler = new LinkStopTrackCommandHandler(chatStateService, linksClient, botClient);
    }

    @Test
    void handle_shouldSendCancelMessageAndSetWaitCommandChatState_whenCommandIsCancel() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/cancel");
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(botClient).sendMessage(eq(chatId), eq(handler.CANCEL_MESSAGE));
    }

    @Test
    void handle_shouldSendMainMessageAndSetWaitCommandChatState_whenStatusIsOk() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, LINK);
        when(linksClient.removeLink(chatId, LINK))
                .thenReturn(new ExternalCallResult(null, ExternalCallResult.Status.OK));
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(linksClient).removeLink(chatId, LINK);
        verify(botClient).sendMessage(eq(chatId), eq(String.format(handler.MAIN_MESSAGE, LINK)));
    }

    @Test
    void handle_shouldSendChatIsNotRegisteredMessageAndSetWaitCommandChatState_whenStatusIsNotFound() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, LINK);
        when(linksClient.removeLink(chatId, LINK))
                .thenReturn(new ExternalCallResult(null, ExternalCallResult.Status.NOT_FOUND));
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(linksClient).removeLink(chatId, LINK);
        verify(botClient).sendMessage(eq(chatId), eq(handler.CHAT_IS_NOT_REGISTERED_MESSAGE));
    }

    @Test
    void handle_shouldSendErrorMessageAndSetWaitCommandChatState_whenStatusIsAnother() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, LINK);
        when(linksClient.removeLink(chatId, LINK))
                .thenReturn(new ExternalCallResult(null, ExternalCallResult.Status.BAD_REQUEST));
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(linksClient).removeLink(chatId, LINK);
        verify(botClient).sendMessage(eq(chatId), eq(handler.ERROR_MESSAGE));
    }
}
