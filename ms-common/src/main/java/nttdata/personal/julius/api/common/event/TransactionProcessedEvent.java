package nttdata.personal.julius.api.common.event;

public record TransactionProcessedEvent(
        Long transactionId,
        boolean approved,
        String reason
) {}
