package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TrackCommandHandler implements DescribedCommandHandler {

    private final BotClient botClient;
    private final ChatStateService chatStateService;
    public final String MAIN_MESSAGE =
            "Введите ссылку для начала отслеживания или нажмите /cancel чтобы прервать процесс начала отслеживания.";

    @Override
    public String getCommand() {
        return "/track";
    }

    @Override
    public String getDescription() {
        return "Начать отслеживание ссылки.";
    }

    @Override
    public boolean supports(String command) {
        return "/track".equals(command);
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        botClient.sendMessage(chatId, MAIN_MESSAGE);
        chatStateService.update(chatId, new ChatState(ChatState.State.WAIT_LINK_TO_TRACK, null));
    }
}
