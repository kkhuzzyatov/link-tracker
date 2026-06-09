package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.link.repository.ChatRepository;
import backend.academy.linktracker.scrapper.link.repository.java_collection.ChatRepositoryInMemoryImpl;
import backend.academy.linktracker.scrapper.link.repository.native_sql.ChatRepositoryJdbcTemplateImpl;
import backend.academy.linktracker.scrapper.link.repository.orm.ChatRepositoryHibernateImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ChatRepositoryConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatRepositoryConfig.class);

    @Value("${access.type}")
    private String accessType;

    @Bean
    public ChatRepository chatRepository(JdbcTemplate jdbcTemplate) {
        String implementationName;
        ChatRepository chatRepository =
                switch (accessType.toUpperCase()) {
                    case "IN_MEMORY" -> {
                        implementationName = ChatRepositoryInMemoryImpl.class.getName();
                        yield new ChatRepositoryInMemoryImpl();
                    }
                    case "ORM" -> {
                        implementationName = ChatRepositoryHibernateImpl.class.getName();
                        yield new ChatRepositoryHibernateImpl();
                    }
                    default -> {
                        implementationName = ChatRepositoryJdbcTemplateImpl.class.getName();
                        yield new ChatRepositoryJdbcTemplateImpl(jdbcTemplate);
                    }
                };
        log.atInfo()
                .addKeyValue("event", "pick_repository_implementation")
                .addKeyValue("entity", "chat")
                .addKeyValue("picked_implementation", implementationName)
                .log("Выбрана реализация репозитория.");
        return chatRepository;
    }
}
