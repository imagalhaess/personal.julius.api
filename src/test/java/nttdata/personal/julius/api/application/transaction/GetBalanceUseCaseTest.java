package nttdata.personal.julius.api.application.transaction;

import nttdata.personal.julius.api.application.transaction.dto.BalanceResponse;
import nttdata.personal.julius.api.domain.transaction.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetBalanceUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetBalanceUseCase getBalanceUseCase;

    @Test
    @DisplayName("Deve calcular o saldo corretamente chamando o banco")
    void shouldCalculateBalanceCorrectly() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        when(transactionRepository.getTotalIncomeByUserId(userId)).thenReturn(new BigDecimal("100.00"));
        when(transactionRepository.getTotalExpenseByUserId(userId)).thenReturn(new BigDecimal("40.00"));

        // ACT
        BalanceResponse response = getBalanceUseCase.execute(userId);

        // ASSERT
        assertEquals(new BigDecimal("100.00"), response.totalIncome());
        assertEquals(new BigDecimal("40.00"), response.totalExpense());
        assertEquals(new BigDecimal("60.00"), response.balance());
    }
}
