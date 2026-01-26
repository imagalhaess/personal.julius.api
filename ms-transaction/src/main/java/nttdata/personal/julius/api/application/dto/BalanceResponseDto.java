package nttdata.personal.julius.api.application.dto;

import java.math.BigDecimal;

public record BalanceResponseDto(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {
}
