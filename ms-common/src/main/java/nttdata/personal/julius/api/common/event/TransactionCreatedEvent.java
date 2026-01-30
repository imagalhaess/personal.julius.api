package nttdata.personal.julius.api.common.event;

import nttdata.personal.julius.api.common.domain.TransactionOrigin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreatedEvent(
        Long transactionId,
        Long userId,
        BigDecimal amount,
        String currency,
        String type,
        String category,
        TransactionOrigin origin,
        LocalDateTime createdAt
) {}
