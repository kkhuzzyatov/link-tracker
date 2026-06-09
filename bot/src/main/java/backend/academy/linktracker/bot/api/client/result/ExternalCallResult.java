package backend.academy.linktracker.bot.api.client.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ExternalCallResult<T> {

    T body;
    Status status;

    public enum Status {
        OK,
        BAD_REQUEST,
        NOT_FOUND,
        CONFLICT
    }

    public static Status mapStatus(int statusCode) {
        return switch (statusCode) {
            case 200 -> Status.OK;
            case 404 -> Status.NOT_FOUND;
            case 409 -> Status.CONFLICT;
            default -> Status.BAD_REQUEST;
        };
    }
}
