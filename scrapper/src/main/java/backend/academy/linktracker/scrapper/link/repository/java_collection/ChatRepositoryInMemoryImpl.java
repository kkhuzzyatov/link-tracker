package backend.academy.linktracker.scrapper.link.repository.java_collection;

import backend.academy.linktracker.scrapper.infrastructure.aop.LoggableQuery;
import backend.academy.linktracker.scrapper.link.repository.ChatRepository;
import java.util.HashSet;
import java.util.Set;

public class ChatRepositoryInMemoryImpl implements ChatRepository {
    private final Set<Long> chatIds = new HashSet<>();

    @Override
    @LoggableQuery("save_chat")
    public void save(Long chatId) {
        chatIds.add(chatId);
    }

    @Override
    @LoggableQuery("check_chat_exists")
    public boolean isChatExists(Long chatId) {
        return chatIds.contains(chatId);
    }

    @Override
    @LoggableQuery("delete_chat")
    public void delete(Long chatId) {
        chatIds.remove(chatId);
    }
}
