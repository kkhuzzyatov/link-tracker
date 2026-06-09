package backend.academy.linktracker.bot.application;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class TelegramUpdatesListener {

    private final TelegramBot telegramBot;
    private final CommandDispatcher dispatcher;

    @PostConstruct
    public void init() {

        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    dispatcher.dispatch(update);

                    Long chatId = update.message().chat().id();
                    String message = update.message().text().trim();
                    log.atInfo()
                            .addKeyValue("event", "received_update")
                            .addKeyValue("chat_id", chatId)
                            .addKeyValue("received_message", message)
                            .log("Пользователь отправил сообщение боту, переданное в обработку.");
                }
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
