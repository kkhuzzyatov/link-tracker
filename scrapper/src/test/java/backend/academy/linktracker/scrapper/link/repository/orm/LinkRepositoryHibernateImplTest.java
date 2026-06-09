package backend.academy.linktracker.scrapper.link.repository.orm;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.link.model.Link;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class LinkRepositoryHibernateImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    private static EntityManagerFactory emf;
    private EntityManager em;
    private LinkRepositoryHibernateImpl repository;
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void init() {
        postgres.start();

        System.setProperty("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
        System.setProperty("jakarta.persistence.jdbc.user", postgres.getUsername());
        System.setProperty("jakarta.persistence.jdbc.password", postgres.getPassword());
        System.setProperty("jakarta.persistence.jdbc.driver", postgres.getDriverClassName());

        emf = Persistence.createEntityManagerFactory("test");
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));

        // Drop and recreate tables
        jdbcTemplate.execute("""
            DROP TABLE IF EXISTS tracked_link CASCADE;
            DROP TABLE IF EXISTS tag CASCADE;
            DROP TABLE IF EXISTS link CASCADE;
            DROP TABLE IF EXISTS chat CASCADE;

            CREATE TABLE chat (
                chat_id BIGINT PRIMARY KEY
            );
            CREATE TABLE link (
                link_id BIGSERIAL PRIMARY KEY,
                uri VARCHAR(256) NOT NULL UNIQUE
            );
            CREATE TABLE tag (
                tag_id BIGSERIAL PRIMARY KEY,
                tag VARCHAR(128) NOT NULL UNIQUE
            );
            CREATE TABLE tracked_link (
                chat_id BIGINT NOT NULL REFERENCES chat(chat_id),
                link_id BIGINT NOT NULL REFERENCES link(link_id),
                tag_id BIGINT NOT NULL REFERENCES tag(tag_id),
                last_updated TIMESTAMPTZ NOT NULL DEFAULT now(),
                PRIMARY KEY (chat_id, link_id, tag_id)
            );
        """);

        em = emf.createEntityManager();
        repository = new LinkRepositoryHibernateImpl();
        repository.setEm(em);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("TRUNCATE TABLE tracked_link, tag, link, chat CASCADE");
        if (em.isOpen()) {
            em.close();
        }
    }

    @Test
    void save_shouldInsertLink() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO chat(chat_id) VALUES (?)", chatId);
        String url = "https://github.com/openai/gpt-3";

        Link link = new Link(chatId, url, Set.of("tag1"), Set.of(chatId), Link.fromLink(url), OffsetDateTime.now());

        em.getTransaction().begin();
        repository.save(link);
        em.getTransaction().commit();

        List<String> urls = jdbcTemplate.queryForList(
                "SELECT l.uri FROM link l JOIN tracked_link tl ON l.link_id = tl.link_id WHERE tl.chat_id = ?",
                String.class,
                chatId);

        assertThat(urls).containsExactly(url);
    }

    @Test
    void findLinksByChatId_shouldReturnAllLinksForChat() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO chat(chat_id) VALUES (?)", chatId);

        Link linkA = new Link(
                chatId,
                "https://github.com/openai/gpt-3",
                Set.of("tag1"),
                Set.of(chatId),
                Link.fromLink("https://github.com/openai/gpt-3"),
                OffsetDateTime.now());
        Link linkB = new Link(
                chatId,
                "https://github.com/openai/gpt-4",
                Set.of("tag1"),
                Set.of(chatId),
                Link.fromLink("https://github.com/openai/gpt-4"),
                OffsetDateTime.now());
        Link linkC = new Link(
                2L,
                "https://github.com/openai/chatgpt",
                Set.of("tag1"),
                Set.of(2L),
                Link.fromLink("https://github.com/openai/chatgpt"),
                OffsetDateTime.now());
        jdbcTemplate.update("INSERT INTO chat(chat_id) VALUES (?)", 2L);

        em.getTransaction().begin();
        repository.save(linkA);
        repository.save(linkB);
        repository.save(linkC);
        em.getTransaction().commit();

        List<Link> links = repository.findAll(chatId, 1000, 0);
        List<String> urls = links.stream().map(Link::getLink).toList();

        assertThat(urls)
                .containsExactlyInAnyOrder("https://github.com/openai/gpt-3", "https://github.com/openai/gpt-4");
    }

    @Test
    void deleteLinksByChatId_shouldRemoveLinks() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO chat(chat_id) VALUES (?)", chatId);

        Link linkA = new Link(
                chatId,
                "https://github.com/openai/gpt-3",
                Set.of("tag1"),
                Set.of(chatId),
                Link.fromLink("https://github.com/openai/gpt-3"),
                OffsetDateTime.now());
        Link linkB = new Link(
                chatId,
                "https://github.com/openai/gpt-4",
                Set.of("tag1"),
                Set.of(chatId),
                Link.fromLink("https://github.com/openai/gpt-4"),
                OffsetDateTime.now());

        em.getTransaction().begin();
        repository.save(linkA);
        repository.save(linkB);
        em.getTransaction().commit();

        List<Link> linksBeforeDelete = repository.findAll(chatId, 1000, 0);
        assertThat(linksBeforeDelete).hasSize(2);

        em.getTransaction().begin();
        repository.deleteAllByChatId(chatId);
        em.getTransaction().commit();

        List<Link> linksAfterDelete = repository.findAll(chatId, 1000, 0);
        assertThat(linksAfterDelete).isEmpty();
    }
}
