package backend.academy.linktracker.scrapper.api.generated.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@Schema(description = "Запрос на добавление отслеживания ссылки")
public record AddLinkRequest(
        @Schema(description = "URL ссылки для отслеживания") @NotNull
        URI link,

        @Schema(description = "Список тегов") List<String> tags,

        @Schema(description = "Список фильтров") List<String> filters) {}
