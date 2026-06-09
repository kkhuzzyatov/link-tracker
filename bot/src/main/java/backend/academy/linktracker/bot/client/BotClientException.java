package backend.academy.linktracker.bot.client;

public class BotClientException extends RuntimeException {
    private final boolean recoverable;

    public BotClientException(String message, Throwable cause, boolean recoverable) {
        super(message, cause);
        this.recoverable = recoverable;
    }

    public boolean isRecoverable() {
        return recoverable;
    }
}
