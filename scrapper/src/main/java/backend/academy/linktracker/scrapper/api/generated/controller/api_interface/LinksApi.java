package backend.academy.linktracker.scrapper.api.generated.controller.api_interface;

import backend.academy.linktracker.scrapper.api.generated.controller.model.AddLinkRequest;
import backend.academy.linktracker.scrapper.api.generated.controller.model.LinkResponse;
import backend.academy.linktracker.scrapper.api.generated.controller.model.ListLinksResponse;
import backend.academy.linktracker.scrapper.api.generated.controller.model.RemoveLinkRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@Tag(name = "links", description = "Управление отслеживаемыми ссылками")
@RequestMapping("/links")
public interface LinksApi {

    @Operation(summary = "Получить все отслеживаемые ссылки")
    @ApiResponse(responseCode = "200", description = "Ссылки успешно получены")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @ApiResponse(responseCode = "404", description = "Чат не существует")
    @GetMapping
    ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") @NotNull Long tgChatId);

    @Operation(summary = "Получить отслеживаемые ссылки по тегу")
    @ApiResponse(responseCode = "200", description = "Ссылки успешно получены")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @ApiResponse(responseCode = "404", description = "Чат не существует")
    @GetMapping(params = "tag")
    ResponseEntity<ListLinksResponse> getLinksByTag(
            @RequestHeader("Tg-Chat-Id") @NotNull Long tgChatId, @NotNull @RequestParam("tag") String tag);

    @Operation(summary = "Добавить отслеживание ссылки")
    @ApiResponse(responseCode = "200", description = "Ссылка успешно добавлена")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @ApiResponse(responseCode = "404", description = "Чат не существует")
    @ApiResponse(responseCode = "409", description = "Ссылка уже отслеживается")
    @PostMapping
    ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") @NotNull Long tgChatId, @Valid @RequestBody AddLinkRequest request);

    @Operation(summary = "Убрать отслеживание ссылки")
    @ApiResponse(responseCode = "200", description = "Ссылка успешно удалена")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @ApiResponse(responseCode = "404", description = "Чат или ссылка не найдены")
    @DeleteMapping
    ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") @NotNull Long tgChatId, @Valid @RequestBody RemoveLinkRequest request);
}
