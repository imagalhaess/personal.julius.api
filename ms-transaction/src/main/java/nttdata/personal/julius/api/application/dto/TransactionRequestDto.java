package nttdata.personal.julius.api.application.dto;

import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDto(
        Long userId,
        BigDecimal amount,
        String currency,
        Transaction.Category category,
        Transaction.TransactionType type,
        String description,
        LocalDate date
) {
}
