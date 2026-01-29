package nttdata.personal.julius.api.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nttdata.personal.julius.api.common.event.TransactionCreatedEvent;
import nttdata.personal.julius.api.common.event.TransactionProcessedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessorService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void process(TransactionCreatedEvent event) {
        log.info("Processando transação {} para o usuário {}. Origem: {}", 
                event.transactionId(), event.userId(), event.origin());

        boolean approved;
        String reason = null;

        if (nttdata.personal.julius.api.common.domain.TransactionOrigin.CASH.equals(event.origin())) {
            log.info("Transação em dinheiro (CASH) aprovada automaticamente.");
            approved = true;
        } else {
            // Lógica para ACCOUNT: Simula consulta a APIs externas
            log.info("Transação de conta (ACCOUNT). Validando limite...");
            approved = event.amount().compareTo(new java.math.BigDecimal("10000.00")) < 0;
            if (!approved) {
                reason = "LIMIT_EXCEEDED";
            }
        }

        kafkaTemplate.send("transaction-processed", new TransactionProcessedEvent(event.transactionId(), approved, reason));
    }
}
