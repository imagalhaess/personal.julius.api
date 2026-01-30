package nttdata.personal.julius.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import nttdata.personal.julius.api.common.domain.TransactionOrigin;
import nttdata.personal.julius.api.common.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Long id;
    private java.util.UUID publicId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;
    private TransactionStatus status = TransactionStatus.PENDING;
    private Category category;
    private TransactionType type;
    private TransactionOrigin origin;
    private String description;
    private LocalDateTime createdAt;

    public enum TransactionStatus { PENDING, APPROVED, REJECTED }
    public enum Category { FOOD, TRANSPORT, LEISURE, HEALTH, EDUCATION, OTHER, SALARY, INVESTMENT }
    public enum TransactionType { INCOME, EXPENSE, EXTERNAL }

    public void approve() {
        if (this.status == TransactionStatus.APPROVED) {
            throw new BusinessException("Transaction already approved");
        }
        this.status = TransactionStatus.APPROVED;
    }

    public void reject() {
        if (this.status == TransactionStatus.REJECTED) {
            throw new BusinessException("Transaction already rejected");
        }
        this.status = TransactionStatus.REJECTED;
    }
}
