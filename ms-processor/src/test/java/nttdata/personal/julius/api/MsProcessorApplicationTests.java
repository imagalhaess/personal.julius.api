package nttdata.personal.julius.api;

import nttdata.personal.julius.api.application.port.TransactionServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"transaction-events"}, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class MsProcessorApplicationTests {

    @MockitoBean
    private TransactionServiceClient transactionServiceClient;

    @Test
    void contextLoads() {
    }
}
