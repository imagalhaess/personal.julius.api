package nttdata.personal.julius.api.application.transaction.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {
}
