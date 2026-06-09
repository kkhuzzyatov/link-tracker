package backend.academy.linktracker.scrapper.api.controller;

import static java.time.OffsetDateTime.now;

import backend.academy.linktracker.scrapper.api.generated.controller.api_interface.LinksApi;
import backend.academy.linktracker.scrapper.api.generated.controller.model.AddLinkRequest;
import backend.academy.linktracker.scrapper.api.generated.controller.model.LinkResponse;
import backend.academy.linktracker.scrapper.api.generated.controller.model.ListLinksResponse;
import backend.academy.linktracker.scrapper.api.generated.controller.model.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.model.ResourceIdentifier;
import backend.academy.linktracker.scrapper.link.service.ChatService;
import backend.academy.linktracker.scrapper.link.service.LinkService;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/links")
public class LinksController implements LinksApi {
    private final ChatService chatService;
    private final LinkService linkService;

    @SneakyThrows
    @Override
    public ResponseEntity<ListLinksResponse> getLinks(Long tgChatId) {
        if (tgChatId == null || tgChatId < 0) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "list_of_links")
                    .addKeyValue("method", "get")
                    .log("Получен запрос с некорректными параметрами на получение отслеживаемых ссылок.");
            return ResponseEntity.badRequest().build();
        }
        if (!chatService.isChatExist(tgChatId)) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "list_of_links")
                    .addKeyValue("method", "get")
                    .log(
                            "Получен запрос на получение отслеживаемых ссылок для несуществующего чата или чата для которого не отслеживается ни одной ссылки.");
            return ResponseEntity.notFound().build();
        }
        int limit = 1000;
        int offset = 0;
        List<Link> linksBatch;
        List<Link> links = new ArrayList<>();

        do {
            linksBatch = linkService.getListLinksByChatId(tgChatId, limit, offset);
            links.addAll(linksBatch);
            offset += limit;
        } while (!linksBatch.isEmpty());
        List<LinkResponse> linkResponses = new ArrayList<>();
        for (int i = 0; i < links.size(); i++) {
            linkResponses.add(new LinkResponse(
                    Long.valueOf(i),
                    new URI(links.get(i).getLink()),
                    links.get(i).getTags().stream().toList(),
                    null));
        }
        ListLinksResponse listLinksResponse = new ListLinksResponse(linkResponses, linkResponses.size());
        log.atInfo()
                .addKeyValue("event", "handle_http_request")
                .addKeyValue("entity", "list_of_links")
                .addKeyValue("method", "get")
                .addKeyValue("link_responses_size", linkResponses.size())
                .log("Получен запрос на получение отслеживаемых ссылок.");
        return ResponseEntity.ok(listLinksResponse);
    }

    @Override
    public ResponseEntity<ListLinksResponse> getLinksByTag(Long tgChatId, String tag) {
        if (tgChatId == null || tgChatId < 0 || tag == null || tag.isBlank()) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "list_of_links")
                    .addKeyValue("method", "get_by_tag")
                    .addKeyValue("tag", tag)
                    .log("Получен запрос с некорректными параметрами на получение отслеживаемых ссылок по тегу.");
            return ResponseEntity.badRequest().build();
        }
        if (!chatService.isChatExist(tgChatId)) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "list_of_links")
                    .addKeyValue("method", "get_by_tag")
                    .addKeyValue("tag", tag)
                    .log(
                            "Получен запрос на получение отслеживаемых ссылок по тегу для несуществующего чата или чата для которого не отслеживается ни одной ссылки.");
            return ResponseEntity.notFound().build();
        }
        int limit = 1000;
        int offset = 0;
        List<Link> filteredLinks = linkService.getListLinksByChatIdAndTag(tgChatId, tag, limit, offset);
        return buildListLinksResponse(tgChatId, filteredLinks, tag, "get_by_tag");
    }

    @Override
    public ResponseEntity<LinkResponse> addLink(Long tgChatId, AddLinkRequest request) {
        if (tgChatId == null || tgChatId < 0 || request == null || request.link() == null) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "link")
                    .addKeyValue("method", "post")
                    .log("Получен запрос с некорректными параметрами на начало отслеживания ссылки.");
            return ResponseEntity.badRequest().build();
        }
        if (!chatService.isChatExist(tgChatId)) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "link")
                    .addKeyValue("method", "post")
                    .log("Получен запрос на начало отслеживания ссылки для несуществующего чата.");
            return ResponseEntity.notFound().build();
        }
        String url = request.link().toString();
        if (linkService.isLinkExist(url)) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "link")
                    .addKeyValue("method", "post")
                    .log("Получен запрос на начало отслеживания ссылки, которая уже отслеживается.");
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        ResourceIdentifier resourceIdentifier;
        try {
            resourceIdentifier = Link.fromLink(url);
        } catch (IllegalArgumentException e) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "link")
                    .addKeyValue("method", "post")
                    .log(
                            "Получен запрос на начало отслеживания ссылки, которая не является ссылкой на поддерживаемый ресурс.");
            return ResponseEntity.badRequest().build();
        }
        linkService.trackLink(
                new Link(tgChatId, url, new HashSet<>(request.tags()), Set.of(tgChatId), resourceIdentifier, now()));
        log.atInfo()
                .addKeyValue("event", "handle_http_request")
                .addKeyValue("entity", "link")
                .addKeyValue("method", "post")
                .log("Получен запрос на начало отслеживания ссылки.");
        return ResponseEntity.ok(new LinkResponse(tgChatId, request.link(), request.tags(), null));
    }

    @Override
    public ResponseEntity<LinkResponse> removeLink(Long tgChatId, RemoveLinkRequest request) {
        if (tgChatId == null || tgChatId < 0 || request == null || request.link() == null) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "link")
                    .addKeyValue("method", "delete")
                    .log("Получен запрос с некорректными параметрами на прекращение отслеживания ссылки.");
            return ResponseEntity.badRequest().build();
        }
        String url = request.link().toString();
        if (!chatService.isChatExist(tgChatId) || !linkService.isLinkExist(url)) {
            log.atWarn()
                    .addKeyValue("event", "handle_http_request")
                    .addKeyValue("entity", "link")
                    .addKeyValue("method", "delete")
                    .log("Получен запрос на прекращение отслеживания ссылки, такой что не существует ссылка или чат.");
            return ResponseEntity.notFound().build();
        }
        linkService.untrackLink(tgChatId, request.link().toString());
        log.atInfo()
                .addKeyValue("event", "handle_http_request")
                .addKeyValue("entity", "link")
                .addKeyValue("method", "delete")
                .log("Получен запрос на прекращение отслеживания ссылки.");
        return ResponseEntity.ok().build();
    }

    @SneakyThrows
    private ResponseEntity<ListLinksResponse> buildListLinksResponse(
            Long tgChatId, List<Link> links, String tag, String method) {
        List<LinkResponse> linkResponses = new ArrayList<>();
        for (int i = 0; i < links.size(); i++) {
            linkResponses.add(new LinkResponse(
                    Long.valueOf(i),
                    new URI(links.get(i).getLink()),
                    links.get(i).getTags().stream().toList(),
                    null));
        }
        ListLinksResponse listLinksResponse = new ListLinksResponse(linkResponses, linkResponses.size());

        LoggingEventBuilder logBuilder = log.atInfo();
        logBuilder = logBuilder.addKeyValue("tg_chat_id", tgChatId);
        logBuilder = logBuilder.addKeyValue("event", "handle_http_request");
        logBuilder = logBuilder.addKeyValue("entity", "list_of_links");
        logBuilder = logBuilder.addKeyValue("method", method);
        logBuilder = logBuilder.addKeyValue("link_responses_size", linkResponses.size());
        if (tag != null) {
            logBuilder = logBuilder.addKeyValue("tag", tag);
            logBuilder.log("Получен запрос на получение отслеживаемых ссылок по тегу.");
        } else {
            logBuilder.log("Получен запрос на получение отслеживаемых ссылок.");
        }

        return ResponseEntity.ok(listLinksResponse);
    }
}
