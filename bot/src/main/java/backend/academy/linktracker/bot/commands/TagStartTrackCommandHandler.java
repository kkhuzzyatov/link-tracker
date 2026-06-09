package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.api.client.LinksClient;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.api.generated.client.model.LinkResponse;
import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import java.net.URISyntaxException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TagStartTrackCommandHandler implements CommandHandler {

    private final ChatStateService chatStateService;
    private final LinksClient linksClient;
    private final BotClient botClient;
    public final String CANCEL_MESSAGE = "Процесс добавления ссылки в список отслеживаемых прерван";
    public final String SKIP_TAG_MESSAGE = "Начато отслеживание ссылки %s без тегов";
    public final String TRACK_WITH_TAG_MESSAGE = "Начато отслеживание ссылки %s с тегом %s";
    public final String TRACK_WITH_MANY_TAGS_MESSAGE = "Начато отслеживание ссылки %s с %d тегами: %s";
    public final String NOT_CORRECT_URI_MESSAGE = "Введенная ссылка (%s) некорректна. Сохранение ссылки прервано.";
    public final String CHAT_IS_NOT_REGISTERED_MESSAGE = """
    Начало отслеживания ссылки невозможно, так как ваш чат не зарегистрирован в системе.
    Нажмите /start, чтобы зарегистрировать чат.
    """;
    public final String CONFLICT_MESSAGE = "Ссылка уже отслеживается.";
    public final String ERROR_MESSAGE = "Что-то пошло не так. Попробуйте еще раз.";

    @Override
    public boolean supports(String command) {
        return false;
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        String input = update.message().text().trim();
        String link = chatStateService.getStateByChatId(chatId).getUri();
        List<String> tags = List.of(input.split(", "));

        try {
            if (input.equals("/cancel")) {
                botClient.sendMessage(chatId, CANCEL_MESSAGE);
            } else if (input.equals("/skip")) {
                ExternalCallResult<LinkResponse> response = linksClient.addLink(chatId, link, null);
                sendMessageByStatus(chatId, response, String.format(SKIP_TAG_MESSAGE, link));
            } else if (tags.size() == 1) {
                ExternalCallResult<LinkResponse> response = linksClient.addLink(chatId, link, tags);
                sendMessageByStatus(chatId, response, String.format(TRACK_WITH_TAG_MESSAGE, link, input));
            } else {
                ExternalCallResult<LinkResponse> response = linksClient.addLink(chatId, link, tags);
                sendMessageByStatus(
                        chatId, response, String.format(TRACK_WITH_MANY_TAGS_MESSAGE, link, tags.size(), input));
            }
        } catch (URISyntaxException e) {
            botClient.sendMessage(chatId, String.format(NOT_CORRECT_URI_MESSAGE, link));
        }
        chatStateService.update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
    }

    private void sendMessageByStatus(Long chatId, ExternalCallResult<LinkResponse> response, String okMessage) {
        switch (response.getStatus()) {
            case OK:
                botClient.sendMessage(chatId, okMessage);
                break;
            case NOT_FOUND:
                botClient.sendMessage(chatId, CHAT_IS_NOT_REGISTERED_MESSAGE);
                break;
            case CONFLICT:
                botClient.sendMessage(chatId, CONFLICT_MESSAGE);
                break;
            default:
                botClient.sendMessage(chatId, ERROR_MESSAGE);
        }
    }
}
