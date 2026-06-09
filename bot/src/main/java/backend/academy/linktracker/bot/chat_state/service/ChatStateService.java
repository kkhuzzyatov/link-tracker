package backend.academy.linktracker.bot.chat_state.service;

import backend.academy.linktracker.bot.chat_state.model.ChatState;

public interface ChatStateService {
    ChatState getStateByChatId(Long chatId);

    void update(Long chatId, ChatState chatState);
}
