package backend.academy.linktracker.scrapper.link.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Link {

    @NotNull
    private Long chatId;

    @NotBlank
    private String link;

    @NotNull
    private Set<String> tags;

    @NotNull
    private Set<Long> tgChatIds;

    @NotNull
    private ResourceIdentifier resourceIdentifier;

    @NotNull
    private OffsetDateTime lastUpdateRequestedAt;

    public static ResourceIdentifier fromLink(String link) {
        if (link == null || link.isBlank()) {
            throw new IllegalArgumentException("Ссылка не должна быть пустой");
        }

        if (isGithubRepo(link)) {
            String[] parts = link.replace("https://github.com/", "").split("/");
            return new GitHubRepositoryId(parts[0], parts[1]);
        }

        if (isStackOverflowQuestion(link)) {
            String[] parts =
                    link.replace("https://stackoverflow.com/questions/", "").split("/");
            Long questionId = Long.parseLong(parts[0]);
            return new StackOverflowQuestionId(questionId);
        }

        throw new IllegalArgumentException("Ссылка должна быть на GitHub репозиторий или вопрос StackOverflow");
    }

    private static boolean isGithubRepo(String url) {
        return url.matches("^https://github\\.com/[^/]+/[^/]+/?$");
    }

    private static boolean isStackOverflowQuestion(String url) {
        return url.matches("^https://stackoverflow\\.com/questions/\\d+/.*$");
    }
}
