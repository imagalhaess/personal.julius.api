package nttdata.personal.julius.api.infrastructure.config;

import nttdata.personal.julius.api.common.config.BaseKafkaConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConfig extends BaseKafkaConfig {

    @Value("${spring.kafka.consumer.group-id:transaction-group}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(baseConsumerConfig(groupId));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name("transaction-events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic transactionProcessedTopic() {
        return TopicBuilder.name("transaction-processed").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic transactionDlqTopic() {
        return TopicBuilder.name("transaction-dlq").partitions(1).replicas(1).build();
    }
}
