package nttdata.personal.julius.api.infrastructure.messaging.kafka;

import nttdata.personal.julius.api.application.transaction.CreateTransactionUseCase;
import nttdata.personal.julius.api.application.transaction.dto.TransactionRequest;
import nttdata.personal.julius.api.application.transaction.dto.TransactionResponse;
import nttdata.personal.julius.api.domain.transaction.*;
import nttdata.personal.julius.api.domain.user.Cpf;
import nttdata.personal.julius.api.domain.user.Email;
import nttdata.personal.julius.api.domain.user.User;
import nttdata.personal.julius.api.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"transaction-events"}, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
public class KafkaIntegrationTest {

    @Autowired
    private CreateTransactionUseCase createTransactionUseCase;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve processar e aprovar uma transação via Kafka com sucesso")
    void shouldProcessAndApproveTransactionViaKafka() throws InterruptedException {

        User user = new User(
                "Isabela Mattos",
                new Email("isa@email.com"),
                new Cpf("331.125.940-88"),
                "senha123"
        );
        userRepository.save(user);

        TransactionRequest request = new TransactionRequest(
                user.getId(),
                new BigDecimal("150.00"),
                "BRL",
                Category.FOOD,
                TransactionType.EXPENSE,
                "Lanche de teste",
                LocalDate.now()
        );

        TransactionResponse response = createTransactionUseCase.execute(request);
        System.out.println("ID DA TRANSAÇÃO CRIADA: " + response.id());

        Thread.sleep(10000);

        Transaction processedTransaction = transactionRepository.findById(response.id())
                .orElseThrow(() -> new AssertionError("Transação não encontrada no banco após o processamento"));

        System.out.println("STATUS DA TRANSAÇÃO NO BANCO: " + processedTransaction.getStatus());

        assertThat(processedTransaction.getStatus())
                .withFailMessage(
                        "O status deveria ser APPROVED, mas o cache do JPA ou atraso no Kafka manteve como PENDING")
                .isEqualTo(TransactionStatus.APPROVED);
    }
}
