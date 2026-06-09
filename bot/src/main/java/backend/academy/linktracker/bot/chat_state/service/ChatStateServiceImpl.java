package backend.academy.linktracker.bot.chat_state.service;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.repository.ChatStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ChatStateServiceImpl implements ChatStateService {

    private final ChatStateRepository chatStateRepository;

    @Override
    public ChatState getStateByChatId(Long chatId) {
        return chatStateRepository.findStateByChatId(chatId);
    }

    @Override
    public void update(Long chatId, ChatState chatState) {
        chatStateRepository.update(chatId, chatState);
        log.atInfo()
                .addKeyValue("event", "update_chat_state")
                .addKeyValue("chat_id", chatId)
                .addKeyValue("state", chatState.getCurrentState())
                .log("Обновлено состояние ожидания ввода для определенного чата.");
    }
}
