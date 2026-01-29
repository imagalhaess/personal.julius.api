package nttdata.personal.julius.api.infrastructure.messaging;

public record TransactionProcessedEvent(Long transactionId, boolean approved, String reason) {
}
