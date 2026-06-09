package backend.academy.linktracker.scrapper.link.repository.java_collection;

import backend.academy.linktracker.scrapper.infrastructure.aop.LoggableQuery;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LinkRepositoryInMemoryImpl implements LinkRepository {

    private static final int PAGE_SIZE = 1000;

    private final Set<Link> links = new HashSet<>();
    private final Set<String> tags = new HashSet<>();

    private final Map<Long, Set<Link>> chatLinks = new HashMap<>();

    @Override
    @LoggableQuery("save_link")
    public void save(Link link) {
        links.add(link);

        chatLinks.computeIfAbsent(link.getChatId(), _ -> new HashSet<>()).add(link);

        tags.addAll(link.getTags());
    }

    @Override
    @LoggableQuery("check_link_exists")
    public boolean isLinkExist(String link) {
        return links.stream().anyMatch(l -> l.getLink().equals(link));
    }

    @Override
    @LoggableQuery("delete_link")
    public void delete(Long chatId, String uri) {
        Set<Link> linksForChat = chatLinks.get(chatId);
        if (linksForChat == null) return;

        Link toRemove = linksForChat.stream()
                .filter(l -> l.getLink().equals(uri))
                .findFirst()
                .orElse(null);

        if (toRemove == null) return;

        linksForChat.remove(toRemove);

        if (linksForChat.isEmpty()) {
            chatLinks.remove(chatId);
        }

        // удалить из общего списка если нигде не используется
        boolean stillUsed = chatLinks.values().stream().anyMatch(set -> set.contains(toRemove));

        if (!stillUsed) {
            links.remove(toRemove);
        }
    }

    @Override
    @LoggableQuery("find_all_links")
    public List<Link> findAll(int limit, int offset) {
        List<Link> allLinks = new ArrayList<>(links);
        int cappedLimit = Math.min(limit, PAGE_SIZE);
        int fromIndex = Math.min(offset, allLinks.size());
        int toIndex = Math.min(fromIndex + cappedLimit, allLinks.size());
        return allLinks.subList(fromIndex, toIndex);
    }

    @Override
    @LoggableQuery("find_links_by_chat")
    public List<Link> findAll(Long chatId, int limit, int offset) {
        List<Link> linksForChat = new ArrayList<>(chatLinks.getOrDefault(chatId, Collections.emptySet()));
        int cappedLimit = Math.min(limit, PAGE_SIZE);
        int fromIndex = Math.min(offset, linksForChat.size());
        int toIndex = Math.min(fromIndex + cappedLimit, linksForChat.size());
        return linksForChat.subList(fromIndex, toIndex);
    }

    @Override
    @LoggableQuery("find_links_by_chat_and_tag")
    public List<Link> findAll(Long chatId, String tag, int limit, int offset) {
        List<Link> filteredLinks = chatLinks.getOrDefault(chatId, Collections.emptySet()).stream()
                .filter(link -> link.getTags().contains(tag))
                .toList();
        int cappedLimit = Math.min(limit, PAGE_SIZE);
        int fromIndex = Math.min(offset, filteredLinks.size());
        int toIndex = Math.min(fromIndex + cappedLimit, filteredLinks.size());
        return filteredLinks.subList(fromIndex, toIndex);
    }

    @Override
    @LoggableQuery("delete_all_links_by_chat")
    public void deleteAllByChatId(Long chatId) {
        Set<Link> linksForChat = chatLinks.get(chatId);
        if (linksForChat != null) {
            for (Link link : linksForChat) {
                boolean stillUsed = chatLinks.entrySet().stream()
                        .anyMatch(
                                e -> !e.getKey().equals(chatId) && e.getValue().contains(link));
                if (!stillUsed) {
                    links.remove(link);
                }
            }
            chatLinks.remove(chatId);
        }
    }

    @Override
    @LoggableQuery("count_links")
    public Long count() {
        return (long) links.size();
    }

    @Override
    @LoggableQuery("count_links_by_chat")
    public Long countByChatId(Long chatId) {
        return (long) chatLinks.getOrDefault(chatId, Collections.emptySet()).size();
    }

    @Override
    @LoggableQuery("update_link_last_updated")
    public void updateLastUpdated(String uri, OffsetDateTime lastUpdated) {
        for (Link link : links) {
            if (link.getLink().equals(uri)) {
                link.setLastUpdateRequestedAt(lastUpdated);
                break;
            }
        }
    }
}
