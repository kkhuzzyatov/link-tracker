package backend.academy.linktracker.scrapper.api.generated.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.net.URI;

@Schema(description = "Запрос на удаление отслеживаемой ссылки")
public record RemoveLinkRequest(
        @Schema(description = "URL ссылки для удаления") @NotNull
        URI link) {}
