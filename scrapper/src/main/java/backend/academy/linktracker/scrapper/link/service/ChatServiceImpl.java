package backend.academy.linktracker.scrapper.link.service;

import backend.academy.linktracker.scrapper.link.repository.ChatRepository;
import backend.academy.linktracker.scrapper.link.repository.LinkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    @Override
    public void registerChat(Long chatId) {
        chatRepository.save(chatId);
    }

    @Override
    public boolean isChatExist(Long chatId) {
        return chatRepository.isChatExists(chatId);
    }

    @Transactional
    @Override
    public void deleteChat(Long chatId) {
        if (!chatRepository.isChatExists(chatId)) {
            return;
        }

        linkRepository.deleteAllByChatId(chatId);
        chatRepository.delete(chatId);
    }
}
