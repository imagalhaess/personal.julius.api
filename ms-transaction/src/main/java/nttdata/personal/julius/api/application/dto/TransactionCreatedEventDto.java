package nttdata.personal.julius.api.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreatedEventDto(
        Long transactionId,
        Long userId,
        BigDecimal amount,
        String currency,
        String type,
        String category,
        LocalDateTime createdAt
) {}
