package backend.academy.linktracker.bot.commands;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnknownCommandHandlerTest {

    private BotClient botClient;
    private UnknownCommandHandler handler;

    @BeforeEach
    void setUp() {
        botClient = mock(BotClient.class);
        handler = new UnknownCommandHandler(botClient);
    }

    @Test
    void handle_shouldSendMainMessage() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "unsupported_command");
        handler.handle(update);
        verify(botClient).sendMessage(eq(chatId), eq(handler.MAIN_MESSAGE));
    }
}
