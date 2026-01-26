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

    @KafkaListener(topics = "${spring.kafka.template.default-topic:transaction-events}", groupId = "${spring.kafka.consumer.group-id:transaction-processor}")
    public void consume(TransactionCreatedEvent event) {
        log.info("Processando transação: {}", event.transactionId());

        try {
            // Regra de negócio simples:
            // - INCOME: sempre aprovada
            // - EXPENSE: aprovada (em produção, consultaria MockAPI para validar saldo)
            boolean approved = validateTransaction(event);

            if (approved) {
                transactionService.approve(event.transactionId());
                log.info("Transação APROVADA: {}", event.transactionId());
            } else {
                transactionService.reject(event.transactionId(), "Transação rejeitada por regra de negócio");
                log.info("Transação REJEITADA: {}", event.transactionId());
            }
        } catch (Exception e) {
            log.error("Erro ao processar transação: {}", event.transactionId(), e);
            dlqProducer.send(event, e.getMessage());
        }
    }

    private boolean validateTransaction(TransactionCreatedEvent event) {
        // INCOME sempre é aprovado
        if ("INCOME".equals(event.type())) {
            return true;
        }

        // Para EXPENSE, em produção consultaria MockAPI para validar saldo
        // Por enquanto, aprova todas as transações
        return true;
    }
}
