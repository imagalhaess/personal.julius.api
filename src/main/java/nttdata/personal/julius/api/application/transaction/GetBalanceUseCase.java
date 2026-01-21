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

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            BigDecimal amount = t.getMoney().amount();

            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(amount);
            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(amount);
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new BalanceResponse(totalIncome, totalExpense, balance);
    }
}
