package nttdata.personal.julius.api.domain.repository;

import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(Long id);

    void delete(Transaction transaction);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findByUserId(Long userId, int page, int size);

    BigDecimal sumIncomeByUserId(Long userId);

    BigDecimal sumExpenseByUserId(Long userId);
}
