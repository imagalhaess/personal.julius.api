package nttdata.personal.julius.api.adapter.dto;

import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String status,
        String description,
        LocalDate date,
        Transaction.Category category,
        Transaction.TransactionType type
) {
}
