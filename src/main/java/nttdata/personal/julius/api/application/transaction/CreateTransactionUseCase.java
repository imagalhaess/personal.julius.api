package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.TransactionRequest;
import nttdata.personal.julius.api.application.transaction.dto.TransactionResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.transaction.*;
import nttdata.personal.julius.api.domain.user.UserRepository;
import nttdata.personal.julius.api.infrastructure.messaging.kafka.events.TransactionCreatedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.kafka.producer.TransactionEventProducer;

public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionEventProducer eventProducer;

    public CreateTransactionUseCase(TransactionRepository transactionRepository, UserRepository userRepository, TransactionEventProducer eventProducer) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.eventProducer = eventProducer;
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

        publishEvent(transaction);

        return TransactionResponse.fromDomain(transaction);
    }

    public void publishEvent(Transaction transaction) {
        var event = new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getMoney().amount(),
                transaction.getMoney().currency(),
                transaction.getType().name(),
                transaction.getCategory().name()
        );
        eventProducer.send(event);
    }
}
