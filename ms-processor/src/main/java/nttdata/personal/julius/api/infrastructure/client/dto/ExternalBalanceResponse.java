package nttdata.personal.julius.api.infrastructure.client.dto;

import java.math.BigDecimal;

public record ExternalBalanceResponse (
        String id,
        Long accountId,
        BigDecimal amount,
        String currency
){
}
