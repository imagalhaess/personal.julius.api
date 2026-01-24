package nttdata.personal.julius.api.infrastructure.messaging.kafka.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreatedEvent(
        @JsonProperty("transactionId") UUID transactionId,
        @JsonProperty("userId") UUID userId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("type") String type,
        @JsonProperty("category") String category
) {
}
