package nttdata.personal.julius.api.infrastructure.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID transactionId,
        Long userId,
        BigDecimal amount,
        String currency,
        String type,
        String category,
        nttdata.personal.julius.api.common.domain.TransactionOrigin origin
) {}
