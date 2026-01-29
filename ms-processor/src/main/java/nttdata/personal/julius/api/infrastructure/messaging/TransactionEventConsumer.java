package nttdata.personal.julius.api.infrastructure.messaging;

import nttdata.personal.julius.api.application.service.TransactionProcessorService;
import nttdata.personal.julius.api.common.event.DlqMessage;
import nttdata.personal.julius.api.common.event.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final TransactionProcessorService processorService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.dlq.topic:transaction-dlq}")
    private String dlqTopic;

    public TransactionEventConsumer(TransactionProcessorService processorService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.processorService = processorService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.transaction-events:transaction-events}",
            groupId = "${spring.kafka.consumer.group-id:processor-group}"
    )
    public void consume(TransactionCreatedEvent event, Acknowledgment ack) {
        log.info("Evento recebido para processamento: transactionId={}, userId={}, amount={}",
                event.transactionId(), event.userId(), event.amount());

        try {
            processorService.process(event);
            ack.acknowledge();
            log.info("Transação {} processada com sucesso", event.transactionId());
        } catch (Exception e) {
            log.error("Erro ao processar transação {}: {}", event.transactionId(), e.getMessage(), e);
            sendToDlq(event, e.getMessage());
            ack.acknowledge();
        }
    }

    private void sendToDlq(TransactionCreatedEvent event, String errorMessage) {
        String transactionId = event.transactionId().toString();
        DlqMessage dlqMessage = new DlqMessage(
                transactionId,
                event,
                errorMessage,
                "ms-processor",
                LocalDateTime.now()
        );

        try {
            kafkaTemplate.send(dlqTopic, transactionId, dlqMessage);
            log.warn("Mensagem enviada para DLQ: transactionId={}", transactionId);
        } catch (Exception ex) {
            log.error("Falha crítica ao enviar para DLQ", ex);
        }
    }
}