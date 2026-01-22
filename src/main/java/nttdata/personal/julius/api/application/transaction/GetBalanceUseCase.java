package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.BalanceResponse;
import nttdata.personal.julius.api.domain.transaction.Transaction;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;
import nttdata.personal.julius.api.domain.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class GetBalanceUseCase {

    private final TransactionRepository transactionRepository;

    public GetBalanceUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public BalanceResponse execute(UUID userId) {
        BigDecimal totalIncome = transactionRepository.getTotalIncomeByUserId(userId);
        BigDecimal totalExpense = transactionRepository.getTotalExpenseByUserId(userId);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new BalanceResponse(totalIncome, totalExpense, balance);
    }
}
