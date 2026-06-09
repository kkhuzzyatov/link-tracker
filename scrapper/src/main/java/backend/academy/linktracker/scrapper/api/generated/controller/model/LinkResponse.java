package backend.academy.linktracker.scrapper.api.generated.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.List;

@Schema(description = "Ответ с информацией о ссылке")
public record LinkResponse(
        @Schema(description = "Идентификатор ссылки") Long id,

        @Schema(description = "URL ссылки") URI url,

        @Schema(description = "Список тегов") List<String> tags,

        @Schema(description = "Список фильтров") List<String> filters) {}
