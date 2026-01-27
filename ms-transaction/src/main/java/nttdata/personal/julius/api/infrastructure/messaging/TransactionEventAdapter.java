package nttdata.personal.julius.api.infrastructure.messaging;

import nttdata.personal.julius.api.application.dto.TransactionCreatedEventDto;
import nttdata.personal.julius.api.application.port.TransactionEventPort;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements the TransactionEventPort interface.
 * Converts application DTOs to infrastructure events and publishes them via Kafka.
 */
@Component
public class TransactionEventAdapter implements TransactionEventPort {

    private final TransactionEventProducer producer;

    public TransactionEventAdapter(TransactionEventProducer producer) {
        this.producer = producer;
    }

    @Override
    public void publishTransactionCreated(TransactionCreatedEventDto dto) {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                dto.transactionId(),
                dto.userId(),
                dto.amount(),
                dto.currency(),
                dto.type(),
                dto.category()
        );
        producer.send(event);
    }
}
