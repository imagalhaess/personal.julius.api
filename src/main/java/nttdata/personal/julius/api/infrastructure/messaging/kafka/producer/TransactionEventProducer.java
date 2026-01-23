package nttdata.personal.julius.api.infrastructure.messaging.kafka.producer;

import nttdata.personal.julius.api.infrastructure.messaging.kafka.events.TransactionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(TransactionCreatedEvent event) {
        this.kafkaTemplate.send("transaction-events", event.transactionId().toString(), event);
    }
}

