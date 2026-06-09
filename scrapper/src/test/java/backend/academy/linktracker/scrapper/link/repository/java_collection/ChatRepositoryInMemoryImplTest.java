package backend.academy.linktracker.scrapper.link.repository.java_collection;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatRepositoryInMemoryImplTest {

    private ChatRepositoryInMemoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ChatRepositoryInMemoryImpl();
    }

    @Test
    void save_and_isChatExists_shouldWork() {
        Long chatId = 1L;

        repository.save(chatId);

        assertTrue(repository.isChatExists(chatId));
        assertFalse(repository.isChatExists(2L));
    }

    @Test
    void delete_shouldRemoveChat() {
        Long chatId = 1L;

        repository.save(chatId);
        repository.delete(chatId);

        assertFalse(repository.isChatExists(chatId));
    }

    @Test
    void delete_nonExisting_shouldDoNothing() {
        Long chatId = 1L;

        repository.delete(chatId);

        assertFalse(repository.isChatExists(chatId));
    }

    @Test
    void save_duplicate_shouldNotBreak() {
        Long chatId = 1L;

        repository.save(chatId);
        repository.save(chatId);

        assertTrue(repository.isChatExists(chatId));
    }
}
