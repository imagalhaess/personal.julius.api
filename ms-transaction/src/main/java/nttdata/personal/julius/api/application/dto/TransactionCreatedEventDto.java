package nttdata.personal.julius.api.application.dto;

import java.math.BigDecimal;

public record TransactionCreatedEventDto(
        Long transactionId,
        Long userId,
        BigDecimal amount,
        String currency,
        String type,
        String category
) {}
