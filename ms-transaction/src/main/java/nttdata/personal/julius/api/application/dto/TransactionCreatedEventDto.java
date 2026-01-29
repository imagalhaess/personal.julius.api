package nttdata.personal.julius.api.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreatedEventDto(
        UUID transactionId,
        Long userId,
        BigDecimal amount,
        String currency,
        String type,
        String category,
        nttdata.personal.julius.api.common.domain.TransactionOrigin origin
) {}
