package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.api.client.TgChatClient;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DeleteChatCommandHandler implements DescribedCommandHandler {

    private final BotClient botClient;
    private final TgChatClient tgChatClient;
    public final String MAIN_MESSAGE = "Чат и все связанные ссылки удалены.";
    public final String CONFLICT_MESSAGE = "Ваш чат не зарегистрирован, поэтому его нельзя удалить.";
    public final String ERROR_MESSAGE = "Что-то пошло не так. Попробуйте еще раз.";

    @Override
    public boolean supports(String command) {
        return "/delete_chat".equals(command);
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        ExternalCallResult<Void> response = tgChatClient.deleteChat(chatId);
        switch (response.getStatus()) {
            case OK:
                botClient.sendMessage(chatId, MAIN_MESSAGE);
                break;
            case CONFLICT:
                botClient.sendMessage(chatId, CONFLICT_MESSAGE);
                break;
            default:
                botClient.sendMessage(chatId, ERROR_MESSAGE);
        }
    }

    @Override
    public String getCommand() {
        return "/delete_chat";
    }

    @Override
    public String getDescription() {
        return "Удалить чат и все связанные с ним отслеживаемые ссылки";
    }
}
