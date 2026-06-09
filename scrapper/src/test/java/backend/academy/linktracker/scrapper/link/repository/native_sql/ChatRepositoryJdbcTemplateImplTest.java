package backend.academy.linktracker.scrapper.link.repository.native_sql;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ChatRepositoryJdbcTemplateImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    private ChatRepositoryJdbcTemplateImpl repository;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new org.springframework.jdbc.datasource.DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));

        repository = new ChatRepositoryJdbcTemplateImpl(jdbcTemplate);

        // создаём таблицу
        jdbcTemplate.execute("""
            create table if not exists chat (
                chat_id bigint primary key
            )
        """);
    }

    @Test
    void save_shouldInsertChat() {
        Long chatId = 1L;

        repository.save(chatId);

        boolean exists = repository.isChatExists(chatId);

        assertThat(exists).isTrue();
    }

    @Test
    void isChatExists_shouldReturnFalse_whenNotExists() {
        boolean exists = repository.isChatExists(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void delete_shouldRemoveChat() {
        Long chatId = 1L;

        repository.save(chatId);
        repository.delete(chatId);

        boolean exists = repository.isChatExists(chatId);

        assertThat(exists).isFalse();
    }
}
