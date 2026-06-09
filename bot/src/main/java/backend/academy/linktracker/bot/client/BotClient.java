package backend.academy.linktracker.bot.client;

/**
 * Интерфейс для работы с Telegram Bot API.
 * Отвечает за отправку сообщений и изоляцию SDK.
 */
public interface BotClient {

    /**
     * Отправляет сообщение в Telegram.
     * Сообщение автоматически разбивается на части, если длина превышает 4096 символов.
     * Авто-экранирование специальных символов Telegram выполняется автоматически.
     *
     * @param chatId идентификатор чата (не null)
     * @param text   текст сообщения (не null, не пустой)
     * @throws IllegalArgumentException если chatId или text некорректны
     * @throws BotClientException       в случае ошибки Telegram API
     */
    void sendMessage(Long chatId, String text);
}
