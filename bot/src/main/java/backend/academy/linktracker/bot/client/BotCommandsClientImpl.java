package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.properties.TelegramProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotCommandsClientImpl implements BotCommandsClient {

    private final RestClient restClient;
    private final TelegramProperties properties;

    @Override
    public void setMyCommands(List<BotCommandDto> commands) {
        String uri = String.format("https://api.telegram.org/bot%s/setMyCommands", properties.getToken());

        SetMyCommandsRequestDto request = new SetMyCommandsRequestDto(commands);

        restClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
        log.atInfo()
                .addKeyValue("event", "setMyCommands")
                .addKeyValue("number_of_commands", request.commands().size())
                .log("В telegram api отправлен запрос на добавление подсказок о доступных командах.");
    }
}
