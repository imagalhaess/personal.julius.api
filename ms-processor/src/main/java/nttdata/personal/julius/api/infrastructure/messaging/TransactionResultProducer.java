package nttdata.personal.julius.api.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.transaction-processed:transaction-processed}")
    private String topicName;

    public void send(TransactionProcessedEvent event) {
        try {
            kafkaTemplate.send(topicName, event.transactionId().toString(), event);
            log.info("Resultado da transação enviado para Kafka: id={}, aprovado={}", event.transactionId(), event.approved());
        } catch (Exception e) {
            log.error("Erro ao enviar resultado da transação para Kafka", e);
            throw e;
        }
    }
}
