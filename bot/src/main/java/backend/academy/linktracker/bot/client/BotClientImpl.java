package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.properties.TelegramProperties;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotClientImpl implements BotClient {

    private static final int TELEGRAM_MAX_LENGTH = 4096;
    private static final int RETRY_COUNT = 2;

    private final RestClient restClient;
    private final TelegramProperties telegramProperties;

    @Override
    public void sendMessage(Long chatId, String text) {
        if (chatId == null) {
            throw new IllegalArgumentException("chatId не может быть null");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Текст сообщения не может быть пустым");
        }

        // Разбиваем длинные сообщения на части
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + TELEGRAM_MAX_LENGTH, text.length());
            String chunk = text.substring(start, end);
            start = end;
            sendMessageAsync(chatId, chunk);
        }
    }

    private void sendMessageAsync(Long chatId, String textChunk) {
        CompletableFuture.runAsync(() -> {
            int attempt = 0;

            while (attempt <= RETRY_COUNT) {
                attempt++;
                try {
                    execute(chatId, textChunk);
                    break; // успех, выходим из retry
                } catch (BotClientException ex) {
                    try {
                        Thread.sleep(500); // простая задержка между попытками
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    /**
     * Внутренний метод для единой обработки вызовов Telegram API
     */
    private void execute(Long chatId, String text) {
        try {
            // Auto-escaping для Telegram
            String escapedText = escapeTelegram(text);

            restClient
                    .post()
                    .uri(String.format("%s%s/sendMessage", telegramProperties.getUrl(), telegramProperties.getToken()))
                    .body(new SendMessageRequest(chatId, escapedText, "HTML"))
                    .retrieve()
                    .toBodilessEntity();

            log.atInfo()
                    .addKeyValue("event", "send_http_request")
                    .addKeyValue("entity", "message")
                    .addKeyValue("method", "post")
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("text", escapedText)
                    .log("В telegram api отправлено сообщение для отправки его пользователю.");
        } catch (RestClientException ex) {
            boolean recoverable = isRecoverable(ex);
            throw new BotClientException("Ошибка при отправке сообщения", ex, recoverable);
        }
    }

    private boolean isRecoverable(RestClientException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "";
        return message.contains("429") || message.contains("Timeout");
    }

    private String escapeTelegram(String text) {
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("`", "\\`");
    }
}
