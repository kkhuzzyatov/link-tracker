package backend.academy.linktracker.scrapper.link.repository.orm;

import backend.academy.linktracker.scrapper.infrastructure.aop.LoggableQuery;
import backend.academy.linktracker.scrapper.link.repository.ChatRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
public class ChatRepositoryHibernateImpl implements ChatRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @LoggableQuery("save_chat")
    public void save(Long chatId) {
        if (!isChatExists(chatId)) {
            ChatEntity chat = new ChatEntity();
            chat.setChatId(chatId);
            entityManager.persist(chat);
        }
    }

    @Override
    @LoggableQuery("check_chat_exists")
    public boolean isChatExists(Long chatId) {
        return entityManager.find(ChatEntity.class, chatId) != null;
    }

    @Override
    @LoggableQuery("delete_chat")
    public void delete(Long chatId) {
        ChatEntity chat = entityManager.find(ChatEntity.class, chatId);
        if (chat != null) {
            entityManager.remove(chat);
        }
    }
}
