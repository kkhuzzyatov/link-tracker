package backend.academy.linktracker.bot.chat_state.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChatState {

    public enum State {
        // Бот ожидает команду от пользователя (например, /track, /untrack, /list)
        WAIT_FOR_COMMAND,

        // Бот ожидает, что пользователь введет ссылку для прекращения отслеживания
        WAIT_LINK_TO_TRACK,

        // Бот ожидает, что пользователь введет ссылку для прекращения отслеживания
        WAIT_LINK_TO_UNTRACK,

        // Бот ожидает, что пользователь введет теги (необязательно) для отслеживаемой ссылки
        WAIT_TAG_TO_TRACK,

        // Бот ожидает, что пользователь введет тег для фильтрации ссылок запрашиваемых для отображения
        WAIT_TAG_TO_SHOW_LINKS
    }

    private State currentState;
    private String uri;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChatState that = (ChatState) obj;
        if (currentState != that.currentState) {
            return false;
        }
        if (uri == null) {
            return that.uri == null;
        }
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        int result = currentState != null ? currentState.hashCode() : 0;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        return result;
    }
}
