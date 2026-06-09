package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfiguration {

    @Bean
    public TelegramBot telegramBot(TelegramProperties properties) {
        return new TelegramBot(properties.getToken());
    }
}
