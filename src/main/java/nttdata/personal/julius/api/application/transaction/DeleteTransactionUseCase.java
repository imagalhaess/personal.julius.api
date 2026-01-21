package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;

import java.util.UUID;

public class DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public DeleteTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void execute(UUID transactionId) {
        transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada."));

        transactionRepository.deleteById(transactionId);
    }
}
