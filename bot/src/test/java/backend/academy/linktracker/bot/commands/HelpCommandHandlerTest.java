package backend.academy.linktracker.bot.commands;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HelpCommandHandlerTest {

    private BotClient botClient;
    private HelpCommandHandler handler;
    private List<DescribedCommandHandler> handlers;

    private final String EXPECTED_MESSAGE = """
    Доступные команды:

    /start - Начать работу с ботом

    Просто отправьте команду 🙂
    """;

    @BeforeEach
    void setUp() {
        botClient = mock(BotClient.class);
        StartCommandHandler startCommandHandler = mock(StartCommandHandler.class);
        when(startCommandHandler.getCommand()).thenReturn("/start");
        when(startCommandHandler.getDescription()).thenReturn("Начать работу с ботом");
        handlers = List.of(startCommandHandler);
        handler = new HelpCommandHandler(botClient, handlers);
    }

    @Test
    void handle_shouldSendHelpMessage() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/start");
        handler.handle(update);
        verify(botClient).sendMessage(eq(chatId), eq(EXPECTED_MESSAGE));
    }
}
