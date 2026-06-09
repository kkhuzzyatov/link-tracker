package backend.academy.linktracker.bot.api.client;

import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Slf4j
@Component
public class TgChatClient {

    private static final String BASE_PATH = "/tg-chat";

    private final RestClient restClient;

    @Value("${scrapper.url}")
    private String scrapperUrl;

    public ExternalCallResult<Void> deleteChat(Long id) {

        String uri = String.format("%s%s/%d", scrapperUrl, BASE_PATH, id);

        ResponseEntity<Void> response = restClient
                .delete()
                .uri(uri)
                .exchange((request, clientResponse) ->
                        ResponseEntity.status(clientResponse.getStatusCode()).build());

        Status status = ExternalCallResult.mapStatus(response.getStatusCode().value());

        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "scrapper")
                .addKeyValue("entity", "chat")
                .addKeyValue("method", "delete")
                .addKeyValue("chat_id", id)
                .addKeyValue("status", status)
                .log("Сделан запрос на удаление чата.");

        return ExternalCallResult.<Void>builder().body(null).status(status).build();
    }

    public ExternalCallResult<Void> registerChat(Long id) {

        String uri = String.format("%s%s/%d", scrapperUrl, BASE_PATH, id);

        ResponseEntity<Void> response = restClient
                .post()
                .uri(uri)
                .exchange((request, clientResponse) ->
                        ResponseEntity.status(clientResponse.getStatusCode()).build());

        Status status = ExternalCallResult.mapStatus(response.getStatusCode().value());

        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "scrapper")
                .addKeyValue("entity", "chat")
                .addKeyValue("method", "post")
                .addKeyValue("chat_id", id)
                .addKeyValue("status", status)
                .log("Сделан запрос на создание чата.");

        return ExternalCallResult.<Void>builder().body(null).status(status).build();
    }
}
