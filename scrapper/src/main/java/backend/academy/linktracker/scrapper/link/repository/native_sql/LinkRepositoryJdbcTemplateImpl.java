package backend.academy.linktracker.scrapper.link.repository.native_sql;

import backend.academy.linktracker.scrapper.infrastructure.aop.LoggableQuery;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.repository.LinkRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class LinkRepositoryJdbcTemplateImpl implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;

    public static final int PAGE_SIZE = 1000;

    @Override
    @Transactional
    @LoggableQuery("save_link")
    public void save(Link link) {
        Long chatId = link.getChatId();

        Long linkId = jdbcTemplate.queryForObject("""
            insert into link(uri)
            values(?)
            on conflict (uri) do update set uri = excluded.uri
            returning link_id
            """, Long.class, link.getLink());

        for (String tag : link.getTags()) {
            Long tagId = jdbcTemplate.queryForObject("""
                insert into tag(tag)
                values(?)
                on conflict (tag) do update set tag = excluded.tag
                returning tag_id
                """, Long.class, tag);

            jdbcTemplate.update("""
                insert into tracked_link(chat_id, link_id, tag_id)
                values(?, ?, ?)
                on conflict do nothing
                """, chatId, linkId, tagId);
        }
    }

    @Override
    @LoggableQuery("check_link_exists")
    public boolean isLinkExist(String link) {
        try {
            jdbcTemplate.queryForObject("select 1 from link where uri=? limit 1", Integer.class, link);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    @LoggableQuery("delete_link")
    public void delete(Long chatId, String uri) {
        jdbcTemplate.update("""
            delete from tracked_link
            where chat_id = ?
              and link_id = (
                  select link_id from link where uri = ?
              )
            """, chatId, uri);
    }

    @Override
    @LoggableQuery("find_all_links")
    public List<Link> findAll(int limit, int offset) {
        int effectiveLimit = Math.min(limit, PAGE_SIZE);

        String sql = """
        select
            l.uri,
            tl.last_updated,
            tl.chat_id
        from tracked_link tl
        join link l on tl.link_id = l.link_id
        order by l.link_id
        limit ? offset ?
        """;

        return jdbcTemplate.query(
                sql, (ResultSetExtractor<List<Link>>) rs -> extractLinks(rs, null), effectiveLimit, offset);
    }

    @Override
    @LoggableQuery("find_links_by_chat")
    public List<Link> findAll(Long chatId, int limit, int offset) {
        int effectiveLimit = Math.min(limit, PAGE_SIZE);

        String sql = """
        select
            l.uri,
            tl.last_updated
        from tracked_link tl
        join link l on tl.link_id = l.link_id
        where tl.chat_id=?
        order by l.link_id
        limit ? offset ?
        """;

        return jdbcTemplate.query(
                sql, (ResultSetExtractor<List<Link>>) rs -> extractLinks(rs, chatId), chatId, effectiveLimit, offset);
    }

    @Override
    @LoggableQuery("find_links_by_chat_and_tag")
    public List<Link> findAll(Long chatId, String tag, int limit, int offset) {
        int effectiveLimit = Math.min(limit, PAGE_SIZE);

        String sql = """
        select
            l.uri,
            tl.last_updated
        from tracked_link tl
        join link l on tl.link_id = l.link_id
        join tag t on tl.tag_id = t.tag_id
        where tl.chat_id=? and t.tag=?
        order by l.link_id
        limit ? offset ?
        """;

        return jdbcTemplate.query(
                sql,
                (ResultSetExtractor<List<Link>>) rs -> extractLinks(rs, chatId),
                chatId,
                tag,
                effectiveLimit,
                offset);
    }

    @Override
    @LoggableQuery("delete_all_links_by_chat")
    public void deleteAllByChatId(Long chatId) {
        jdbcTemplate.update("""
            delete from tracked_link
            where chat_id = ?
            """, chatId);
    }

    @Override
    @LoggableQuery("count_links")
    public Long count() {
        String sql = "select count(*) from link";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Override
    @LoggableQuery("count_links_by_chat")
    public Long countByChatId(Long chatId) {
        String sql = """
        select count(distinct tl.link_id)
        from tracked_link tl
        where tl.chat_id = ?
        """;
        return jdbcTemplate.queryForObject(sql, Long.class, chatId);
    }

    @Override
    @LoggableQuery("update_link_last_updated")
    public void updateLastUpdated(String uri, OffsetDateTime lastUpdated) {
        jdbcTemplate.update(
                "update tracked_link set last_updated = ? where link_id = (select link_id from link where uri = ?)",
                lastUpdated,
                uri);
    }

    private List<Link> extractLinks(ResultSet rs, Long chatIdOverride) throws SQLException {
        Map<String, Link> map = new HashMap<>();

        while (rs.next()) {
            String uri = rs.getString("uri");

            Link link = map.computeIfAbsent(uri, k -> createLink(rs, uri));
            if (link == null) continue; // пропускаем невалидную запись

            Long chatId = chatIdOverride != null ? chatIdOverride : rs.getLong("chat_id");
            link.getTgChatIds().add(chatId);
        }

        return new ArrayList<>(map.values());
    }

    private Link createLink(ResultSet rs, String uri) {
        try {
            return new Link(
                    null,
                    uri,
                    new HashSet<>(),
                    new HashSet<>(),
                    Link.fromLink(uri),
                    rs.getObject("last_updated", OffsetDateTime.class));
        } catch (SQLException e) {
            log.atError()
                    .addKeyValue("event", "map_sql_request_result")
                    .addKeyValue("entity", "link")
                    .log("Ошибка создания Link для URI: {}", uri, e);
            return null;
        }
    }
}
