package backend.academy.linktracker.scrapper.link.service;

import backend.academy.linktracker.scrapper.link.model.Link;
import java.util.List;

public interface LinkService {
    void trackLink(Link link);

    boolean isLinkExist(String link);

    void untrackLink(Long chatId, String uri);

    List<Link> getAllLinks(int limit, int offset);

    List<Link> getListLinksByChatId(Long chatId, int limit, int offset);

    List<Link> getListLinksByChatIdAndTag(Long chatId, String tag, int limit, int offset);

    void updateTime(String uri, java.time.OffsetDateTime lastUpdated);
}
