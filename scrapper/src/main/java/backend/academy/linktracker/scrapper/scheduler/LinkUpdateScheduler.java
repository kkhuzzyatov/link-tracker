package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.api.client.LinkUpdateClient;
import backend.academy.linktracker.scrapper.api.client.UpdatesClient;
import backend.academy.linktracker.scrapper.api.generated.client.model.LinkUpdate;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.service.LinkService;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class LinkUpdateScheduler {

    private final UpdatesClient updatesClient;
    private final LinkService linkService;
    private final List<LinkUpdateClient> linkUpdateClients;

    @Value("${batch.size}")
    private int BATCH_SIZE;

    @SneakyThrows
    @PostConstruct
    @Scheduled(fixedDelay = 40_000)
    public void updatesPost() {
        int limit = 1000;
        int offset = 0;
        List<Link> links;
        List<LinkUpdate> linkUpdates = new ArrayList<>();

        do {
            links = linkService.getAllLinks(limit, offset);

            for (Link link : links) {
                LinkUpdateClient linkUpdateClient = linkUpdateClients.stream()
                        .filter(luc -> luc.supports(link.getResourceIdentifier()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Не найден LinkUpdateClient."));
                List<String> updates = linkUpdateClient.getNewLinkChanges(link);
                if (!updates.isEmpty()) {
                    for (String update : updates) {
                        linkUpdates.add(new LinkUpdate(
                                null, new URI(link.getLink()), update, new ArrayList<>(link.getTgChatIds())));
                        if (linkUpdates.size() == BATCH_SIZE) {
                            updatesClient.sendUpdates(linkUpdates);
                        }
                    }
                }
                linkService.updateTime(link.getLink(), java.time.OffsetDateTime.now());
            }
            updatesClient.sendUpdates(linkUpdates);

            offset += limit;
        } while (!links.isEmpty());
        updatesClient.sendUpdates(linkUpdates);
    }
}
