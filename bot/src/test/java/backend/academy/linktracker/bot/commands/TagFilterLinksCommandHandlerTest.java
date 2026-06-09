package backend.academy.linktracker.bot.commands;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.api.client.LinksClient;
import backend.academy.linktracker.bot.api.client.result.ExternalCallResult;
import backend.academy.linktracker.bot.api.generated.client.model.LinkResponse;
import backend.academy.linktracker.bot.api.generated.client.model.ListLinksResponse;
import backend.academy.linktracker.bot.chat_state.model.ChatState;
import backend.academy.linktracker.bot.chat_state.service.ChatStateService;
import backend.academy.linktracker.bot.client.BotClient;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TagFilterLinksCommandHandlerTest {

    private ChatStateService chatStateService;
    private LinksClient linksClient;
    private BotClient botClient;

    private TagFilterLinksCommandHandler handler;

    private final String TAG = "tag";

    @BeforeEach
    void setUp() {
        chatStateService = mock(ChatStateService.class);
        linksClient = mock(LinksClient.class);
        botClient = mock(BotClient.class);
        handler = new TagFilterLinksCommandHandler(chatStateService, linksClient, botClient);
    }

    @Test
    void handle_shouldSendCancelMessageAndSetWaitCommandChatState_whenCommandIsCancel() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/cancel");
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(botClient).sendMessage(eq(chatId), eq(handler.CANCEL_MESSAGE));
    }

    @SneakyThrows
    @Test
    void handle_shouldSendListOfAllLinksAndSetWaitCommandChatState_whenStatusIsOkAndInputIsSkipCommand() {
        Long chatId = 100L;
        ListLinksResponse linkGetMethodResult = createLinksResponse(List.of(
                createLink("https://github.com/Tinkoff/career", List.of("career")),
                createLink("https://github.com/Tinkoff/invest-openapi", null)));
        List<String> links = List.of("https://github.com/Tinkoff/career", "https://github.com/Tinkoff/invest-openapi");
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, "/skip");
        when(linksClient.getLinks(chatId))
                .thenReturn(new ExternalCallResult(linkGetMethodResult, ExternalCallResult.Status.OK));
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(linksClient).getLinks(chatId);
        verify(botClient).sendMessage(eq(chatId), eq(String.join(", ", links)));
    }

    @SneakyThrows
    @Test
    void handle_shouldSendListOfLinksWithSpecialTagAndSetWaitCommandChatState_whenStatusIsOkAndInputIsSpecialTag() {
        Long chatId = 100L;
        ListLinksResponse linkGetMethodResult =
                createLinksResponse(List.of(createLink("https://github.com/Tinkoff/career", List.of(TAG))));
        List<String> expectedOutput = List.of("https://github.com/Tinkoff/career");
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, TAG);
        when(linksClient.getLinksByTag(chatId, TAG))
                .thenReturn(new ExternalCallResult(linkGetMethodResult, ExternalCallResult.Status.OK));
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(linksClient).getLinksByTag(chatId, TAG);
        verify(botClient).sendMessage(eq(chatId), eq(String.join(", ", expectedOutput)));
    }

    @Test
    void handle_shouldSendChatIsNotRegisteredMessageAndSetWaitCommandChatState_whenStatusIsNotFound() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, TAG);
        when(linksClient.getLinksByTag(chatId, TAG))
                .thenReturn(new ExternalCallResult(null, ExternalCallResult.Status.NOT_FOUND));
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(linksClient).getLinksByTag(chatId, TAG);
        verify(botClient).sendMessage(eq(chatId), eq(handler.CHAT_IS_NOT_REGISTERED_MESSAGE));
    }

    @Test
    void handle_shouldSendErrorMessageAndSetWaitCommandChatState_whenStatusIsAnother() {
        Long chatId = 100L;
        Update update = CommandHandlerTestUtils.mockUpdate(chatId, TAG);
        when(linksClient.getLinksByTag(chatId, TAG))
                .thenReturn(new ExternalCallResult(null, ExternalCallResult.Status.BAD_REQUEST));
        handler.handle(update);
        verify(chatStateService).update(chatId, new ChatState(ChatState.State.WAIT_FOR_COMMAND, null));
        verify(linksClient).getLinksByTag(chatId, TAG);
        verify(botClient).sendMessage(eq(chatId), eq(handler.ERROR_MESSAGE));
    }

    private ListLinksResponse createLinksResponse(List<LinkResponse> links) {
        return new ListLinksResponse(links, links.size());
    }

    private LinkResponse createLink(String url, List<String> tags) throws Exception {
        return new LinkResponse(null, new URI(url), tags, null);
    }
}
