package nttdata.personal.julius.api.infrastructure.messaging;

import nttdata.personal.julius.api.application.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final TransactionService transactionService;
    private final DlqProducer dlqProducer;

    public TransactionEventConsumer(TransactionService transactionService, DlqProducer dlqProducer) {
        this.transactionService = transactionService;
        this.dlqProducer = dlqProducer;
    }

    @KafkaListener(topics = "${app.kafka.topics.transaction-processed:transaction-processed}", groupId = "transaction-result-group")
    public void consume(TransactionProcessedEvent event) {
        log.info("Recebido resultado de processamento para transação: id={}, aprovado={}", event.transactionId(), event.approved());

        try {
            if (event.approved()) {
                transactionService.approve(event.transactionId());
                log.info("Transação {} finalizada com sucesso (APROVADA).", event.transactionId());
            } else {
                transactionService.reject(event.transactionId(), event.reason());
                log.info("Transação {} finalizada com sucesso (REJEITADA): {}", event.transactionId(), event.reason());
            }
        } catch (Exception e) {
            log.error("Erro ao finalizar transação {}: {}", event.transactionId(), e.getMessage(), e);
            dlqProducer.send(event, String.valueOf(event.transactionId()), e.getMessage());
        }
    }
}
