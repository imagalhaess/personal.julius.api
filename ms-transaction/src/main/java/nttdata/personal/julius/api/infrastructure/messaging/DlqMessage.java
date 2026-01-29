package nttdata.personal.julius.api.infrastructure.messaging;

import java.time.LocalDateTime;

public record DlqMessage(
        Object originalEvent,
        String errorMessage,
        LocalDateTime failedAt,
        int retryCount
) {
}
