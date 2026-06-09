package backend.academy.linktracker.scrapper.link.service;

public interface ChatService {
    void registerChat(Long chatId);

    boolean isChatExist(Long chatId);

    void deleteChat(Long chatId);
}
