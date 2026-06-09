package backend.academy.linktracker.scrapper.api.generated.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Унифицированный ответ об ошибке API
 */
@Schema(description = "Ответ об ошибке")
public record ApiErrorResponse(
        @Schema(description = "Описание ошибки") String description,

        @Schema(description = "Код ошибки") String code,

        @Schema(description = "Имя исключения") String exceptionName,

        @Schema(description = "Сообщение исключения") String exceptionMessage) {}
