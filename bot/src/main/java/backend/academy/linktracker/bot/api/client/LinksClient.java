package backend.academy.linktracker.bot.api.client;

import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult.Status;
import backend.academy.linktracker.bot.api.generated.client.model.AddLinkRequest;
import backend.academy.linktracker.bot.api.generated.client.model.LinkResponse;
import backend.academy.linktracker.bot.api.generated.client.model.ListLinksResponse;
import backend.academy.linktracker.bot.api.generated.client.model.RemoveLinkRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Slf4j
@Component
public class LinksClient {

    private static final String BASE_PATH = "/links";

    private static final String CHAT_HEADER = "Tg-Chat-Id";

    private final RestClient restClient;

    @Value("${scrapper.url}")
    private String scrapperUrl;

    public ExternalCallResult<ListLinksResponse> getLinks(Long chatId) {
        String uri = String.format("%s%s", scrapperUrl, BASE_PATH);
        return executeGetLinksRequest(uri, chatId, null);
    }

    public ExternalCallResult<ListLinksResponse> getLinksByTag(Long chatId, String tag) {
        String uri = String.format("%s%s?tag=%s", scrapperUrl, BASE_PATH, tag);
        return executeGetLinksRequest(uri, chatId, tag);
    }

    private ExternalCallResult<ListLinksResponse> executeGetLinksRequest(String uri, Long chatId, String tag) {
        ResponseEntity<ListLinksResponse> response = restClient
                .get()
                .uri(uri)
                .header(CHAT_HEADER, chatId.toString())
                .exchange((_, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode())
                        .body(clientResponse.bodyTo(ListLinksResponse.class)));

        Status status = ExternalCallResult.mapStatus(response.getStatusCode().value());

        var logBuilder = log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "scrapper")
                .addKeyValue("entity", "list_of_links")
                .addKeyValue("method", "get")
                .addKeyValue("chat_id", chatId)
                .addKeyValue("status", status);

        if (tag != null) {
            logBuilder = logBuilder.addKeyValue("tag", tag);
            logBuilder.log("Сделан запрос на получение всех отслеживаемых определенным чатом ссылок по тегу.");
        } else {
            logBuilder.log("Сделан запрос на получение всех отслеживаемых определенным чатом ссылок.");
        }

        return ExternalCallResult.<ListLinksResponse>builder()
                .body(response.getBody())
                .status(status)
                .build();
    }

    public ExternalCallResult<LinkResponse> addLink(Long chatId, String link, List<String> tags)
            throws URISyntaxException {
        String uri = String.format("%s%s", scrapperUrl, BASE_PATH);

        ResponseEntity<LinkResponse> response = restClient
                .post()
                .uri(uri)
                .header(CHAT_HEADER, chatId.toString())
                .body(new AddLinkRequest(new URI(link), tags, null))
                .exchange((_, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode())
                        .body(clientResponse.bodyTo(LinkResponse.class)));

        Status status = ExternalCallResult.mapStatus(response.getStatusCode().value());

        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "scrapper")
                .addKeyValue("entity", "link")
                .addKeyValue("method", "post")
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link_of_body", link)
                .addKeyValue("status", status)
                .log("Сделан запрос на начало отслеживания ссылки для определенного чата.");

        return ExternalCallResult.<LinkResponse>builder()
                .body(response.getBody())
                .status(status)
                .build();
    }

    @SneakyThrows
    public ExternalCallResult<LinkResponse> removeLink(Long chatId, String link) {
        String uri = String.format("%s%s", scrapperUrl, BASE_PATH);

        ResponseEntity<LinkResponse> response = restClient
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri(uri)
                .header(CHAT_HEADER, chatId.toString())
                .body(new RemoveLinkRequest(new URI(link)))
                .exchange((_, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode())
                        .body(clientResponse.bodyTo(LinkResponse.class)));

        Status status = ExternalCallResult.mapStatus(response.getStatusCode().value());

        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "scrapper")
                .addKeyValue("entity", "link")
                .addKeyValue("method", "delete")
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link_of_body", link)
                .addKeyValue("status", status)
                .log("Сделан запрос на прекращение отслеживания ссылки для определенного чата.");

        return ExternalCallResult.<LinkResponse>builder()
                .body(response.getBody())
                .status(status)
                .build();
    }
}
