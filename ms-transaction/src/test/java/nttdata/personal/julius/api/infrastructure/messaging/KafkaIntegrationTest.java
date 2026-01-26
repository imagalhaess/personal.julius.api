package nttdata.personal.julius.api.infrastructure.messaging;

import nttdata.personal.julius.api.PersonalJuliusApiApplication;
import nttdata.personal.julius.api.application.dto.TransactionRequestDto;
import nttdata.personal.julius.api.application.dto.TransactionResponseDto;
import nttdata.personal.julius.api.application.service.TransactionService;
import nttdata.personal.julius.api.domain.model.Transaction;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import nttdata.personal.julius.api.infrastructure.messaging.config.KafkaTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PersonalJuliusApiApplication.class)
@ActiveProfiles("test")
@Import(KafkaTestConfig.class) // Importa a configuração manual do Template
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"transaction-events"}, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
public class KafkaIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Deve processar e criar uma transação com sucesso")
    void shouldCreateTransactionSuccessfully() throws InterruptedException {

        UUID userId = UUID.randomUUID();

        TransactionRequestDto request = new TransactionRequestDto(
                userId,
                new BigDecimal("150.00"),
                "BRL",
                Transaction.Category.FOOD,
                Transaction.TransactionType.EXPENSE,
                "Lanche de teste",
                LocalDate.now()
        );

        TransactionResponseDto response = transactionService.create(request);

        assertThat(response.id()).isNotNull();

        Transaction saved = transactionRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getAmount()).isEqualByComparingTo("150.00");
    }
}
