package backend.academy.linktracker.bot.commands;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.api.client.TgChatClient;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StartCommandHandlerTest {

    private BotClient botClient;
    private StartCommandHandler handler;
    private TgChatClient tgChatClient;

    @BeforeEach
    void setUp() {
        botClient = mock(BotClient.class);
        tgChatClient = mock(TgChatClient.class);
        handler = new StartCommandHandler(botClient, tgChatClient);
    }

    @Test
    void handle_shouldSendMainMessage_whenStatusIsOk() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/start");
        when(tgChatClient.registerChat(anyLong()))
                .thenReturn(new ExternalCallResult(null, ExternalCallResult.Status.OK));
        handler.handle(update);
        verify(botClient).sendMessage(eq(chatId), eq(handler.MAIN_MESSAGE));
        verify(tgChatClient).registerChat(anyLong());
    }

    @Test
    void handle_shouldSendConflictMessage_whenStatusIsConflict() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/start");
        when(tgChatClient.registerChat(anyLong()))
                .thenReturn(new ExternalCallResult(null, ExternalCallResult.Status.CONFLICT));
        handler.handle(update);
        verify(botClient).sendMessage(eq(chatId), eq(handler.CONFLICT_MESSAGE));
        verify(tgChatClient).registerChat(anyLong());
    }

    @Test
    void handle_shouldSendErrorMessage_whenStatusIsAnother() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/start");
        when(tgChatClient.registerChat(anyLong()))
                .thenReturn(new ExternalCallResult(null, ExternalCallResult.Status.BAD_REQUEST));
        handler.handle(update);
        verify(botClient).sendMessage(eq(chatId), eq(handler.ERROR_MESSAGE));
        verify(tgChatClient).registerChat(anyLong());
    }
}
