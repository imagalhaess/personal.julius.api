package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.TransactionRequest;
import nttdata.personal.julius.api.application.transaction.dto.TransactionResponse;
import nttdata.personal.julius.api.domain.BusinessException;
import nttdata.personal.julius.api.domain.transaction.Category;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;
import nttdata.personal.julius.api.domain.transaction.TransactionType;
import nttdata.personal.julius.api.domain.user.User;
import nttdata.personal.julius.api.domain.user.UserRepository;
import nttdata.personal.julius.api.infrastructure.messaging.kafka.producer.TransactionEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionEventProducer eventProducer;

    @InjectMocks
    private CreateTransactionUseCase createTransactionUseCase;

    @Test
    @DisplayName("Deve criar transação com sucesso")
    void shouldCreateTransactionSuccessfully() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(
                userId, new BigDecimal("50.00"), "BRL",
                Category.FOOD, TransactionType.EXPENSE, "Almoço", LocalDate.now()
        );

        // ACT
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));

        TransactionResponse response = createTransactionUseCase.execute(request);

        // ASSERT
        assertNotNull(response);
        assertEquals(new BigDecimal("50.00"), response.amount());
        assertEquals("PENDING", response.status());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar transação para usuário inexistente")
    void shouldThrowExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(userId, BigDecimal.TEN, "BRL", Category.OTHER,
                                                            TransactionType.INCOME, "Teste", LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> createTransactionUseCase.execute(request));
        verify(transactionRepository, never()).save(any());
    }
}
