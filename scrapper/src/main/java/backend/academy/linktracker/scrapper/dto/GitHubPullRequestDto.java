package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GitHubPullRequestDto {
    private String title;

    private User user;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    private String body;

    @AllArgsConstructor
    @Getter
    public static class User {
        private String login;
    }
}
