package backend.academy.linktracker.bot.commands;

import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class HelpCommandHandler implements DescribedCommandHandler {

    private final BotClient botClient;
    private final List<DescribedCommandHandler> handlers;

    private String message;

    public final String FRAME_FOR_MESSAGE =
            String.format("Доступные команды:%n%n%s%n%nПросто отправьте команду 🙂%n", "%s");

    private void createMessage() {
        StringBuilder commandString = new StringBuilder();

        for (DescribedCommandHandler handler : handlers) {
            commandString.append(String.format("%s - %s", handler.getCommand(), handler.getDescription()));
        }

        message = String.format(FRAME_FOR_MESSAGE, commandString);
    }

    public String getMessage() {
        if (message == null) {
            createMessage();
        }
        return message;
    }

    @Override
    public boolean supports(String receivedCommand) {
        return receivedCommand.equals("/help");
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();

        botClient.sendMessage(chatId, getMessage());
    }

    public String getCommand() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Показать список команд";
    }
}
