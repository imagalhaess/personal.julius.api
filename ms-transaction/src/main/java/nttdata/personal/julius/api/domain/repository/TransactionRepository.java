package nttdata.personal.julius.api.domain.repository;

import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID id);

    void delete(Transaction transaction);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findByUserId(UUID userId, int page, int size);

    BigDecimal sumIncomeByUserId(UUID userId);

    BigDecimal sumExpenseByUserId(UUID userId);
}
