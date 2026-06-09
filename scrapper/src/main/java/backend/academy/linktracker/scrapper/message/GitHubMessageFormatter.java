package backend.academy.linktracker.scrapper.message;

import backend.academy.linktracker.scrapper.dto.GitHubIssueDto;
import backend.academy.linktracker.scrapper.dto.GitHubPullRequestDto;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class GitHubMessageFormatter {

    public String formatPR(GitHubPullRequestDto gitHubPullRequestDto, String owner, String repository) {
        return format(
                "📩 Новый pull request в репозитории",
                gitHubPullRequestDto.getTitle(),
                gitHubPullRequestDto.getUser().getLogin(),
                gitHubPullRequestDto.getCreatedAt(),
                "💬 Pull request (превью):",
                gitHubPullRequestDto.getBody(),
                owner,
                repository);
    }

    public String formatIssue(GitHubIssueDto gitHubIssueDto, String owner, String repository) {
        return format(
                "📩 Новый issue в репозитории",
                gitHubIssueDto.getTitle(),
                gitHubIssueDto.getUser().getLogin(),
                gitHubIssueDto.getCreatedAt(),
                "💬 Issue (превью):",
                gitHubIssueDto.getBody(),
                owner,
                repository);
    }

    private String format(
            String header,
            String title,
            String user,
            OffsetDateTime creationDate,
            String rawBody,
            String previewLabel,
            String owner,
            String repository) {
        /*
        Сообщение об изменениях содержит:
        • название Issue
        • имя пользователя
        • время создания
        • превью описания (первые 200 символов)
        */
        String safeTitle = HtmlUtils.htmlEscape(title);
        String safeUser = HtmlUtils.htmlEscape(user);

        return String.format(
                "<b>%s</b>%n%n<b>👤 Владелец репозитория:</b>%n%s%n%n<b>📦 Название репозитория:</b>%n%s%n%n<b>📝 Название:</b>%n%s%n%n<b>👤 Пользователь:</b>%n%s%n%n<b>⏱ Время(UTC):</b>%n%s%n%n%s%n%s",
                header, owner, repository, safeTitle, safeUser, creationDate, rawBody, previewLabel);
    }
}
