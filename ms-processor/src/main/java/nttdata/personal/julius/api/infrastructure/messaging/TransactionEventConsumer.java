package nttdata.personal.julius.api.infrastructure.messaging;

import nttdata.personal.julius.api.application.service.TransactionProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final TransactionProcessorService processorService;

    public TransactionEventConsumer(TransactionProcessorService processorService) {
        this.processorService = processorService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.transaction-events:transaction-events}",
            groupId = "${spring.kafka.consumer.group-id:processor-group}"
    )
    public void consume(TransactionCreatedEvent event, Acknowledgment ack) {
        log.info("Evento recebido para processamento: transactionId={}, userId={}, amount={}",
                event.transactionId(), event.userId(), event.amount());

        try {
            processorService.process(event);
            ack.acknowledge();
            log.info("Transação {} processada com sucesso", event.transactionId());
        } catch (Exception e) {
            log.error("Erro ao processar transação {}: {}", event.transactionId(), e.getMessage(), e);
            throw e;
        }
    }
}
