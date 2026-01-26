package nttdata.personal.julius.api.adapter.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {
}
