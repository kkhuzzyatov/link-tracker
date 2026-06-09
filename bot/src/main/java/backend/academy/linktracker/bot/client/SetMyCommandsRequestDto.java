package backend.academy.linktracker.bot.client;

import java.util.List;

public record SetMyCommandsRequestDto(List<BotCommandDto> commands) {}
