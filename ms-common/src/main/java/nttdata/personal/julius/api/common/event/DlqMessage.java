package nttdata.personal.julius.api.common.event;

import java.time.LocalDateTime;

public record DlqMessage(
        String transactionId,
        Object originalEvent,
        String errorMessage,
        String sourceService,
        LocalDateTime failedAt
) {}
