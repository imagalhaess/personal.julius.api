package nttdata.personal.julius.api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private Transaction.Category category;

    @Enumerated(EnumType.STRING)
    private Transaction.TransactionType type;

    private String description;
    private LocalDateTime createdAt;

    public static TransactionEntity fromDomain(Transaction t) {
        return new TransactionEntity(
                t.getId(), t.getUserId(), t.getAmount(), t.getCurrency(), t.getStatus(),
                t.getCategory(), t.getType(), t.getDescription(), t.getCreatedAt()
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Transaction.TransactionStatus.PENDING;
        }
    }

    public Transaction toDomain() {
        return new Transaction(
                this.id, this.userId, this.amount, this.currency, this.status,
                this.category, this.type, this.description, this.createdAt
        );
    }
}
