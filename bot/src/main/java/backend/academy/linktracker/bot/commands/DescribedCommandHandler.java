package backend.academy.linktracker.bot.commands;

/**
 * Интерфейс для CommandHandler, который может возвращать команду и описание.
 * Используется для автоматической регистрации команд в Telegram (setMyCommands).
 */
public interface DescribedCommandHandler extends CommandHandler {

    /**
     * Возвращает команду, например "/start"
     */
    String getCommand();

    /**
     * Короткое описание команды (1-256 символов)
     */
    String getDescription();
}
