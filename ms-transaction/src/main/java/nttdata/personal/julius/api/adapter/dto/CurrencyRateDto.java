package nttdata.personal.julius.api.adapter.dto;

import java.math.BigDecimal;

public record CurrencyRateDto(
        String code,
        String codein,
        String name,
        BigDecimal bid,
        BigDecimal ask
) {
}
