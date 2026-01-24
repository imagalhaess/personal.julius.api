package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.TransactionResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.transaction.Transaction;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;

import java.util.UUID;

public class ProcessTransactionUseCase {

    private final TransactionRepository transactionRepository;
    public ProcessTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse execute(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).
                orElseThrow(() -> new BusinessException("Transação não encontrada: "
                + transactionId));

        transaction.approve();

        transactionRepository.save(transaction);

        return TransactionResponse.fromDomain(transaction);
    }
}
