package backend.academy.linktracker.bot.commands;

import com.pengrad.telegrambot.model.Update;

/**
 * Интерфейс обработчика команд Telegram бота.
 * Каждый CommandHandler отвечает за одну или несколько команд.
 */
public interface CommandHandler {

    /**
     * Проверяет, поддерживает ли данный обработчик указанную команду.
     *
     * @param command текст команды (например, "/start")
     * @return true, если обработчик поддерживает команду
     */
    boolean supports(String command);

    /**
     * Обрабатывает команду из Update.
     * Все исключения должны обрабатываться внутри метода.
     * Для отправки сообщений использовать BotClient.
     *
     * @param update объект Update из Telegram API
     */
    void handle(Update update);
}
