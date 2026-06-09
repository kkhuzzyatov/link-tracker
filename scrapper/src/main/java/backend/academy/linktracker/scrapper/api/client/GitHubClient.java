package backend.academy.linktracker.scrapper.api.client;

import backend.academy.linktracker.scrapper.dto.GitHubIssueDto;
import backend.academy.linktracker.scrapper.dto.GitHubPullRequestDto;
import backend.academy.linktracker.scrapper.link.model.GitHubRepositoryId;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.model.ResourceIdentifier;
import backend.academy.linktracker.scrapper.message.GitHubMessageFormatter;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;

@RequiredArgsConstructor
@Slf4j
@Component
public class GitHubClient implements LinkUpdateClient {

    private final RestClient restClient;
    private final GithubProperties githubProperties;
    private final GitHubMessageFormatter messageFormatter;

    @Override
    public boolean supports(ResourceIdentifier identifier) {
        return identifier instanceof GitHubRepositoryId;
    }

    @Override
    public List<String> getNewLinkChanges(Link link) {
        String repository = String.format(
                "%s/%s",
                extractRepositoryId(link).owner(), extractRepositoryId(link).repository());

        List<String> pullRequests = fetchPRs(link, repository);
        List<String> issues = fetchIssues(link, repository);

        return Stream.concat(pullRequests.stream(), issues.stream()).toList();
    }

    private List<String> fetchPRs(Link link, String repository) {
        /*
        Сообщение об изменениях содержит:
        • название PR
        • имя пользователя
        • время создания
        • превью описания (первые 200 символов)
        */
        GitHubRepositoryId repositoryId = extractRepositoryId(link);

        String uri = String.format(
                "https://api.github.com/repos/%s/%s/pulls", repositoryId.owner(), repositoryId.repository());

        RequestHeadersSpec<?> request = restClient.get().uri(uri);
        String token = githubProperties.getToken();
        if (token != null && !token.isBlank()) {
            request = request.header("Authorization", String.format("Bearer %s", token));
        }

        ResponseEntity<List<GitHubPullRequestDto>> response;
        try {
            response = request.exchange((req, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode())
                    .body(clientResponse.bodyTo(new ParameterizedTypeReference<List<GitHubPullRequestDto>>() {})));
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("event", "send_http_request")
                    .addKeyValue("server_name", "github_api")
                    .addKeyValue("entity", "list_of_pull_requests")
                    .addKeyValue("method", "get")
                    .addKeyValue("repository", repository)
                    .addKeyValue("error", e.getMessage())
                    .log("Ошибка при выполнении запроса к GitHub API (pulls)");
            return List.of();
        }

        List<GitHubPullRequestDto> pullRequests = response.getBody();
        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "github_api")
                .addKeyValue("entity", "list_of_pull_requests")
                .addKeyValue("method", "get")
                .addKeyValue("repository", repository)
                .addKeyValue("number_of_pull_requests", pullRequests == null ? 0 : pullRequests.size())
                .log("Получены pull requests");

        if (pullRequests == null || pullRequests.isEmpty()) return List.of();

        OffsetDateTime lastUpdate = link.getLastUpdateRequestedAt();
        if (lastUpdate == null) return List.of();

        return pullRequests.stream()
                .filter(item ->
                        item.getCreatedAt() != null && item.getCreatedAt().isAfter(lastUpdate))
                .map(item -> messageFormatter.formatPR(item, repositoryId.owner(), repositoryId.repository()))
                .toList();
    }

    private List<String> fetchIssues(Link link, String repository) {
        /*
        Сообщение об изменениях содержит:
        • название Issue
        • имя пользователя
        • время создания
        • превью описания (первые 200 символов)
        */
        GitHubRepositoryId repositoryId = extractRepositoryId(link);

        String uri = String.format(
                "https://api.github.com/repos/%s/%s/issues", repositoryId.owner(), repositoryId.repository());

        RequestHeadersSpec<?> request = restClient.get().uri(uri);
        String token = githubProperties.getToken();
        if (token != null && !token.isBlank()) {
            request = request.header("Authorization", String.format("Bearer %s", token));
        }

        ResponseEntity<List<GitHubIssueDto>> response;
        try {
            response = request.exchange((req, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode())
                    .body(clientResponse.bodyTo(new ParameterizedTypeReference<List<GitHubIssueDto>>() {})));
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("event", "send_http_request")
                    .addKeyValue("server_name", "github_api")
                    .addKeyValue("entity", "list_of_issues")
                    .addKeyValue("method", "get")
                    .addKeyValue("repository", repository)
                    .addKeyValue("error", e.getMessage())
                    .log("Ошибка при выполнении запроса к GitHub API (issues)");
            return List.of();
        }

        List<GitHubIssueDto> pullRequests = response.getBody();
        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "github_api")
                .addKeyValue("entity", "list_of_issues")
                .addKeyValue("method", "get")
                .addKeyValue("repository", repository)
                .addKeyValue("number_of_issues", pullRequests == null ? 0 : pullRequests.size())
                .log("Получены issues");

        if (pullRequests == null || pullRequests.isEmpty()) return List.of();

        OffsetDateTime lastUpdate = link.getLastUpdateRequestedAt();
        if (lastUpdate == null) return List.of();

        return pullRequests.stream()
                .filter(item ->
                        item.getCreatedAt() != null && item.getCreatedAt().isAfter(lastUpdate))
                .map(item -> messageFormatter.formatIssue(item, repositoryId.owner(), repositoryId.repository()))
                .toList();
    }

    private GitHubRepositoryId extractRepositoryId(Link link) {
        return (GitHubRepositoryId) link.getResourceIdentifier();
    }
}
