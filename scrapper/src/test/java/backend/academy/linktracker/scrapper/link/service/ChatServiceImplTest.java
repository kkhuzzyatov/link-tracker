package backend.academy.linktracker.scrapper.link.service;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.link.repository.ChatRepository;
import backend.academy.linktracker.scrapper.link.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatServiceImplTest {

    private ChatRepository chatRepository;
    private LinkRepository linkRepository;
    private ChatServiceImpl service;

    @BeforeEach
    void setUp() {
        chatRepository = mock(ChatRepository.class);
        linkRepository = mock(LinkRepository.class);
        service = new ChatServiceImpl(chatRepository, linkRepository);
    }

    @Test
    void registerChat_shouldCallRepositorySave() {
        Long chatId = 1L;

        service.registerChat(chatId);

        verify(chatRepository).save(chatId);
    }

    @Test
    void isChatExist_shouldDelegateToRepository() {
        Long chatId = 1L;
        when(chatRepository.isChatExists(chatId)).thenReturn(true);

        boolean result = service.isChatExist(chatId);

        assert result;
        verify(chatRepository).isChatExists(chatId);
    }

    @Test
    void deleteChat_shouldDoNothingIfChatNotExists() {
        Long chatId = 1L;
        when(chatRepository.isChatExists(chatId)).thenReturn(false);

        service.deleteChat(chatId);

        verify(chatRepository).isChatExists(chatId);
        verifyNoMoreInteractions(chatRepository);
        verifyNoInteractions(linkRepository);
    }

    @Test
    void deleteChat_shouldDeleteAllLinksAndChat() {
        Long chatId = 1L;

        when(chatRepository.isChatExists(chatId)).thenReturn(true);

        service.deleteChat(chatId);

        verify(linkRepository).deleteAllByChatId(chatId);
        verify(chatRepository).delete(chatId);
    }
}
