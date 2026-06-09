package backend.academy.linktracker.scrapper.api.client;

import backend.academy.linktracker.scrapper.dto.StackOverflowAnswersDto;
import backend.academy.linktracker.scrapper.dto.StackOverflowCommentsDto;
import backend.academy.linktracker.scrapper.dto.StackOverflowQuestionDto;
import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.model.ResourceIdentifier;
import backend.academy.linktracker.scrapper.link.model.StackOverflowQuestionId;
import backend.academy.linktracker.scrapper.message.StackOverFlowMessageFormatter;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;

@RequiredArgsConstructor
@Slf4j
@Component
public class StackOverflowClient implements LinkUpdateClient {

    private final RestClient restClient;
    private final StackoverflowProperties stackoverflowProperties;
    private final StackOverFlowMessageFormatter stackOverFlowMessageFormatter;

    @Override
    public boolean supports(ResourceIdentifier identifier) {
        return identifier instanceof StackOverflowQuestionId;
    }

    @Override
    public List<String> getNewLinkChanges(Link link) {
        StackOverflowQuestionId questionId = extractQuestionId(link);
        String title = getQuestionTitle(questionId);
        if (title == null) {
            log.atWarn()
                    .addKeyValue("event", "send_http_request")
                    .addKeyValue("server_name", "stack_exchange_api")
                    .addKeyValue("method", "get")
                    .addKeyValue("question_id", questionId.questionId())
                    .log("Процесс получения данных прерван из-за ошибки получения текста темы вопроса");
            return List.of();
        }

        List<String> answers = fetchAnswers(link, title);
        List<String> comments = fetchComments(link, title);

        return Stream.concat(answers.stream(), comments.stream()).toList();
    }

    private List<String> fetchAnswers(Link link, String title) {
        /*
        Сообщение об изменениях содержит:
        • текст темы вопроса
        • имя пользователя
        • время создания
        • превью ответа или комментария (первые 200 символов)
        */
        StackOverflowQuestionId questionId = extractQuestionId(link);

        String uri = String.format(
                "https://api.stackexchange.com/2.3/questions/%s/answers?site=stackoverflow&filter=withbody&key=%s",
                questionId.questionId(), stackoverflowProperties.getKey());

        RequestHeadersSpec<?> request = restClient.get().uri(uri);

        ResponseEntity<StackOverflowAnswersDto> response;
        try {
            response = request.exchange((req, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode())
                    .body(clientResponse.bodyTo(StackOverflowAnswersDto.class)));
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("event", "send_http_request")
                    .addKeyValue("server_name", "stack_exchange_api")
                    .addKeyValue("entity", "list_of_answers")
                    .addKeyValue("method", "get")
                    .addKeyValue("question_id", questionId.questionId())
                    .addKeyValue("error", e.getMessage())
                    .log("Ошибка при выполнении запроса к StackOverflow API (answers)");
            return List.of();
        }

        StackOverflowAnswersDto answersDto = response.getBody();
        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "stack_exchange_api")
                .addKeyValue("entity", "list_of_answers")
                .addKeyValue("method", "get")
                .addKeyValue("question_id", questionId.questionId())
                .addKeyValue(
                        "number_of_answers_in_question",
                        answersDto == null ? 0 : answersDto.getItems().size())
                .log("Получены ответы на вопрос StackOverflow");

        if (answersDto == null || answersDto.getItems().isEmpty()) return List.of();

        OffsetDateTime lastUpdate = link.getLastUpdateRequestedAt();
        if (lastUpdate == null) return List.of();

        return answersDto.getItems().stream()
                .filter(item -> item.getCreationDate() != null
                        && Instant.ofEpochSecond(item.getCreationDate())
                                .atOffset(ZoneOffset.UTC)
                                .isAfter(lastUpdate))
                .map(item -> stackOverFlowMessageFormatter.formatAnswer(item, title))
                .toList();
    }

    private List<String> fetchComments(Link link, String title) {
        StackOverflowQuestionId questionId = extractQuestionId(link);
        String uri = String.format(
                "https://api.stackexchange.com/2.3/questions/%s/comments?site=stackoverflow&filter=withbody&key=%s",
                questionId.questionId(), stackoverflowProperties.getKey());

        RequestHeadersSpec<?> request = restClient.get().uri(uri);

        ResponseEntity<StackOverflowCommentsDto> response;
        try {
            response = request.exchange((req, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode())
                    .body(clientResponse.bodyTo(StackOverflowCommentsDto.class)));
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("event", "send_http_request")
                    .addKeyValue("server_name", "stack_exchange_api")
                    .addKeyValue("entity", "list_of_comments")
                    .addKeyValue("method", "get")
                    .addKeyValue("question_id", questionId.questionId())
                    .addKeyValue("error", e.getMessage())
                    .log("Ошибка при выполнении запроса к StackOverflow API (comments)");
            return List.of();
        }

        StackOverflowCommentsDto commentsDto = response.getBody();
        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "stack_exchange_api")
                .addKeyValue("entity", "list_of_comments")
                .addKeyValue("method", "get")
                .addKeyValue("question_id", questionId.questionId())
                .addKeyValue(
                        "number_of_comments_in_question",
                        commentsDto == null ? 0 : commentsDto.getItems().size())
                .log("Получены комментарии на вопрос StackOverflow");

        if (commentsDto == null || commentsDto.getItems().isEmpty()) return List.of();

        OffsetDateTime lastUpdate = link.getLastUpdateRequestedAt();
        if (lastUpdate == null) return List.of();

        return commentsDto.getItems().stream()
                .filter(item -> item.getCreationDate() != null
                        && Instant.ofEpochSecond(item.getCreationDate())
                                .atOffset(ZoneOffset.UTC)
                                .isAfter(lastUpdate))
                .map(item -> stackOverFlowMessageFormatter.formatComment(item, title))
                .toList();
    }

    private String getQuestionTitle(StackOverflowQuestionId questionId) {
        String uri = String.format(
                "https://api.stackexchange.com/2.3/questions/%s?site=stackoverflow",
                questionId.questionId(), stackoverflowProperties.getKey());

        RequestHeadersSpec<?> request = restClient.get().uri(uri);

        ResponseEntity<StackOverflowQuestionDto> response;
        try {
            response = request.exchange((req, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode())
                    .body(clientResponse.bodyTo(StackOverflowQuestionDto.class)));
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("event", "send_http_request")
                    .addKeyValue("server_name", "stack_exchange_api")
                    .addKeyValue("entity", "question")
                    .addKeyValue("method", "get")
                    .addKeyValue("question_id", questionId.questionId())
                    .addKeyValue("error", e.getMessage())
                    .log("Ошибка при выполнении запроса к StackOverflow API (question)");
            return null;
        }

        StackOverflowQuestionDto questionDto = response.getBody();
        log.atInfo()
                .addKeyValue("event", "send_http_request")
                .addKeyValue("server_name", "stack_exchange_api")
                .addKeyValue("entity", "question")
                .addKeyValue("method", "get")
                .addKeyValue("question_id", questionId.questionId())
                .log("Получены данные вопроса StackOverflow");

        return questionDto.getItems().get(0).getTitle();
    }

    private StackOverflowQuestionId extractQuestionId(Link link) {
        return (StackOverflowQuestionId) link.getResourceIdentifier();
    }
}
