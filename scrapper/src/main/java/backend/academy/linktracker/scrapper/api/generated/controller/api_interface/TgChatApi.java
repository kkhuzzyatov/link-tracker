package backend.academy.linktracker.scrapper.api.generated.controller.api_interface;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Tag(name = "tg-chat", description = "Управление Telegram чатами")
@RequestMapping("/tg-chat")
public interface TgChatApi {

    @Operation(summary = "Удалить чат")
    @ApiResponse(responseCode = "200", description = "Чат успешно удалён")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @ApiResponse(responseCode = "404", description = "Чат не существует")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteChat(@PathVariable @NotNull Long id);

    @Operation(summary = "Зарегистрировать чат")
    @ApiResponse(responseCode = "200", description = "Чат зарегистрирован")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @ApiResponse(responseCode = "409", description = "Чат уже существует")
    @PostMapping("/{id}")
    ResponseEntity<Void> registerChat(@PathVariable @NotNull Long id);
}
