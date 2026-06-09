package backend.academy.linktracker.bot.chat_state.repository;

import backend.academy.linktracker.bot.chat_state.model.ChatState;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class ChatStateRepositoryInMemoryImpl implements ChatStateRepository {

    private final Map<Long, ChatState> chatStateMap = new HashMap<>();

    @Override
    public ChatState findStateByChatId(Long chatId) {
        return chatStateMap.getOrDefault(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
    }

    @Override
    public void update(Long chatId, ChatState chatState) {
        chatStateMap.put(chatId, chatState);
    }
}
