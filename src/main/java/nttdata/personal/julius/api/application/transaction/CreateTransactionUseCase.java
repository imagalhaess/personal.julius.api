package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.TransactionRequest;
import nttdata.personal.julius.api.application.transaction.dto.TransactionResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.transaction.*;
import nttdata.personal.julius.api.domain.user.UserRepository;

public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository; // Para validar o dono da transação

    public CreateTransactionUseCase(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public TransactionResponse execute(TransactionRequest request) {

        userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado para esta transação."));

        Money money = new Money(request.amount(), request.currency());

        Transaction transaction = new Transaction(
                request.userId(),
                money,
                request.category(),
                request.type(),
                request.description(),
                request.date()
        );

        transactionRepository.save(transaction);

        return TransactionResponse.fromDomain(transaction);
    }
}
