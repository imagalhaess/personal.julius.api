package nttdata.personal.julius.api.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DlqProducer {

    private static final Logger log = LoggerFactory.getLogger(DlqProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.dlq.topic:transaction-dlq}")
    private String dlqTopic;

    public DlqProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(TransactionCreatedEvent event, String errorMessage) {
        DlqMessage dlqMessage = new DlqMessage(
                event,
                errorMessage,
                LocalDateTime.now(),
                0
        );

        try {
            kafkaTemplate.send(dlqTopic, event.transactionId().toString(), dlqMessage);
            log.warn("Mensagem enviada para DLQ: {}", event.transactionId());
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para DLQ: {}", event.transactionId(), e);
        }
    }
}
