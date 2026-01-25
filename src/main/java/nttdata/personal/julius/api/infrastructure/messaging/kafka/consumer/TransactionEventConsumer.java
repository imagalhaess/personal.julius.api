package nttdata.personal.julius.api.infrastructure.messaging.kafka.consumer;

import nttdata.personal.julius.api.application.transaction.ProcessTransactionUseCase;
import nttdata.personal.julius.api.infrastructure.messaging.kafka.events.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final ProcessTransactionUseCase processTransactionUseCase;

    public TransactionEventConsumer(ProcessTransactionUseCase processTransactionUseCase) {
        this.processTransactionUseCase = processTransactionUseCase;
    }

    @Transactional
    @KafkaListener(topics = "transaction-events", groupId = "julius-group")
    public void consume(TransactionCreatedEvent event) {
        log.info("Evento recebido do Kafka: Transação ID {}", event.transactionId());

        try{
            processTransactionUseCase.execute(event.transactionId());
            log.info("Transação {} processada com sucesso", event.transactionId());
        } catch (Exception e){
            log.info("Erro ao processar transação {} : {}", event.transactionId(), e.getMessage());
        }

        log.info("Processando transação de valor {} {} para o usuário {}",
                 event.amount(), event.currency(), event.userId());
    }
}
