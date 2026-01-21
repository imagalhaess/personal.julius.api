package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.TransactionRequest;
import nttdata.personal.julius.api.application.transaction.dto.TransactionResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.transaction.Money;
import nttdata.personal.julius.api.domain.transaction.Transaction;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;

import java.util.UUID;

public class UpdateTransactionUseCase {
    public class UpdateTransactionsUseCase {

        private final TransactionRepository transactionRepository;

        public UpdateTransactionsUseCase(TransactionRepository transactionRepository) {
            this.transactionRepository = transactionRepository;
        }

        public TransactionResponse execute(UUID id, TransactionRequest request) {

            Transaction transaction = transactionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("Transação não encontrada."));

            Money newMoney = new Money(request.amount(), request.currency());

            transaction.update(newMoney, request.category(), request.description(), request.date());

            transactionRepository.save(transaction);
            return TransactionResponse.fromDomain(transaction);
        }
    }
}
