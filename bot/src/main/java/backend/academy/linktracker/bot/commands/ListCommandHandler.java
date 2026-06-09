package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ListCommandHandler implements DescribedCommandHandler {

    private final ChatStateService chatStateService;
    private final BotClient botClient;
    public final String MAIN_MESSAGE = """
    Введите тег для фильтрации отсиживаемых ссылок или нажмите /skip, чтобы получить все ссылки отслеживаемые пользователем.
    Нажмите /cancel чтобы прервать отменить запрос на вывод ссылок.
    """;

    @Override
    public boolean supports(String command) {
        return "/list".equals(command);
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        botClient.sendMessage(chatId, MAIN_MESSAGE);
        chatStateService.update(chatId, new ChatState(ChatState.State.WAIT_TAG_TO_SHOW_LINKS, null));
    }

    @Override
    public String getCommand() {
        return "/list";
    }

    @Override
    public String getDescription() {
        return "Вывести список отслеживаемых ссылок";
    }
}
