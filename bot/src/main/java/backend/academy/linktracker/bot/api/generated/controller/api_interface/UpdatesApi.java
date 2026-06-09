package backend.academy.linktracker.bot.api.generated.controller.api_interface;

import backend.academy.linktracker.bot.api.generated.controller.model.ApiErrorResponse;
import backend.academy.linktracker.bot.api.generated.controller.model.LinkUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Tag(name = "updates", description = "API для обработки обновлений")
@RequestMapping("/updates")
public interface UpdatesApi {

    @Operation(summary = "Отправить обновление")
    @ApiResponse(responseCode = "200", description = "Обновление обработано")
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updatesPost(@Valid @RequestBody List<LinkUpdate> linkUpdates);
}
