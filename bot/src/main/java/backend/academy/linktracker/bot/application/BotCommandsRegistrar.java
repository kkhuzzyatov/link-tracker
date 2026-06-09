package backend.academy.linktracker.bot.application;

import backend.academy.linktracker.bot.client.BotCommandDto;
import backend.academy.linktracker.bot.client.BotCommandsClient;
import backend.academy.linktracker.bot.commands.CommandHandler;
import backend.academy.linktracker.bot.commands.DescribedCommandHandler;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotCommandsRegistrar {

    private final List<CommandHandler> handlers;
    private final BotCommandsClient botCommandsClient;

    @PostConstruct
    public void init() {
        List<BotCommandDto> commands = handlers.stream()
                .filter(h -> h instanceof DescribedCommandHandler)
                .map(h -> (DescribedCommandHandler) h)
                .map(d -> new BotCommandDto(stripSlash(d.getCommand()), d.getDescription()))
                .toList();

        if (!commands.isEmpty()) {
            botCommandsClient.setMyCommands(commands);
        }
    }

    private String stripSlash(String cmd) {
        return cmd != null && cmd.startsWith("/") ? cmd.substring(1) : cmd;
    }
}
