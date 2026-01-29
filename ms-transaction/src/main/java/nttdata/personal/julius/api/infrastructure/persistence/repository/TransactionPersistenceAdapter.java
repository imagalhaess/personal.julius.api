package nttdata.personal.julius.api.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import nttdata.personal.julius.api.domain.model.Transaction;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import nttdata.personal.julius.api.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);
        TransactionEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Transaction> findByUserId(Long userId, int page, int size) {
        return jpaRepository.findByUserId(userId, PageRequest.of(page, size))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public void delete(Transaction transaction) {
        jpaRepository.deleteById(transaction.getId());
    }

    @Override
    public BigDecimal sumIncomeByUserId(Long userId) {
        return jpaRepository.sumIncomeByUserId(userId);
    }

    @Override
    public BigDecimal sumExpenseByUserId(Long userId) {
        return jpaRepository.sumExpenseByUserId(userId);
    }

    private TransactionEntity toEntity(Transaction t) {
        TransactionEntity e = new TransactionEntity();
        e.setId(t.getId());
        e.setUserId(t.getUserId());
        e.setAmount(t.getAmount());
        e.setCurrency(t.getCurrency());
        e.setConvertedAmount(t.getConvertedAmount());
        e.setExchangeRate(t.getExchangeRate());
        e.setCategory(t.getCategory());
        e.setType(t.getType());
        e.setDescription(t.getDescription());
        e.setCreatedAt(t.getCreatedAt());
        e.setStatus(t.getStatus());
        return e;
    }

    private Transaction toDomain(TransactionEntity e) {
        return e.toDomain();
    }
}
