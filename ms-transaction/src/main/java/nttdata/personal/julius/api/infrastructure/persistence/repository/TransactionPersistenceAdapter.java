package nttdata.personal.julius.api.infrastructure.persistence.repository;

import nttdata.personal.julius.api.domain.model.Transaction;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import nttdata.personal.julius.api.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TransactionPersistenceAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    public TransactionPersistenceAdapter(TransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = TransactionEntity.fromDomain(transaction);
        TransactionEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpaRepository.findById(id).map(TransactionEntity::toDomain);
    }

    @Override
    public void delete(Transaction transaction) {
        jpaRepository.deleteById(transaction.getId());
    }

    @Override
    public Optional<Transaction> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(TransactionEntity::toDomain);
    }

    @Override
    public List<Transaction> findByUserId(UUID userId, int page, int size) {
        return jpaRepository.findByUserId(userId, PageRequest.of(page, size))
                .stream()
                .map(TransactionEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal sumIncomeByUserId(UUID userId) {
        return jpaRepository.sumIncomeByUserId(userId);
    }

    @Override
    public BigDecimal sumExpenseByUserId(UUID userId) {
        return jpaRepository.sumExpenseByUserId(userId);
    }
}
