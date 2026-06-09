package backend.academy.linktracker.scrapper.link.repository.java_collection;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.link.model.Link;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkRepositoryInMemoryImplTest {

    private LinkRepositoryInMemoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new LinkRepositoryInMemoryImpl();
    }

    private Link createLink(Long chatId, String url, String... tags) {
        return new Link(chatId, url, Set.of(tags), Collections.emptySet(), null, null);
    }

    @Test
    void testSaveAndIsLinkExist() {
        Link link = createLink(1L, "http://example.com", "tag1", "tag2");
        repository.save(link);

        assertTrue(repository.isLinkExist("http://example.com"));
        assertFalse(repository.isLinkExist("http://notexist.com"));
    }

    @Test
    void testDelete() {
        Link link1 = createLink(1L, "http://example.com/1", "tag1");
        Link link2 = createLink(1L, "http://example.com/2", "tag2");
        repository.save(link1);
        repository.save(link2);

        repository.delete(1L, "http://example.com/1");

        assertFalse(repository.isLinkExist("http://example.com/1"));
        assertTrue(repository.isLinkExist("http://example.com/2"));
    }

    @Test
    void testFindAllWithLimitAndOffset() {
        for (int i = 0; i < 10; i++) {
            repository.save(createLink(1L, "http://example.com/" + i, "tag" + i));
        }
        List<Link> allLinks = repository.findAll(10, 0);
        assertEquals(10, allLinks.size());

        List<Link> limitedLinks = repository.findAll(5, 0);
        assertEquals(5, limitedLinks.size());

        List<Link> offsetLinks = repository.findAll(5, 5);
        assertEquals(5, offsetLinks.size());

        List<Link> emptyLinks = repository.findAll(5, 10);
        assertTrue(emptyLinks.isEmpty());
    }

    @Test
    void testFindAllByChatIdWithLimitAndOffset() {
        for (int i = 0; i < 10; i++) {
            repository.save(createLink(2L, "http://example.com/" + i, "tag" + i));
        }
        List<Link> allLinks = repository.findAll(2L, 10, 0);
        assertEquals(10, allLinks.size());

        List<Link> limitedLinks = repository.findAll(2L, 5, 0);
        assertEquals(5, limitedLinks.size());

        List<Link> offsetLinks = repository.findAll(2L, 5, 5);
        assertEquals(5, offsetLinks.size());

        List<Link> emptyLinks = repository.findAll(2L, 5, 10);
        assertTrue(emptyLinks.isEmpty());
    }

    @Test
    void testFindAllByChatIdAndTagWithLimitAndOffset() {
        repository.save(createLink(3L, "http://example.com/1", "tag1", "tag2"));
        repository.save(createLink(3L, "http://example.com/2", "tag2"));
        repository.save(createLink(3L, "http://example.com/3", "tag3"));

        List<Link> tag2Links = repository.findAll(3L, "tag2", 10, 0);
        assertEquals(2, tag2Links.size());

        List<Link> tag1Links = repository.findAll(3L, "tag1", 10, 0);
        assertEquals(1, tag1Links.size());

        List<Link> noTagLinks = repository.findAll(3L, "tag4", 10, 0);
        assertTrue(noTagLinks.isEmpty());
    }

    @Test
    void testDeleteAllByChatId() {
        repository.save(createLink(4L, "http://example.com/1", "tag1"));
        repository.save(createLink(4L, "http://example.com/2", "tag2"));
        repository.save(createLink(5L, "http://example.com/3", "tag3"));

        repository.deleteAllByChatId(4L);

        assertFalse(repository.isLinkExist("http://example.com/1"));
        assertFalse(repository.isLinkExist("http://example.com/2"));
        assertTrue(repository.isLinkExist("http://example.com/3"));
    }

    @Test
    void testCountAndCountByChatId() {
        repository.save(createLink(6L, "http://example.com/1", "tag1"));
        repository.save(createLink(6L, "http://example.com/2", "tag2"));
        repository.save(createLink(7L, "http://example.com/3", "tag3"));

        assertEquals(3L, repository.count());
        assertEquals(2L, repository.countByChatId(6L));
        assertEquals(1L, repository.countByChatId(7L));
        assertEquals(0L, repository.countByChatId(999L));
    }
}
