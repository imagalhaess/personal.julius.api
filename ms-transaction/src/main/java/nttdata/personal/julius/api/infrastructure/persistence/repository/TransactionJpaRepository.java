package nttdata.personal.julius.api.infrastructure.persistence.repository;

import nttdata.personal.julius.api.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<TransactionEntity> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.userId = :userId AND t.type = 'INCOME'")
    BigDecimal sumIncomeByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.userId = :userId AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseByUserId(@Param("userId") UUID userId);
}
