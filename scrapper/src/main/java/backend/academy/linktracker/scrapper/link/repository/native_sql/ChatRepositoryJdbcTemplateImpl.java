package backend.academy.linktracker.scrapper.link.repository.native_sql;

import backend.academy.linktracker.scrapper.infrastructure.aop.LoggableQuery;
import backend.academy.linktracker.scrapper.link.repository.ChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@AllArgsConstructor
public class ChatRepositoryJdbcTemplateImpl implements ChatRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @LoggableQuery("save_chat")
    public void save(Long chatId) {
        String sql = "insert into chat (chat_id) values (?) on conflict (chat_id) do nothing";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    @LoggableQuery("check_chat_exists")
    public boolean isChatExists(Long chatId) {
        String sql = "select chat_id from chat where chat_id = ?";
        try {
            jdbcTemplate.queryForObject(sql, Long.class, chatId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    @LoggableQuery("check_chat_exists")
    public void delete(Long chatId) {
        String sql = "delete from chat where chat_id = ?";
        jdbcTemplate.update(sql, chatId);
    }
}
