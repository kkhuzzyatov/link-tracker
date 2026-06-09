package backend.academy.linktracker.bot.commands;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UntrackCommandHandlerTest {

    private BotClient botClient;
    private ChatStateService chatStateService;
    private UntrackCommandHandler handler;

    @BeforeEach
    void setUp() {
        botClient = mock(BotClient.class);
        chatStateService = mock(ChatStateService.class);
        handler = new UntrackCommandHandler(botClient, chatStateService);
    }

    @Test
    void handle_shouldSendMainMessage() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/untrack");

        handler.handle(update);

        verify(botClient).sendMessage(eq(update.message().chat().id()), eq(handler.MAIN_MESSAGE));
    }
}
