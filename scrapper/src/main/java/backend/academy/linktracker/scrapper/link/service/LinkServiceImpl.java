package backend.academy.linktracker.scrapper.link.service;

import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.repository.ChatRepository;
import backend.academy.linktracker.scrapper.link.repository.LinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;

    @Override
    public void trackLink(Link link) {
        if (!chatRepository.isChatExists(link.getChatId())) {
            throw new IllegalArgumentException(
                    "Невозможно привязать ссылку к несуществующему чату.  Необходимо сначала создать чат.");
        }
        linkRepository.save(link);
    }

    @Override
    public boolean isLinkExist(String link) {
        return linkRepository.isLinkExist(link);
    }

    @Override
    public void untrackLink(Long chatId, String uri) {
        linkRepository.delete(chatId, uri);
    }

    @Override
    public List<Link> getAllLinks(int limit, int offset) {
        return linkRepository.findAll(limit, offset);
    }

    @Override
    public List<Link> getListLinksByChatId(Long chatId, int limit, int offset) {
        return linkRepository.findAll(chatId, limit, offset);
    }

    @Override
    public List<Link> getListLinksByChatIdAndTag(Long chatId, String tag, int limit, int offset) {
        return linkRepository.findAll(chatId, tag, limit, offset);
    }

    @Override
    public void updateTime(String uri, java.time.OffsetDateTime lastUpdated) {
        linkRepository.updateLastUpdated(uri, lastUpdated);
    }
}
