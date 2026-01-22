package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.domain.transaction.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetTransactionUseCaseTest {

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    private GetTransactionUseCase getTransactionUseCase;

    @Test
    @DisplayName("Deve retornar lista de transações paginada")
    void shouldReturnPagedTransactions() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        Transaction t1 = new Transaction(userId, new Money(BigDecimal.TEN, "BRL"), Category.FOOD,
                                         TransactionType.EXPENSE, "T1", LocalDate.now());

        // O mock agora precisa dos 3 argumentos
        when(transactionRepository.findByUserId(userId, 0, 10)).thenReturn(List.of(t1));

        // ACT - Passe a página e o tamanho
        var response = getTransactionUseCase.execute(userId, 0, 10);

        // ASSERT
        assertEquals(1, response.size());
    }
}
