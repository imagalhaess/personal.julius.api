package nttdata.personal.julius.api.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public DlqConsumer(DlqJpaRepository dlqRepository, ObjectMapper objectMapper) {
        this.dlqRepository = dlqRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${kafka.dlq.topic:transaction-dlq}",
            groupId = "dlq-persistence-group"
    )
    public void consume(DlqMessage message) {
        log.info("Recebendo mensagem DLQ para persistência: transactionId={}", message.transactionId());

        try {
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

        } catch (Exception e) {
            log.error("Erro ao persistir mensagem DLQ: {}", e.getMessage(), e);
        }
    }
}
