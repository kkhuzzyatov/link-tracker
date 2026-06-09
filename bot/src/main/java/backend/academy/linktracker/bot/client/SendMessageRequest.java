package backend.academy.linktracker.bot.client;

public record SendMessageRequest(Long chat_id, String text, String parse_mode) {}
