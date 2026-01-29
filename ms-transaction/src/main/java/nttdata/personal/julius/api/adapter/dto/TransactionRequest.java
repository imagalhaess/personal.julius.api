package nttdata.personal.julius.api.adapter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;

public record TransactionRequest(
        Long userId,

        @NotNull @Positive
        BigDecimal amount,

        String currency,
        Transaction.Category category,
        Transaction.TransactionType type,
        String description
) {
    public TransactionRequest withUserId(Long userId) {
        return new TransactionRequest(userId, amount, currency, category, type, description);
    }
}
