package backend.academy.linktracker.bot.client;

import java.util.List;

public interface BotCommandsClient {
    /**
     * Устанавливает список команд бота
     */
    void setMyCommands(List<BotCommandDto> commands);
}
