package backend.academy.linktracker.bot.api.generated.client.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Ответ со списком отслеживаемых ссылок")
public record ListLinksResponse(
        @Schema(description = "Список ссылок") List<LinkResponse> links,

        @Schema(description = "Общее количество ссылок") Integer size) {}
