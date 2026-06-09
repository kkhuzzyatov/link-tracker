package backend.academy.linktracker.scrapper.api.generated.client.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@Schema(description = "Описание обновления ссылки")
public record LinkUpdate(
        @Schema(description = "Идентификатор обновления") Long id,

        @Schema(description = "URL ссылки") URI url,

        @Schema(description = "Описание ссылки") String description,

        @Schema(description = "Список Telegram chatId для обновления")
        List<@NotNull Long> tgChatIds) {}
