package nttdata.personal.julius.api.common.event;

import java.math.BigDecimal;

public record TransactionProcessedEvent(
        Long transactionId,
        boolean approved,
        String reason,
        BigDecimal convertedAmount,
        BigDecimal exchangeRate
) {}
