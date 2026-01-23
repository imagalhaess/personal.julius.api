package nttdata.personal.julius.api.domain.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(UUID id);

    void delete(UUID id);

    List<Transaction> findByUserId(UUID userId, int page, int size);

    BigDecimal getTotalIncomeByUserId(UUID userId);

    BigDecimal getTotalExpenseByUserId(UUID userId);

}
