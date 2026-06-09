package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.api.client.LinksClient;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.api.generated.client.model.LinkResponse;
import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LinkStopTrackCommandHandler implements CommandHandler {

    private final ChatStateService chatStateService;
    private final LinksClient linksClient;
    private final BotClient botClient;
    public final String CANCEL_MESSAGE = "Процесс удаления ссылки из списка отслеживаемых прерван.";
    public final String MAIN_MESSAGE = "Прекращено отслеживание ссылки %s.";
    public final String CHAT_IS_NOT_REGISTERED_MESSAGE = """
    Прекращение отслеживания ссылки невозможно, так как ваш чат не зарегистрирован в системе или данная ссылка не отслеживается.
    Нажмите /start, чтобы зарегистрировать чат.
    Нажмите /list, чтобы увидеть список отслеживаемых вами ссылок.
    """;
    public final String ERROR_MESSAGE = "Что-то пошло не так. Попробуйте еще раз.";

    @Override
    public boolean supports(String command) {
        return false;
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        String input = update.message().text().trim();

        if (input.equals("/cancel")) {
            botClient.sendMessage(chatId, CANCEL_MESSAGE);
        } else {
            ExternalCallResult<LinkResponse> response = linksClient.removeLink(chatId, input);
            switch (response.getStatus()) {
                case OK:
                    botClient.sendMessage(chatId, String.format(MAIN_MESSAGE, input));
                    break;
                case NOT_FOUND:
                    botClient.sendMessage(chatId, CHAT_IS_NOT_REGISTERED_MESSAGE);
                    break;
                default:
                    botClient.sendMessage(chatId, ERROR_MESSAGE);
            }
        }
        chatStateService.update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
    }
}
