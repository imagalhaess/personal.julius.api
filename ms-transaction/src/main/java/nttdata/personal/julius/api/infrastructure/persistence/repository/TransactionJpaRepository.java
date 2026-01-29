package nttdata.personal.julius.api.infrastructure.persistence.repository;

import nttdata.personal.julius.api.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
    Page<TransactionEntity> findByUserId(Long userId, Pageable pageable);
    Optional<TransactionEntity> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(t.convertedAmount) FROM TransactionEntity t WHERE t.userId = :userId AND t.type = 'INCOME' AND t.status = 'APPROVED'")
    BigDecimal sumIncomeByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(t.convertedAmount) FROM TransactionEntity t WHERE t.userId = :userId AND (t.type = 'EXPENSE' OR t.type = 'EXTERNAL') AND t.status = 'APPROVED'")
    BigDecimal sumExpenseByUserId(@Param("userId") Long userId);
}
