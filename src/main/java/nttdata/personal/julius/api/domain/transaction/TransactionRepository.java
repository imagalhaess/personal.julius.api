package nttdata.personal.julius.api.domain.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    void save(Transaction transaction);

    Optional<Transaction> findById(UUID id);

    List<Transaction> findByUserId(UUID userId);

    void deleteById(UUID id);

    List<Transaction> findByUserId(UUID userId, int page, int size);

}
