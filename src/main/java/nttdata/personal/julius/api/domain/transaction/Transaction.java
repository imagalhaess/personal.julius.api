package nttdata.personal.julius.api.domain.transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private UUID id;
    private UUID userId;
    private Money money;
    private Category category;
    private TransactionType type;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;

    public Transaction(UUID userId, Money money, Category category,
                       TransactionType type, String description, LocalDate transactionDate) {

        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();

        this.userId = userId;
        this.money = money;
        this.category = category;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public Transaction(UUID id, UUID userId, Money money, Category category,
                       TransactionType type, String description,
                       LocalDate transactionDate, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.money = money;
        this.category = category;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Money getMoney() {
        return money;
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

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void update(Money money, Category category, String description, LocalDate date) {
        this.money = money;
        this.category = category;
        this.description = description;
        this.transactionDate = date;
    }

    public void delete(UUID transactionId) {
        this.id = transactionId;
    }
}
