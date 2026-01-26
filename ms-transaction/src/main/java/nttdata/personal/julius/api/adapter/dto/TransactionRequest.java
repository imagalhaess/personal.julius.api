package nttdata.personal.julius.api.adapter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull @Positive
        BigDecimal amount,

        String currency,
        Transaction.Category category,
        Transaction.TransactionType type,
        String description,
        LocalDate date
) {
}
