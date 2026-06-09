package backend.academy.linktracker.scrapper.api.client;

import backend.academy.linktracker.scrapper.api.generated.client.model.LinkUpdate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Slf4j
@Component
public class UpdatesClient {

    private final RestClient restClient;

    @Value("${bot.url}")
    private String botUrl;

    public void sendUpdates(List<LinkUpdate> linkUpdates) {
        String uri = String.format("%s/updates", botUrl);

        restClient.post().uri(uri).body(linkUpdates).exchange((request, clientResponse) -> ResponseEntity.status(
                        clientResponse.getStatusCode())
                .build());

        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "bot")
                .addKeyValue("entity", "link")
                .addKeyValue("number_of_entity", linkUpdates.size())
                .addKeyValue("method", "post")
                .log("Сделан запрос к bot о появлении изменений в отслеживаемых ссылках.");
    }
}
