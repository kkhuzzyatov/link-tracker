package backend.academy.linktracker.scrapper.api.controller;

import backend.academy.linktracker.scrapper.link.service.ChatService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TgChatControllerTestConfig {

    @Bean
    public ChatService chatService() {
        return Mockito.mock(ChatService.class);
    }
}
