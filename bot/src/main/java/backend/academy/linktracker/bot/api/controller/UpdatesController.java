package backend.academy.linktracker.bot.api.controller;

import backend.academy.linktracker.bot.api.generated.controller.api_interface.UpdatesApi;
import backend.academy.linktracker.bot.api.generated.controller.model.LinkUpdate;
import backend.academy.linktracker.bot.client.BotClient;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/updates")
public class UpdatesController implements UpdatesApi {
    private final BotClient botClient;

    @Override
    public ResponseEntity<Void> updatesPost(List<LinkUpdate> linkUpdates) {
        for (LinkUpdate linkUpdate : linkUpdates) {
            URI uri = linkUpdate.url();
            String description = linkUpdate.description();

            if (uri == null || description == null) {
                log.atWarn()
                        .addKeyValue("event", "handle_http_request")
                        .addKeyValue("entity", "link")
                        .addKeyValue("method", "post")
                        .addKeyValue("link", linkUpdate.url())
                        .log(
                                "Получен запрос о появлении изменения в отслеживаемой ссылки с некорректными параметрами.");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            for (Long chatId : linkUpdate.tgChatIds()) {
                botClient.sendMessage(chatId, description);
            }

            log.atInfo()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "link")
                    .addKeyValue("method", "post")
                    .addKeyValue("link", linkUpdate.url())
                    .log("Получен корректный запрос о появлении изменения в отслеживаемой ссылки.");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
