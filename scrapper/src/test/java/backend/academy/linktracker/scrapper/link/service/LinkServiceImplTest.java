package backend.academy.linktracker.scrapper.link.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.link.model.GitHubRepositoryId;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.repository.ChatRepository;
import backend.academy.linktracker.scrapper.link.repository.LinkRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkServiceImplTest {

    private LinkRepository linkRepository;
    private ChatRepository chatRepository;
    private LinkServiceImpl service;

    @BeforeEach
    void setUp() {
        linkRepository = mock(LinkRepository.class);
        chatRepository = mock(ChatRepository.class);
        service = new LinkServiceImpl(linkRepository, chatRepository);
    }

    private Link createLink(Long chatId, String url, String... tags) {
        return new Link(chatId, url, Set.of(tags), Set.of(1L, 2L), new GitHubRepositoryId("Tinkoff", "career"), null);
    }

    @Test
    void trackLink_shouldSave_whenChatExists() {
        Link link = createLink(1L, "https://a.com");

        when(chatRepository.isChatExists(1L)).thenReturn(true);

        service.trackLink(link);

        verify(chatRepository).isChatExists(1L);
        verify(linkRepository).save(link);
    }

    @Test
    void trackLink_shouldThrow_whenChatNotExists() {
        Link link = createLink(1L, "https://a.com");

        when(chatRepository.isChatExists(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.trackLink(link));

        verify(chatRepository).isChatExists(1L);
        verifyNoInteractions(linkRepository);
    }

    @Test
    void isLinkExist_shouldDelegateToRepository() {
        when(linkRepository.isLinkExist("https://a.com")).thenReturn(true);

        boolean result = service.isLinkExist("https://a.com");

        assertTrue(result);
        verify(linkRepository).isLinkExist("https://a.com");
    }

    @Test
    void untrackLink_shouldCallRepositoryDelete() {
        service.untrackLink(1L, "https://a.com");

        verify(linkRepository).delete(1L, "https://a.com");
    }

    @Test
    void getAllLinks_shouldReturnFromRepository() {
        List<Link> links = List.of(createLink(1L, "https://a.com"));
        when(linkRepository.findAll(10, 0)).thenReturn(links);

        List<Link> result = service.getAllLinks(10, 0);

        assertEquals(links, result);
        verify(linkRepository).findAll(10, 0);
    }

    @Test
    void getListLinksByChatId_shouldReturnFiltered() {
        List<Link> links = List.of(createLink(1L, "https://a.com"));
        when(linkRepository.findAll(1L, 10, 0)).thenReturn(links);

        List<Link> result = service.getListLinksByChatId(1L, 10, 0);

        assertEquals(links, result);
        verify(linkRepository).findAll(1L, 10, 0);
    }

    @Test
    void getListLinksByChatIdAndTag_shouldReturnFiltered() {
        List<Link> links = List.of(createLink(1L, "https://a.com", "tag1"));
        when(linkRepository.findAll(1L, "tag1", 10, 0)).thenReturn(links);

        List<Link> result = service.getListLinksByChatIdAndTag(1L, "tag1", 10, 0);

        assertEquals(links, result);
        verify(linkRepository).findAll(1L, "tag1", 10, 0);
    }
}
