package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.TransactionResponse;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public GetTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> execute(UUID userId, int page, int size) {

        var transactions = transactionRepository.findByUserId(userId, page, size);

        return transactions.stream()
                .map(TransactionResponse::fromDomain)
                .collect(Collectors.toList());
    }
}
