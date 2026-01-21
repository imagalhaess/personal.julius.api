package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.TransactionRequest;
import nttdata.personal.julius.api.domain.transaction.*;
import nttdata.personal.julius.api.domain.user.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateTransactionUseCase updateTransactionUseCase;

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    void shouldUpdateTransactionSuccessfully() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        Transaction original = new Transaction(UUID.randomUUID(), new Money(BigDecimal.TEN, "BRL"), Category.OTHER,
                                               TransactionType.INCOME, "Antiga", LocalDate.now());
        TransactionRequest request = new TransactionRequest(null, new BigDecimal("20.00"), "BRL", Category.FOOD,
                                                            TransactionType.EXPENSE, "Nova", LocalDate.now());

        when(transactionRepository.findById(id)).thenReturn(Optional.of(original));

        // ACT
        var response = updateTransactionUseCase.execute(id, request);

        // ASSSERT
        assertEquals(new BigDecimal("20.00"), response.amount());
        assertEquals("Nova", response.description());
        verify(transactionRepository).save(any());
    }
}
