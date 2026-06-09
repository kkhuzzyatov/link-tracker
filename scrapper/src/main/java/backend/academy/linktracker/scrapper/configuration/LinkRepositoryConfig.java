package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.link.repository.LinkRepository;
import backend.academy.linktracker.scrapper.link.repository.java_collection.LinkRepositoryInMemoryImpl;
import backend.academy.linktracker.scrapper.link.repository.native_sql.LinkRepositoryJdbcTemplateImpl;
import backend.academy.linktracker.scrapper.link.repository.orm.LinkRepositoryHibernateImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class LinkRepositoryConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LinkRepositoryConfig.class);

    @Value("${access.type}")
    private String accessType;

    @Bean
    public LinkRepository linkRepository(JdbcTemplate jdbcTemplate) {
        String implementationName;
        LinkRepository linkRepository =
                switch (accessType.toUpperCase()) {
                    case "IN_MEMORY" -> {
                        implementationName = LinkRepositoryInMemoryImpl.class.getName();
                        yield new LinkRepositoryInMemoryImpl();
                    }
                    case "ORM" -> {
                        implementationName = LinkRepositoryHibernateImpl.class.getName();
                        yield new LinkRepositoryHibernateImpl();
                    }
                    default -> {
                        implementationName = LinkRepositoryJdbcTemplateImpl.class.getName();
                        yield new LinkRepositoryJdbcTemplateImpl(jdbcTemplate);
                    }
                };
        log.atInfo()
                .addKeyValue("event", "pick_repository_implementation")
                .addKeyValue("entity", "link")
                .addKeyValue("picked_implementation", implementationName)
                .log("Выбрана реализация репозитория.");
        return linkRepository;
    }
}
