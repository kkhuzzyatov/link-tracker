package backend.academy.linktracker.scrapper.link.repository;

public interface ChatRepository {
    void save(Long chatId);

    boolean isChatExists(Long chatId);

    void delete(Long chatId);
}
