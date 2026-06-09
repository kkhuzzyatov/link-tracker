package backend.academy.linktracker.scrapper.link.repository.orm;

import backend.academy.linktracker.scrapper.infrastructure.aop.LoggableQuery;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.repository.LinkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import lombok.Setter;

@Setter
public class LinkRepositoryHibernateImpl implements LinkRepository {
    public static final int PAGE_SIZE = 1000;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    @LoggableQuery("save_link")
    public void save(Link link) {
        Long chatId = link.getChatId();

        Long linkId = (Long)
                em.createNativeQuery("""
            insert into link(uri)
            values(:uri)
            on conflict (uri) do update set uri = excluded.uri
            returning link_id
        """).setParameter("uri", link.getLink()).getSingleResult();

        for (String tag : link.getTags()) {
            Long tagId =
                    (Long) em.createNativeQuery("""
                insert into tag(tag)
                values(:tag)
                on conflict (tag) do update set tag = excluded.tag
                returning tag_id
            """).setParameter("tag", tag).getSingleResult();

            em.createNativeQuery("""
                insert into tracked_link(chat_id, link_id, tag_id)
                values(:chatId, :linkId, :tagId)
                on conflict do nothing
            """)
                    .setParameter("chatId", chatId)
                    .setParameter("linkId", linkId)
                    .setParameter("tagId", tagId)
                    .executeUpdate();
        }
    }

    @Override
    @LoggableQuery("check_link_exists")
    public boolean isLinkExist(String link) {
        List<?> result = em.createNativeQuery("""
            select 1 from link where uri = :uri limit 1
        """).setParameter("uri", link).getResultList();

        return !result.isEmpty();
    }

    @Override
    @Transactional
    @LoggableQuery("delete_link")
    public void delete(Long chatId, String uri) {
        em.createNativeQuery("""
            delete from tracked_link
            where chat_id = :chatId
              and link_id = (
                  select link_id from link where uri = :uri
              )
        """)
                .setParameter("chatId", chatId)
                .setParameter("uri", uri)
                .executeUpdate();
    }

    @Override
    @LoggableQuery("find_all_links")
    public List<Link> findAll(int limit, int offset) {
        int effectiveLimit = Math.min(limit, PAGE_SIZE);

        List<Object[]> rows = em.createNativeQuery("""
            select
                l.uri,
                tl.last_updated,
                tl.chat_id
            from tracked_link tl
            join link l on tl.link_id = l.link_id
            order by l.link_id
            limit :limit offset :offset
        """)
                .setParameter("limit", effectiveLimit)
                .setParameter("offset", offset)
                .getResultList();

        return mapRows(rows, null);
    }

    @Override
    @LoggableQuery("find_links_by_chat")
    public List<Link> findAll(Long chatId, int limit, int offset) {
        int effectiveLimit = Math.min(limit, PAGE_SIZE);

        List<Object[]> rows = em.createNativeQuery("""
            select
                l.uri,
                tl.last_updated
            from tracked_link tl
            join link l on tl.link_id = l.link_id
            where tl.chat_id = :chatId
            order by l.link_id
            limit :limit offset :offset
        """)
                .setParameter("chatId", chatId)
                .setParameter("limit", effectiveLimit)
                .setParameter("offset", offset)
                .getResultList();

        return mapRows(rows, chatId);
    }

    @Override
    @LoggableQuery("find_links_by_chat_and_tag")
    public List<Link> findAll(Long chatId, String tag, int limit, int offset) {
        int effectiveLimit = Math.min(limit, PAGE_SIZE);

        List<Object[]> rows = em.createNativeQuery("""
            select
                l.uri,
                tl.last_updated
            from tracked_link tl
            join link l on tl.link_id = l.link_id
            join tag t on tl.tag_id = t.tag_id
            where tl.chat_id = :chatId and t.tag = :tag
            order by l.link_id
            limit :limit offset :offset
        """)
                .setParameter("chatId", chatId)
                .setParameter("tag", tag)
                .setParameter("limit", effectiveLimit)
                .setParameter("offset", offset)
                .getResultList();

        return mapRows(rows, chatId);
    }

    @Override
    @LoggableQuery("delete_all_links_by_chat")
    public void deleteAllByChatId(Long chatId) {
        em.createNativeQuery("""
            delete from tracked_link
            where chat_id = :chatId
        """).setParameter("chatId", chatId).executeUpdate();
    }

    @Override
    @LoggableQuery("count_links")
    public Long count() {
        Number result = (Number) em.createNativeQuery("""
            select count(distinct link_id) from tracked_link
        """).getSingleResult();
        return result.longValue();
    }

    @Override
    @LoggableQuery("count_links_by_chat")
    public Long countByChatId(Long chatId) {
        Number result = (Number)
                em.createNativeQuery("""
            select count(distinct link_id) from tracked_link where chat_id = :chatId
        """).setParameter("chatId", chatId).getSingleResult();
        return result.longValue();
    }

    @Override
    @LoggableQuery("update_link_last_updated")
    public void updateLastUpdated(String uri, OffsetDateTime lastUpdated) {
        em.createNativeQuery("""
            update tracked_link
            set last_updated = :lastUpdated
            where link_id = (select link_id from link where uri = :uri)
        """)
                .setParameter("lastUpdated", lastUpdated)
                .setParameter("uri", uri)
                .executeUpdate();
    }

    private List<Link> mapRows(List<Object[]> rows, Long chatIdOverride) {
        java.util.Map<String, Link> map = new java.util.HashMap<>();

        for (Object[] row : rows) {
            String uri = (String) row[0];
            Instant instant = (Instant) row[1];
            OffsetDateTime lastUpdated = instant.atOffset(ZoneOffset.UTC);

            Link link = map.computeIfAbsent(
                    uri, k -> new Link(null, uri, new HashSet<>(), new HashSet<>(), Link.fromLink(uri), lastUpdated));

            Long chatId =
                    chatIdOverride != null ? chatIdOverride : row.length > 2 ? ((Number) row[2]).longValue() : null;
            if (chatId != null) {
                link.getTgChatIds().add(chatId);
            }
        }

        return new java.util.ArrayList<>(map.values());
    }
}
