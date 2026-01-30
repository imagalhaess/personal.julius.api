package nttdata.personal.julius.api.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import nttdata.personal.julius.api.application.service.TransactionService;
import nttdata.personal.julius.api.common.event.DlqMessage;
import nttdata.personal.julius.api.infrastructure.persistence.entity.DlqEntity;
import nttdata.personal.julius.api.infrastructure.persistence.repository.DlqJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);

    private final DlqJpaRepository dlqRepository;
    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;

    public DlqConsumer(DlqJpaRepository dlqRepository, ObjectMapper objectMapper, TransactionService transactionService) {
        this.dlqRepository = dlqRepository;
        this.objectMapper = objectMapper;
        this.transactionService = transactionService;
    }

    @KafkaListener(
            topics = "${kafka.dlq.topic:transaction-dlq}",
            groupId = "dlq-persistence-group"
    )
    public void consume(DlqMessage message) {
        log.info("Recebendo mensagem DLQ para persistência: transactionId={}", message.transactionId());

        try {
            // 1. Persistir DLQ
            String eventJson = objectMapper.writeValueAsString(message.originalEvent());
            DlqEntity entity = new DlqEntity(
                    message.transactionId(),
                    eventJson,
                    message.errorMessage(),
                    message.sourceService(),
                    message.failedAt()
            );
            dlqRepository.save(entity);
            log.info("Mensagem DLQ persistida: transactionId={}", message.transactionId());

            // 2. Rejeitar Transação Original (Fallback)
            try {
                Long transactionId = Long.parseLong(message.transactionId());
                transactionService.reject(transactionId, "Processing Failed: " + message.errorMessage());
                log.info("Transação {} marcada como REJEITADA via DLQ.", transactionId);
            } catch (NumberFormatException e) {
                log.error("ID da transação inválido no DLQ: {}", message.transactionId());
            } catch (Exception e) {
                log.error("Erro ao rejeitar transação via DLQ: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Erro ao processar mensagem DLQ: {}", e.getMessage(), e);
        }
    }
}
