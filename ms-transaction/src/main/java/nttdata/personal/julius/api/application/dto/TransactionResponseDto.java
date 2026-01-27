package nttdata.personal.julius.api.application.dto;

import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDto(
        Long id,
        BigDecimal amount,
        String status,
        String description,
        LocalDate date,
        Transaction.Category category,
        Transaction.TransactionType type
) {
}
