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
        System.out.println("[DEBUG] Iniciando processamento da transação: " + transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada"));

        System.out.println("[DEBUG] Status atual no banco: " + transaction.getStatus());

        transaction.approve();
        System.out.println("[DEBUG] Status alterado na memória para: " + transaction.getStatus());

        transactionRepository.save(transaction);
        System.out.println("[DEBUG] Comando save() executado!");

        return TransactionResponse.fromDomain(transaction);
    }
}
