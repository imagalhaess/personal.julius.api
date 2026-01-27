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
    Optional<TransactionEntity> findByIdAndUserId(Long id, Long userId);

    Page<TransactionEntity> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.userId = :userId AND t.type = 'INCOME'")
    BigDecimal sumIncomeByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.userId = :userId AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseByUserId(@Param("userId") Long userId);
}
