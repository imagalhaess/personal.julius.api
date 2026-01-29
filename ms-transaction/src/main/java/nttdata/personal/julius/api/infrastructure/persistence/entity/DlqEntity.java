package nttdata.personal.julius.api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dlq_messages")
public class DlqEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "original_event", columnDefinition = "TEXT")
    private String originalEvent;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "source_service")
    private String sourceService;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public DlqEntity() {}

    public DlqEntity(String transactionId, String originalEvent, String errorMessage,
                     String sourceService, LocalDateTime failedAt) {
        this.transactionId = transactionId;
        this.originalEvent = originalEvent;
        this.errorMessage = errorMessage;
        this.sourceService = sourceService;
        this.failedAt = failedAt;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public String getOriginalEvent() { return originalEvent; }
    public String getErrorMessage() { return errorMessage; }
    public String getSourceService() { return sourceService; }
    public LocalDateTime getFailedAt() { return failedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
