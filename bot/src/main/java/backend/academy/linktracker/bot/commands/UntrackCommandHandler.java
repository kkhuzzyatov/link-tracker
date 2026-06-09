package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UntrackCommandHandler implements DescribedCommandHandler {

    private final BotClient botClient;
    private final ChatStateService chatStateService;
    public final String MAIN_MESSAGE =
            "Введите ссылку для прекращения отслеживания или нажмите /cancel чтобы прервать процесс прекращения отслеживания.";

    @Override
    public String getCommand() {
        return "/untrack";
    }

    @Override
    public String getDescription() {
        return "Отменить отслеживание ссылки.";
    }

    @Override
    public boolean supports(String command) {
        return "/untrack".equals(command);
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        chatStateService.update(chatId, new ChatState(ChatState.State.WAIT_LINK_TO_UNTRACK, null));
        botClient.sendMessage(chatId, MAIN_MESSAGE);
    }
}
