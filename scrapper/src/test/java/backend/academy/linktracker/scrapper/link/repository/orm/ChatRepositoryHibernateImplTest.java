package backend.academy.linktracker.scrapper.link.repository.orm;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ChatRepositoryHibernateImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    private static EntityManagerFactory emf;
    private EntityManager entityManager;
    private ChatRepositoryHibernateImpl repository;

    @BeforeAll
    static void init() {
        postgres.start();

        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
        props.put("jakarta.persistence.jdbc.user", postgres.getUsername());
        props.put("jakarta.persistence.jdbc.password", postgres.getPassword());
        props.put("jakarta.persistence.jdbc.driver", postgres.getDriverClassName());

        emf = Persistence.createEntityManagerFactory("test", props);
    }

    @AfterAll
    static void close() {
        emf.close();
    }

    @BeforeEach
    void setUp() {
        entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();

        repository = new ChatRepositoryHibernateImpl();

        // вручную инжектим EntityManager (так как нет Spring)
        try {
            var field = ChatRepositoryHibernateImpl.class.getDeclaredField("entityManager");
            field.setAccessible(true);
            field.set(repository, entityManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        entityManager.getTransaction().rollback();
        entityManager.close();
    }

    @Test
    void save_shouldInsertChat() {
        Long chatId = 1L;

        repository.save(chatId);

        boolean exists = repository.isChatExists(chatId);

        assertThat(exists).isTrue();
    }

    @Test
    void isChatExists_shouldReturnFalse() {
        boolean exists = repository.isChatExists(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void delete_shouldRemoveChat() {
        Long chatId = 1L;

        repository.save(chatId);
        repository.delete(chatId);

        boolean exists = repository.isChatExists(chatId);

        assertThat(exists).isFalse();
    }
}
