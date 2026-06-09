package backend.academy.linktracker.bot.chat_state.repository;

import backend.academy.linktracker.bot.chat_state.model.ChatState;

public interface ChatStateRepository {
    ChatState findStateByChatId(Long chatId);

    void update(Long chatId, ChatState chatState);
}
