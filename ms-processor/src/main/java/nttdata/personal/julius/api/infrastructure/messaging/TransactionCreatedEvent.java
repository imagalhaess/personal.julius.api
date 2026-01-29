package nttdata.personal.julius.api.infrastructure.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreatedEvent(
        Long transactionId,
        Long userId,
        BigDecimal amount,
        String currency,
        String type,
        String category,
        LocalDateTime createdAt
) {}
