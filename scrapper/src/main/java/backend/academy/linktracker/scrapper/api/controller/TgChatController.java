package backend.academy.linktracker.scrapper.api.controller;

import backend.academy.linktracker.scrapper.api.generated.controller.api_interface.TgChatApi;
import backend.academy.linktracker.scrapper.link.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/tg-chat")
public class TgChatController implements TgChatApi {
    private final ChatService chatService;

    @Override
    public ResponseEntity<Void> deleteChat(Long id) {
        if (id == null || id < 0) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "chat")
                    .addKeyValue("method", "delete")
                    .log("Получен запрос с некорректными параметрами на удаление чата.");
            return ResponseEntity.badRequest().build();
        }
        if (!chatService.isChatExist(id)) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "chat")
                    .addKeyValue("method", "delete")
                    .log("Получен запрос на удаление несуществующего чата.");
            return ResponseEntity.notFound().build();
        }
        chatService.deleteChat(id);
        log.atInfo()
                .addKeyValue("event", "handle_http_request")
                .addKeyValue("entity", "chat")
                .addKeyValue("method", "delete")
                .log("Получен запрос на удаление чата.");
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> registerChat(Long id) {
        if (id == null || id < 0) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "chat")
                    .addKeyValue("method", "post")
                    .log("Получен запрос с некорректными параметрами на сохранение чата.");
            return ResponseEntity.badRequest().build();
        }
        if (chatService.isChatExist(id)) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "chat")
                    .addKeyValue("method", "post")
                    .log("Получен запрос на сохранение уже сохраненного чата.");
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        chatService.registerChat(id);
        log.atInfo()
                .addKeyValue("event", "handle_http_request")
                .addKeyValue("entity", "chat")
                .addKeyValue("method", "post")
                .log("Получен запрос на сохранение чата.");
        return ResponseEntity.ok().build();
    }
}
