package nttdata.personal.julius.api.application.transaction.dto;

import nttdata.personal.julius.api.domain.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID userId,
        BigDecimal amount,
        String currency,
        String status,
        String category,
        String type,
        String description,
        LocalDate date
) {
    public static TransactionResponse fromDomain(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getMoney().amount(),
                transaction.getMoney().currency(),
                transaction.getStatus().name(),
                transaction.getCategory().name(),
                transaction.getType().name(),
                transaction.getDescription(),
                transaction.getTransactionDate()
        );
    }
}

