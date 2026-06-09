package backend.academy.linktracker.scrapper.message;

import backend.academy.linktracker.scrapper.dto.StackOverflowAnswersDto;
import backend.academy.linktracker.scrapper.dto.StackOverflowCommentsDto;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class StackOverFlowMessageFormatter {

    public String formatAnswer(StackOverflowAnswersDto.Item item, String title) {
        return format(
                "📩 Новый ответ на вопрос",
                title,
                item.getOwner().getName(),
                item.getCreationDate(),
                item.getBody(),
                "💬 Ответ (превью):");
    }

    public String formatComment(StackOverflowCommentsDto.Item item, String title) {
        return format(
                "📩 Новый комментарий на вопрос",
                title,
                item.getOwner().getName(),
                item.getCreationDate(),
                item.getBody(),
                "💬 Комментарий (превью):");
    }

    private String format(
            String header, String title, String user, long creationDate, String rawBody, String previewLabel) {
        String safeTitle = HtmlUtils.htmlEscape(title);
        String safeUser = HtmlUtils.htmlEscape(user);

        String preview = buildPreview(rawBody);

        return String.format(
                "<b>%s</b>%n%n<b>📝 Тема:</b>%n%s%n%n<b>👤 Пользователь:</b>%n%s%n%n<b>⏱ Время(UTC):</b>%n%s%n%n<b>%s</b>%n%s",
                header, safeTitle, safeUser, toReadableTime(creationDate), previewLabel, preview);
    }

    private String buildPreview(String html) {
        if (html == null) return "";

        String plain = html.replaceAll("<[^>]*>", "");

        String escaped = HtmlUtils.htmlEscape(plain);

        return escaped.substring(0, Math.min(escaped.length(), 200));
    }

    private String toReadableTime(long seconds) {

        return Instant.ofEpochSecond(seconds)
                .atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
