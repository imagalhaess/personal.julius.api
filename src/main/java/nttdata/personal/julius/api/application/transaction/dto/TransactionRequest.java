package nttdata.personal.julius.api.application.transaction.dto;

import nttdata.personal.julius.api.domain.transaction.Category;
import nttdata.personal.julius.api.domain.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(
        UUID userId,
        BigDecimal amount,
        String currency,
        Category category,
        TransactionType type,
        String description,
        LocalDate date
) {
}

