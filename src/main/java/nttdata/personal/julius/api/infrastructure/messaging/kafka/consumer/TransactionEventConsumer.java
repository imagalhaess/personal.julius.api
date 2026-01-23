package nttdata.personal.julius.api.infrastructure.messaging.kafka.consumer;

import nttdata.personal.julius.api.infrastructure.messaging.kafka.events.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    @KafkaListener(topics = "transaction-events", groupId = "julius-group")
    public void consume(TransactionCreatedEvent event) {
        log.info("Evento recebido do Kafka: Transação ID {}", event.transactionId());

        // TODO: Cahmar aqui o Use Case de Processamento (UpdateStatus)

        log.info("Processando transação de valor {} {} para o usuário {}",
                 event.amount(), event.currency(), event.userId());
    }
}
