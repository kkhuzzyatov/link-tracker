package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LinkStartTrackCommandHandler implements CommandHandler {

    private final ChatStateService chatStateService;
    private final BotClient botClient;
    public final String CANCEL_MESSAGE = "Процесс добавления ссылки в список отслеживаемых прерван.";
    public final String MAIN_MESSAGE = """
    Введите теги для этой ссылки (например, работа, баг, документация) или нажмите /skip, чтобы пропустить.
    Нажмите /cancel чтобы прервать добавление ссылки в список отслеживаемых.
    """;

    @Override
    public boolean supports(String command) {
        return false;
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        String input = update.message().text().trim();

        if (input.equals("/cancel")) {
            chatStateService.update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
            botClient.sendMessage(chatId, CANCEL_MESSAGE);
        } else {
            chatStateService.update(chatId, new ChatState(ChatState.State.WAIT_TAG_TO_TRACK, input));
            botClient.sendMessage(chatId, MAIN_MESSAGE);
        }
    }
}
