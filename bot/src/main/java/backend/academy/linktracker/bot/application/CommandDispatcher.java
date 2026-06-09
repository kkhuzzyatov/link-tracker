package backend.academy.linktracker.bot.application;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.commands.CommandHandler;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Диспетчер команд Telegram бота.
 * Отвечает за:
 * - поиск подходящего CommandHandler
 * - fallback на default handler
 */
@Component
@RequiredArgsConstructor
public class CommandDispatcher {
    private final Map<String, CommandHandler> handlers;
    private final ChatStateService chatStateService;

    /**
     * Обрабатывает Update, вызывая соответствующий CommandHandler.
     *
     * @param update объект Update из Telegram API
     */
    public void dispatch(Update update) {
        Long chatId = update.message().chat().id();

        ChatState chatState = chatStateService.getStateByChatId(chatId);

        CommandHandler handler;
        switch (chatState.getCurrentState()) {
            case WAIT_LINK_TO_TRACK:
                handler = handlers.get("linkStartTrackCommandHandler");
                break;
            case WAIT_LINK_TO_UNTRACK:
                handler = handlers.get("linkStopTrackCommandHandler");
                break;
            case WAIT_TAG_TO_TRACK:
                handler = handlers.get("tagStartTrackCommandHandler");
                break;
            case WAIT_TAG_TO_SHOW_LINKS:
                handler = handlers.get("tagFilterLinksCommandHandler");
                break;
            default:
                String command = update.message().text().trim();
                handler = handlers.values().stream()
                        .filter(h -> h.supports(command))
                        .findFirst()
                        .orElse(handlers.get("unknownCommandHandler"));
        }

        handler.handle(update);
    }
}
