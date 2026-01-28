package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.application.port.TransactionServiceClient;
import nttdata.personal.julius.api.infrastructure.messaging.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionProcessorService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessorService.class);

    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("10000.00");

    private final TransactionServiceClient transactionServiceClient;

    public TransactionProcessorService(TransactionServiceClient transactionServiceClient) {
        this.transactionServiceClient = transactionServiceClient;
    }

    public void process(TransactionCreatedEvent event) {
        log.info("Processando transação: id={}, tipo={}, categoria={}, valor={}",
                event.transactionId(), event.type(), event.category(), event.amount());

        boolean approved = validateTransaction(event);

        if (approved) {
            transactionServiceClient.approveTransaction(event.transactionId());
            log.info("Transação {} aprovada", event.transactionId());
        } else {
            String reason = "Valor acima do limite permitido: " + APPROVAL_THRESHOLD;
            transactionServiceClient.rejectTransaction(event.transactionId(), reason);
            log.warn("Transação {} rejeitada: {}", event.transactionId(), reason);
        }
    }

    private boolean validateTransaction(TransactionCreatedEvent event) {
        if (event.amount().compareTo(APPROVAL_THRESHOLD) > 0) {
            log.warn("Transação {} excede o limite de aprovação automática: {} > {}",
                    event.transactionId(), event.amount(), APPROVAL_THRESHOLD);
            return false;
        }

        return true;
    }
}
