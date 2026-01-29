package nttdata.personal.julius.api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import nttdata.personal.julius.api.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private java.util.UUID publicId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal convertedAmount;

    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private nttdata.personal.julius.api.common.domain.TransactionOrigin origin;

    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.TransactionStatus status;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = java.util.UUID.randomUUID();
        }
    }

    public Transaction toDomain() {
        Transaction t = new Transaction();
        t.setId(this.id);
        t.setPublicId(this.publicId);
        t.setUserId(this.userId);
        t.setAmount(this.amount);
        t.setCurrency(this.currency);
        t.setConvertedAmount(this.convertedAmount);
        t.setExchangeRate(this.exchangeRate);
        t.setCategory(this.category);
        t.setType(this.type);
        t.setOrigin(this.origin);
        t.setDescription(this.description);
        t.setCreatedAt(this.createdAt);
        t.setStatus(this.status);
        return t;
    }

    public static TransactionEntity fromDomain(Transaction t) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(t.getId());
        entity.setPublicId(t.getPublicId());
        entity.setUserId(t.getUserId());
        entity.setAmount(t.getAmount());
        entity.setCurrency(t.getCurrency());
        entity.setConvertedAmount(t.getConvertedAmount());
        entity.setExchangeRate(t.getExchangeRate());
        entity.setCategory(t.getCategory());
        entity.setType(t.getType());
        entity.setOrigin(t.getOrigin());
        entity.setDescription(t.getDescription());
        entity.setCreatedAt(t.getCreatedAt());
        entity.setStatus(t.getStatus());
        return entity;
    }
}
