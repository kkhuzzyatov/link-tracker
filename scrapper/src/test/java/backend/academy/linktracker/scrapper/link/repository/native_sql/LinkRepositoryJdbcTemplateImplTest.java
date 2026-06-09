package backend.academy.linktracker.scrapper.link.repository.native_sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.link.model.Link;
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
class LinkRepositoryJdbcTemplateImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    private JdbcTemplate jdbcTemplate;
    private LinkRepositoryJdbcTemplateImpl repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));

        repository = new LinkRepositoryJdbcTemplateImpl(jdbcTemplate);

        // Дропаем таблицы, если существуют, и создаём их заново для тестов
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
                last_updated TIMESTAMP NOT NULL DEFAULT now(),
                PRIMARY KEY (chat_id, link_id, tag_id)
            );
        """);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("TRUNCATE TABLE tracked_link, tag, link, chat CASCADE");
    }

    @Test
    void save_shouldInsertLink() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO chat(chat_id) VALUES (?)", chatId);
        String url = "https://github.com/openai/gpt-3";

        Link link = new Link(chatId, url, Set.of("tag1"), Set.of(chatId), Link.fromLink(url), OffsetDateTime.now());
        repository.save(link);

        List<String> urls = jdbcTemplate.queryForList("""
            SELECT l.uri
            FROM link l
            JOIN tracked_link tl ON l.link_id = tl.link_id
            WHERE tl.chat_id = ?
            """, String.class, chatId);

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
        repository.save(linkA);

        Link linkB = new Link(
                chatId,
                "https://github.com/openai/gpt-4",
                Set.of("tag1"),
                Set.of(chatId),
                Link.fromLink("https://github.com/openai/gpt-4"),
                OffsetDateTime.now());
        repository.save(linkB);

        Link linkC = new Link(
                2L,
                "https://github.com/openai/chatgpt",
                Set.of("tag1"),
                Set.of(2L),
                Link.fromLink("https://github.com/openai/chatgpt"),
                OffsetDateTime.now());
        jdbcTemplate.update("INSERT INTO chat(chat_id) VALUES (?)", 2L);
        repository.save(linkC); // другой чат

        List<Link> links = repository.findAll(chatId, 1000, 0);
        List<String> urls =
                links == null ? List.of() : links.stream().map(Link::getLink).toList();

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
        repository.save(linkA);

        Link linkB = new Link(
                chatId,
                "https://github.com/openai/gpt-4",
                Set.of("tag1"),
                Set.of(chatId),
                Link.fromLink("https://github.com/openai/gpt-4"),
                OffsetDateTime.now());
        repository.save(linkB);

        List<Link> links = repository.findAll(chatId, 1000, 0);
        List<String> urlsBeforeDelete =
                links == null ? List.of() : links.stream().map(Link::getLink).toList();
        assertThat(urlsBeforeDelete)
                .containsExactlyInAnyOrder("https://github.com/openai/gpt-3", "https://github.com/openai/gpt-4");

        jdbcTemplate.execute("DELETE FROM tracked_link WHERE chat_id = " + chatId);

        List<Link> linksAfterDelete = repository.findAll(chatId, 1000, 0);
        List<String> urls = linksAfterDelete == null
                ? List.of()
                : linksAfterDelete.stream().map(Link::getLink).toList();
        assertThat(urls).isEmpty();
    }
}
