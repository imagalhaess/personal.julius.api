package nttdata.personal.julius.api.infrastructure.messaging;

import nttdata.personal.julius.api.common.event.DlqMessage;
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

    @Value("${spring.application.name:ms-transaction}")
    private String serviceName;

    public DlqProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Object event, String transactionId, String errorMessage) {
        DlqMessage dlqMessage = new DlqMessage(
                transactionId,
                event,
                errorMessage,
                serviceName,
                LocalDateTime.now()
        );

        try {
            kafkaTemplate.send(dlqTopic, transactionId, dlqMessage);
            log.warn("Mensagem enviada para DLQ. TransactionId: {}", transactionId);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para DLQ. TransactionId: {}", transactionId, e);
        }
    }
}
