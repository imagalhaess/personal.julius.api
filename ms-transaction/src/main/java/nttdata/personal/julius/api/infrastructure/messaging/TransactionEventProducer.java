package nttdata.personal.julius.api.infrastructure.messaging;

import nttdata.personal.julius.api.common.event.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic:transaction-events}")
    private String topicName;

    public TransactionEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(TransactionCreatedEvent event) {
        try {
            kafkaTemplate.send(topicName, event.transactionId().toString(), event);
            log.info("Evento enviado para o Kafka: {}", event);
        } catch (Exception e) {
            log.error("Erro ao enviar evento para Kafka", e);
        }
    }
}
