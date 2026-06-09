package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.api.client.LinksClient;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.api.generated.client.model.LinkResponse;
import backend.academy.linktracker.bot.api.generated.client.model.ListLinksResponse;
import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TagFilterLinksCommandHandler implements CommandHandler {
    private final ChatStateService chatStateService;
    private final LinksClient linksClient;
    private final BotClient botClient;
    public final String CANCEL_MESSAGE = "Вывод отслеживаемых ссылок отменен.";
    public final String NO_LINKS_FOUND_MESSAGE = "Не найдено не одной ссылки.";
    public final String CHAT_IS_NOT_REGISTERED_MESSAGE = """
    Вывод отслеживаемых ссылок невозможен, так как ваш чат не зарегистрирован в системе.
    Нажмите /start, чтобы зарегистрировать чат.
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

        chatStateService.update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));

        if (input.equals("/cancel")) {
            botClient.sendMessage(chatId, CANCEL_MESSAGE);
            return;
        }

        List<String> links;
        ExternalCallResult<ListLinksResponse> response;

        if (input.equals("/skip")) {
            response = linksClient.getLinks(chatId);
        } else {
            response = linksClient.getLinksByTag(chatId, input);
        }

        switch (response.getStatus()) {
            case OK:
                links = response.getBody().links().stream()
                        .map(LinkResponse::url)
                        .map(URI::toString)
                        .toList();
                break;
            case NOT_FOUND:
                botClient.sendMessage(chatId, CHAT_IS_NOT_REGISTERED_MESSAGE);
                return;
            default:
                botClient.sendMessage(chatId, ERROR_MESSAGE);
                return;
        }

        String message;
        if (links.isEmpty()) {
            message = NO_LINKS_FOUND_MESSAGE;
        } else {
            message = String.join(", ", links);
        }
        botClient.sendMessage(chatId, message);
    }
}
