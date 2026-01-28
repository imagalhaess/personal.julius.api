package nttdata.personal.julius.api.domain.model;

import nttdata.personal.julius.api.common.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private Category category;
    private TransactionType type;
    private String description;
    private LocalDateTime createdAt;

    public Transaction() {
        this.status = TransactionStatus.PENDING;
    }

    public Transaction(Long id, Long userId, BigDecimal amount, String currency, TransactionStatus status,
                       Category category, TransactionType type, String description,
                       LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.category = category;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    /**
     * Approves the transaction. Can only be called on PENDING transactions.
     *
     * @throws BusinessException if the transaction is not in PENDING status
     */
    public void approve() {
        if (this.status != TransactionStatus.PENDING) {
            throw new BusinessException("Transação não pode ser aprovada. Status atual: " + this.status);
        }
        this.status = TransactionStatus.APPROVED;
    }

    /**
     * Rejects the transaction. Can only be called on PENDING transactions.
     *
     * @throws BusinessException if the transaction is not in PENDING status
     */
    public void reject() {
        if (this.status != TransactionStatus.PENDING) {
            throw new BusinessException("Transação não pode ser rejeitada. Status atual: " + this.status);
        }
        this.status = TransactionStatus.REJECTED;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }

    public TransactionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStatus(TransactionStatus transactionStatus) {
        this.status = transactionStatus;
    }

    public enum TransactionStatus {PENDING, APPROVED, REJECTED}

    public enum TransactionType {INCOME, EXPENSE}

    public enum Category {FOOD, TRANSPORT, LEISURE, HEALTH, EDUCATION, SALARY, INVESTMENT, OTHER}
}
