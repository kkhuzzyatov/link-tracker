package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UnknownCommandHandler implements CommandHandler {

    private final BotClient botClient;
    public final String MAIN_MESSAGE =
            "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд.";

    /**
     * Никогда не участвует в обычном поиске.
     * Вызывается диспетчером как fallback.
     */
    @Override
    public boolean supports(String command) {
        return false;
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        botClient.sendMessage(chatId, MAIN_MESSAGE);
    }
}
