package nttdata.personal.julius.api.infrastructure.persistence.transaction;

import nttdata.personal.julius.api.domain.transaction.Transaction;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Transaction save(Transaction transaction) {
        Optional<TransactionEntity> existingEntity = jpaRepository.findById(transaction.getId());

        if (existingEntity.isPresent()) {
            TransactionEntity entity = existingEntity.get();
            entity.setUserId(transaction.getUserId());
            entity.setAmount(transaction.getMoney().amount());
            entity.setCurrency(transaction.getMoney().currency());
            entity.setStatus(transaction.getStatus());
            entity.setCategory(transaction.getCategory());
            entity.setType(transaction.getType());
            entity.setDescription(transaction.getDescription());
            entity.setTransactionDate(transaction.getTransactionDate());
            jpaRepository.saveAndFlush(entity);
            return transaction;
        }

        TransactionEntity entity = TransactionMapper.toEntity(transaction);
        jpaRepository.saveAndFlush(entity);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(TransactionMapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Transaction> findByUserId(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return jpaRepository.findByUserId(userId, pageable)
                .stream()
                .map(TransactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalIncomeByUserId(UUID userId) {
        BigDecimal total = jpaRepository.sumIncomeByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalExpenseByUserId(UUID userId) {
        BigDecimal total = jpaRepository.sumExpenseByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }
}
