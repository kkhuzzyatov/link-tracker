package backend.academy.linktracker.scrapper.link.repository;

import backend.academy.linktracker.scrapper.link.model.Link;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkRepository {
    void save(Link link);

    boolean isLinkExist(String link);

    void delete(Long chatId, String uri);

    List<Link> findAll(int offset, int limit);

    List<Link> findAll(Long chatId, int offset, int limit);

    List<Link> findAll(Long chatId, String tag, int offset, int limit);

    void deleteAllByChatId(Long chatId);

    Long count();

    Long countByChatId(Long chatId);

    void updateLastUpdated(String uri, OffsetDateTime lastUpdated);
}
